<?php

session_start ();

// Include database connection information
require_once('config.php');

/* Connect to database */
$mysqli = new mysqli ($dbServer, $dbUser, $dbPassword, $databaseName);

if ($mysqli->connect_error)
{
	die ("$mysqli->connect_errno: $myslqi->connect_error");
}

// Get the user ID, or go back to the login page.
if (!($userId = $_SESSION['userId']))
{
	header ('HTTP/1.1 401 Unauthorized');
	header ('Content-Type: application/json');
	die (json_encode (array ('message' => 'Not logged in.')));
}

// Build query.
$sql = "SELECT categoryId,categoryName FROM Categories";

// Run the query
if ($result = $mysqli->query ($sql))
{
	// Convert the results to an array of arrays that can be encoded as JSON.
	$categories = array ();
	while ($row = $result->fetch_assoc ())
	{
		$categories[] = $row;
	}
	$result->close ();

	// Set headers for JSON data
	header ('Cache-Control: no-cache, must-revalidate');
	header ('Expires: Mon, 26 Jul 1997 05:00:00 GMT');
	header ('Content-type: application/json');

	echo json_encode ($categories);
}
else
{
	print "Failed to execute query\n";
	die ("Error " . $mysqli->errno . ": " . $mysqli->error);
}
