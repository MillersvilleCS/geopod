<?php

session_start ();

require_once('config.php');
require_once('DatabaseConnection.php');

require_once('encodingUtility.php');

require_once('encryptionUtility.php');

$dbConnection = new DatabaseConnection ($dbServer, $dbUser, $dbPassword, $databaseName);

$userId = $_SESSION['userId'];
if (!$userId)
{
	// No user ID, so return to missionControl page
	// when it reloads, will get the "login invalid" popup
	header('Location: ../missionControl.html');
	exit();
}

// get mission id from php request
$missionId = $_REQUEST['missionId'];

// get "Missions" entry for this mission
$missionColumns = array('missionName', 'backgroundText', 'missionStatusId', 'isPublic', 'lastModifiedByUserId');
$resultIterator = $dbConnection->select("Missions", $missionColumns, "missionId = $missionId");
$missionData = $resultIterator->current();

// determine user allowed to export this mission
// can export mission if 1) it's complete and 2) either user is owner, or it's public and user is instructor
if($missionData->missionStatusId == 0 &&
		($missionData->lastModifiedByUserId == $userId || ($missionData->isPublic == 1 && $_SESSION['userStatus'] == 0)) )
{
	// update the last exported date if this mission was a favorite
	$exportedOnDate['lastExportedOnDate'] = "NOW()";
	$wasFavoriteMission = "missionId = $missionId AND userId = $userId";
	$dbConnection->update("UserFavorites", $exportedOnDate, $wasFavoriteMission);

	// header stuff to make user able to download
	header("Cache-Control: public");
	header("Content-Description: File Transfer");
	header("Content-Disposition: attachment; filename=" . str_replace(' ', '_', $missionData->missionName) . ".geo");
	header("Content-Type: application/geopod");
	header("Content-Transfer-Encoding: base64");

	// create xml content
	$missionXml = beginTag("mission");

	$missionXml .= xmlEntry("missionId", $missionId);
	$missionXml .= xmlEntry("missionTitle", $missionData->missionName);
	// remove &nbsp (inserted by CKeditor) which kills xml & html readers
	$backgroundWithoutNbsp = str_replace("&nbsp;", " ", $missionData->backgroundText);
	$missionXml .= xmlEntry("background", $backgroundWithoutNbsp);
	$missionXml .= xmlEntry("objectives", encodeObjectives($dbConnection, $missionId));

	$missionXml .= endTag("mission");

	$missionXmlEncrypted = encryptText($missionXml);
	echo ($missionXmlEncrypted);
}







