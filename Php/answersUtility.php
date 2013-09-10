<?php
// given a specific question id, returns a list of answers attached to
// that question, markered with an answerNumber that is unique within this
// mission
// returned format:
// {[counter#]={[answerId]="--" [answerText]="--" [isCorrectt]="--" [points]="---" [placementMarker]="---" [order]="--"} [counter#]={---} ... }
function getAnswers ($questionId, &$answerCounter, $dbConnection)
{
	$answersColumns = array('answerId', 'answerText', 'isCorrect', 'points', 'placementMarker', 'order');
	$resultIterator = $dbConnection->select("Answers", $answersColumns, "questionId = $questionId", 'order', true);
	$answersList = array();
	while($resultIterator->valid())
	{
		$answer = $resultIterator->current();
		$answersList[$answerCounter] = $answer;
		$resultIterator->next();
		++$answerCounter;
	}
	return ((object)$answersList);
}


// adds given answers to database, associated with correct questionIds from list provided
// returns map of answer numbers to answerIds
function addAnswers($answers, $questionIds, $dbConnection)
{
	$answerIds = array();
	foreach($answers as $answerNumber => $answerData)
	{
		// convert data to array form for database connection
		$answerData = (array) $answerData;

		// replace question number with appropriate id
		$questionNumber = $answerData['questionId'];
		$questionId = $questionIds[$questionNumber];
		$answerData['questionId'] = $questionId;

		// quote answer data but preserve placement marker's value,
		// in case is null, don't want that quoted
		$placementMarker = $answerData['placementMarker'];
		$dbConnection->quoteValues($answerData);
		$answerData['placementMarker'] = $placementMarker;

		// insert into database, keep track of insert id by answer number
		$result = $dbConnection->insert('Answers', $answerData);
		$answerIds[$answerNumber] = $result[1];
	}
	return ($answerIds);
}