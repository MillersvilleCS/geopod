<?php
// Con

session_start ();

require_once ('config.php');

require_once ('DatabaseConnection.php');

// Get the user ID, or go back to the login page.
if (!($userId = $_SESSION['userId']))
{
	header ('HTTP/1.1 401 Unauthorized');
	header ('Content-Type: application/json');
	die (json_encode (array ('message' => 'Not logged in.')));
}

$dbConnection = new DatabaseConnection ($dbServer, $dbUser, $dbPassword,
		$databaseName);

$oldPassword = $dbConnection->quoteValue($_REQUEST['oldPassword']);

$whereClauses  = " userId = '$userId' \n";
$whereClauses .= "AND password = SHA1 (CONCAT ($oldPassword, `salt`)) LIMIT 1";
// Query the database for a record with userId and provided old password
$dbItr = $dbConnection->select("Users", "firstName", $whereClauses);

// Confirm entered old password by seeing if iterator is valid.
$oldPasswordConfirmed = $dbItr->valid ();

//Let client know if old password was successfully confirmed.
echo (json_encode ($oldPasswordConfirmed));