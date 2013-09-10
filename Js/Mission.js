// Convenience objects for dealing with Mission data

function Mission ()
{
	this.missionId = -1;
	this.missionName = "";
	this.missionDescription = "";
	this.categoryId = 0;
	this.topicId = 0;
	this.isPublic = 0;
	this.geopodVersion = "";
	this.missionStatusId = -1;
	this.createdOnDate = null;
	this.createdByUserId = -1;
	this.lastModifiedOnDate = null;
	this.lastModifiedByUserId = -1;
	this.estimatedTimeToComplete = "00:00";
	this.isFrozenCopy = 0;

	this.backgroundText = "";
	this.objectives =
	{};
	this.questions =
	{};
	this.answers =
	{};
}

Mission.prototype.toString = function ()
{
	var str = JSON.stringify (this);
	return str;
};

function Category ()
{
	this.categoryId = -1;
	this.categoryName = "";
	this.categoryDescription = "";
	this.createdByUserId = -1;
	this.createdOnDate = null;
}

function Objective ()
{
	this.objectiveId = -1;
	this.missionId = -1;
	this.objectiveTitle = "";
	this.objectiveDescription = "";
}

function Question (type)
{
	this.questionId = -1;
	this.objectiveId = -1;
	this.questionTypeId = type;
	this.questionText = "";
	this.order = -1;
}

function Answer (placementMarker)
{
	this.answerText = "";
	this.isCorrect = 0;
	this.points = 0;
	this.placementMarker = placementMarker;
	this.order = -1;
}