package geopod.mission;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MultiDropdownQuestion
		extends Question
{
	private List<List<Answer>> m_dropdowns;

	public MultiDropdownQuestion (Element question)
	{
		m_dropdowns = new ArrayList<List<Answer>> ();
		readQuestionText (question);
		readAnswers (question);
	}

	private void readAnswers (Element question)
	{
		NodeList dropdowns = question.getElementsByTagName ("dropdown");
		for (int i = 0; i < dropdowns.getLength (); ++i)
		{
			m_dropdowns.add (new ArrayList<Answer> ());
			NodeList dropdownAnswers = ((Element) dropdowns.item (i)).getElementsByTagName ("answer");
			for (int j = 0; j < dropdownAnswers.getLength (); ++j)
			{
				Answer dropdownAnswer = new Answer ((Element) dropdownAnswers.item (j));
				m_dropdowns.get (i).add (dropdownAnswer);
				checkForFailure (dropdownAnswer);
			}
		}
	}

	public List<List<Answer>> getDropdowns ()
	{
		return (m_dropdowns);
	}

	public int calculateTotalQuestionValue ()
	{
		int totalPoints = 0;
		for (List<Answer> dropdown : m_dropdowns)
		{
			totalPoints += getDropdownValue (dropdown);
		}
		return (totalPoints);
	}

	private int getDropdownValue (List<Answer> dropdown)
	{
		for (Answer answer : dropdown)
		{
			if (answer.getIsCorrect ())
				return (answer.getPointValue ());
		}
		return (0);
	}

}
