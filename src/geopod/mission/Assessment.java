package geopod.mission;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Assessment
		extends Failable
{
	private List<Question> m_questions;

	public Assessment (Element assessment)
	{
		m_questions = new ArrayList<Question> ();

		NodeList questions = assessment.getElementsByTagName ("question");
		for (int i = 0; i < questions.getLength (); ++i)
		{
			NodeList dropdowns = ((Element) questions.item (i)).getElementsByTagName ("dropdown");
			Question nextQuestion;
			if (dropdowns.getLength () > 0)
			{
				nextQuestion = new MultiDropdownQuestion ((Element) questions.item (i));
			}
			else
			{
				nextQuestion = new MultipleChoiceQuestion ((Element) questions.item (i));
			}
			m_questions.add (nextQuestion);
			checkForFailure (nextQuestion);
		}
	}

	public List<Question> getQuestions ()
	{
		return (m_questions);
	}

	public boolean hasQuestions ()
	{
		return (m_questions.size () > 0);
	}
}
