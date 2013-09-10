<?php

// Util.php contains utility functions we don't know where else to put.

require_once('config.php');

require_once('DatabaseConnection.php');

class Form
{
	public $firstName;
	public $lastName;
	public $userName;
	public $organization;
	public $email;
	public $confirmCode;

	public function __construct ($firstName, $lastName, $userName,
			$organization, $email, $confirmCode)
	{
		$this->firstName = $firstName;
		$this->lastName = $lastName;
		$this->userName = $userName;
		$this->organization = $organization;
		$this->email = $email;
		$this->confirmCode = $confirmCode;
	}
}

// Returns an array containing the insert success and the new user id
function insertIntoTable (Form $form, $userPassword)
{
	$columnValuePairs['firstName'] = $form->firstName;
	$columnValuePairs['lastName'] = $form->lastName;
	$columnValuePairs['userName'] = $form->userName;
	$columnValuePairs['organization'] = $form->organization;
	$columnValuePairs['email'] = $form->email;

	// Create random salt
	$salt = sha1 ($form->userName . time ());

	$columnValuePairs['salt'] = $salt;
	$columnValuePairs['password'] = sha1 ($userPassword . $salt);
	$columnValuePairs['confirmation'] = $form->confirmCode;
	$columnValuePairs['userStatusId'] = 2;

	// Put global config variables in function scope
	global $dbServer, $dbUser, $dbPassword, $databaseName;

	// Connect to database
	$dbConnection = new DatabaseConnection ($dbServer, $dbUser, $dbPassword,
			$databaseName);

	// Escape all values to prevent people from trying to delete our table
	$dbConnection->quoteValues ($columnValuePairs);

	// Only inserting one row
	$results = $dbConnection->insert ("Users", $columnValuePairs);

	// returns [bool success, userId]
	return $results;
}

function generateRandomPassword ()
{
	$lowercase = "qwertyuipasdfghjkzxcvbnm";
	$uppercase = "MNBVCXZLKJHGFDSAPUYTREWQ";
	$special = "_!@#$)%^&*(?-+=";
	$number = "98765432";

	$categories = array ($lowercase, $special, $number, $uppercase);

	$password = "";
	$PASSWORD_LENGTH = 10;

	for ($i = 0; $i < $PASSWORD_LENGTH; $i++)
	{
		// Ranges aren't picked for a particular reason,
		// except that random tends to pick low values
		// and I like prime numbers.
		$type = rand (11, 101) % 4;
		$group = $categories[$type];
		$index = rand (3, 103) % strlen ($group);

		$password .= $group[$index];
	}

	return $password;
}

function generateRandomConfirmationCode ()
{
	$lowercase = "qwertyuiopasddfghjklzxcvbnm";
	$uppercase = "MNBVCXZLKJHGFDSAPIUYTREWOQ";
	$special = "-_.~!*();:@&=$,?%#[]";
	$number = "987654321";

	$categories = array ($lowercase, $special, $number, $uppercase);

	$password = "";
	$PASSWORD_LENGTH = 40;

	for ($i = 0; $i <$PASSWORD_LENGTH; $i++)
	{
		// Ranges aren't picked for a particular reason,
		// except that random tends to pick low values
		// and I like prime numbers.
		$type = rand (11, 101) % 4;
		$group = $categories[$type];
		$index = rand (3, 103) % strlen ($group);

		$password .= $group[$index];
	}

	return $password;
}
