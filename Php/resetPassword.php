<?php

// Send password reset link
function generate_password ($len = 8)
{
	$chars = str_split (
			'0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#');
	$pwd = '';

	for ($i = 0; $i < $len; $i++)
	{
		$pwd .= $chars[rand (1, sizeof ($chars)) - 1];
	}
	return $pwd;
}

// Include database connection information
require_once('config.php');

// Connect to database 
$mysqli = new mysqli ($dbServer, $dbUser, $dbPassword, $databaseName);

// Get email address from database
$stmt = $mysqli
		->prepare (
				"SELECT userName, email FROM Users "
						. " WHERE email = ? OR userName = ? LIMIT 1;");

$usernameOrEmail = $_REQUEST['usernameOrEmail'];
$stmt->bind_param ("ss", $usernameOrEmail, $usernameOrEmail);
$stmt->execute ();

$stmt->bind_result ($username, $email);
$stmt->fetch ();

$stmt->close ();

// Create new password
$newPassword = generate_password (8);

// send reset message to $email.
$subject = 'Geopod password reset';

$message = 'Your new login information for Geopod Mission Control follows:'
		. PHP_EOL;
$message .= 'Username: ' . $username . PHP_EOL;
$message .= 'Password: ' . $newPassword . PHP_EOL . PHP_EOL;
$message .= 'You may log in using the following URL:' . PHP_EOL;
$message .= 'http://csheadnode.cs.millersville.edu/~geopod/missionSystemOverview.html'
		. PHP_EOL;

// Wrap to 70 characters wide
$message = wordwrap ($message, 70);

$headers = 'From: noreply@example.com' . "\r\n";
$headers .= 'To: ' . $email . "\r\n";
$headers .= 'Reply-To: noreply@example.com' . "\r\n";
$headers .= 'X-Mailer: PHP/' . phpversion ();

$success = false;
$success = @mail ($email, $subject, $message, $headers);

if ($success)
{
	// Email was sent, so set new password in dataase
	$updateStmt = $mysqli
			->prepare (
					"UPDATE Users SET password = SHA1 (CONCAT (?, `salt`))"
							. " WHERE userName = ? AND email = ? LIMIT 1;");

	if ($updateStmt->errno > 0)
	{
		header ('HTTP/1.1 400 Invalid Form Data');
		header ('Content-Type: application/json');
		die (
				json_encode (
						array ('code' => $errno,
								'message' => $updateStmt->error)));
	}

	$updateStmt->bind_param ("sss", $newPassword, $username, $email);
	if ($updateStmt->execute ())
	{
		$rowsInserted = $updateStmt->affected_rows;
		$updateStmt->close ();
	}
	else
	{
		// An error occured
		$errno = $updateStmt->errno;
		$error = $updateStmt->error;

		// Send error message to client
		// DEBUG: send raw SQL error. Clean up for production.
		header ('HTTP/1.1 400 Invalid Form Data');
		header ('Content-Type: application/json');
		die (json_encode (array ('code' => $errno, 'message' => $error)));
	}
}
else
{
	$mailServerError = stripos ($php_errormsg,
			'Failed to connect to mailserver');
	if ($mailServerError === false)
	{
		// Generic error
		header ('HTTP/1.1 400 Invalid Form Data');
		header ('Content-Type: application/json');
		die (
				json_encode (
						array ('email' => $email, 'message' => 'Email not sent')));
	}
	else
	{
		header ('HTTP/1.1 500 Internal Server Error');
		header ('Content-Type: application/json');
		die (
				json_encode (
						array ('email' => $email,
								'message' => 'Email server not configured')));
	}
}
