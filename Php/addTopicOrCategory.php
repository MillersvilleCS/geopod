<?php

session_start ();

require_once('config.php');

require_once('DatabaseConnection.php');

$dbConnection = new DatabaseConnection ($dbServer, $dbUser, $dbPassword,
		$databaseName);

if (!$_SESSION['userId'] || $_SESSION['userStatus'] != 0)
{
	// No user ID, so go back to the login page
	header ('HTTP/1.1 401 Unauthorized');
	header ('Content-Type: application/json');
	die (json_encode (array ('message' => 'Not logged in.')));
}

$categoryId = $_REQUEST['categoryId'];
$addingCategory = ($categoryId == -1);
$data = json_decode ($_REQUEST['data'], true);

if (!$addingCategory)
{
	$data["categoryId"] = $categoryId;
}

$data["createdByUserId"] = $_SESSION['userId'];

// quote values already in data
$dbConnection->quoteValues($data);

$data["createdOnDate"] = "CURDATE()";

$tableName = ($addingCategory) ? "Categories" : "Topics";

$result = $dbConnection->insert ($tableName, $data);

echo $result[1];
