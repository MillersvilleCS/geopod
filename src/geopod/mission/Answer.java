package geopod.mission;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Answer
		extends Failable
{
	private String m_answerText;
	private boolean m_isCorrect;
	private int m_pointValue;

	public Answer (Element answerElement)
	{
		readAnswerText (answerElement);
		readCorrectness (answerElement);
		readPointValue (answerElement);
	}

	private void readAnswerText (Element answerElement)
	{
		NodeList answerTextNodes = answerElement.getElementsByTagName ("answerText");
		if (answerTextNodes.getLength () == 1)
		{
			m_answerText = answerTextNodes.item (0).getChildNodes ().item (0).getNodeValue ();
		}
		else
		{
			recordInitializationFailure ("Mission initialization failed: missing answer text");
		}
	}

	private void readCorrectness (Element answerElement)
	{
		NodeList isCorrectNodes = answerElement.getElementsByTagName ("isCorrect");
		if (isCorrectNodes.getLength () == 1)
		{
			String isCorrectString = isCorrectNodes.item (0).getChildNodes ().item (0).getNodeValue ();
			m_isCorrect = isCorrectString.equals ("1");
		}
		else
		{
			recordInitializationFailure ("Mission initialization failed: missing answer isCorrect field");
		}
	}

	private void readPointValue (Element answerElement)
	{
		NodeList pointValueNodes = answerElement.getElementsByTagName ("points");
		if (pointValueNodes.getLength () == 1)
		{
			String pointValueString = pointValueNodes.item (0).getChildNodes ().item (0).getNodeValue ();
			try
			{
				m_pointValue = Integer.parseInt (pointValueString);
			}
			catch (NumberFormatException e)
			{
				recordInitializationFailure ("Mission initialization failed: invalid value in answer points field");
			}
		}
		else
		{
			recordInitializationFailure ("Mission initialization failed: missing answer points field");
		}
	}

	public String getAnswerText ()
	{
		return (m_answerText);
	}

	public boolean getIsCorrect ()
	{
		return (m_isCorrect);
	}

	public int getPointValue ()
	{
		return (m_pointValue);
	}
}
