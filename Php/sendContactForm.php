<?php

$name = $_POST['name'];
$email = 'geopoddevelopers@gmail.com';
$replyEmail = $_POST['replyEmail'];
$subject = $_POST['subject'];
$message = $_POST['message'];

$message = wordwrap ($message, 70);

$headers = 'From: ' . $name . ' <' . $replyEmail . ">\r\n";
// TODO: Create the following email address.
$headers .= 'To: Geopod Developers <' . $email . ">\r\n";
$headers .= 'Reply-To: ' . $name . ' <' . $replyEmail . ">\r\n";
$headers .= 'X-Mailer: PHP/' . phpversion ();

$success = false;
$success = @mail ($email, $subject, $message, $headers);

if (!$success)
{
	$mailServerError = stripos ($php_errormsg,
			'Failed to connect to mailserver');
	// The identity operator needs to be used because stripos does not
	// necessarialy return boolean false
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
