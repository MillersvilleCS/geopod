<?php

session_start ();

date_default_timezone_set('America/New_York');

// Include database connection information
require_once('config.php');

require_once('DatabaseConnection.php');

$dbConnection = new DatabaseConnection ($dbServer, $dbUser, $dbPassword,
		$databaseName);
/* Connect to database */
// $mysqli = new mysqli ($dbServer, $dbUser, $dbPassword, $databaseName);

// if ($mysqli->connect_error)
	// {
	// 	die ("$mysqli->connect_errno: $myslqi->connect_error");
	// }

$userId = $_SESSION['userId'];
if (!$userId)
{
	// No user ID, so go back to the login page
	header ('HTTP/1.1 401 Unauthorized');
	header ('Content-Type: application/json');
	die (json_encode (array ('message' => 'Not logged in.')));
}

$safeOperators = array ('=', '>=', '<=', '!=');
$orderList = array ('id' => 'Missions.missionId',
		'missionName' => 'Missions.missionName',
		'userName' => 'Users.userName');

// Build SQL query
$sql  = "SELECT Missions.*, \n";
$sql .= "OriginalAuthor.userName AS originalAuthor, \n";
$sql .= "LastAuthor.userName AS author, LastAuthor.firstName, LastAuthor.lastName, \n";
$sql .= "Categories.categoryName, Topics.topicName, \n";
$sql .= "IF (UserFavorites.missionId IS NULL, '0', '1') AS isFavorite \n";
$sql .= "FROM Missions \n";

// Join on the Users table twice to get the names of both the original author and the extending author
$sql .= "LEFT OUTER JOIN Users AS OriginalAuthor ON OriginalAuthor.userId = Missions.createdByUserId \n";
$sql .= "LEFT OUTER JOIN Users AS LastAuthor ON LastAuthor.userId = Missions.lastModifiedByUserId \n";
$sql .= "LEFT OUTER JOIN Categories ON Missions.categoryId = Categories.categoryId \n";
$sql .= "LEFT OUTER JOIN Topics ON Missions.topicId = Topics.topicId \n";
$sql .= "LEFT OUTER JOIN UserFavorites ON (Missions.missionId = UserFavorites.missionId \n";
$sql .= "  AND UserFavorites.userId = $userId) \n";

// Add where clauses that can be auto-generated to the query
$autoFieldList = array ('lastModifiedByUserId' => '=', 'categoryId' => '=',
		'topicId' => '=', 'isPublic' => '=', 'completionLower' => '>=',
		'completionUpper' => '<=', 'missionStatusId' => '=', 'createdByUserId' => '=', 'isFrozenCopy' => '=');

// print_r($_REQUEST);
// echo "\n";

$clauseCount = 0;
foreach ($autoFieldList as $fieldName => $operator)
{
	// If the field was sent to the server, add to the query
	if (isset ($_REQUEST[$fieldName]) && $_REQUEST[$fieldName] != '')
	{
		$field = 'Missions.' . $fieldName;
		$operatorValuePair = $_REQUEST[$fieldName];
		if (is_array ($operatorValuePair))
		{
			// Both the operator and value have been provided
			$operator = $operatorValuePair['operator'];
			$value = $operatorValuePair['value'];
		}
		else
		{
			// Only the value was provided
			$value = $operatorValuePair;
		}

		// Make the string safe
		$dbConnection->escapeString($value);
		//$value = $mysqli->real_escape_string ($value);

		// Only allow operators that are on the safe list
		$operator = in_array ($operator, $safeOperators, true) ? $operator : '=';

		$sql .= ($clauseCount == 0) ? "WHERE " : "AND ";
		$sql .= "$field $operator '$value' \n";
		$clauseCount++;
	}
}

if (isset ($_REQUEST['author']) && $_REQUEST['author'] != '')
{
	$value = $dbConnection->escapeString($_REQUEST['author']);
	//$value = $mysqli->real_escape_string ($_REQUEST['author']);

	$sql .= ($clauseCount == 0) ? "WHERE " : "AND ";
	$sql .= "(`OriginalAuthor`.userName='$value' OR `LastAuthor`.userName='$value' OR `OriginalAuthor`.lastName='$value' OR `LastAuthor`.lastName='$value') \n";
	$clauseCount++;
}
if (isset ($_REQUEST['createdLower']))
{
	// 	$phpdate = strtotime (
	// 			$mysqli->real_escape_string ($_REQUEST['createdLower']));
	$phpdate = strtotime (
			$dbConnection->escapeString($_REQUEST['createdLower']));
	$mysqldate = date ('Y-m-d 23:59:59', $phpdate);

	$sql .= ($clauseCount == 0) ? "WHERE " : "AND ";
	$sql .= "Missions.createdOnDate >'$mysqldate' \n";
	$clauseCount++;
}
if (isset ($_REQUEST['createdUpper']))
{
	// 	$phpdate = strtotime (
	// 			$mysqli->real_escape_string ($_REQUEST['createdUpper']));
	$phpdate = strtotime (
			$dbConnection->escapeString($_REQUEST['createdUpper']));
	$mysqldate = date ('Y-m-d 00:00:00', $phpdate);

	$sql .= ($clauseCount == 0) ? "WHERE " : "AND ";
	$sql .= "Missions.createdOnDate <'$mysqldate' \n";
	$clauseCount++;
}

if (isset ($_REQUEST['modifiedLower']))
{
	// 	$phpdate = strtotime (
	// 			$mysqli->real_escape_string ($_REQUEST['modifiedLower']));
	$phpdate = strtotime (
			$dbConnection->escapeString($_REQUEST['modifiedLower']));
	$mysqldate = date ('Y-m-d 23:59:59', $phpdate);

	$sql .= ($clauseCount == 0) ? "WHERE " : "AND ";
	$sql .= "lastModifiedOnDate >'$mysqldate' \n";
	$clauseCount++;
}
if (isset ($_REQUEST['modifiedUpper']))
{
	// 	 $phpdate = strtotime (
	// 			$mysqli->real_escape_string ($_REQUEST['modifiedUpper']));
	$phpdate = strtotime (
			$dbConnection->escapeString($_REQUEST['modifiedUpper']));
	$mysqldate = date ('Y-m-d 00:00:00', $phpdate);

	$sql .= ($clauseCount == 0) ? "WHERE " : "AND ";
	$sql .= "lastModifiedOnDate <'$mysqldate' \n";
	$clauseCount++;
}
if (isset ($_REQUEST['completionTime']) && isset ($_REQUEST['completionDelta']))
{
	// 	$time = $mysqli->real_escape_string ($_REQUEST['completionTime']);
	// 	$delta = $mysqli->real_escape_string ($_REQUEST['completionDelta']);
	$time = $dbConnection->escapeString($_REQUEST['completionTime']);
	$delta = $dbConnection->escapeString($_REQUEST['completionDelta']);
	list($deltaHours, $deltaMinutes) = explode (':', $delta);

	// Convert to UNIX time stamp
	if(strtotime ($time) <= strtotime ($delta))
	{
		$timeLower = strtotime ("00:00");
	}
	else
	{
		$timeLower = strtotime (
				$time . " -$deltaHours hours -$deltaMinutes minutes");
	}
	$timeUpper = strtotime (
			$time . " +$deltaHours hours +$deltaMinutes minutes");

	// Convert to MySQL format
	$timeLower = date ('H:i:s', $timeLower);
	$timeUpper = date ('H:i:s', $timeUpper);

	// Add clause to query
	$sql .= ($clauseCount == 0) ? "WHERE " : "AND ";
	$sql .= "estimatedTimeToComplete BETWEEN '$timeLower' AND '$timeUpper' \n";
	$clauseCount++;
}

// Setup query for search terms
// NOTE: must have a FULLTEXT index on both missionName and missionDescription columns.
if (isset ($_REQUEST['searchTerms']) && $_REQUEST['searchTerms'] != '')
{
	// 	$searchTerms = $mysqli->real_escape_string ($_REQUEST['searchTerms']);
	$searchTerms = $dbConnection->escapeString($_REQUEST['searchTerms']);
	$excludedTerms = '';
	if(isset($_REQUEST['excludedTerms']))
	{
		// 		$excludedTerms = $mysqli->real_escape_string ($_REQUEST['excludedTerms']);
		$excludedTerms = $dbConnection->escapeString($_REQUEST['excludedTerms']);
	}

	if ($_REQUEST['searchTermModifier'] == 'all')
	{
		// Strip out extra plus signs
		$searchTerms = str_replace (' +', ' ', $searchTerms);

		// Add a single plus sign before each term
		$searchTerms = ' +' . implode (' +', explode (' ', $searchTerms));
	}

	// Append negative sign to excluded words and add to search terms
	if ($excludedTerms != '')
	{
		// Strip any extra negative signs
		$excludedTerms = str_replace (' -', ' ', $excludedTerms);

		// Add a single negative sign before each term
		$searchTerms .= ' -' . implode (' -', explode (' ', $excludedTerms));
	}

	$searchIn = array("missionName");
	if($_REQUEST['searchDescription'])
	{
		$searchIn[] = "missionDescription";
	}
	if($_REQUEST['searchBackground'])
	{
		$searchIn[] = "backgroundText";
	}

	$matchTerms = implode(",", $searchIn);

	$sql .= ($clauseCount == 0) ? "WHERE " : "AND ";
	$sql .= "MATCH ($matchTerms) AGAINST ('$searchTerms' IN BOOLEAN MODE) \n";

	$clauseCount++;
}

// Prevent showing private missions, or other users missions if not instructor.
$sql .= ($clauseCount == 0) ? "WHERE " : "AND ";
$sql .= "(lastModifiedByUserId = $userId ";

if($_SESSION['userStatus'] == 0)
{
	// instructor
	$sql .= "OR Missions.isPublic = 1) \n";
}
else
{
	$sql .= ") \n";
}

if (isset ($_REQUEST['isFavorite']))
{
	// 	$isFavorite = $mysqli->real_escape_string ($_REQUEST['isFavorite']);
	$isFavorite = $dbConnection->escapeString($_REQUEST['isFavorite']);
	$sql .= "HAVING isFavorite = '$isFavorite' \n";
}

if (isset ($_REQUEST['sort']))
{
	$sortByModifiers = array();
	foreach ($_REQUEST['sort'] as $value)
	{
		$column = $value['column'];
		// real_escape_string does not escape backticks, so remove them. Not sure if this is safe enough or not.
		$column = str_replace ('`', '', $column);

		$direction = ($value['direction'] == 'DESC') ? 'DESC' : 'ASC';

		$sortByModifiers[] = " `$column` $direction";
	}

	$sql .= "ORDER BY" . implode(",", $sortByModifiers) . "\n";
}

// Add page limits
$page = 0;
$itemsPerPage = 10;
if (isset ($_REQUEST['page']))
{
	// 	$page = $mysqli->real_escape_string ($_REQUEST['page']);
	$page = $dbConnection->escapeString($_REQUEST['page']);
}
if (isset ($_REQUEST['limit']))
{
	// 	$itemsPerPage = $mysqli->real_escape_string ($_REQUEST['limit']);
	$itemsPerPage = $dbConnection->escapeString($_REQUEST['limit']);
}
$sql .= 'LIMIT ' . $page . ',' . $itemsPerPage;

// Echo SQL query for debuging
//echo $sql . "\n\n";

// Run the query
// if ($result = $mysqli->query ($sql))
if ($result = $dbConnection->performQuery($sql))
{
	// Convert the results to an array of arrays that can be encoded as JSON.
	$rows = array ();
	while ($row = $result->fetch_assoc ())
	{
		$rows[] = $row;
	}
	$result->close ();

	// Package mission data for jsTemplate
	$missionData = array ('missionData' => $rows, 'request' => $sql);

	if (count ($rows) == 0)
	{
		// 		echo $sql . "\n\n";
	}

	// Set headers for JSON data
	header ('Cache-Control: no-cache, must-revalidate');
	header ('Expires: Mon, 26 Jul 1997 05:00:00 GMT');
	header ('Content-type: application/json');

	echo json_encode ($missionData);
}
else
{
	print "Failed to prepare statement\n";
	echo $sql . "\n\n";
// 	die ("Error " . $mysqli->errno . ": " . $mysqli->error);
	die ($dbConnection->getErrorMessage());
}
