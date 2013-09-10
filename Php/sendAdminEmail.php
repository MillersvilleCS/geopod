<?php

function sendAdminEmail (Instructor $instructor)
{
	$auth = encryptText ($instructor->userName);
	$auth = urlencode ($auth);
	$link = "http://csheadnode.cs.millersville.edu/~geopod/Php/confirmInstructorByAdmin.php?auth=$auth";

	$salutation = "Dear Geopod Administrator,\n\n";
	$body1 = "The following person wants to register for a Geopod instructor account:\n\n";

	$name = "Name: $instructor->name\n";
	$organization = "Organization: $instructor->organization\n";
	$position = "Position: $instructor->position\n";
	$email = "Email: $instructor->email\n";
	$phoneNumber = "Phone number: $instructor->phoneNumber\n";
	$website = "Website: $instructor->website\n\n";

	$body2 = "To confirm this account, please click on the following link or paste the URL into your browser.\n\n";
	$body3 = "\n\nTaking no action will cause this account to be deleted.\n";

	$message = $salutation . $body1 . $name . $organization . $position
			. $email . $phoneNumber . $website . $body2 . $link . $body3;

	$admin = new Person ("Geopod Admin", "geopoddevelopers@gmail.com");
	$developer = new Person ("Geopod Developers",
			"geopoddevelopers@gmail.com");

	$mailClient = new MailClient ($developer, $admin, $developer);
	$mailClient->sendEmail ("Geopod Instructor Account Registration", $message);
}

