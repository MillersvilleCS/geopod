<?php

// given a mission id (passed in data as 'missionId') return a mission object containing mission contents.

session_start ();

require_once('config.php');

require_once ('DatabaseConnection.php');

require_once ('questionsUtility.php');

require_once ('answersUtility.php');

$dbConnection = new DatabaseConnection ($dbServer, $dbUser, $dbPassword, $databaseName);

$userId = $_SESSION['userId'];
if (!$userId)
{
	// No user ID, so go back to the login page
	header ('HTTP/1.1 401 Unauthorized');
	header ('Content-Type: application/json');
	die (json_encode (array ('message' => 'Not logged in.')));
}

$missionId = $_REQUEST['missionId'];

// get mission data
if($_REQUEST['viewMissionRequest'])
{
	require_once ('getViewerInfo.php');
	$mission = getMissionViewerInfo($missionId, $dbConnection);
}
else
{
	$resultIterator = $dbConnection->select("Missions", "*", "missionId = $missionId");
	$mission = $resultIterator->current();
}

// check if this user can access this mission
// can get mission contents if 1) user is owner or 2) it's complete, it's public and user is instructor
if($mission->lastModifiedByUserId == $userId ||
		($mission->missionStatusId == 0 && ($mission->isPublic == 1 && $_SESSION['userStatus'] == 0)) )
{

	$mission->estimatedTimeToComplete = substr($mission->estimatedTimeToComplete, 0, 5);
	$missionInfo["missionData"] = $mission;

	//get objectives data
	$objectivesColumns = array('objectiveId', 'objectiveTitle', 'objectiveText', 'order');
	$resultIterator = $dbConnection->select("Objectives", $objectivesColumns, "missionId = $missionId", 'order', true);
	$objectivesList = array();
	$questionNumberCounter = 0;
	$answerNumberCounter = 0;
	$allQuestionsList = array();
	$allAnswersList = array();
	while($resultIterator->valid())
	{
		$objective = $resultIterator->current();
		$objectivesList[$resultIterator->key()] = $objective;
		$questions = getQuestions($objective->objectiveId, $questionNumberCounter, $answerNumberCounter, $allAnswersList, $dbConnection);
		$allQuestionsList[$resultIterator->key()] = $questions;
		$resultIterator->next();
	}
	$missionInfo["objectiveData"] = (object)$objectivesList;

	$missionInfo["questionData"] = (object)$allQuestionsList;

	$missionInfo["answerData"] = (object)$allAnswersList;

	// json encode & return
	$missionInfoJsonString = json_encode ($missionInfo);
	echo $missionInfoJsonString;
}
else
{
	die (json_encode (array ('message' => 'User does not have permission to access mission.')));
}
