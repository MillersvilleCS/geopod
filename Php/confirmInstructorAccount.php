<?php

// Confirms instructor accounts by changes their status field to 0

require_once('config.php');

require_once('encryptionUtility.php');

require_once('DatabaseConnection.php');

session_start ();

$db = new DatabaseConnection ($dbServer, $dbUser, $dbPassword, $databaseName);

$userName = $_REQUEST['auth'];
$userName = $db->escapeString (decryptText ($userName));

$confirmCode = $_REQUEST['key'];
$confirmCode = $db->escapeString (decryptText ($confirmCode));

$columnValues['userStatusId'] = 0;
$columnValues['confirmation'] = NULL;
$db->quoteValues ($columnValues);
$whereClause = " userName = '$userName' AND confirmation = '$confirmCode' ";

$updated = $db->update ('Users', $columnValues, $whereClause);
$status = ($updated > 0) ? "succeeded" : "failed";

echo ("<p>");
echo ("Confirmation $status.");
echo ("</p>");

