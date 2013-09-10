<?php
// Make sure the specified username is not in the database

session_start ();

require_once ('config.php');

require_once ('DatabaseConnection.php');

$dbConnection = new DatabaseConnection ($dbServer, $dbUser, $dbPassword,
		$databaseName);

$username = $dbConnection->quoteValue($_REQUEST['username']);

// Query the database for a record with the specified field
$dbItr = $dbConnection->select("Users", "firstName", " username = $username");

// If iterator is not valid, then username is taken  
$taken = !$dbItr->valid ();

// Send server response as a boolean
echo json_encode ($taken);