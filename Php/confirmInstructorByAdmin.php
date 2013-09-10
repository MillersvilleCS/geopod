<?php

// Links to the administrators email link and sends an instructor a confirmation email

require_once('config.php');

require_once('registrationUtil.php');

require_once('sendConfirmationEmail.php');

require_once('DatabaseConnection.php');

session_start ();

$db = new DatabaseConnection ($dbServer, $dbUser, $dbPassword, $databaseName);

$userName = $_REQUEST['auth'];
$userName = $db->escapeString (decryptText ($userName));

$resultItr = $db->select ("Users", "*", " userName = '$userName' ");

if ($resultItr->valid ())
{
	$rowObject = $resultItr->current ();

	$userId = $rowObject->userId;
	$userName = $rowObject->userName;

	// regenerate password for instructor
	$userPassword = generateRandomPassword ();
	$salt = sha1 ($userName . time ());
	$passwordSha1 = sha1 ($userPassword . $salt);

	$columnValues['salt'] = $salt;
	$columnValues['password'] = $passwordSha1;
	$whereClause = " userId = $userId";

	$db->quoteValues ($columnValues);
	$db->update ('Users', $columnValues, $whereClause);

	$firstName = $rowObject->firstName;
	$lastName = $rowObject->lastName;
	$email = $rowObject->email;
	$confirmCode = $rowObject->confirmation;

	$instructor = new Person ("$firstName $lastName", $email);

	sendInstructorConfirmationEmail (new Person ("$firstName $lastName", $email),
			new Authentication ($userName, $userPassword,
					$confirmCode));

	echo ("<p>");
	echo ("Sending email to instructor.");
	echo ("</p>");

}

