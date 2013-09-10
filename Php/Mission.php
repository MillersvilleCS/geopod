<?php

class Mission
{
	private $missionId = -1;
	private $missionName = "";
	private $missionDescription = "";
	private $isPublic = false;
	private $geopodVersion = "";
	private $missionStatusId = -1;
	private $createdOnDate = null;
	private $createdByUserId = -1;
	private $lastModifiedOnDate = null;
	private $lastModifiedByUserId = -1;
	private $estimatedTimeToComplete = null;
	private $isFrozen = false;

	private $category = null;
	private $background = null;
	private $objectives = array ();

	public function __construct ($id, $missionName)
	{
		$this->missionId = $id;
		$this->missionName = $missionName;
		$this->category = new Category ();
		$this->background = new Background ();
	}

}

class Category
{
	private $categoryId = -1;
	private $categoryName = "";
	private $categoryDescription = "";
	private $createdByUserId = -1;
	private $createdOnDate = null;
	private $topics = array ();

	public function __construct ($param)
	{
		;
	}

}

class Topics
{
	private $topicId = -1;
	private $categoryId = -1;
	private $topicName = "";
	private $topicDescription = "";
	private $createdByUserId = -1;
	private $createdOnDate = null;

}

class Background
{
	private $backgroundId = -1;
	private $missionId = -1;
	private $backgroundText = "";

	public function __construct ()
	{

	}

}
