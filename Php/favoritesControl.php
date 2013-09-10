<?php
session_start ();

// Include database connection information
require_once('./config.php');
require_once('./DatabaseConnection.php');
$db = new DatabaseConnection ($dbServer, $dbUser, $dbPassword, $databaseName);

// Get the user ID, or go back to the login page.
if (!($userId = $_SESSION['userId']))
{
	header ('HTTP/1.1 401 Unauthorized');
	header ('Content-Type: application/json');
	die (json_encode (array ('message' => 'Not logged in.')));
}

$missionId = $db->escapeString ($_REQUEST['missionId']);
$action = $_REQUEST['action'];

if ($action == 'remove')
{
	$whereCondition = "missionId = $missionId AND userId = $userId";
	$db->delete ('UserFavorites', $whereCondition);
	
	$resultIterator = $db->select("Missions", "isFrozenCopy", "missionId = $missionId");
	$isFrozenCopy = $resultIterator->current ()->isFrozenCopy;
	if($isFrozenCopy == '1')
	{
		// This mission was saved as a frozen copy 
		// in favorites. No longer in favorites, so remove
		require_once('deleteMission.php');
		deleteMission($db, $missionId);
	}
}
else if ($action == 'add' || $action == 'freezeCopy')
{
	if($action == 'freezeCopy')
	{
		$missionId = copyMission($missionId, $db);
	}

	// Add mission to favorites
	$results = $db
	->insert ('UserFavorites',
	array ('userId' => $userId, 'missionId' => $missionId,
							'favoritedOnDate' => 'NOW()',
							'lastExportedOnDate' => '0000-00-00'));
}

function copyMission($oldMissionId, $dbConnection)
{
	// get Missions table entry for old mission
	$result = $dbConnection->select("Missions", "*", "missionId = $oldMissionId");
	$missionColumnsAndValues = (array)$result->current();

	// remove missionId from columnsAndValues and set isFrozenCopy appropriately
	unset($missionColumnsAndValues['missionId']);
	$missionColumnsAndValues['isFrozenCopy'] = 1;

	$dbConnection->quoteValues($missionColumnsAndValues);

	// put new entry in Missions table
	$result = $dbConnection->insert("Missions", $missionColumnsAndValues);
	$newMissionId = $result[1];

	// get Objectives table entries for old mission
	$result = $dbConnection->select("Objectives", "*", "missionId = $oldMissionId");

	// build array of objectives need to duplicate
	$objectivesToDuplicate = array();
	while ($result->valid()) {
		// get each objective in old mission
		$objective = (array) $result->current();

		// add to list to duplicate
		// can't do inserts inside here (right??)
		$objectivesToDuplicate[] = $objective;

		$result->next();
	}

	// duplicate each objective that was in mission,
	// and add new and old objective ids to list so can do same for questions
	$listOfCopiedObjectives = array();
	foreach ($objectivesToDuplicate as $objective) {
		// get id of objective
		$oldObjectiveId = $objective['objectiveId'];

		// remove objectiveId field and set missionId appropriately
		unset($objective['objectiveId']);
		$objective['missionId'] = $newMissionId;

		$dbConnection->quoteValues($objective);

		$result = $dbConnection->insert("Objectives", $objective);
		$newObjectiveId  = $result[1];

		$listOfCopiedObjectives[$oldObjectiveId] = $newObjectiveId;
	}

	// list to keep track of questions that were duplicated, so can 
	// go through later and duplicate their answers
	$listOfCopiedQuestions = array();

	// for each objective copied, get any questions for that objective and
	// duplicate them with new objective Id
	foreach ($listOfCopiedObjectives as $oldObjectiveId => $newObjectiveId) {
		// get all questions for this objective
		$result = $dbConnection->select("Questions", "*", "objectiveId = $oldObjectiveId");

		// build list of questions that will need to duplicate
		$questionsToDuplicate = array();
		while($result->valid())
		{
			$question = (array)$result->current();
			$questionsToDuplicate[] = $question;
			$result->next();
		}

		// duplicate each question with new objective id
		foreach ($questionsToDuplicate as $question) {
				
			$oldQuestionId = $question['questionId'];
				
			unset($question['questionId']);
			$question['objectiveId'] = $newObjectiveId;
				
			$dbConnection->quoteValues($question);
				
			$result = $dbConnection->insert("Questions", $question);
			$newQuestionId = $result[1];
				
			$listOfCopiedQuestions[$oldQuestionId] = $newQuestionId;
		}

	}

	// for each question copied, duplicate any answers with new questionIds
	foreach ($listOfCopiedQuestions as $oldQuestionId => $newQuestionId) {
		
		// get all answers for particular question
		$result = $dbConnection->select("Answers", "*", "questionId = $oldQuestionId");
		
		// bulid list of answers to duplicate
		$answersToDuplicate = array();
		while ($result->valid()) {
			$answer = (array) $result->current();
			$answersToDuplicate[] = $answer;
			$result->next();
		}
		
		// duplicate answers with correct questionId
		foreach ($answersToDuplicate as $answer) {
				
			unset($answer['answerId']);
			$answer['questionId'] = $newQuestionId;
				
			$dbConnection->quoteValues($answer);
				
			$dbConnection->insert("Answers", $answer);
		}
	}

	return ($newMissionId);
}