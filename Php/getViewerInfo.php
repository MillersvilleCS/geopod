<?php

function getMissionViewerInfo ($missionId, $dbConnection)
{	
	$sql  = "SELECT Missions.*, \n";
	$sql .= "OriginalAuthor.userName AS originalAuthor, \n";
	$sql .= "LastAuthor.userName AS author, \n";
	$sql .= "Categories.categoryName, Topics.topicName \n";
	$sql .= "FROM Missions \n";
	$sql .= "LEFT OUTER JOIN Users AS OriginalAuthor ON OriginalAuthor.userId = Missions.createdByUserId \n";
	$sql .= "LEFT OUTER JOIN Users AS LastAuthor ON LastAuthor.userId = Missions.lastModifiedByUserId \n";
	$sql .= "LEFT OUTER JOIN Categories ON Missions.categoryId = Categories.categoryId \n";
	$sql .= "LEFT OUTER JOIN Topics ON Missions.topicId = Topics.topicId \n";
	$sql .= "WHERE Missions.missionId = $missionId";
	
	$result = $dbConnection->performQuery($sql)->fetch_object ();
	return ($result);
}