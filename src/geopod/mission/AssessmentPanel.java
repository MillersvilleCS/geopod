package geopod.mission;

import geopod.ConfigurationManager;
import geopod.constants.UIConstants;
import geopod.gui.components.ButtonFactory;
import geopod.gui.styles.GeopodComboBoxUI;
import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AssessmentPanel
		extends JPanel
{
	private static final long serialVersionUID = -4839162914718719177L;
	private static final int QUESTION_WIDTH;
	private static final String QUESTION_DATA_KEY;
	private static final String ANSWER_POINTS_KEY;
	private static final String QUESTION_TAG;
	private static final String DROPDOWN_INDICATOR_TAG;
	private static final String QUESTION_TEXT_TAG;
	private static final String QUESTION_POINTS_TAG;
	private static final String QUESTION_SCORE_TAG;
	private static final String ANSWER_TAG;
	private static final String SELECTED_ANSWER_TAG;
	private static final String SELECTED_SCORE_TAG;
	private static final String OVERALL_SCORE_TAG;
	private static final String NOTIFICATION_START;
	private static final String NOTIFICATION_FIRST_MID;
	private static final String NOTIFICATION_SECOND_MID;
	private static final String NOTIFICATION_END;
	static
	{
		QUESTION_WIDTH = 600;

		QUESTION_DATA_KEY = "QuestionData";
		ANSWER_POINTS_KEY = "AnswerPointsData";

		QUESTION_TAG = "question";
		DROPDOWN_INDICATOR_TAG = "dropdownQuestion";
		QUESTION_TEXT_TAG = "questionText";
		QUESTION_POINTS_TAG = "totalPoints";
		QUESTION_SCORE_TAG = "studentScore";
		ANSWER_TAG = "answer";
		SELECTED_ANSWER_TAG = "selectedAnswerText";
		SELECTED_SCORE_TAG = "selectedAnswerScore";
		OVERALL_SCORE_TAG = "overallStudentScore";

		NOTIFICATION_START = "Your score is ";
		NOTIFICATION_FIRST_MID = " out of a total of ";
		NOTIFICATION_SECOND_MID = " possible points (";
		NOTIFICATION_END = "%).";
	}
	private Assessment m_assessment;
	private JTextField m_nameEntryField;
	private JCheckBox m_preserveNameCheckbox;
	private JLabel m_scoreNotificationLabel;

	public AssessmentPanel (Assessment assessment, AssessmentListener listener)
	{
		m_assessment = assessment;

		this.setBackground (UIConstants.GEOPOD_GREEN);

		this.setLayout (new MigLayout ("fillx, wrap 1, hidemode 3", "[align center]", ""));

		addHeader ();

		addNameEntryPanel ();

		addQuestions ();

		addNotificationArea ();

		addButtons (listener);
	}

	/*
	 * GUI creation methods
	 */

	private void addHeader ()
	{
		JLabel headerLabel = new JLabel ("ASSESSMENT", JLabel.CENTER);
		Font font = UIConstants.GEOPOD_BANDY.deriveFont (20.0f);
		headerLabel.setFont (font);
		this.add (headerLabel, "gaptop 20, gapbottom 20");
	}

	private void addNameEntryPanel ()
	{
		m_nameEntryField = new JTextField ();
		m_preserveNameCheckbox = new JCheckBox ();
		JPanel namePanel = createNameEntryPanel (m_nameEntryField, m_preserveNameCheckbox);
		this.add (namePanel);

		String userSavedName = ConfigurationManager.getProperty (ConfigurationManager.UserName);
		setUserEnteredName (userSavedName);
		setPreserveName (!userSavedName.isEmpty ());
		ConfigurationManager.addPropertyChangeListener (ConfigurationManager.UserName, new PropertyChangeListener ()
		{
			@Override
			public void propertyChange (PropertyChangeEvent evt)
			{
				String newUserSavedName = ConfigurationManager.getProperty (ConfigurationManager.UserName);
				setUserEnteredName (newUserSavedName);
			}
		});
	}

	public JPanel createNameEntryPanel (JTextField textField, JCheckBox checkBox)
	{
		JPanel namePanel = new JPanel ();
		namePanel.setLayout (new MigLayout ("hidemode 3", "[align right]0[align left, fill]", "[]0[]"));
		namePanel.setBackground (UIConstants.GEOPOD_GREEN);

		JLabel nameLabel = new JLabel ("Name: ");
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (UIConstants.CONTENT_FONT_SIZE);
		nameLabel.setFont (font);
		namePanel.add (nameLabel);

		textField.setFont (font);
		namePanel.add (textField);

		checkBox.setText ("use this name for all assessments");
		Font checkboxFont = UIConstants.GEOPOD_VERDANA.deriveFont (10.0f);
		checkBox.setFont (checkboxFont);
		checkBox.setBackground (UIConstants.GEOPOD_GREEN);
		namePanel.add (checkBox, "cell 1 1");

		return (namePanel);
	}

	private void addQuestions ()
	{
		List<Question> assessmentQuestions = m_assessment.getQuestions ();
		for (int i = 0; i < assessmentQuestions.size (); ++i)
		{
			Question question = assessmentQuestions.get (i);

			String questionText = (i + 1) + ". " + question.getQuestionText ();

			if (question instanceof MultiDropdownQuestion)
			{
				Debug.println (DebugLevel.MEDIUM, "Added a multi dropdown question");
				addMultiDropdownQuestion ((MultiDropdownQuestion) question, questionText);
			}
			else if (question instanceof MultipleChoiceQuestion)
			{
				Debug.println (DebugLevel.MEDIUM, "Added a multiple choice question");
				addMultipleChoiceQuestion ((MultipleChoiceQuestion) question, questionText);
			}
			else
			{
				Debug.println (DebugLevel.MEDIUM, "Error: invalid question type");
			}

		}
	}

	private void addMultiDropdownQuestion (MultiDropdownQuestion multiDropdownQuestion, String questionText)
	{
		String[] questionTextPieces = questionText.split ("\\{\\d+\\}");

		JPanel questionContents = new JPanel ();
		questionContents.setLayout (new MigLayout ("insets 0 0 20 0, gapx 0, novisualpadding", "", ""));
		questionContents.setBackground (UIConstants.GEOPOD_GREEN);

		// get metrics for determining widths in subsequent code
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (12.0f);
		JLabel sampleTextLabel = new JLabel ();
		sampleTextLabel.setFont (font);
		FontMetrics labelMetrics = sampleTextLabel.getFontMetrics (font);
		JComboBox sampleCombobox = new JComboBox ();
		sampleCombobox.setFont (font);
		FontMetrics comboboxMetrics = sampleCombobox.getFontMetrics (font);

		// start with text before first combobox
		int nextStartingOffset;
		if (questionTextPieces.length > 0 && !questionTextPieces[0].isEmpty ())
		{
			nextStartingOffset = addText (0, questionTextPieces[0], questionContents, labelMetrics);
		}
		else
		{
			nextStartingOffset = 0;
		}

		List<List<Answer>> dropdowns = multiDropdownQuestion.getDropdowns ();
		for (int dropdownNum = 0; dropdownNum < dropdowns.size (); ++dropdownNum)
		{
			// add the dropdown
			List<Answer> dropdownAnswers = dropdowns.get (dropdownNum);
			List<AnswerEntry> dropdownOptions = new ArrayList<AnswerEntry> ();
			dropdownOptions.add (new AnswerEntry ("", 0));
			for (Answer answer : dropdownAnswers)
			{
				AnswerEntry answerEntry = new AnswerEntry (answer.getAnswerText (), answer.getPointValue ());
				dropdownOptions.add (answerEntry);
			}
			nextStartingOffset = addDropdown (nextStartingOffset, dropdownOptions, questionContents, comboboxMetrics);

			// add text after dropdown, if there is any
			if (questionTextPieces.length > dropdownNum + 1)
			{
				String textAfterDropdown = questionTextPieces[dropdownNum + 1];
				if (!textAfterDropdown.isEmpty ())
				{
					nextStartingOffset = addText (nextStartingOffset, textAfterDropdown, questionContents, labelMetrics);
				}
			}
		}

		questionContents.putClientProperty (QUESTION_DATA_KEY, multiDropdownQuestion);

		this.add (questionContents, "align center, width " + QUESTION_WIDTH + "!");
	}

	private int addText (int startingReservedWidth, String text, JPanel parentPanel, FontMetrics metrics)
	{
		return (addText (startingReservedWidth, text, parentPanel, metrics, false));
	}

	/**
	 * Add text for mulitdropdown
	 * 
	 * @param startingReservedWidth
	 * @param text
	 * @param parentPanel
	 * @param metrics
	 * @param layoutConstraints
	 * @return
	 */
	private int addText (int startingReservedWidth, String text, JPanel parentPanel, FontMetrics metrics,
			boolean pushNewLine)
	{
		// whether or not this the current line was filled
		boolean filledLine = false;

		// how much room on the line do we have to put the text in?
		// 2 pixels padding in case of any unforseen problems because 
		// it's better to cut line too short than have text hidden because too long
		int availableLineWidth = QUESTION_WIDTH - 2 - startingReservedWidth;

		// for text that does not fit on this line
		String leftover = "";

		int stringWidth = SwingUtilities.computeStringWidth (metrics, text);
		while (stringWidth > availableLineWidth)
		{
			// does not fit on line
			filledLine = true;

			// find last whitespace in string
			Matcher lastWhitespaceMatcher = Pattern.compile ("\\s(\\S)+\\s*\\z").matcher (text);
			if (!lastWhitespaceMatcher.find ())
			{
				// no whitespace in string
				if (startingReservedWidth != 0)
				{
					// go to next line, maybe it will fit there
					return (addText (0, text, parentPanel, metrics, true));
				}
				else
				{
					// take off last character, see if that helps
					leftover = text.substring (text.length () - 1) + leftover;
					text = text.substring (0, text.length () - 1);
				}
			}
			else
			{
				lastWhitespaceMatcher.find (0);

				// 'beforeWhitespace; includes the whitespace character found
				String beforeWhitespace = text.substring (0, lastWhitespaceMatcher.start () + 1);
				String afterWhitespace = text.substring (lastWhitespaceMatcher.start () + 1);

				text = beforeWhitespace;
				leftover = afterWhitespace + leftover;
			}

			stringWidth = SwingUtilities.computeStringWidth (metrics, text);
		}

		// contents of 'text' will fit now, build label
		JLabel textLabel = new JLabel (text);
		textLabel.setBackground (UIConstants.GEOPOD_GREEN);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (12.0f);
		textLabel.setFont (font);

		String layoutConstraints = "gap 0 0";
		if (pushNewLine)
		{
			// we need a newline first
			layoutConstraints += ", newline";
		}
		if (filledLine)
		{
			// go to next line
			layoutConstraints += ", wrap";
		}
		else
		{
			// stay on same line
			layoutConstraints += ", split";
		}
		parentPanel.add (textLabel, layoutConstraints);

		if (leftover.isEmpty ())
		{
			// compensate for label width, return value is how many pixels of row has been used up
			//int labelWidth = SwingUtilities.computeStringWidth (metrics, text);
			int labelWidth = textLabel.getPreferredSize ().width;
			int offsetFromLeft = startingReservedWidth + labelWidth;
			return (offsetFromLeft);
		}
		else
		{
			// we filled up this line & have extra text to put on next row
			// return value (#pixels of lowest open row have been filled up)
			// will come from result of adding the leftover text to the next row
			return (addText (0, leftover, parentPanel, metrics));
		}
	}

	/**
	 * 
	 * @param startingReservedWidth
	 * @param options
	 * @param parentPanel
	 * @param metrics
	 * @return
	 */
	private int addDropdown (int startingReservedWidth, List<AnswerEntry> options, JPanel parentPanel,
			FontMetrics metrics)
	{
		JComboBox dropdown = new JComboBox ();
		dropdown.setUI (new GeopodComboBoxUI ());
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (UIConstants.CONTENT_FONT_SIZE);
		dropdown.setFont (font);
		for (AnswerEntry option : options)
		{
			dropdown.addItem (option);
		}
		dropdown.setSelectedIndex (0);

		int comboboxWidth = dropdown.getPreferredSize ().width;

		String layoutConstraints = "split, gap 0 0";
		if (startingReservedWidth + comboboxWidth > QUESTION_WIDTH - 2)
		{
			layoutConstraints += ", newline";
		}
		else
		{
			comboboxWidth += startingReservedWidth;
		}

		parentPanel.add (dropdown, layoutConstraints);
		return (comboboxWidth);

	}

	private void addMultipleChoiceQuestion (MultipleChoiceQuestion multipleChoiceQuestion, String questionText)
	{
		// build text area for question text
		JTextArea questionTextArea = new JTextArea ();
		questionTextArea.setEditable (false);
		questionTextArea.setLineWrap (true);
		questionTextArea.setWrapStyleWord (true);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (UIConstants.CONTENT_FONT_SIZE);
		questionTextArea.setFont (font);
		questionTextArea.setBackground (UIConstants.GEOPOD_GREEN);
		questionTextArea.setText (questionText);

		questionTextArea.putClientProperty (QUESTION_DATA_KEY, multipleChoiceQuestion);

		this.add (questionTextArea, "gaptop 10, width " + QUESTION_WIDTH + "!");

		// build panel with answers
		List<Answer> answers = multipleChoiceQuestion.getAnswers ();
		int numAnswers = answers.size ();

		ButtonGroup buttonGroup = new ButtonGroup ();

		JPanel allAnswers = new JPanel ();
		allAnswers.setLayout (new MigLayout ("wrap 2", "[align left][align left, grow]", ""));
		allAnswers.setBackground (UIConstants.GEOPOD_GREEN);

		boolean isMultiChooseMany = multipleChoiceQuestion.isMultiChooseMany ();

		for (int i = 0; i < numAnswers; ++i)
		{
			Answer answer = answers.get (i);
			String answerText = answer.getAnswerText ();
			Debug.println (DebugLevel.HIGH, "    Answer is " + answerText);
			JToggleButton answerSelection;
			if (isMultiChooseMany)
			{
				answerSelection = new JCheckBox ();
			}
			else
			{
				answerSelection = new JRadioButton ();
				buttonGroup.add (answerSelection);
			}
			answerSelection.setBackground (UIConstants.GEOPOD_GREEN);
			answerSelection.putClientProperty (ANSWER_POINTS_KEY, (Integer) answer.getPointValue ());
			allAnswers.add (answerSelection, "gapleft 12");

			JTextArea answerTextDisplay = new JTextArea ();
			answerTextDisplay.setEditable (false);
			answerTextDisplay.setLineWrap (true);
			answerTextDisplay.setWrapStyleWord (true);
			answerTextDisplay.setBackground (UIConstants.GEOPOD_GREEN);
			Font answerFont = UIConstants.GEOPOD_VERDANA.deriveFont (12.0f);
			answerTextDisplay.setFont (answerFont);
			answerTextDisplay.setText (answerText);
			allAnswers.add (answerTextDisplay, "gapbottom 2, width " + (QUESTION_WIDTH - 100) + "!");
		}
		this.add (allAnswers, "align center, width " + QUESTION_WIDTH + "!");
	}

	private void addNotificationArea ()
	{
		m_scoreNotificationLabel = new JLabel ("", JLabel.CENTER);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (UIConstants.SUBTITLE_SIZE);
		m_scoreNotificationLabel.setFont (font);
		m_scoreNotificationLabel.setVisible (false);
		m_scoreNotificationLabel.setForeground (UIConstants.GEOPOD_RED);

		this.add (m_scoreNotificationLabel, "align center, width 1:" + (QUESTION_WIDTH + 100) + ":2000");
	}

	private void addButtons (ActionListener listener)
	{
		JButton submitButton = createButton ("SUBMIT", listener);
		this.add (submitButton, "split 2, align center, gapright 100, gaptop 50, gapbottom 20");

		JButton backButton = createButton ("BACK", listener);
		this.add (backButton, "align center, gaptop 50, gapbottom 20");
	}

	private JButton createButton (String buttonName, ActionListener listener)
	{
		JButton newButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE, UIConstants.GEOPOD_GREEN,
				false);
		newButton.setText (buttonName);
		newButton.addActionListener (listener);
		newButton.setActionCommand (buttonName);
		return (newButton);
	}

	/*
	 * Submit-related methods
	 */

	public void setUserEnteredName (String name)
	{
		if (m_nameEntryField.isEditable ())
			m_nameEntryField.setText (name);
	}

	public void setPreserveName (boolean checked)
	{
		m_preserveNameCheckbox.setSelected (checked);
	}

	public String getUserEnteredName ()
	{
		return (m_nameEntryField.getText ());
	}

	public boolean hasValidName ()
	{
		return (!m_nameEntryField.getText ().isEmpty ());
	}

	public void generateAssessmentContent (Document assessmentXml)
	{
		Element mainElement = assessmentXml.getDocumentElement ();
		int potentialAssessmentScore = 0;
		int actualAssessmentScore = 0;

		for (int i = 2; i < this.getComponentCount () - 3; ++i)
		{
			Element questionElement = assessmentXml.createElement (QUESTION_TAG);

			JComponent questionComponent = (JComponent) this.getComponent (i);
			Object question = questionComponent.getClientProperty (QUESTION_DATA_KEY);
			if (question instanceof MultiDropdownQuestion)
			{
				MultiDropdownQuestion multiDropdownQuestion = (MultiDropdownQuestion) question;

				XmlUtility.generateXmlElement (assessmentXml, questionElement, DROPDOWN_INDICATOR_TAG, "true");

				String questionText = multiDropdownQuestion.getQuestionText ();
				XmlUtility.generateXmlElement (assessmentXml, questionElement, QUESTION_TEXT_TAG, questionText);

				int totalQuestionPoints = multiDropdownQuestion.calculateTotalQuestionValue ();
				XmlUtility.generateXmlElement (assessmentXml, questionElement, QUESTION_POINTS_TAG,
						String.valueOf (totalQuestionPoints));

				int questionScore = 0;
				for (int j = 0; j < questionComponent.getComponentCount (); ++j)
				{
					JComponent component = (JComponent) questionComponent.getComponent (j);
					if (component instanceof JComboBox)
					{
						JComboBox dropdown = (JComboBox) component;
						dropdown.setEnabled (false);

						AnswerEntry answer = (AnswerEntry) dropdown.getSelectedItem ();
						int answerValue = answer.getPointValue ();
						questionScore += answerValue;

						Element answerElement = assessmentXml.createElement (ANSWER_TAG);
						String answerText = answer.toString ();
						if (answerText.isEmpty ())
						{
							answerText = "<blank>";
						}
						XmlUtility.generateXmlElement (assessmentXml, answerElement, SELECTED_ANSWER_TAG, answerText);
						XmlUtility.generateXmlElement (assessmentXml, answerElement, SELECTED_SCORE_TAG,
								String.valueOf (answerValue));
						questionElement.appendChild (answerElement);

					}
				}
				// make sure question score is in range [0 - totalQuestionPoints]
				questionScore = Math.max (0, Math.min (questionScore, totalQuestionPoints));
				
				XmlUtility.generateXmlElement (assessmentXml, questionElement, QUESTION_SCORE_TAG,
						String.valueOf (questionScore));

				potentialAssessmentScore += totalQuestionPoints;
				actualAssessmentScore += questionScore;
			}
			else if (question instanceof MultipleChoiceQuestion)
			{
				MultipleChoiceQuestion multipleChoiceQuestion = (MultipleChoiceQuestion) question;

				String questionText = multipleChoiceQuestion.getQuestionText ();
				XmlUtility.generateXmlElement (assessmentXml, questionElement, QUESTION_TEXT_TAG, questionText);

				int totalQuestionPoints = multipleChoiceQuestion.calculateTotalQuestionValue ();
				XmlUtility.generateXmlElement (assessmentXml, questionElement, QUESTION_POINTS_TAG,
						String.valueOf (totalQuestionPoints));

				int questionScore = 0;
				JComponent allAnswersPanel = (JComponent) this.getComponent (++i);
				for (int j = 0; j < allAnswersPanel.getComponentCount (); j += 2)
				{
					JToggleButton selector = (JToggleButton) allAnswersPanel.getComponent (j);
					selector.setEnabled (false);
					if (selector.isSelected ())
					{
						int answerValue = (Integer) selector.getClientProperty (ANSWER_POINTS_KEY);
						questionScore += answerValue;

						JTextArea answerTextArea = (JTextArea) allAnswersPanel.getComponent (j + 1);
						String selectedAnswerText = answerTextArea.getText ();

						Element answerElement = assessmentXml.createElement (ANSWER_TAG);
						XmlUtility.generateXmlElement (assessmentXml, answerElement, SELECTED_ANSWER_TAG,
								selectedAnswerText);
						XmlUtility.generateXmlElement (assessmentXml, answerElement, SELECTED_SCORE_TAG,
								String.valueOf (answerValue));
						questionElement.appendChild (answerElement);
					}
				}
				// make sure question score is in range [0 - totalQuestionPoints]
				questionScore = Math.max (0, Math.min (questionScore, totalQuestionPoints));

				XmlUtility.generateXmlElement (assessmentXml, questionElement, QUESTION_SCORE_TAG,
						String.valueOf (questionScore));

				potentialAssessmentScore += totalQuestionPoints;
				actualAssessmentScore += questionScore;
			}
			else
			{
				Debug.println ("Error: invalid question type");
			}

			mainElement.appendChild (questionElement);
		}

		String overallStudentScoreLine = actualAssessmentScore + " out of " + potentialAssessmentScore;
		XmlUtility.generateXmlElement (assessmentXml, mainElement, OVERALL_SCORE_TAG,
				String.valueOf (overallStudentScoreLine));

		displayScoreNotification (actualAssessmentScore, potentialAssessmentScore);

		boolean shouldPreserveName = m_preserveNameCheckbox.isSelected ();
		if (shouldPreserveName)
		{
			ConfigurationManager.setProperty (ConfigurationManager.UserName, getUserEnteredName ());
		}

		makeFinalDisplayModifications ();
	}

	private void makeFinalDisplayModifications ()
	{
		int submitButtonIndex = this.getComponentCount () - 2;
		JComponent submitButton = (JComponent) this.getComponent (submitButtonIndex);
		submitButton.setVisible (false);

		m_nameEntryField.setEditable (false);
		m_nameEntryField.setBackground (UIConstants.GEOPOD_GREEN);

		m_preserveNameCheckbox.setVisible (false);
	}

	private void displayScoreNotification (int actualScore, int potentialScore)
	{
		String notificationText = NOTIFICATION_START;
		notificationText += actualScore;
		notificationText += NOTIFICATION_FIRST_MID;
		notificationText += potentialScore;
		notificationText += NOTIFICATION_SECOND_MID;
		String percentCorrectString;
		if (potentialScore == 0)
		{
			// this should never happen, but check just in case
			percentCorrectString = "0.0";
		}
		else
		{
			double percentCorrect = (actualScore * 100.0) / potentialScore;
			percentCorrect = Math.max (percentCorrect, 0);
			percentCorrectString = String.format ("%.1f", percentCorrect);
		}
		notificationText += percentCorrectString;
		notificationText += NOTIFICATION_END;

		m_scoreNotificationLabel.setText (notificationText);
		m_scoreNotificationLabel.setVisible (true);
	}

	/**
	 * Class to store an integer point value for an answer along with the answer
	 * text. Used as the content of {@link JComboBox}s used for dropdowns, to
	 * make obtaining answer point value when scoring easier.
	 */
	private class AnswerEntry
	{
		private String m_answerText;
		private int m_pointValue;

		public AnswerEntry (String string, int points)
		{
			m_answerText = string;
			m_pointValue = points;
		}

		public int getPointValue ()
		{
			return (m_pointValue);
		}

		@Override
		public String toString ()
		{
			return (m_answerText);
		}
	}
}
