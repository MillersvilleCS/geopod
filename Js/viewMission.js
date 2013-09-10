//Php files used by this page:
// getMissionInfo - will return a mission object with all fields set from one in db with give id

$ (document).ready (function ()
{
	// get mission id from local storage and then get rid of it
	var missionId = localStorage["ViewMissionId"];
	if (missionId == null)
	{
		returnToMissionControl ();
	}
	else
	{
		// store mission id in body, will use this later
		$ ('body').data ('missionId', missionId);

		// get the mission data from database, store in
		// body as 'mission'
		obtainMissionFromDatabase ();

		initUiElements ();

		populateFields ();

		setUpButtonCallbacks ();

		showButtons ();
	}
});

function handleLoginError (jqXHR)
{
	isAuthenticationError = (jqXHR.status == '401');
	if (isAuthenticationError)
	{
		// login error, clear storage
		localStorage.clear ();
		returnToMissionControl ();
	}
	return (isAuthenticationError);
}

function initUiElements ()
{
	initAccordion ();
	initPopups ();
}

function initAccordion ()
{
	$ ("#ObjectivesAccordion").multiAccordion (
	{
		sortable : false
	});

	$ ("#ObjectivesAccordion").bind ("onChange",
			function (event, panel, isOpening)
			{
				renameAccordionHeaders (panel, isOpening, getObjectivesTitle);
			});
}

function renameAccordionHeaders (panel, isOpening, titleFunction)
{
	var orderNumber = $ ("input[data-name=order]", panel).first ().val ();
	var headerText = (parseInt (orderNumber) + 1) + ": ";
	if (!isOpening)
	{
		headerText += titleFunction (panel);
	}
	$ (panel).children ("h3").children ("a").html (headerText);
}

function getObjectivesTitle (panel)
{
	var objectiveTitle = $ ("[data-name=objectiveTitle]", panel).html ();
	return (objectiveTitle);
}

function getQuestionTitle (panel)
{
	var questionText = $ ("[data-name=questionText]", panel).html ();
	var takePortion = questionText.length > 100;
	var questionTitle = (takePortion) ? questionText.substring (0, 100) + "..."
			: questionText;
	return (questionTitle);
}

function initPopups ()
{
	$ (".Popup").dialog (
	{
		modal : true,
		autoOpen : false,
		resizable : false,
		draggable : false
	});

	$ ("#DiscardPrompt").dialog ("option", "buttons",
	{
		"Discard" : function ()
		{
			deleteSpecificMission ();
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");
		}
	});

	$ ("#MakePublicPrompt").dialog ("option", "buttons",
	{
		"Make Public" : function ()
		{
			changeMissionVisibility ();
			$ (this).dialog ("close");
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");
		}
	}).html (getPublicPopUpText ());

	$ ("#MakePrivatePrompt").dialog ("option", "buttons",
	{
		"Make Private" : function ()
		{
			changeMissionVisibility ();
			$ (this).dialog ("close");
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");
		}
	}).html (getPrivatePopUpText ());
}

function populateFields ()
{
	var mission = $ ('body').data ('mission');

	var ownedByThisUser = mission['lastModifiedByUserId'] == localStorage['userId'];
	var isComplete = (mission['missionStatusId'] == 0);
	var isDraftNewMission = (mission['missionStatusId'] == 1);
	var isPublic = (mission['isPublic'] == 1);
	var isFrozenCopy = (mission['isFrozenCopy'] == 1);

	if (!ownedByThisUser && !isPublic)
	{
		alert ("You do not have permission to view this mission");
		returnToMissionControl ();
	}

	// fill out initial info at top
	var author = mission['originalAuthor'];
	var lastAuthor = mission['author'];
	if (lastAuthor != author)
	{
		author += ", " + lastAuthor;
	}
	$ ("#AuthorDisplay").html (author);

	var status = "Draft";
	if (isComplete)
	{
		status = isFrozenCopy ? "Copy, saved in Favorites"
				: isPublic ? "Public" : "Private";
	}
	else if (isDraftNewMission)
	{
		status += " of New Mission";
	}
	else
	{
		status += " of Edits to Existing Mission";
	}
	$ ("#StatusDisplay").html (status);

	if (ownedByThisUser && isComplete)
	{
		setChangeStatusLink (isPublic);
		$ ("#ChangeStatusLink").click (
				function ()
				{
					// isPublic --> 1, isPrivate --> 0
					var isPublic = (mission['isPublic'] == 1);
					var missionId = mission['missionId'];
					var promptSelector = (isPublic) ? "#MakePrivatePrompt"
							: "#MakePublicPrompt";
					// set html and DOM to reflect change in visibility
					refresh = refreshStatus.bind (undefined, !isPublic);

					showChangeMissionVisibilityPrompt (promptSelector,
							!isPublic, missionId, refresh);

				});
	}

	// fill form with values from global mission, which will either have
	// defaults or be initialized from one in database
	$ (".MissionDataField").each (function ()
	{
		var missionField = $ (this).attr ('data-name');
		$ (this).html (mission[missionField]);
	});

	// go thru objectives, construct accordion with them using template
	var objectives = mission['objectives'];

	// store the objective# -> questions map
	var objectiveQuestionsList = mission['questions'];
	// clear Mission.questions
	mission['questions'] =
	{};

	// store the question# -> answers map
	var questionAnswersList = mission['answers'];
	// clear Mission.answers
	mission['answers'] =
	{};

	for ( var objectiveNumber in objectives)
	{
		// create the objective
		var newObjective = addObjective (objectives[objectiveNumber]);
		// add questions for this objective
		var questions = objectiveQuestionsList[objectiveNumber];
		for ( var questionNumber in questions)
		{
			// get data for each question attached to this objective
			var questionData = questions[questionNumber];
			// create and attach the html representation of question
			var newQuestion = addQuestion (newObjective, questionData);
			//
			// if it was a multi-dropdown question, find any markers in question
			// text, create marker list and add corresponding dropdown groups
			if (questionData['questionTypeId'] == 1)
			{
				generateAnswerGroups (newQuestion);
			}

			var answers = questionAnswersList[questionNumber];
			for ( var answerNumber in answers)
			{
				var answerData = answers[answerNumber];
				addAnswer (newQuestion, answerData);
				mission['answers'][answerNumber] = answerData;
			}
			mission['questions'][questionNumber] = questionData;
		}
	}
}

function setChangeStatusLink (isPublic)
{
	var changeStatusText = (isPublic) ? "make Private" : "make Public";
	$ ("#ChangeStatusLink").html (changeStatusText);
}

function setChangeStatus (isPublic)
{
	var statusText = (isPublic) ? "Public" : "Private";
	$ ("#StatusDisplay").html (statusText);
}

function refreshStatus (isPublic)
{
	setChangeStatusLink (isPublic);
	setChangeStatus (isPublic);
	var mission = $ ('body').data ('mission');
	// isPublic is gotten from server as an integer string, this
	// just makes sure mission data is consistent.
	mission['isPublic'] = (isPublic) ? '1' : '0';
}

function addObjective (baseObjective)
{
	// create the html for the new objective & put in page
	var newObjective = $ ("#ObjectiveTemplate").tmpl (baseObjective);
	$ ("#ObjectivesAccordion").multiAccordion ("append", newObjective);

	// initialize tabs for new objective
	$ (".ObjectiveTabs", newObjective).tabs ();

	// initialize questions accordion for new objective
	$ (".QuestionsAccordion", newObjective).multiAccordion (
	{
		sortable : false
	});

	$ (".QuestionsAccordion", newObjective).bind ("onChange",
			function (event, panel, isOpening)
			{
				renameAccordionHeaders (panel, isOpening, getQuestionTitle);
			});

	// advance objective counter
	incrementNumberCounter ('objective');

	return (newObjective);
}

// when call this with new question, can't just pass in default, must set
// order in it appropriately
function addQuestion (parentObjective, questionInfo)
{
	var newQuestion = $ ("#QuestionTemplate").tmpl (questionInfo);
	$ (".QuestionsAccordion", parentObjective).multiAccordion ("append",
			newQuestion);

	incrementNumberCounter ('question');

	return (newQuestion);
}

function generateAnswerGroups (question)
{
	var questionText = $ ('[data-name=questionText]', question).html ();
	var searchFrom = 0;
	for ( var markerNum = 0;; ++markerNum)
	{
		var textToFind = "{" + (markerNum + 1).toString () + "}";
		searchFrom = questionText.indexOf (textToFind, searchFrom);
		if (searchFrom == -1)
			break;
		addAnswerGroup (markerNum, question);
		searchFrom += textToFind.length;
	}
	renumberAnswerGroups (question);
}

function addAnswer (parentQuestion, answerInfo)
{
	// make answer from template
	var newAnswer = $ ("#AnswerTemplate").tmpl (answerInfo);

	var answerListToAddTo = $ (".AnswersList[data-placement-marker="
			+ answerInfo.placementMarker + "]", parentQuestion);
	answerListToAddTo.append (newAnswer);

	$ (".AnswersLabel", parentQuestion).show ();

	incrementNumberCounter ('answer');
}

function addAnswerGroup (groupNumber, parentQuestion)
{
	var newAnswerGroup = $ ("#AnswerGroupTemplate").tmpl ();

	if (groupNumber == 0)
	{
		newAnswerGroup.prependTo ($ (".AnswersArea", parentQuestion));
	}
	else
	{
		$ (
				".AnswersArea .AnswerGroup:nth-child("
						+ (groupNumber).toString () + ")", parentQuestion)
				.after (newAnswerGroup);
	}

	$ (".AnswersLabel", parentQuestion).show ();
}

function renumberAnswerGroups (parentQuestion)
{
	var counter = 0;
	$ (".AnswerGroup", parentQuestion).each (
			function ()
			{
				$ (".AnswerGroupHeader", this).html (
						"{" + (counter + 1).toString () + "}");
				$ ('.AnswersList', this)
						.attr ("data-placement-marker", counter);
				++counter;
			});
}

function obtainMissionFromDatabase ()
{
	var missionId = $ ('body').data ('missionId');

	// get info from DB with for mission with specified mission id
	// creat page with this info
	$
			.ajax (
			{
				url : './Php/getMissionInfo.php',
				data :
				{
					missionId : missionId,
					viewMissionRequest : true
				},
				success : function (result)
				{
					prepareMissionFromExisting (JSON.parse (result));
				},
				error : function (jqXHR)
				{
					if (!handleLoginError (jqXHR))
					{
						alert ("There has been an error retrieving this mission from the database.");
						returnToMissionControl ();
					}
				},
				async : false
			});
}

// creates a new mission with data from missionContents
// stores in dom, to be used to create page
function prepareMissionFromExisting (missionContents)
{
	// get default one
	var mission = new Mission ();

	// init mission with values in existing
	var missionData = missionContents["missionData"];
	for (attributeName in missionData)
	{
		mission[attributeName] = missionData[attributeName];
	}

	var objectiveData = missionContents["objectiveData"];
	mission.objectives = objectiveData;

	// NOTE: question data will be in the format
	// {[obj#] => {[quest#]=>{data}, [quest#]=>{data} ... } ... }
	// this is not the typical format for Mission.questions,
	// which in all normal cases will be {[quest#}=>{data} ... }
	// but it simplifies attaching the questions to the appropriate
	// objectives. After using this structure in populateFields(),
	// which is called after this and before anything else will done
	// with Mission.questions, the correct structure will be established
	var questionData = missionContents["questionData"];
	mission.questions = questionData;

	// NOTE: answer data will be similarly in a different format
	// for ease of constructing mission from data. likewise is guarenteed to
	// be changed to correct structure before any operations are done
	var answerData = missionContents["answerData"];
	mission.answers = answerData;

	// store in dom
	$ ('body').data ('mission', mission);

	// counter for objectives, to give unique id for use in editor
	// start at 0 here, will fill in when populate template
	initCounters ();
}

function initCounters ()
{
	$ ('body').data ('objectiveCounter', 0);

	$ ('body').data ('questionCounter', 0);

	$ ('body').data ('answerCounter', 0);
}

function getNextNumber (counterName)
{
	return $ ('body').data (counterName + 'Counter');
}

function incrementNumberCounter (counterName)
{
	var oldVal = $ ('body').data (counterName + 'Counter');
	$ ('body').data (counterName + 'Counter', oldVal + 1);
}

function setUpButtonCallbacks ()
{
	var mission = $ ('body').data ('mission');
	var missionId = mission.missionId;

	$ ("Button#Edit").click (function ()
	{
		editMission (missionId, false);
	});
	$ ("Button#EditDraft").click (function ()
	{
		editMission (missionId, true);
	});
	$ ("Button#Extend").click (function ()
	{
		extendMission (missionId);
	});
	$ ("Button#ExportAsXml").click (function ()
	{
		exportAsXml (missionId);
	});
	$ ("Button#Discard")
			.click (
					function ()
					{
						var wasCompleteAndPublic = (mission.missionStatusId == 0 && mission.isPublic == 1);

						showDeleteMissionPrompt ("#DiscardPrompt",
								wasCompleteAndPublic, missionId,
								returnToMissionControl);
					});
	$ ("Button#Back").click (returnToMissionControl);
}

// show the buttons appropriate for state
function showButtons ()
{
	var mission = $ ('body').data ('mission');

	var ownedByThisUser = (mission['lastModifiedByUserId'] == localStorage['userId']);
	var isDraft = (mission['missionStatusId'] != 0);

	var buttonClassSelector;
	if (!ownedByThisUser)
	{
		buttonClassSelector = ".Owner";
	}
	else if (isDraft)
	{
		buttonClassSelector = ".Complete";
	}
	else
	{
		buttonClassSelector = ".Draft";
	}

	$ (".OptionButton").show ();
	$ (".OptionButton" + buttonClassSelector).hide ();
}

function returnToMissionControl ()
{
	location.href = "./missionControl.html";
}
