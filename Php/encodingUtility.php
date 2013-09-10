<?php

// returns xml beginning tag for given tag name
function beginTag($tagText)
{
	return ("<" . $tagText . ">");
}

// returns xml ending tag for given tag name
function endTag($tagText)
{
	return ("</" . $tagText . ">");
}

// returns text for xml entry, given tag name and entry text
function xmlEntry($tagText, $data)
{
	return (beginTag($tagText) . $data . endTag($tagText));
}

// return text that should be contained in "conditions" entry
function encodeConditions($dbConnection, $objectiveId)
{
	return ("");
}

// return xml representation of an answer
function encodeAnswer($answer)
{
	$answerEncoded = xmlEntry("answerText", $answer->answerText);
	$answerEncoded .= xmlEntry("isCorrect", $answer->isCorrect);
	$answerEncoded .= xmlEntry("points", $answer->points);
	return ($answerEncoded);
}

// return xml representation of a question, will be
//    <questionText>.....</questionText>
// followed by, for multi choice:
//     <answer>...</answer>
//     <answer>...</answer>
//     ...
// or, for multi dropdown, followed by:
//     <dropdown>
//     		<answer>...</answer>
//     		<answer>...</answer>
//    		...
//     </dropdown>
//     <dropdown>
//    		...
//     </dropdown>
//     ...
function encodeQuestion($dbConnection, $question)
{
	$questionId = $question->questionId;
	
	$questionEncoded = xmlEntry("questionText",$question->questionText);
	
	if($question->questionTypeId == 1)
	{
		// multi dropdown question
		$answersColumns = array('placementMarker', 'answerText', 'isCorrect', 'points');
		$orderByColumns = array('placementMarker', 'order');
		$resultIterator = $dbConnection->select("Answers", $answersColumns, "questionId = $questionId", $orderByColumns, true);
		
		$questionEncoded .= beginTag("dropdown");
		$previousDropdownNumber = 0;
		while($resultIterator->valid())
		{
			$answer = $resultIterator->current();
			$currentDropdownMarker = $answer->placementMarker;
			if($currentDropdownMarker > $previousDropdownNumber)
			{
				// we're in a new marker now
				$questionEncoded .= endTag("dropdown");
				$questionEncoded .= beginTag("dropdown");
				$previousDropdownNumber = $currentDropdownMarker;
			}
			$answerEncoded = encodeAnswer($answer);
			$questionEncoded .= xmlEntry("answer", $answerEncoded);
			$resultIterator->next();
		}
		$questionEncoded .= endTag("dropdown");
	}
	else
	{
		// multiple choice question
		$answersColumns = array('answerText', 'isCorrect', 'points');
		$resultIterator = $dbConnection->select("Answers", $answersColumns, "questionId = $questionId", 'order', true);
		
		while($resultIterator->valid())
		{
			$answer = $resultIterator->current();
			$answerEncoded = encodeAnswer($answer);
			$questionEncoded .= xmlEntry("answer", $answerEncoded);
			$resultIterator->next();
		}
	}
	return ($questionEncoded);
}

// return text that should be contained in "assessment" entry
function encodeQuestions($dbConnection, $objectiveId)
{
	$questionsEncoded = "";

	$questionsColumns = array('questionId', 'questionTypeId', 'questionText');
	$resultIterator = $dbConnection->select("Questions", $questionsColumns, "objectiveId = $objectiveId", 'order', true);

	while($resultIterator->valid())
	{
		$question = $resultIterator->current();
		$questionEncodedText = encodeQuestion($dbConnection, $question);
		$questionsEncoded .= xmlEntry("question", $questionEncodedText);
		$resultIterator->next();
	}

	return ($questionsEncoded);
}

// return xml representation of an objective
function encodeObjective($dbConnection, $objective)
{
	$objectiveId = $objective->objectiveId;
	$objectiveEncoded = xmlEntry("title", $objective->objectiveTitle);
	$objectiveEncoded .= xmlEntry("description", $objective->objectiveText);
	$objectiveEncoded .= xmlEntry("conditions", encodeConditions($dbConnection, $objectiveId));
	$objectiveEncoded .= xmlEntry("assessment", encodeQuestions($dbConnection, $objectiveId));
	return ($objectiveEncoded);
}

// return text that should be contained in "objectives" entry
function encodeObjectives($dbConnection, $missionId)
{
	$objectivesEncoded = "";

	$objectivesColumns = array('objectiveId', 'objectiveTitle', 'objectiveText');
	$resultIterator = $dbConnection->select("Objectives", $objectivesColumns, "missionId = $missionId", 'order', true);
	
	while($resultIterator->valid())
	{
		$objective = $resultIterator->current();
		$objectiveEncodedText = encodeObjective($dbConnection, $objective);
		$objectivesEncoded .= xmlEntry("objective", $objectiveEncodedText);
		$resultIterator->next();
	}

	return ($objectivesEncoded);
}