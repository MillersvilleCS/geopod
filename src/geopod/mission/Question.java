package geopod.mission;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Question
		extends Failable
{
	protected String m_questionText = "No Question";

	public String getQuestionText ()
	{
		return (m_questionText);
	}

	protected void readQuestionText (Element question)
	{
		NodeList questionText = question.getElementsByTagName ("questionText");
		if (questionText.getLength () == 1)
		{
			m_questionText = questionText.item (0).getChildNodes ().item (0).getNodeValue ();
		}
		else
		{
			recordInitializationFailure ("Mission initialization failed: missing question text");
		}
	}
}
