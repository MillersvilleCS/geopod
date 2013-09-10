<?php
// given a specific objective id, returns an array 
// of questionNumbers mapping to arrays of question data
// questionNumbers will be set by counter, so will be
// in form javascript will like (easy to use just one counter)
// {[counter#]={[questionId]="--" [questionTypeId]="--" [questionText]="--" [order]="--"} [counter#]={---} ... }
function getQuestions ($objectiveId, &$questionCounter, &$answerCounter, &$allAnswersList, $dbConnection)
{
	$questionsColumns = array('questionId', 'questionTypeId', 'questionText', 'order');
	$resultIterator = $dbConnection->select("Questions", $questionsColumns, "objectiveId = $objectiveId", 'order', true);
	$questionsList = array();
	while($resultIterator->valid())
	{
		$question = $resultIterator->current();
		$questionsList[$questionCounter] = $question;
		$answers = getAnswers($question->questionId, $answerCounter, $dbConnection);
		$allAnswersList[$questionCounter] = $answers;
		$resultIterator->next();
		++$questionCounter; 
	}
	return ((object)$questionsList);
}

// add given questions to database for given objectiveId
// $questions parameter will be in the form:
// {[question#]=>{[questionTypeId]=>"--" [questionText]=>"--" [order]=>"--"} ... }
// will return corrisponding question ids in form: [[question#]=>"--" ... ]
function addQuestions ($questions, $objectiveIds, $dbConnection)
{
	$questionIds = array();
	foreach ($questions as $questionNumber => $questionData)
	{
		// convert to array since thats what dbConnection likes
		$questionData = (array)$questionData;
		
		// get objective number, look up & substitute appropriate id
		$objectiveNumber = $questionData['objectiveId'];
		$questionData['objectiveId'] = $objectiveIds[$objectiveNumber];
		
		$dbConnection->quoteValues($questionData);
		
		$result = $dbConnection->insert('Questions', $questionData);
		$questionIds[$questionNumber] = $result[1];
	}
	return ($questionIds);
}