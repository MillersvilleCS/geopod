<?php

require_once('emailUtility.php');

class MailClient
{
	private $toEmail;
	private $headers;

	public function __construct (Person $sender, Person $reciever,
			Person $reply)
	{
		$this->toEmail = $reciever->email;
		$this->createHeaders ($sender, $reciever, $reply);
	}

	// Returns true if email is accepted to be delivered, false otherwise
	public function sendEmail ($subject, $message)
	{
		$message = wordwrap ($message, 70);

		// The @ symbol disables error reporting for mail function.
		$sent = @mail ($this->toEmail, $subject, $message, $this->headers);

		return $sent;
	}

	private function createHeaders (Person $sender, Person $reciever,
			Person $reply)
	{
		$headers = "From: $sender->name <$sender->email>\r\n";
		$headers .= "To: $reciever->name <$reciever->email>\r\n";
		$headers .= "Reply-To: $reply->name <$reply->email>\r\n";
		$headers .= 'X-Mailer: PHP/' . phpversion ();
		$this->headers = $headers;
	}
}

