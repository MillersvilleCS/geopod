<?php

// Changes the visibility of a mission with specified id to public or private

session_start ();

require_once('config.php');

require_once('DatabaseConnection.php');

$dbConnection = new DatabaseConnection ($dbServer, $dbUser, $dbPassword,
		$databaseName);

if (!$_SESSION['userId'])
{
	// No user ID, so go back to the login page
	header ('HTTP/1.1 401 Unauthorized');
	header ('Content-Type: application/json');
	die (json_encode (array ('message' => 'Not logged in.')));
}

// Get mission id from website
$missionId = $dbConnection->escapeString($_REQUEST['missionId']);
// Get visibility from website
$isPublic = $dbConnection->escapeString($_REQUEST['isPublic']);

// ensure current user can change this mission
$userId = $_SESSION['userId'];
// get the mission last modified by the user
$result = $dbConnection->select ("Missions", "lastModifiedByUserId", "missionId = $missionId");
$ownerId = $result->current ()->lastModifiedByUserId;

if ($userId == $ownerId)
{
	// Map the value in column isPublic to current visibility
	$columnValues['isPublic'] = "$isPublic";
	// Only change visibility for missions with given id
	$whereClause  = " missionId = '$missionId' ";
	
	$value .= $dbConnection->update('Missions',$columnValues, $whereClause);
	echo ($value);
}
