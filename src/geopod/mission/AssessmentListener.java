package geopod.mission;

import geopod.devices.FlightDataRecorder;
import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AssessmentListener
		implements ActionListener
{
	private ObjectivesPane m_objectivesTab;
	private FlightDataRecorder m_flightRecorder;
	private Objective m_objective;
	private Conditions m_conditions;
	private AssessmentPanel m_assessmentPanel;
	private ConditionsNotMetPanel m_conditionsNotMetPanel;
	private final String m_missionTitle;
	private final String m_missionId;

	private static final String ASSESSMENT_FILE_EXT;
	private static final String ASSESSMENT_XML_TAG;
	private static final String MISSION_TITLE_TAG;
	private static final String MISSION_ID_TAG;
	private static final String OBJECTIVE_NUMBER_TAG;
	private static final String OBJECTIVE_TITLE_TAG;
	private static final String USER_NAME_TAG;
	private static final String LOGIN_NAME_TAG;
	private static final String SUBMIT_TIME_TAG;
	static
	{
		ASSESSMENT_FILE_EXT = ".geo";

		ASSESSMENT_XML_TAG = "StudentAssessment";
		MISSION_TITLE_TAG = "missionTitle";
		MISSION_ID_TAG = "missionId";
		OBJECTIVE_NUMBER_TAG = "objectiveNumber";
		OBJECTIVE_TITLE_TAG = "objectiveTitle";
		USER_NAME_TAG = "userEnteredName";
		LOGIN_NAME_TAG = "userLoginName";
		SUBMIT_TIME_TAG = "submitTime";
	}

	public AssessmentListener (Objective objective, ObjectivesPane objectivesTab, FlightDataRecorder flightRecorder,
			Mission mission)
	{
		m_objectivesTab = objectivesTab;
		m_flightRecorder = flightRecorder;
		m_objective = objective;
		m_conditions = m_objective.getConditions ();
		Assessment currentAssessment = m_objective.getAssessment ();
		m_assessmentPanel = new AssessmentPanel (currentAssessment, this);
		m_conditionsNotMetPanel = new ConditionsNotMetPanel (m_conditions, this);
		m_missionTitle = mission.getMissionTitle ();
		m_missionId = mission.getMissionId ();
	}

	@Override
	public void actionPerformed (ActionEvent e)
	{
		if (e.getActionCommand ().equals ("Assessment"))
		{
			Debug.print (DebugLevel.MEDIUM, "Reassessing...");
			boolean conditionsMet = m_conditions.haveConditionsBeenMet (m_flightRecorder);
			if (conditionsMet)
			{
				m_objectivesTab.setViewportView (m_assessmentPanel);
				Debug.println (DebugLevel.MEDIUM, "can take assessment");

			}
			else
			{
				m_conditionsNotMetPanel.refreshConditionsList (m_flightRecorder);
				m_objectivesTab.setViewportView (m_conditionsNotMetPanel);
				Debug.println (DebugLevel.MEDIUM, "cannot take assessment");
			}
		}
		else if (e.getActionCommand ().equals ("BACK"))
		{
			m_objectivesTab.resetToObjectivesView ();
		}
		else if (e.getActionCommand ().equals ("SUBMIT"))
		{
			boolean proceedWithSubmit = checkNameBeforeSubmit ();
			if (proceedWithSubmit)
			{
				boolean assessmentFileOk = produceAssessmentFile ();
				if (!assessmentFileOk)
					System.err.println ("Error: could not create assessment file");
			}
		}
	}

	private boolean checkNameBeforeSubmit ()
	{
		if (!m_assessmentPanel.hasValidName ())
		{
			NameEntryWindow nameEntryWindow = new NameEntryWindow (m_assessmentPanel);
			nameEntryWindow.setVisible (true);
			while (nameEntryWindow.isVisible ())
			{
				try
				{
					wait ();
				}
				catch (InterruptedException e)
				{
					if (Debug.isDebuggingOn ())
						e.printStackTrace ();
					return (false);
				}
			}
			return (m_assessmentPanel.hasValidName ());
		}

		return (true);
	}

	private boolean produceAssessmentFile ()
	{
		String assessmentFileTitle = m_missionTitle + "-";
		assessmentFileTitle += m_objective.getTitle () + "-";
		assessmentFileTitle += m_assessmentPanel.getUserEnteredName ();
		assessmentFileTitle = assessmentFileTitle.replace (' ', '_');
		assessmentFileTitle += ASSESSMENT_FILE_EXT;

		try
		{
			String fileWritePath = System.getProperty ("user.home");
			fileWritePath += "/Desktop/" + assessmentFileTitle;

			Document assessmentXml = createAssessmentDocument ();

			Transformer xmlTransformer = TransformerFactory.newInstance ().newTransformer ();
			xmlTransformer.setOutputProperty (OutputKeys.OMIT_XML_DECLARATION, "yes");
			DOMSource source = new DOMSource (assessmentXml);
			StreamResult result = new StreamResult (new StringWriter ());
			xmlTransformer.transform (source, result);

			String xmlAsString = result.getWriter ().toString ();
			// now we have a string, encode with encoder then write to file
			String encodedXml = MissionEncrypter.getEncrypter ().encrypt (xmlAsString);
			FileUtils.writeStringToFile (new File (fileWritePath), encodedXml);

		}
		catch (Exception e)
		{
			if (Debug.isDebuggingOn ())
				e.printStackTrace ();
			return (false);
		}

		return (true);
	}

	private Document createAssessmentDocument ()
	{
		Document assessmentXml = XmlUtility.createDocument ();
		if (assessmentXml != null)
		{
			Element mainElement = assessmentXml.createElement (ASSESSMENT_XML_TAG);
			assessmentXml.appendChild (mainElement);

			XmlUtility.generateXmlElement (assessmentXml, mainElement, MISSION_TITLE_TAG, m_missionTitle);
			XmlUtility.generateXmlElement (assessmentXml, mainElement, MISSION_ID_TAG, m_missionId);
			String objectiveNumber = String.valueOf (m_objective.getNumber ());
			XmlUtility.generateXmlElement (assessmentXml, mainElement, OBJECTIVE_NUMBER_TAG, objectiveNumber);
			String objectiveTitle = m_objective.getTitle ();
			XmlUtility.generateXmlElement (assessmentXml, mainElement, OBJECTIVE_TITLE_TAG, objectiveTitle);
			String userEnteredName = m_assessmentPanel.getUserEnteredName ();
			XmlUtility.generateXmlElement (assessmentXml, mainElement, USER_NAME_TAG, userEnteredName);
			String userLoginName = System.getProperty ("user.name");
			XmlUtility.generateXmlElement (assessmentXml, mainElement, LOGIN_NAME_TAG, userLoginName);
			SimpleDateFormat dateFormatter = new SimpleDateFormat ("hh:mm:ss a z 'on' E MMMM dd, yyyy");
			String dateFormatedText = dateFormatter.format (new Date ());
			XmlUtility.generateXmlElement (assessmentXml, mainElement, SUBMIT_TIME_TAG, dateFormatedText);

			m_assessmentPanel.generateAssessmentContent (assessmentXml);
		}

		return (assessmentXml);
	}
}
