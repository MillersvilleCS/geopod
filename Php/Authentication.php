<?php

class Authentication
{
	public $userName;
	public $password;
	public $confirmCode;

	public function __construct ($userName, $password, $confirmCode)
	{
		$this->userName = $userName;
		$this->password = $password;
		$this->confirmCode = $confirmCode;
	}
}

