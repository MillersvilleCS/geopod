package geopod.mission;

import org.w3c.dom.Element;

public class ShortAnswerQuestion
		extends Question
{

	public ShortAnswerQuestion (Element question)
	{
		readQuestionText (question);
	}

}
