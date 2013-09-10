<?php
session_start ();

// Include database connection information
require_once('./config.php');
require_once('./DatabaseConnection.php');
$db = new DatabaseConnection ($dbServer, $dbUser, $dbPassword, $databaseName);

// Try to login
$username = $_POST['username'];
$password = $_POST['password'];

$table = 'Users';
$columns = array ('userId', 'firstName', 'userName', 'userStatusId');
$orderByColumn = null;
$ascending = 'ASC';

$username = $db->escapeString ($username);
$password = $db->escapeString ($password);

$whereClauses = " (userName = '$username' OR email = '$username') \n";
$whereClauses .= " AND password = SHA1 (CONCAT ('$password', `salt`)) LIMIT 1";

$resultItr = $db
		->select ($table, $columns, $whereClauses, $orderByColumn, $ascending);

if ($resultItr->valid ())
{
	$rowObject = $resultItr->current ();

	$userId = $rowObject->userId;
	$firstName = $rowObject->firstName;
	$userName = $rowObject->userName;
	$userStatus = $rowObject->userStatusId;

	if ($userStatus == 2)
	{
		header ('HTTP/1.1 401 Unauthorized');
		header ('Content-Type: application/json');
		die (json_encode (array ('message' => 'Account is unverified.')));
		return false;
	}

	// Set session data
	$_SESSION['userId'] = $userId;
	$_SESSION['userStatus'] = $userStatus;

	echo json_encode (
			array ('userId' => $userId, 'firstName' => $firstName,
					'userName' => $userName, 'userStatus' => $userStatus));
}
else
{
	header ('HTTP/1.1 401 Unauthorized');
	header ('Content-Type: application/json');
	die (json_encode (array ('message' => 'Username or password is incorrect.')));
	return false;
}
