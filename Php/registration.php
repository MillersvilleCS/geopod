<?php

// Include database connection information

session_start ();

require_once('registrationUtil.php');

require_once('Authentication.php');

require_once('sendAdminEmail.php');

require_once('sendConfirmationEmail.php');

if (isset ($_POST['username']))
{
	// Get post data common to all registrations
	$firstName = $_POST['firstname'];
	$lastName = $_POST['lastname'];
	$userName = $_POST['username'];
	$organization = $_POST['organization'];
	$email = $_POST['email'];

	// Get instructor post data
	$position = $_POST['position'];
	$phoneNumber = $_POST['phoneNumber'];
	$website = $_POST['website'];

	// Don't rely on users to create passwords.
	$userPassword = generateRandomPassword ();
	$confirmCode = generateRandomConfirmationCode ();

	$form = new Form ($firstName, $lastName, $userName, $organization, $email,
			$confirmCode);

	$result = insertIntoTable ($form, $userPassword);
	$success = $result[0];

	if ($success)
	{
		if ($position == null)
		{
			// Sending a student confirmation email
			sendStudentConfirmationEmail (
					new Person ("$firstName $lastName", $email),
					new Authentication ($userName, $userPassword, $confirmCode));
		}
		else
		{
			sendAdminEmail (
					new Instructor ($firstName, $lastName, $userName,
							$organization, $email, $position, $phoneNumber,
							$website));
		}
	}
}

