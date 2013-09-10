<?php

// get a 8 character string of form xx.xx.xx specifying latest version of Geopod
require_once('config.php');

$mysqliConnection = new mysqli ($dbServer, $dbUser, $dbPassword, $databaseName);

if ($mysqliConnection->connect_error)
{
	die ("$mysqliConnection->connect_errno: $myslqiConnection->connect_error");
}

$queryText = "SELECT latestVersion FROM Geopod ";

if ($queryResult = $mysqliConnection->query ($queryText))
{
	$resultObject = $queryResult->fetch_object();
	$queryResult->close();

	$latestGeopodVersion =  $resultObject->latestVersion;
}
else
{
	die ("Error " . $mysqliConnection->errno . ": " . $mysqliConnection->error);
}