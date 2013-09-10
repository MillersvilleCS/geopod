<?php

// makes changes to mission specified with 'missionId'. Changes are encapsulated in 'mission' object -
// an array of fieldNames and new values, so that just changed fields are sent & updated.

session_start ();

require_once('config.php');

require_once('DatabaseConnection.php');

require_once('objectivesUtility.php');
require_once ('questionsUtility.php');
require_once ('answersUtility.php');

$dbConnection = new DatabaseConnection ($dbServer, $dbUser, $dbPassword,
		$databaseName);

if (!$_SESSION['userId'])
{
	// No user ID, so go back to the login page
	header ('HTTP/1.1 401 Unauthorized');
	header ('Content-Type: application/json');
	die (json_encode (array ('message' => 'Not logged in.')));
}

/***Id's***/
// id of mission to update
$missionId = $_REQUEST['missionId'];
// array of objective#s to objectiveIds, sent from javascript
// for those objectives that were in database and alredy have ids
$objectiveIds = json_decode($_REQUEST['objectiveIds'], true);
// array of question numbers mapped to qustionIds, to be used
// when adding answers
$questionIds = json_decode($_REQUEST['questionIds'], true);

/***Mission***/
$missionAsString = $_REQUEST['mission'];
$mission = json_decode ($missionAsString, true);

// ensure any values in mission get quoted
$dbConnection->quoteValues($mission);

// would not be calling this file if something were not changed,
// so reflect that in mission
$mission["lastModifiedOnDate"] = "NOW()";

// will always have soemthing to update here, if only the date
$dbConnection->update ("Missions", $mission, "missionId = $missionId");



/***Objectives***/
$objectivesInfoAsString = $_REQUEST['objectivesInfo'];
$objectivesInfo = json_decode ($objectivesInfoAsString, true);

// add any new objectives
// {[objectiveNumber] => {objectiveTitle: "--", objectiveDescription: "--", order: "--"}, ...}
$objectivesToAdd = $objectivesInfo['objectivesToAdd'];
$addedObjectiveIds = addObjectives ($objectivesToAdd, $missionId, $dbConnection);
// ensire have complete map of objective numbers to objectiveIds
// contains all possible objective numbers, so can get id for any question
$objectiveIds = array_merge($objectiveIds, $addedObjectiveIds);

// update any existing objectives that were changed
// {[objectiveId] => {columns and values to change}, ...}
$objectivesToUpdate = $objectivesInfo['objectivesToUpdate'];
foreach ($objectivesToUpdate as $objectiveId => $objectiveFields)
{
	$dbConnection->quoteValues($objectiveFields);
	$dbConnection
	->update ('Objectives', $objectiveFields,
			"objectiveId = $objectiveId");
}

// delete objectives that were removed
// [objectiveId, objectiveId, ...]
$objectivesToDelete = $objectivesInfo['objectivesToDelete'];
foreach ($objectivesToDelete as $objectiveId)
{
	$dbConnection->delete ('Objectives', "objectiveId = $objectiveId");
}



/***Questions***/
$questionsInfoAsString = $_REQUEST['questionsInfo'];
$questionsInfo = json_decode ($questionsInfoAsString, true);

// add new questions, get id's and add to list for future use
// { [quest#]=>{ questionTypeId->"--" questionText->"--" order->"--" objectiveId->"obj#" } ... }
$questionsToAdd = $questionsInfo['questionsToAdd'];
$addedQuestionIds = addQuestions($questionsToAdd, $objectiveIds, $dbConnection);
$questionIds = array_merge($questionIds, $addedQuestionIds);

// make changes to existing questions
// { [questionId]=>{columns and values to change} ... }
$questionsToUpdate = $questionsInfo['questionsToUpdate'];
foreach ($questionsToUpdate as $questionId => $questionFields) {
	$dbConnection->quoteValues($questionFields);
	$dbConnection->update('Questions', $questionFields, "questionId = $questionId");
}

// delete questions
// [questionId, questionId, ... ]
$questionsToDelete = $questionsInfo['questionsToDelete'];
foreach ($questionsToDelete as $questionId) {
	$dbConnection->delete('Questions', "questionId = $questionId");
}

/***Answers***/
$answerInfoAsString = $_REQUEST['answersInfo'];
$answersInfo = json_decode ($answerInfoAsString, true);

// add any new answers
$answersToAdd = $answersInfo['answersToAdd'];
$answerIds = addAnswers($answersToAdd, $questionIds, $dbConnection);

// change existiong answers
$answersToUpdate = $answersInfo['answersToUpdate'];
foreach ($answersToUpdate as $answerId => $answerFields) {
	$dbConnection->quoteValues($answerFields);
	$dbConnection->update('Answers', $answerFields, "answerId = $answerId");
}

// delete answers
$answersToDelete = $answersInfo['answersToDelete'];
foreach ($answersToDelete as $answerId) {
	$dbConnection->delete('Answers', "answerId = $answerId");
}

/***Handle mission id change***/
// if we're changing mission id in update, need to make sure objectives reflect this as well
$changingMissionId = isset ($mission['missionId']);
if($changingMissionId)
{
	$missionIdUpdate['missionId'] = $mission['missionId'];
	$dbConnection->update("Objectives", $missionIdUpdate, "missionId = $missionId");
}

// return id's of added objectives & questions
$updateResults['newObjectiveIds'] = $addedObjectiveIds;
$updateResults['newQuestionIds'] = $addedQuestionIds;
$updateResults['newAnswerIds'] = $answerIds;
echo json_encode ($updateResults);
