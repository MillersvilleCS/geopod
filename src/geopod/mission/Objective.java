package geopod.mission;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Objective
		extends Failable
{
	private int m_number;
	private String m_title;
	private String m_description;
	private Conditions m_conditions;
	private Assessment m_assessment;

	public Objective (Element objective, int number)
	{
		m_number = number;
		m_title = "No Title";
		m_description = "No description available";
		m_conditions = null;
		m_assessment = null;

		readTitle (objective);
		readDescription (objective);
		readConditions (objective);
		readAssessment (objective);
	}

	private void readTitle (Element objective)
	{
		NodeList titleElements = objective.getElementsByTagName ("title");
		if (titleElements.getLength () == 1)
		{
			m_title = titleElements.item (0).getChildNodes ().item (0).getNodeValue ();
		}
		else
		{
			recordInitializationFailure ("Mission initialization failed: missing objective title");
		}
	}

	private void readDescription (Element objective)
	{
		NodeList description = objective.getElementsByTagName ("description");
		if (description.getLength () == 1)
		{
			m_description = description.item (0).getChildNodes ().item (0).getNodeValue ();
		}
		else
		{
			recordInitializationFailure ("Mission initialization failed: missing objective description");
		}
	}

	private void readConditions (Element objective)
	{
		NodeList conditions = objective.getElementsByTagName ("conditions");
		if (conditions.getLength () == 1)
		{
			Element condition = (Element) conditions.item (0);
			m_conditions = new Conditions (condition);
		}
		else
		{
			recordInitializationFailure ("Mission initialization failed: missing objective conditions");
		}
	}

	private void readAssessment (Element objective)
	{
		NodeList assessments = objective.getElementsByTagName ("assessment");
		if (assessments.getLength () == 1)
		{
			Element assessment = (Element) assessments.item (0);
			m_assessment = new Assessment (assessment);
			checkForFailure (m_assessment);
		}
		else
		{
			recordInitializationFailure ("Mission initialization failed: missing objective assessment");
		}
	}
	
	public int getNumber()
	{
		return (m_number);
	}

	public String getTitle ()
	{
		return (m_title);
	}

	public String getDescription ()
	{
		return (m_description);
	}

	public Assessment getAssessment ()
	{
		return (m_assessment);
	}

	public Conditions getConditions ()
	{
		return (m_conditions);
	}

	public boolean hasAssessmentQuestions ()
	{
		return (m_assessment.hasQuestions ());
	}
}
