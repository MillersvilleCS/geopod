<?php

require_once('encryptionUtility.php');

require_once('emailUtility.php');

require_once('Authentication.php');

require_once('MailClient.php');

function sendConfirmationEmail (Person $person, Authentication $authentication, $accountType, $page)
{
	// Create the message
	$auth = encryptText ($authentication->userName);
	$auth = urlencode ($auth);
	$key = encryptText ($authentication->confirmCode);
	$key = urlencode ($key);
	$confirmationLink = "http://csheadnode.cs.millersville.edu/~geopod/Php/$page?auth=$auth&key=$key";
	$salutation = "Dear $person->name, \n\n";
	$lowercaseType = strtolower($accountType);
	$body1 = "You have requested a Geopod $lowercaseType account. Your password is\n\n";
	$body2 = "Password: $authentication->password\n\n";
	$body3 = "Please click on the following link or copy the URL into your browser to confirm your registration:\n\n";
	$body4 = "\n\nIf you believe you have recieved this email in error, please contact the administrator at gzoppetti@millersville.edu.\n\n";
	$noReply = "Note, this address only sends mail, it does not recieve mail. If you reply to this address, you will not recieve a response.\n";
	$message = $salutation . $body1 . $body2 . $body3 . $confirmationLink
			. $body4 . $noReply;

	$developer = new Person ("Geopod Developers",
			"geopoddevelopers@gmail.com");
	$mailClient = new MailClient ($developer, $person, $developer);
	$mailClient->sendEmail ("Geopod $accountType Account Registration", $message);
}

function sendInstructorConfirmationEmail (Person $person, Authentication $authentication)
{
	sendConfirmationEmail ($person, $authentication, "Instructor", "confirmInstructorAccount.php");
}

function sendStudentConfirmationEmail (Person $person, Authentication $authentication)
{
	sendConfirmationEmail ($person, $authentication, "Student", "confirmStudentAccount.php");
}




