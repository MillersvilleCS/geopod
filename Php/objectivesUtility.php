<?php
function addObjectives ($objectives, $missionId, $dbConnection)
{
	$objectiveIds = array();
	foreach ($objectives as $objectiveNum => $objective)
	{
		// make it be array so can add fields
		$objective = (array)$objective;
		// add mission id
		$objective["missionId"] = $missionId;
		// quote values
		$dbConnection->quoteValues($objective);
		// insert into table
		$result = $dbConnection->insert('Objectives', $objective);
		// fill in map of nums to objectiveId's to return to program
		$objectiveIds[$objectiveNum] = $result[1];
	}
	return ($objectiveIds);
}