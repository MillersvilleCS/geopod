<?php

// removes mission with id in 'missionId' from database

session_start ();

require_once('config.php');

require_once('DatabaseConnection.php');

$db = new DatabaseConnection ($dbServer, $dbUser, $dbPassword, $databaseName);

// Get the user ID, or go back to the login page.
if (!($userId = $_SESSION['userId']))
{
	header ('HTTP/1.1 401 Unauthorized');
	header ('Content-Type: application/json');
	die (json_encode (array ('message' => 'Not logged in.')));
}

$oldPassword = $db->escapeString($_REQUEST['oldPassword']);
$newPassword = $db->escapeString($_REQUEST['newPassword']);
$newPasswordConfirm = $db->escapeString($_REQUEST['newPasswordConfirm']);

// Ensure the passwords match
if ( $newPassword !== $newPasswordConfirm)
{
	header ('HTTP/1.1 400 Invalid Data');
	header ('Content-Type: application/json');
	die (json_encode (array ('message' => 'Passwords do not match.')));
}

$columnValues['password'] = " SHA1(CONCAT('$newPassword', `salt`)) \n";

$whereClauses  = " userId = '$userId' \n";
$whereClauses .= " AND password = SHA1 (CONCAT ('$oldPassword', `salt`)) LIMIT 1";

$db->update('Users', $columnValues, $whereClauses);
