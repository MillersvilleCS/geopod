<?php

// removes mission with id in 'missionId' from database

session_start ();

require_once('config.php');

require_once('DatabaseConnection.php');

require_once('deleteMission.php');

$dbConnection = new DatabaseConnection ($dbServer, $dbUser, $dbPassword,
		$databaseName);

if (!$_SESSION['userId'])
{
	// No user ID, so go back to the login page
	header ('HTTP/1.1 401 Unauthorized');
	header ('Content-Type: application/json');
	die (json_encode (array ('message' => 'Not logged in.')));
}

$missionId = $dbConnection->escapeString($_REQUEST['missionId']);

// ensure current user can delete this mission
$userId = $_SESSION['userId'];
$result = $dbConnection->select ("Missions", "lastModifiedByUserId", "missionId = $missionId");
$ownerId = $result->current ()->lastModifiedByUserId;

if ($userId == $ownerId)
{
	// remove mission from favorites table
	$dbConnection->delete ('UserFavorites', "missionId = $missionId");

	deleteMission($dbConnection, $missionId);
}