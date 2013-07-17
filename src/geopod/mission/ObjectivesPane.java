package geopod.mission;

import geopod.constants.UIConstants;
import geopod.devices.FlightDataRecorder;
import geopod.gui.components.ButtonFactory;
import geopod.gui.components.GeopodLabel;

import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import net.miginfocom.swing.MigLayout;

/**
 * An ObjectivesPane creates, stores, and initially displays a list of
 * objectives. An ObjectivesPane may be set display other panels as appropriate,
 * but can always be reset to show its list of objectives.
 * 
 * @author Geopod Team
 * 
 */
public class ObjectivesPane
		extends JScrollPane
{
	private static final long serialVersionUID = 220800299167839328L;
	private static final int SCROLLBAR_UNIT_INCREMENT;
	static
	{
		SCROLLBAR_UNIT_INCREMENT = 10;
	}
	private JPanel m_objectivesPanel;

	/**
	 * Creates a new ObjectivesPane with a panel initially displaying the list
	 * of objectives
	 * 
	 * @param objectives
	 *            - list of objectives to display
	 * @param flightRecorder
	 *            - FlightDataRecorder used to determine assessment eligibility
	 *            of each objective
	 */
	public ObjectivesPane (Mission mission, FlightDataRecorder flightRecorder)
	{
		setUpObjectivesPanel (mission, flightRecorder);

		super.getVerticalScrollBar ().setUnitIncrement (SCROLLBAR_UNIT_INCREMENT);
		super.setViewportView (m_objectivesPanel);
	}

	private void setUpObjectivesPanel (Mission mission, FlightDataRecorder flightRecorder)
	{
		m_objectivesPanel = new JPanel ();
		m_objectivesPanel.setBackground (UIConstants.GEOPOD_GREEN);
		m_objectivesPanel.setLayout (new MigLayout ("fill, wrap 1", "[align center]", ""));

		List<Objective> objectives = mission.getObjectives ();
		for (int i = 0; i < objectives.size (); ++i)
		{
			Objective objective = objectives.get (i);

			String title = objective.getTitle ();
			GeopodLabel objectivesLabel = new GeopodLabel (title.toUpperCase (), JLabel.CENTER);
			Font font = UIConstants.GEOPOD_BANDY.deriveFont (16.0f);
			objectivesLabel.setFont (font);
			objectivesLabel.setToolTipText (title);
			m_objectivesPanel.add (objectivesLabel, "gaptop 20, width 600!");

			String description = objective.getDescription ();
			JTextArea descriptionArea = createWrappingDescriptionArea (description);
			m_objectivesPanel.add (descriptionArea, "width 600!");

			if (objective.hasAssessmentQuestions ())
			{
				JButton assessmentButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
						UIConstants.GEOPOD_GREEN, false);
				assessmentButton.setText ("ASSESSMENT " + (i + 1));
				assessmentButton.addActionListener (new AssessmentListener (objective, this, flightRecorder, mission));
				assessmentButton.setActionCommand ("Assessment");
				m_objectivesPanel.add (assessmentButton, "gapbottom 30");
			}
			else
			{
				JLabel noAssessmentLabel = new JLabel ("No Assessment", JLabel.CENTER);
				Font labelfont = UIConstants.GEOPOD_VERDANA.deriveFont (Font.BOLD | Font.ITALIC,
						UIConstants.BUTTON_FONT_SIZE);
				noAssessmentLabel.setFont (labelfont);
				m_objectivesPanel.add (noAssessmentLabel, "gapbottom 30");
			}

		}
	}

	private JTextArea createWrappingDescriptionArea (String description)
	{
		JTextArea descriptionArea = new JTextArea ();
		DefaultCaret caret = (DefaultCaret) descriptionArea.getCaret ();
		caret.setUpdatePolicy (DefaultCaret.NEVER_UPDATE);
		descriptionArea.setEditable (false);
		descriptionArea.setLineWrap (true);
		descriptionArea.setWrapStyleWord (true);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (UIConstants.CONTENT_FONT_SIZE);
		descriptionArea.setFont (font);
		descriptionArea.setBackground (UIConstants.GEOPOD_GREEN);
		descriptionArea.setText (description);

		return (descriptionArea);
	}

	/**
	 * Resets the contents of the ObjectivesPane to the list of objectives
	 */
	public void resetToObjectivesView ()
	{
		super.setViewportView (m_objectivesPanel);
	}

	/**
	 * Returns the currently displayed component
	 * 
	 * @return The currently displayed component, which will be either the list
	 *         of objectives, or a ConditionsNotMetPanel or AssessmentPanel for
	 *         some specific objective
	 */
	public Component getCurrentContent ()
	{
		Component currentlyDisplayedComponent = getViewport ().getView ();
		return (currentlyDisplayedComponent);
	}

	/**
	 * @return true if a ConditionsNotMetPanel is displayed
	 */
	public boolean conditionsNotMetPanelDisplayed ()
	{
		Component currentComponent = getCurrentContent ();
		return (currentComponent instanceof ConditionsNotMetPanel);
	}
}
