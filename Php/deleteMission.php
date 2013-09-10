<?php

function deleteMission($dbConnection, $missionId)
{
	// take mission out of Missions table
	$dbConnection->delete ("Missions", "missionId = $missionId");
	
	// delete questions for objectives in this mission
	$resultItr = $dbConnection->select('Objectives', 'objectiveId', "missionId = $missionId");
	while ($resultItr->valid()) {
		$objectiveId = $resultItr->current()->objectiveId;
		$resultItr2 = $dbConnection->select('Questions', 'questionId', "objectiveId = $objectiveId");
		while ($resultItr2->valid())
		{
			// delete answers to questions from objectives for this mission
			$questionId = $resultItr2->current()->questionId;
			$dbConnection->delete('Answers', "questionId = $questionId");
			$resultItr2->next();
		}
		$dbConnection->delete('Questions', "objectiveId = $objectiveId");
		$resultItr->next();
	}
	
	// get rid of any objectives for this mission
	$dbConnection->delete ('Objectives', "missionId = $missionId");
}