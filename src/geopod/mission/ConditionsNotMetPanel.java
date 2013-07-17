package geopod.mission;

import geopod.constants.UIConstants;
import geopod.devices.FlightDataRecorder;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.components.ButtonFactory;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ConditionsNotMetPanel
		extends JPanel
		implements ItemListener
{
	private static final long serialVersionUID = -2591932348481022400L;
	private static final String HEADER_STRING;
	private static final String OCCURRENCE_CONDITIONS_STRING;
	private static final String REQUIRED_DATA_SOURCES_STRING;
	private static final String REQUIRED_PARTICLE_IMAGES_STRING;
	private static final String CHECKBOX_TITLE;
	static
	{
		HEADER_STRING = "The following conditions must be met before you can take the assessment:";
		OCCURRENCE_CONDITIONS_STRING = " You must perform the following actions the indicated number of times:";
		REQUIRED_DATA_SOURCES_STRING = " You must load the following data set(s):";
		REQUIRED_PARTICLE_IMAGES_STRING = " You must have found particle formation for the following category(s):";

		CHECKBOX_TITLE = "show only unmet conditions";
	}
	private Conditions m_conditions;
	private GridBagConstraints m_constraints;
	private JPanel m_conditionsPanel;
	private AssessmentListener m_assessmentListener;
	private boolean m_unmetConditionsDisplayed;

	public ConditionsNotMetPanel (Conditions conditions, AssessmentListener assessmentListener)
	{
		m_conditions = conditions;

		m_assessmentListener = assessmentListener;

		setUpPanel ();

		addDescriptiveHeader ();

		createCheckbox ();

		addConditionsPanel ();

		createBackButton ();

		m_unmetConditionsDisplayed = false;
	}

	private void setUpPanel ()
	{
		this.setBackground (UIConstants.GEOPOD_GREEN);

		this.setLayout (new GridBagLayout ());
		m_constraints = new GridBagConstraints ();
		m_constraints.gridx = 1;
		m_constraints.gridy = 0;
		m_constraints.weightx = 0.5;
		m_constraints.weighty = 0.1;
		m_constraints.gridwidth = 2;
	}

	private void addDescriptiveHeader ()
	{
		m_constraints.fill = GridBagConstraints.HORIZONTAL;
		m_constraints.insets = new Insets (30, 0, 0, 0);
		JLabel headerLabel = new JLabel (HEADER_STRING, JLabel.CENTER);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (Font.BOLD, 18.0f);
		headerLabel.setFont (font);
		this.add (headerLabel, m_constraints);
		m_constraints.fill = GridBagConstraints.NONE;
		m_constraints.gridy++;
	}

	private void createCheckbox ()
	{
		JCheckBox conditionDisplayOptions = new JCheckBox (CHECKBOX_TITLE);
		conditionDisplayOptions.setSelected (false);
		conditionDisplayOptions.addItemListener (this);
		conditionDisplayOptions.setFocusPainted (false);
		conditionDisplayOptions.setBackground (UIConstants.GEOPOD_GREEN);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (12.0f);
		conditionDisplayOptions.setFont (font);

		m_constraints.anchor = GridBagConstraints.LINE_START;
		m_constraints.gridwidth = 1;
		m_constraints.insets = new Insets (10, 70, 0, 0);
		this.add (conditionDisplayOptions, m_constraints);
		m_constraints.anchor = GridBagConstraints.CENTER;
		m_constraints.gridwidth = 2;
		m_constraints.gridy++;
	}

	private void addConditionsPanel ()
	{
		m_conditionsPanel = new JPanel ();
		m_conditionsPanel.setBackground (UIConstants.GEOPOD_GREEN);
		m_conditionsPanel.setLayout (new GridBagLayout ());

		addConditions (m_conditions);

		//m_constraints.anchor = GridBagConstraints.LINE_START;
		this.add (m_conditionsPanel, m_constraints);
		//m_constraints.anchor = GridBagConstraints.CENTER;
		m_constraints.gridy++;
	}

	private void addConditions (Conditions conditionsToDisplay)
	{
		m_conditionsPanel.removeAll ();

		GridBagConstraints constraints = new GridBagConstraints ();
		constraints.gridx = 0;
		constraints.gridy = 0;

		List<String> requiredDataSources = conditionsToDisplay.getDataSourceConditions ();
		if (!requiredDataSources.isEmpty ())
		{
			addHeaderLabel (REQUIRED_DATA_SOURCES_STRING, constraints);
			for (int i = 0; i < requiredDataSources.size (); i++)
			{
				String dataSourceName = requiredDataSources.get (i);
				addConditionLabel (dataSourceName, constraints);
			}
		}

		Map<GeopodEventId, Integer> conditions = conditionsToDisplay.getOccurenceConditions ();
		Set<GeopodEventId> conditionSet = conditions.keySet ();
		List<GeopodEventId> conditionList = new ArrayList<GeopodEventId> (conditionSet);
		if (!conditionList.isEmpty ())
		{
			addHeaderLabel (OCCURRENCE_CONDITIONS_STRING, constraints);
			for (int i = 0; i < conditionList.size (); i++)
			{
				GeopodEventId eventId = conditionList.get (i);
				Integer requiredNumEvents = conditions.get (eventId);
				String conditionString = eventId.toString () + ":  " + requiredNumEvents;
				addConditionLabel (conditionString, constraints);
			}
		}

		List<String> requiredParticleImageCategories = conditionsToDisplay.getParticleImageCategoryConditions ();
		if (!requiredParticleImageCategories.isEmpty ())
		{
			addHeaderLabel (REQUIRED_PARTICLE_IMAGES_STRING, constraints);
			for (int i = 0; i < requiredParticleImageCategories.size (); ++i)
			{
				String particleImageCategory = requiredParticleImageCategories.get (i);
				addConditionLabel (particleImageCategory, constraints);
			}
		}
	}

	private void addHeaderLabel (String labelText, GridBagConstraints constraints)
	{
		constraints.fill = GridBagConstraints.HORIZONTAL;
		JLabel headerLabel = new JLabel (labelText);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (Font.BOLD, 12.0f);
		headerLabel.setFont (font);
		headerLabel.setPreferredSize (new Dimension (600, 50));
		m_conditionsPanel.add (headerLabel, constraints);
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridy++;

	}

	private void addConditionLabel (String conditionName, GridBagConstraints constraints)
	{
		JLabel conditionLabel = new JLabel (conditionName);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (12.0f);
		conditionLabel.setFont (font);
		conditionLabel.setPreferredSize (new Dimension (400, 25));
		m_conditionsPanel.add (conditionLabel, constraints);
		constraints.gridy++;
	}

	private void createBackButton ()
	{
		JButton backButton = ButtonFactory.createGradientButton (16.0f, UIConstants.GEOPOD_GREEN, false);
		backButton.setText ("BACK");
		backButton.addActionListener (m_assessmentListener);
		backButton.setActionCommand ("BACK");
		backButton.setFocusPainted (false);

		m_constraints.fill = GridBagConstraints.NONE;
		m_constraints.anchor = GridBagConstraints.PAGE_START;
		m_constraints.weighty = 1.0;
		m_constraints.insets = new Insets (0, 0, 30, 0);
		this.add (backButton, m_constraints);
		m_constraints.gridy++;
	}

	@Override
	public void itemStateChanged (ItemEvent e)
	{

		m_unmetConditionsDisplayed = !m_unmetConditionsDisplayed;

		/*if (m_unmetConditionsDisplayed)
		{
			// Going to be displaying list of unmetConditions, so call reCheckAssessment, 
			//which fires an assessment event, which will
			// cause an AssessmentPanel to be displayed if 
			//appropriate, otherwise, the list of unmet 
			//conditions will be updated and displayed
			reCheckAssessment ();
		}
		else
		{
			// Were displaying only unmet conditions, now want to display all, so have to add all again
			refreshConditionsList ();
		}*/
		reCheckAssessment();
	}

	public void refreshConditionsList (FlightDataRecorder currentDataRecord)
	{
		if (m_unmetConditionsDisplayed)
		{
			Conditions unmetConditions = Conditions.determineUnmetConditions (m_conditions, currentDataRecord);
			addConditions (unmetConditions);
		}
		else
		{
			addConditions (m_conditions);
		}
		this.updateUI ();
	}

	// Fires Assessment event, which will make AssessmentPanel visible if appropriate,
	// or if not, updates list of unmet conditions
	public void reCheckAssessment ()
	{
		ActionEvent actionEvent = new ActionEvent (this, ActionEvent.ACTION_PERFORMED, "Assessment");
		m_assessmentListener.actionPerformed (actionEvent);
	}
}
