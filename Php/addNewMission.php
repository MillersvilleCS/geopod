<?php

// inserts a mission with the given values (as a JSON string passed in data with identifier 'mission') into database
// list of values to include all data assocaited with a row in Missions, except for lastModifiedOn, lastModifiedByUserId, and geopodVersion
// returns the mission id of the newly inserted mission

session_start ();

require_once('config.php');

require_once('DatabaseConnection.php');

require_once ('objectivesUtility.php');
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

// adding mission stuff
$missionAsString = $_REQUEST['mission'];
$mission = json_decode ($missionAsString, true);

$needToSetCreatedDate = ($mission["createdOnDate"] == -1);

$latestGeopodVersion = "99.99.99";
include_once('getGeopodVersion.php');

$userId = $_SESSION['userId'];

$mission['estimatedTimeToComplete'] .= ":00";

$mission['lastModifiedByUserId'] = $userId;
$mission['geopodVersion'] = $latestGeopodVersion;

$dbConnection->quoteValues($mission);

if ($needToSetCreatedDate)
{
	$mission["createdOnDate"] = "NOW()";
}

$mission['lastModifiedOnDate'] = "NOW()";

$result = $dbConnection->insert ('Missions', $mission);

$missionId = $result[1];
$returnArray["missionId"] = $missionId;


// add objectives stuff, use missionId
$objectivesAsString = $_REQUEST['objectives'];
$objectives = json_decode ($objectivesAsString);
$objectiveIds = addObjectives($objectives, $missionId, $dbConnection);

$returnArray["objectiveIds"] = $objectiveIds;


// add questions stuff, use objectiveIds
$questionsAsString = $_REQUEST['questions'];
$questions = json_decode($questionsAsString);
$questionIds = addQuestions($questions, $objectiveIds, $dbConnection);

$returnArray["questionIds"] = $questionIds;


// add answers, using questionIds
$answersAsString = $_REQUEST['answers'];
$answers = json_decode($answersAsString);
$answerIds = addAnswers($answers, $questionIds, $dbConnection);

$returnArray["answerIds"] = $answerIds;

echo json_encode($returnArray);
