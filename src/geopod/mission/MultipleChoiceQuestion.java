package geopod.mission;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MultipleChoiceQuestion
		extends Question
{
	private List<Answer> m_answers = null;

	public MultipleChoiceQuestion (Element question)
	{
		m_answers = new ArrayList<Answer> ();
		readQuestionText (question);
		readAnswers (question);
	}

	private void readAnswers (Element question)
	{
		NodeList answers = question.getElementsByTagName ("answer");
		for (int i = 0; i < answers.getLength (); ++i)
		{
			Answer newAnswer = new Answer ((Element) answers.item (i));
			m_answers.add (newAnswer);
			checkForFailure (newAnswer);
		}
	}

	public List<Answer> getAnswers ()
	{
		return (m_answers);
	}
	
	public boolean isMultiChooseMany ()
	{
		boolean sawOneCorrect = false;
		for (Answer answer : m_answers)
		{
			if(!sawOneCorrect)
				sawOneCorrect = answer.getIsCorrect ();
			else if (answer.getIsCorrect ())
				return (true);
		}
		return (false);
	}
	
	public int calculateTotalQuestionValue()
	{
		int totalPoints = 0;
		for (Answer answer : m_answers)
		{
			if (answer.getIsCorrect ())
			{
				totalPoints += answer.getPointValue ();
			}
		}
		return (totalPoints);
	}
}
