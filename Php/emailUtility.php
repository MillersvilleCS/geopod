<?php

class Person
{
        public $name;
        public $email;

        public function __construct ($name, $email)
        {
                $this->name = $name;
                $this->email = $email;
        }
}

class Student extends Person
{
	public $firstName;
	public $lastName;
	public $userName;
	public $organization;

	public function __construct ($firstName, $lastName, $userName, $organization,
			$email)
	{
		parent::__construct ("$firstName $lastName", $email);
		$this->firstName = $firstName;
		$this->lastName = $lastName;
		$this->userName = $userName;
		$this->organization = $organization;
	}
}

class Instructor extends Person
{
	public $firstName;
	public $lastName;
	public $userName;
	public $organization;

	public $position;
	public $phoneNumber;
	public $website;

	public function Instructor ($firstName, $lastName, $userName, $organization,
			$email, $position, $phoneNumber, $website)
	{
		parent::__construct ("$firstName $lastName", $email);
		$this->firstName = $firstName;
		$this->lastName = $lastName;
		$this->userName = $userName;
		$this->organization = $organization;

		$this->position = $position;
		$this->phoneNumber = $phoneNumber;
		$this->website = $website;
	}
}
