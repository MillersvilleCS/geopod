//Php files used by this page:
// getMissionInfo - will return a mission object with all fields set from one in db with give id
// addNewMission - adds mission to db, given properties in array, returns id
// updateMission - makes changes to mission in db, given array of properties to set and missionId
// discardMission - gets rid of mission with given id

$ (document).ready (function ()
{
	// get status info from local storage and then get rid of it
	var missionEditingInfo = localStorage["MissionEditorInfo"];
	if (missionEditingInfo == null)
	{
		location.href = "./missionControl.html";
	}
	else
	{
		missionEditingInfo = JSON.parse (missionEditingInfo);
		localStorage.removeItem ("MissionEditorInfo");

		// store status info in html - will update this later if
		// editing 'mode' changes
		// use this info to determine what header is, what buttons
		// are available & how they behave
		$ ('body').data ('editModeInfo', missionEditingInfo);

		initializeMission ();

		initUiElements ();

		attachHeader ();

		populateFields ();

		setUpButtonCallbacks ();

		showButtons ();

		// start autosave loop
		resetAutosave ();
	}
});

function handleLoginError (jqXHR)
{
	isAuthenticationError = (jqXHR.status == '401');
	if (isAuthenticationError)
	{
		// login error, clear storage
		localStorage.clear ();
		location.href = "./missionControl.html";
	}
	return (isAuthenticationError);
}

function initUiElements ()
{
	populateCategoryDropdown ("#CategoryDropdown", "Choose Category...", true);

	linkTopicToCategory ("#CategoryDropdown", "#TopicDropdown",
			"Choose Topic...", "Choose Topic...", true);

	$ ('#EstimatedTimeInput').timepicker (
	{
		showPeriodLabels : false
	});

	$ ("#BackgroundEditor").ckeditor (
	{
		customConfig : '../ckeditorCustomConfig/customConfig.js',
		resize_enabled : false,
		fillEmptyBlocks : false,
		extraPlugins : 'onchange'
	});

	if (localStorage['userStatus'] != 0)
	{
		$ (".ModifyOption").attr ('hidden', 'true');
	}

	initValidation ();

	initAccordion ();

	initPopups ();
}

function initValidation ()
{
	$ ("#missionForm")
			.bValidator (
					{
						validateOnSubmit : false,
						onAfterElementValidation : function (element,
								errorMessages)
						{
							// don't show popup messages
							return 0;
						},
						onAfterValidate : function (validatedObject,
								validatorAction, validationSuccess)
						{
							// custom error message display routine
							// temporary pending page's final CSS look
							var elementName = validatedObject.attr ('name');
							var paramsStart = validatorAction.indexOf ('[');
							if (paramsStart != -1)
								validatorAction = validatorAction.substring (0,
										paramsStart);

							var errorMessageSelector = ".errorMessage";
							errorMessageSelector += "[data-element-for="
									+ elementName + "]";
							errorMessageSelector += "[data-action*="
									+ validatorAction + "]";

							var errorMessageArea = validatedObject
									.siblings (errorMessageSelector);

							if (validationSuccess)
							{
								errorMessageArea.hide ();
							}
							else
							{
								errorMessageArea.show ();
							}

						},
						onAfterAllValidations : function ()
						{
							// indicate error status on outer container, so can
							// call attention to errors on hidden elements

							// clear all previous error indications
							$ (".ChildErrorIndicator").removeClass (
									"ChildErrorIndicator");

							// add error class to accordion headers if
							// appropriate
							$ (".ui-accordion-header")
									.each (
											function ()
											{
												var numValidationErrors = $ (
														this)
														.siblings (
																".ui-accordion-content")
														.find (
																".bvalidator_invalid").length;
												if (numValidationErrors > 0)
												{
													$ (this)
															.find (
																	"[data-parent-alert]")
															.addClass (
																	"ChildErrorIndicator");
												}
											});

							// add error class to tab headers if appropriate
							$ (".ui-tabs-nav > li")
									.each (
											function ()
											{
												var contentSelector = $ ("a",
														this).attr ('href');
												var numValidationErrors = $ (
														this)
														.parents (".ui-tabs")
														.find (
																contentSelector
																		+ " .bvalidator_invalid").length;
												if (numValidationErrors > 0)
												{
													$ (this)
															.find (
																	"[data-parent-alert]")
															.addClass (
																	"ChildErrorIndicator");
												}
											});
						}
					});

	// custom validation routine for topic/category dropdowns
	$ ("#missionForm").data ('bValidator').getActions ().select = function (
			element)
	{
		return (element.value != 0);
	};

	// custom validation routine for elements that must contain at least a
	// certain number of children of a certain class
	$ ("#missionForm").data ('bValidator').getActions ().minChildren = function (
			element, childClass, minRequired)
	{
		return ($ ("." + childClass, element['value']).length >= minRequired);
	};

	// custom validation routine for answers, ensures that at least one answer
	// is marked correct
	$ ("#missionForm").data ('bValidator').getActions ().oneOrMoreCorrect = function (
			element)
	{
		return ($ ("[name=isCorrect]", element['value']).length == 0 || $ (
				"[name=isCorrect]:checked", element['value']).length >= 1);
	};

	// get background CKeditor
	// NOTE: the following must come after editor is already initialized
	var ckeditor = $ ("#BackgroundEditor").ckeditorGet ();
	// fire validator's error validation event when ckeditor is changed
	// done this way because if simply get change event in validator and look
	// at element value, would get outdated content not reflecting change
	// that caused change event to fire. Also change event is fired by editor
	// not original text area element validator observes
	ckeditor.on ('change', function (e)
	{
		// timeout hack to get updated content, courtesy of stackoverflow.com
		CKEDITOR.tools.setTimeout (function ()
		{
			var errorValidateEvent = $ ("#missionForm").data ('bValidator')
					.getOptions ().errorValidateOn;
			$ ("#BackgroundEditor").trigger (errorValidateEvent);
		}, 0);

	});
}

function initAccordion ()
{
	$ ("#ObjectivesAccordion").multiAccordion ();

	$ ("#ObjectivesAccordion").bind ("onChange",
			function (event, panel, isOpening)
			{
				console.log ("changed objective, isOpening is " + isOpening);
				renameAccordionHeaders (panel, isOpening, getObjectivesTitle);
			});

	$ ("#ObjectivesAccordion").bind (
			"onAppend",
			function ()
			{
				console.log ("appended objective");
				var numPanels = $ ("#ObjectivesAccordion").multiAccordion (
						"getNumPanels");
				if (numPanels > 2)
					$ (".AddObjectiveButton#auxiliary").show ();
				renumberOrderedItems ('Objective', this);
			});

	$ ("#ObjectivesAccordion").bind (
			"onRemove",
			function ()
			{
				console.log ("removed objective");
				var numPanels = $ ("#ObjectivesAccordion").multiAccordion (
						"getNumPanels");
				if (numPanels <= 2)
					$ (".AddObjectiveButton#auxiliary").hide ();
				renumberOrderedItems ('Objective', this);
			});

	$ ("#ObjectivesAccordion").bind ("sortstop", function (event)
	{
		console.log ("reordered objective");
		renumberOrderedItems ('Objective', this);
		event.stopPropagation ();
	});
}

function renameAccordionHeaders (panel, isOpening, titleFunction)
{
	var orderNumber = $ ("input[name=order]", panel).first ().val ();
	var headerText = (parseInt (orderNumber) + 1) + ": ";
	if (!isOpening)
	{
		headerText += titleFunction (panel);
	}
	$ (panel).children ("h3").children ("a").html (headerText);
	// $ ("h3 > a", panel).first().html (headerText);
}

function getObjectivesTitle (panel)
{
	var objectiveTitle = $ ("input[name=objectiveTitle]", panel).val ();
	return (objectiveTitle);
}

function getQuestionTitle (panel)
{
	var questionText = $ ("textarea[name=questionText]", panel).val ();
	var takePortion = questionText.length > 100;
	var questionTitle = (takePortion) ? questionText.substring (0, 100) + "..."
			: questionText;
	return (questionTitle);
}

// goes through list of things, updates numbers at beginning of headers and
// order values
// called whenever order of objectives changes
// note: assumes certain structure, that is, things to order
// all have class 'selectorName', and inside that have h3 header
// selectorName+Header
function renumberOrderedItems (selectorName, parentSelectorName)
{
	var counter = 0;
	$ ("." + selectorName, parentSelectorName).each (function ()
	{
		var selectedHeader = $ ("." + selectorName + "Header > a", this);
		var headerText = selectedHeader.html ();
		var restOfHeader = headerText.substr (headerText.indexOf (":"));
		selectedHeader.html ((counter + 1) + restOfHeader);

		// console.log("reordering to " + counter);
		// console.log($ ("input[name=order]", this).first());
		$ ("input[name=order]", this).first ().val (counter);
		++counter;
	});
}

function reorderAnswers (answersList)
{
	var counter = 0;
	$ (".Answer", answersList).each (function ()
	{
		$ ("input[name=order]", this).first ().val (counter++);
	});
}

function initPopups ()
{
	$ ("#SavedToDraftsAlert,#CancelChangesPrompt").bind (
			"dialogopen",
			function ()
			{
				$ ("#MissionName", this).html (
						$ ('body').data ('mission').missionName);

			});

	$ ("#CreateTopicDialog")
			.bind (
					"dialogopen",
					function ()
					{
						// clear fields
						$ ("input", this).val ("");

						var currentCategoryId = $ (
								".MissionDataField[name=categoryId]").val ();

						var currentCategoryName = $ (
								"#CategoryDropdown > option[value="
										+ currentCategoryId + "]").html ();

						$ ("#CategoryName", this).html (currentCategoryName);
					});

	$ ("#CreateCategoryDialog").bind ("dialogopen", function ()
	{
		// clear fields
		$ ("input", this).val ("");
	});

	$ (".Popup").dialog (
	{
		modal : true,
		autoOpen : false,
		resizable : false,
		draggable : false
	});

	$ ("#SavedToDraftsAlert").dialog ("option", "buttons",
	{
		"OK" : function ()
		{
			$ (this).dialog ("close");
		}
	});

	$ ("#CancelChangesPrompt").dialog ("option", "buttons",
	{
		"Yes, exit" : function ()
		{
			$ (this).dialog ("close");

			discardAutosaveData ();

			// return to previous page
			window.history.back ();
		},
		"No, don't exit" : function ()
		{
			$ (this).dialog ("close");

			resetAutosave ();
		}
	});

	$ ("#DiscardPrompt").dialog ("option", "buttons",
	{
		"Discard" : function ()
		{
			discardMission ();
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");

			resetAutosave ();
		}
	});

	$ ("#PublicPrivatePrompt").dialog ("option", "buttons",
	{
		"OK" : function ()
		{
			$ (this).dialog ("close");

			var isPublic = $ ("input:radio:checked").val ();
			$ ('body').data ('isPublic', isPublic);
			saveMissionAsComplete ();
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");
		}
	});

	$ ("#CommitChangesPrompt").dialog ("option", "buttons",
	{
		"Save" : function ()
		{
			$ (this).dialog ("close");

			saveMissionAsComplete ();
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");

			resetAutosave ();
		}
	});

	$ ("#SavedAsCompleteAlert").dialog ("option", "buttons",
	{
		"Ok" : function ()
		{
			// return to previous page
			window.history.back ();
		}
	});

	$ ("#SelectCategoryAlert,#ValidateMissionPrompt").dialog ("option",
			"buttons",
			{
				"Ok" : function ()
				{
					$ (this).dialog ("close");
				}
			});

	$ ("#CreateTopicDialog").dialog ("option", "buttons",
	{
		"Create" : function ()
		{
			$ (this).dialog ("close");
			addTopicOrCategory ('Topic');
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");
		}
	});

	$ ("#CreateCategoryDialog").dialog ("option", "buttons",
	{
		"Create" : function ()
		{
			$ (this).dialog ("close");
			addTopicOrCategory ('Category');
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");
		}
	});

	$ ("#DeleteObjectivePrompt").dialog ("option", "buttons",
	{
		"Delete" : function ()
		{
			$ (this).dialog ("close");
			deleteSpecificObjective ();
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");
		}
	});

	$ ("#DeleteQuestionPrompt").dialog ("option", "buttons",
	{
		"Delete" : function ()
		{
			$ (this).dialog ("close");
			deleteSpecificQuestion ();
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");
		}
	});

	$ ("#DeleteAnswerGroupPrompt").dialog ("option", "buttons",
	{
		"Delete" : function ()
		{
			$ (this).dialog ("close");
			deleteSpecificDropdown ();
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");
		}
	});

	$ ("#DeleteAnswerPrompt").dialog ("option", "buttons",
	{
		"Delete" : function ()
		{
			$ (this).dialog ("close");
			deleteSpecificAnswer ();
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");
		}
	});

}

function attachHeader ()
{
	var missionEditingInfo = $ ('body').data ('editModeInfo');
	var headerTitle = "Edit Mission";
	if (missionEditingInfo.editMode == "new")
	{
		headerTitle = (missionEditingInfo.missionId == null) ? "Create New Mission"
				: "Extend Mission";
	}
	$ ('#EditMissionHeader').html (headerTitle);
}

function populateFields ()
{
	var mission = $ ('body').data ('mission');
	// fill form with values from global mission, which will either have
	// defaults or be initialized from one in database
	$ (".MissionDataField").each (function ()
	{
		var attributeName = $ (this).attr ('name');
		$ (this).val (mission[attributeName]).change ();
		// alert("name is " + attributeName + " val is now " + $(this).val());
	});

	// go thru objectives, contruct accordion with them using template
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

	// alert(JSON.stringify(objectiveQuestionsList));
	// alert(JSON.stringify(mission['questions']));

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
			// alert(questionNumber + " => " + JSON.stringify(questionData));
			// create and attach the html representation of question
			var newQuestion = addQuestion (newObjective, questionData);

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
				// console.log (answerNumber + " ==> "
				// + JSON.stringify (answerData));
				mission['answers'][answerNumber] = answerData;
			}
			mission['questions'][questionNumber] = questionData;
		}
	}

	// alert("format for questions now: \n" +
	// JSON.stringify($('body').data('mission')['questions']));
}

function deleteObjective ()
{
	$ (".Objective[id=" + arguments[0] + "]").multiAccordion ("remove");
}

function addObjective (baseObjective)
{
	// create the html for the new objective & put in page
	var newObjective = $ ("#ObjectiveTemplate").tmpl (baseObjective);
	$ ("#ObjectivesAccordion").multiAccordion ("append", newObjective);

	// initialize tabs for new objective
	$ (".ObjectiveTabs", newObjective).tabs ();

	// bind delete button for new objective
	$ (".DeleteObjectiveButton", newObjective).click (
			function ()
			{
				var parentObjective = $ (this).closest (".Objective");
				deleteSpecificObjective = deleteObjective.bind (undefined,
						parentObjective.attr ('id'));
				$ ("#DeleteObjectivePrompt #ObjectiveName").html (
						$ ("input[name=objectiveTitle]", parentObjective)
								.val ());
				$ ("#DeleteObjectivePrompt").dialog ('open');
			});

	// initialize questions accordion for new objective
	$ (".QuestionsAccordion", newObjective).multiAccordion ();

	$ (".QuestionsAccordion", newObjective).bind ("onChange",
			function (event, panel, isOpening)
			{
				// console.log ("changed question, isOpening is " + isOpening);
				renameAccordionHeaders (panel, isOpening, getQuestionTitle);
			});

	$ (".QuestionsAccordion", newObjective).bind ("sortstop", function (event)
	{
		// console.log ("renumbered questions");
		renumberOrderedItems ('Question', this);
		event.stopPropagation ();
	});

	$ (".QuestionsAccordion", newObjective).bind ("onAppend", function ()
	{
		// console.log ("appended question");
		var numPanels = $ (this).multiAccordion ("getNumPanels");
		if (numPanels >= 5)
			$ (".AddQuestionWidget#auxiliary", newObjective).show ();
		renumberOrderedItems ('Question', this);
	});

	$ (".QuestionsAccordion", newObjective).bind ("onRemove", function ()
	{
		// console.log ("removed question");
		var numPanels = $ (this).multiAccordion ("getNumPanels");
		if (numPanels < 5)
			$ (".AddQuestionWidget#auxiliary", newObjective).hide ();
		renumberOrderedItems ('Question', this);
	});

	$ (".AddQuestionButton", newObjective).click (
			function ()
			{
				// create & add new question of specified type
				var typeSelector = $ (this).siblings (
						"select.QuestionTypeSelector");
				var newQuestion = new Question (typeSelector.val ());
				var questionElement = addQuestion (newObjective, newQuestion);

				// open this question for editing
				var numPanels = $ (".QuestionsAccordion", newObjective)
						.multiAccordion ("getNumPanels");
				$ (".QuestionsAccordion", newObjective).multiAccordion (
						"openPanelByIndex", numPanels);

				// focus in the question text area
				$ ('textarea', questionElement).focus ();
			});

	// advance objective counter
	incrementNumberCounter ('objective');

	return (newObjective);
}

function deleteQuestion ()
{
	$ (".Question[id=" + arguments[0] + "]").multiAccordion ("remove");
}

// when call this with new question, can't just pass in default, must set
// order in it appropriately
function addQuestion (parentObjective, questionInfo)
{
	var newQuestion = $ ("#QuestionTemplate").tmpl (questionInfo);
	$ (".QuestionsAccordion", parentObjective).multiAccordion ("append",
			newQuestion);

	$ (".DeleteQuestionButton", newQuestion).click (
			function ()
			{
				deleteSpecificQuestion = deleteQuestion.bind (undefined,
						newQuestion.attr ('id'));
				$ ("#DeleteQuestionPrompt").dialog ('open');
			});

	if (questionInfo['questionTypeId'] == 1)
	// multidropdown question, do some stuff
	{
		newQuestion.data ("markerPositionsArray", []);

		newQuestion.data ("needReadjust", false);

		$ (".InsertMarker", newQuestion).click (function ()
		{
			insertMarker (newQuestion);
			$ ('textarea', newQuestion).focus ();
		});

		$ ('textarea', newQuestion).keydown (function (event)
		{
			handleKeyPress (event, newQuestion);
		});

		$ ('textarea', newQuestion).click (function ()
		{
			ensureNotOnMarker (newQuestion);
		});

		$ ('textarea', newQuestion).bind ('onDrop', function (event)
		{
			console.log ("dropped");
			event.preventDefault ();
		});

		$ ('textarea', newQuestion).bind ('onDragOver', function (event)
		{
			event.preventDefault ();
		});

		$ ('textarea', newQuestion).bind ('paste', function (event)
		{
			ensureNotOnMarker (newQuestion);
		});

		$ ('textarea', newQuestion).bind ('cut', function (event)
		{
			ensureNotOnMarker (newQuestion);
		});

		$ ('textarea', newQuestion).change (function (event)
		{
			checkForMissingMarkers (newQuestion);
		});
	}
	else
	// not multiDropdown, bind one addAnswer button
	{
		$ (".AddAnswerButton", newQuestion).click (function ()
		{
			var answerList = $ (".AnswersList", newQuestion);
			attachAnswer (newQuestion, answerList);
			// trigger 'onclick' event for answers list when number of answers
			// could change
			$ (".AnswersList", newQuestion).trigger ('onclick');
		});

		$ (".AnswersList", newQuestion).sortable ();
		$ (".AnswersList", newQuestion).bind ("sortupdate",
				function (event, ui)
				{
					reorderAnswers (this);
				});
	}

	incrementNumberCounter ('question');

	return (newQuestion);
}

function attachAnswer (parentQuestion, nearestAnswersList)
{
	var placementMarker = nearestAnswersList.attr ("data-placement-marker");
	var blankAnswer = new Answer (placementMarker);
	addAnswer (parentQuestion, blankAnswer);
	reorderAnswers (nearestAnswersList);
}

function generateAnswerGroups (question)
{
	var questionText = $ ('textarea', question).val ();
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

function deleteAnswer ()
{
	var answerToDelete = arguments[0];
	var answersListToDeleteFrom = answerToDelete.closest (".AnswersList");
	answerToDelete.remove ();
	reorderAnswers (answersListToDeleteFrom);
	// trigger 'onclick' event for answers list when number of answers could
	// change
	answersListToDeleteFrom.trigger ('onclick');
}

function addAnswer (parentQuestion, answerInfo)
{
	console.log ("adding new answer");
	// make answer from template
	var isCorrectInputType = (parentQuestion.data ("typeid") == 1) ? 'radio'
			: 'checkbox';
	var newAnswer = $ ("#AnswerTemplate").tmpl (answerInfo,
	{
		type : isCorrectInputType
	});

	// attach to appropriate spot, in parent question with right marker data
	// console.log ("possible answer lists ");
	// console.log ($ (".AnswersList", parentQuestion));
	// console.log ("right answer list ");
	// console.log ($ (".AnswersList[data-placement-marker="
	// + answerInfo.placementMarker + "]", parentQuestion));
	var answerListToAddTo = $ (".AnswersList[data-placement-marker="
			+ answerInfo.placementMarker + "]", parentQuestion);
	answerListToAddTo.append (newAnswer);

	// bind function to radio/checkbox selection so values change appropriately
	$ ('input[name=isCorrect]', newAnswer).click (function ()
	{
		if ($ (this).is (':checked'))
		{
			// just checked this checkbox/radio button. If it is a radio button,
			// change values of all others to reflect that have become
			// unselected
			if ($ (this).attr ('type') == "radio")
			{
				var allAnswersList = newAnswer.closest (".AnswersList");
				$ ('input[name=isCorrect]', allAnswersList).val ("0");
			}

			// change this one's value to show has been selected
			$ (this).val ("1");
		}
		else
		{
			// no longer checked, so set value to 0
			$ (this).val ("0");
		}

	});

	// set up delete button binding
	$ (".DeleteAnswerButton", newAnswer).click (function ()
	{
		deleteSpecificAnswer = deleteAnswer.bind (undefined, newAnswer);
		$ ("#DeleteAnswerPrompt").dialog ('open');
	});

	incrementNumberCounter ('answer');
}

function ignore (event)
{
	event.preventDefault ();
}

function insertMarker (parentQuestion)
{
	// variables to use elsewhere
	var questiontextArea = $ ('textarea', parentQuestion);
	var questiontextAreaElement = questiontextArea.get (0);

	// get cursor postion or starting index if selection
	var cursorPosition = questiontextAreaElement.selectionStart;

	insertMarkerOverPositions (parentQuestion, questiontextArea,
			questiontextAreaElement, cursorPosition, cursorPosition);

}

function insertMarkerOverPositions (parentQuestion, questiontextArea,
		questiontextAreaElement, positionStart, positionEnd)
{
	// find index of marker that will now follow this one, ensure not inside a
	// marker
	var returnVals = ensureNotOnMarker (parentQuestion);
	var nextMarkerIndex = returnVals['nextMarker'];

	// add marker info to text area
	addMarkerToText (questiontextArea, positionStart, positionEnd,
			nextMarkerIndex);

	// determine where newly inserted marker ends
	var newMarkerEnd = positionStart + (nextMarkerIndex + 1).toString ().length
			+ 2;

	// increment numbers of all markers after this one
	incrementMarkersAfter (questiontextArea, newMarkerEnd, nextMarkerIndex);

	// position cursor right after added marker
	setSelectedRange (questiontextAreaElement, newMarkerEnd, newMarkerEnd);

	// add answer group to html
	addAnswerGroup (nextMarkerIndex, parentQuestion);

	// update markers in html
	renumberAnswerGroups (parentQuestion);

	return (nextMarkerIndex);
}

function deleteDropdown ()
{
	var groupToRemove = parseInt (arguments[0]);
	// console.log (" remove -- " + groupToRemove);
	var answerGroup = arguments[1];
	var parentQuestion = answerGroup.closest (".Question");
	var questiontextArea = $ ('textarea', parentQuestion);

	// remove marker from text
	var oldMarkerPosition = removeMarkerFromText (questiontextArea,
			groupToRemove);

	// renumber remaining markers
	decrementMarkersAfter (questiontextArea, oldMarkerPosition, groupToRemove);

	// remove answers group from html
	answerGroup.remove ();
	renumberAnswerGroups (parentQuestion);

	checkLabelStatus (parentQuestion);

	// for validation for answers areas
	$ (".AnswersArea", parentQuestion).trigger ('onclick');
}

function checkLabelStatus (dropdownQuestion)
{
	if ($ (".AnswerGroup", dropdownQuestion).size () == 0)
	{
		// got rid of last dropdown, so remve label
		$ (".AnswersLabel", dropdownQuestion).hide ();
	}
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

	$ (".AnswersList", newAnswerGroup).sortable ();
	$ (".AnswersList", newAnswerGroup).bind ("sortupdate", function (event, ui)
	{
		reorderAnswers (this);
	});

	$ (".DeleteDropdownButton", newAnswerGroup).click (
			function ()
			{
				var groupNumber = $ ('.AnswersList', newAnswerGroup).data (
						"placementMarker");
				deleteSpecificDropdown = deleteDropdown.bind (undefined,
						groupNumber, newAnswerGroup);
				$ ("#DeleteAnswerGroupPrompt").dialog ('open');
			});

	$ (".AddAnswerButton", newAnswerGroup).click (function ()
	{
		var answersList = $ (".AnswersList", newAnswerGroup);
		attachAnswer (parentQuestion, answersList);
		// for validation
		$ (".AnswersList", parentQuestion).trigger ('onclick');
	});

	$ (".AnswersLabel", parentQuestion).show ();

	// a new answer group was added, so let parent answers area indicate change
	// for validation
	$ (".AnswersArea", parentQuestion).trigger ('onclick');
}

function renumberAnswerGroups (parentQuestion)
{
	var counter = 0;
	$ (".AnswerGroup", parentQuestion).each (
			function ()
			{
				$ (".AnswerGroupHeader", this).html (
						"{" + (counter + 1).toString () + "}");
				// $ ('.AnswersList', this).data ("placementMarker", counter);
				$ ('.AnswersList', this)
						.attr ("data-placement-marker", counter);
				++counter;
			});
}

// handle key presses in text area, ensure no markers are altered or removed
function handleKeyPress (event, parentQuestion)
{
	if (isInsertShortcut (event))
	{
		insertMarker (parentQuestion);
	}
	else if (isCtrlZ (event))
	{
		event.preventDefault ();
	}
	else if (!checkManualInsert (event, parentQuestion))
	{
		var questiontextArea = $ ('textarea', parentQuestion);

		// ensure cursor is not inside a marker, or if text is selected,
		// selection does not contain a marker
		var returnVals = ensureNotOnMarker (parentQuestion);
		var markerAfterCursor = returnVals['nextMarker'];
		var positionsArray = returnVals['positionsArray'];

		handleSpecialKeys (event, questiontextArea, positionsArray,
				markerAfterCursor);
	}
}

function handleSpecialKeys (event, questiontextArea, positionsArray,
		markerAfterCursor)
{
	// get cursor start and end positions
	var questiontextAreaElement = questiontextArea.get (0);
	var cursorStartPosition = questiontextAreaElement.selectionStart;
	var cursorEndPosition = questiontextAreaElement.selectionEnd;

	if (isBackspace (event))
	{
		// backspace - make sure was not at end of marker. know cannot be in
		// middle of marker at this point, becuase of call to
		// "ensureNotOnMarker"
		var directlyBehindMarker = isDirectlyAfterMarker (questiontextArea
				.val (), positionsArray, markerAfterCursor,
				cursorStartPosition, cursorEndPosition);
		if (directlyBehindMarker)
		{
			// prevent from backspacing away marker
			event.preventDefault ();
		}
	}
	else if (isDeleteKey (event))
	{
		// delete - make sure was not at beginning of marker
		var directlyBeforeMarker = isDirectlyBeforeMarker (positionsArray,
				markerAfterCursor, cursorStartPosition, cursorEndPosition);
		if (directlyBeforeMarker)
		{
			// don't delete marker
			event.preventDefault ();
		}
	}
	else if (isLeftArrowKey (event))
	{
		var directlyBehindMarker = isDirectlyAfterMarker (questiontextArea
				.val (), positionsArray, markerAfterCursor,
				cursorStartPosition, cursorEndPosition);
		if (directlyBehindMarker)
		{
			var markerStart = positionsArray[markerAfterCursor - 1];
			setSelectedRange (questiontextArea.get (0), markerStart,
					markerStart);
			event.preventDefault ();
		}
	}
	else if (isRightArrowKey (event))
	{
		var directlyBeforeMarker = isDirectlyBeforeMarker (positionsArray,
				markerAfterCursor, cursorStartPosition, cursorEndPosition);
		if (directlyBeforeMarker)
		{
			positionCursorAtEnd (markerAfterCursor, positionsArray,
					questiontextArea.get (0));
			event.preventDefault ();
		}
	}
}

// ensures cursor is not on marker, or selection does not contain marker
// returns array where ['positionsArray'] is list of markers and starting
// positions, and ['nextMarker'] is number of marker directly after cursor
// position or selection area
// after calling this function, is safe to insert text at the cursor location,
// or if there is a selection, remove or replace that selection with text
function ensureNotOnMarker (parentQuestion)
{
	// returns a list of positions of markers we need to preserve
	// doing this every time ensures that we know correct positions
	// of all markers
	var markerPositions = readjustPositions (parentQuestion);

	var questiontextAreaElement = $ ('textarea', parentQuestion).get (0);
	var cursorStartPosition = questiontextAreaElement.selectionStart;
	var cursorEndPosition = questiontextAreaElement.selectionEnd;

	var returnResults =
	{};
	returnResults['positionsArray'] = markerPositions;

	if (cursorStartPosition != cursorEndPosition)
	{
		// portion selected, ensure contains no markers, reset to first if does
		for ( var markerNum in markerPositions)
		{
			var markerStart = markerPositions[markerNum];

			// convert markerNum to an int for convenience later
			markerNum = parseInt (markerNum);

			var markerText = "{" + (markerNum + 1).toString () + "}";
			var markerEnd = markerStart + markerText.length;

			if (cursorStartPosition < markerEnd)
			{
				// selection starts in front of or in this marker
				var newCursorStart;
				var newCursorEnd = cursorEndPosition;
				var nextMarkerNum = markerNum;

				if (cursorEndPosition <= markerEnd)
				{
					// selection ends in front of or in this marker as well
					newCursorEnd = Math.min (cursorEndPosition, markerStart);
					newCursorStart = Math
							.min (cursorStartPosition, markerStart);
				}
				else
				{
					// selection starts in front of this marker but selection
					// ends behind it, move selected area to beyond marker
					newCursorStart = markerEnd;
					nextMarkerNum = markerNum + 1;
					if ((markerNum + 1) < markerPositions.length)
					{
						newCursorEnd = Math.min (cursorEndPosition,
								markerPositions[markerNum + 1]);
					}
				}

				if (newCursorStart != cursorStartPosition
						|| newCursorEnd != cursorEndPosition)
				{
					console.log ("set selected: " + newCursorStart + ","
							+ newCursorEnd);
					// set cursor positions to reflect adjustments
					// only OK to do this if adjustments have been made,
					// otherwise, strange behavior
					setSelectedRange (questiontextAreaElement, newCursorStart,
							newCursorEnd);
				}

				returnResults['nextMarker'] = nextMarkerNum;
				return (returnResults);
			}
		}
	}
	else
	{
		// nothing selected, just make sure is not inside marker
		for ( var markerNum in markerPositions)
		{
			var markerStart = markerPositions[markerNum];

			// convert markerNum to an int for convenience later
			markerNum = parseInt (markerNum);

			var markerText = "{" + (markerNum + 1).toString () + "}";
			var markerEnd = markerStart + markerText.length;
			if (cursorStartPosition < markerEnd)
			{
				var newStartPos = Math.min (cursorStartPosition, markerStart);
				setSelectedRange (questiontextAreaElement, newStartPos,
						newStartPos);
				returnResults['nextMarker'] = markerNum;
				return (returnResults);
			}
		}
	}

	// there is no marker after the current selection or cursor position
	returnResults['nextMarker'] = markerPositions.length;
	return (returnResults);
}

function removeMarkerFromText (questiontextArea, markerNum)
{
	var markerText = "{" + (markerNum + 1).toString () + "}";
	var markerLength = markerText.length;

	var questionText = questiontextArea.val ();

	var markerStart = questionText.indexOf (markerText);

	var beginningText = questionText.substring (0, markerStart);
	var endingText = questionText.substring (markerStart + markerLength);

	questiontextArea.val (beginningText + endingText);

	return (markerStart);
}

// replace the text in the given textArea between positionStart and positionEnd
// with a marker for the specified number
function addMarkerToText (textArea, positionStart, positionEnd, markerNumber)
{
	var questionText = textArea.val ();
	var beginningText = questionText.substring (0, positionStart);
	var endingText = questionText.substring (positionEnd);
	var marker = "{" + (markerNumber + 1) + "}";
	textArea.val (beginningText + marker + endingText);

	return (marker.length);
}

function incrementMarkersAfter (textArea, searchStart, startingMarkerNum)
{
	var parentQuestion = textArea.closest (".Question");
	var maxMarkerNum = $ (".AnswerGroup", parentQuestion).size ();
	for ( var markerNum = startingMarkerNum; markerNum < maxMarkerNum; ++markerNum)
	{
		var markerText = "{" + (markerNum + 1).toString () + "}";
		var markerStartPos = textArea.val ().indexOf (markerText, searchStart);
		var markerEndPos = markerStartPos + markerText.length;
		addMarkerToText (textArea, markerStartPos, markerEndPos, markerNum + 1);

		searchStart = markerEndPos;
	}
}

function decrementMarkersAfter (textArea, searchStart, startingMarkerNum)
{
	var parentQuestion = textArea.closest (".Question");
	var maxMarkerNum = $ (".AnswerGroup", parentQuestion).size ();
	// console.log ("search start " + searchStart);
	// console.log ("start marker num " + startingMarkerNum);
	// console.log ("max marker " + maxMarkerNum);
	for ( var markerNum = startingMarkerNum + 1; markerNum < maxMarkerNum; ++markerNum)
	{
		// console.log ("marker num " + markerNum);
		var markerText = "{" + (markerNum + 1).toString () + "}";
		// console.log ("marker text " + markerText);
		var markerStartPos = textArea.val ().indexOf (markerText, searchStart);
		// console.log ("marker start " + markerStartPos);
		var markerEndPos = markerStartPos + markerText.length;
		// console.log ("marker end " + markerEndPos);
		// console.log ("replacing " + markerStartPos + " to " + markerEndPos +
		// " with number " + (markerNum - 1).toString());
		var replacementMarkerLength = addMarkerToText (textArea,
				markerStartPos, markerEndPos, markerNum - 1);

		searchStart = markerStartPos + replacementMarkerLength;
	}
}

function readjustPositions (question)
{
	var positionsArray = [];
	var textArea = $ ('textarea', question);
	var questionText = textArea.val ();
	var numMarkers = $ ('.AnswerGroup', question).size ();
	var markersToRemove = [];
	var startingSearchPosition = 0;
	var counter = 0;
	for ( var markerNum = 0; markerNum < numMarkers; ++markerNum)
	{
		// console.log ("search starts on " + startingSearchPosition);
		// console.log ("for marker " + markerNum + " that started on " +
		// positionsArray[markerNum]);
		var markerText = "{" + (markerNum + 1) + "}";
		var markerLength = markerText.length;
		var markerStart = questionText.indexOf (markerText,
				startingSearchPosition);
		if (markerStart == -1)
		{
			// marker gotremoved by an undo operation, take out of html
			markersToRemove.push (markerNum);
		}
		else
		{
			positionsArray[counter] = markerStart;
			if (counter != markerNum)
			{
				// need to renumber
				markerLength = addMarkerToText (textArea, markerStart,
						markerStart + markerLength, counter);
			}
			startingSearchPosition = markerStart + markerLength;
			++counter;
		}
		// console.log ("new start is " + markerStart);
	}
	// remove missing markers from html
	for ( var index in markersToRemove)
	{
		var markerToRemove = markersToRemove[index];
		// console.log ("must remove group # " + markerToRemove);
		var answersListForGroup = $ (".AnswersList[data-placement-marker="
				+ markerToRemove + "]", question);
		// console.log (answersListForGroup);
		var answerGroup = answersListForGroup.closest (".AnswerGroup");
		// console.log (answerGroup);
		answerGroup.remove ();
	}

	renumberAnswerGroups (question);

	checkLabelStatus (question);

	return (positionsArray);
}

function checkForMissingMarkers (question)
{
	console.log ("check for missingngngngngngngngngngngngn");
	var textArea = $ ('textarea', question);
	var questionText = textArea.val ();
	var numMarkers = $ ('.AnswerGroup', question).size ();
	var markersToRemove = [];
	var startingSearchPosition = 0;
	var counter = 0;
	for ( var markerNum = 0; markerNum < numMarkers; ++markerNum)
	{
		// console.log ("search starts on " + startingSearchPosition);
		// console.log ("for marker " + markerNum + " that started on " +
		// positionsArray[markerNum]);
		var markerText = "{" + (markerNum + 1) + "}";
		var markerLength = markerText.length;
		var markerStart = questionText.indexOf (markerText,
				startingSearchPosition);
		if (markerStart == -1)
		{
			// marker gotremoved by an undo operation, take out of html
			markersToRemove.push (counter);
		}
		else
		{
			if (counter != markerNum)
			{
				// need to renumber
				markerLength = addMarkerToText (textArea, markerStart,
						markerStart + markerLength, counter);
			}
			startingSearchPosition = markerStart + markerLength;
			++counter;
		}
		// console.log ("new start is " + markerStart);
	}
	// remove missing markers from html
	for ( var markerToRemove in markersToRemove)
	{
		var answersListForGroup = $ (".AnswersList[data-placement-marker="
				+ markerToRemove + "]", question);
		var answerGroup = answersListForGroup.closest (".AnswerGroup");
		answerGroup.remove ();
		// for validation for answers area
		$ (".AnswersArea", question).trigger ('onclick');
	}

	renumberAnswerGroups (question);

	if ($ (".AnswerGroup", question).size () == 0)
	{
		// got rid of last dropdown, so remve label
		$ (".AnswersLabel", question).hide ();
	}

}

function isDirectlyBeforeMarker (positionsArray, markerNum,
		cursorStartPosition, cursorEndPosition)
{
	var directlyBeforeMarker = false;
	if (cursorEndPosition == cursorStartPosition)
	{
		var startNextMarker = positionsArray[markerNum];
		directlyBeforeMarker = (cursorStartPosition == startNextMarker);
	}
	return (directlyBeforeMarker);
}

function isDirectlyAfterMarker (questionText, positionsArray,
		markerAfterCursor, cursorStartPosition, cursorEndPosition)
{
	var directlyBehindMarker = false;
	if (markerAfterCursor != 0 && cursorEndPosition == cursorStartPosition)
	{
		// cursor is not before the first marker and no text is selected,
		// so need to ensure not behind a marker. If cursor was before first
		// marker, or there is a text selection to be backspaced, there
		// would be no need to do anything
		var previousMarkerNum = markerAfterCursor - 1;
		var startPreviousMarker = positionsArray[previousMarkerNum];
		var endPreviousMarker = questionText.indexOf ("}", startPreviousMarker) + 1;
		directlyBehindMarker = (cursorStartPosition == endPreviousMarker);
	}
	return (directlyBehindMarker);
}

// keypress detection functions - to be used inside the keypress event only

function isInsertShortcut (event)
{
	var asciiCode = event.which || event.keyCode;
	// 191 = ?/ key in keydown, w or w/o shift, in chrome or firefox
	var isShortcut = (asciiCode == 191 && event.ctrlKey);
	return (isShortcut);
}

// true if undo/redo key binding
function isCtrlZ (event)
{
	var asciiCode = event.which || event.keyCode;
	// console.log(asciiCode);
	return (asciiCode == 90 && event.ctrlKey);
}

function isDeleteKey (event)
{
	// ------- firefox, opera ---------- safari---------konqueror (whatever that
	// is)
	return (event.keyCode == 46 || event.keyCode == 63272 || event.keyCode == 127);
}

function isBackspace (event)
{
	return (event.keyCode == 8);
}

function isRightArrowKey (event)
{
	return (event.keyCode == 39);
}

function isLeftArrowKey (event)
{
	return (event.keyCode == 37);
}

function isEnterKey (event)
{
	return (event.keyCode == 13);
}
function checkManualInsert (event, parentQuestion)
{
	if (!isEnterKey (event))
	{
		// manual insert must be done with enter key
		return (false);
	}

	// get text area & text area element
	var questiontextArea = $ ('textarea', parentQuestion);
	var questiontextAreaElement = questiontextArea.get (0);
	// consider when cursor behond insert expression, or when there is a
	// selection that ends directly behind insert expression
	var cursorEndPosition = Math.max (questiontextAreaElement.selectionStart,
			questiontextAreaElement.selectionEnd);
	// search from beginning of question text up until current cursor position
	var textToSearch = questiontextAreaElement.value.substring (0,
			cursorEndPosition);
	// {answer text,points[* if correct]; ... }
	var dropdownFormat = /\{([^{},;\n]*,(-?[0-9]*)?\*?;)*\}/gi;
	var matches = textToSearch.match (dropdownFormat);
	if (!matches)
	{
		// no correctly formated insert sections in text anywhere
		return (false);
	}

	// last one will be the one that we could be inserting
	var lastMatch = matches.pop ();
	// check to see if cursor was directly behind
	var lastMatchPotentialStart = cursorEndPosition - lastMatch.length;
	var textBeforeCursor = textToSearch.substring (lastMatchPotentialStart);
	if (lastMatch != textBeforeCursor)
	{
		// match is not directly before cursor when enter pressed, do not insert
		// anything
		return (false);
	}

	// prevent enter event from occuring
	event.preventDefault ();

	// insert marker over regex structure
	var nextMarkerIndex = insertMarkerOverPositions (parentQuestion,
			questiontextArea, questiontextAreaElement, lastMatchPotentialStart,
			cursorEndPosition);

	// get answers area for newly inserted dropdown
	var newlyAddedAnswerGroup = $ (".AnswersArea .AnswerGroup:nth-child("
			+ (nextMarkerIndex + 1).toString () + ")", parentQuestion);
	console.log (newlyAddedAnswerGroup);
	var answersList = $ (".AnswersList", newlyAddedAnswerGroup);
	var placementMarker = answersList.attr ("data-placement-marker");

	// get array for each of the answer data sections
	var dropdownAnswers = lastMatch.slice (1, lastMatch.length - 1).split (";");
	// last one will be empty
	dropdownAnswers.pop ();

	// add any answers provided to dropdown
	$.each (dropdownAnswers, function ()
	{
		var answerContent = new Answer (placementMarker);
		var data = this.split (",");
		answerContent.answerText = data[0];
		var isCorrect = (data[1].indexOf ("*") == -1) ? 0 : 1;
		if (isCorrect)
		{
			data[1] = data[1].substring (0, data[1].length - 1);
		}
		answerContent.isCorrect = isCorrect;
		answerContent.points = !data[1] ? 0 : data[1];
		addAnswer (parentQuestion, answerContent);
		reorderAnswers (answersList);
	});

	// succesfully inserted marker from regex
	// lets keydown handler know nothing else needs to be done
	return (true);

}

function setSelectedRange (element, cursorStart, cursorEnd)
{
	if (element.setSelectionRange)
	{
		element.setSelectionRange (cursorStart, cursorEnd);
	}
	else if (element.createTextRange)
	{
		// code from stackoverflow.com
		var newRange = element.createTextRange ();
		newRange.collapse (true);
		newRange.moveEnd ('character', cursorEnd);
		newRange.moveStart ('character', cursorStart);
		newRange.select ();
	}
	else
	{
		console.log ("Too Bad");
	}
}

function positionCursorAtEnd (markerNum, positionsArray,
		questiontextAreaElement)
{
	var markerStartPos = positionsArray[markerNum];
	var markerLength = ("{" + (markerNum + 1).toString () + "}").length;
	var markerEndPos = markerStartPos + markerLength;
	setSelectedRange (questiontextAreaElement, markerEndPos, markerEndPos);
}

// set up mission object for this page
// For new missions, will be used to get values of fields for record in database
// For missions already in database, keeps track of latest values sent to
// database, so know what needs to be updated
function initializeMission ()
{
	var missionEditingInfo = $ ('body').data ('editModeInfo');
	if (missionEditingInfo.missionId == null)
	{
		// global mission variable initialized, will have default
		// values for all fields
		var mission = new Mission ();
		mission.createdByUserId = localStorage['userId'];
		mission.createdOnDate = -1; // indicate to php
		// when new mission is added that will need to fill.
		$ ('body').data ('mission', mission);

		initCounters ();
	}
	else
	{
		// get info from DB with for mission with specified mission id
		// initialize global mission variable with this info
		$
				.ajax (
				{
					url : './Php/getMissionInfo.php',
					data :
					{
						missionId : missionEditingInfo.missionId
					},
					success : function (result)
					{
						// alert ("successful, result is " + result);
						initMissionFromExisting (JSON.parse (result));
					},
					error : function (jqXHR)
					{
						if (!handleLoginError (jqXHR))
						{
							alert ("There has been an error retrieving this mission from the database.");
							// return to previous page
							window.history.back ();
						}
					},
					async : false
				});
	}
	// alert ("mission" + JSON.stringify ($ ('body').data ('mission')));
}

// creates a new mission with data from missionContents
function initMissionFromExisting (missionContents)
{
	// alert("contents:" + JSON.stringify(missionContents));
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

	// console.log(JSON.stringify(mission.answers));

	// ensure not frozen, regardless of whether original was or not
	mission.isFrozenCopy = 0;

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
	$ (".AddObjectiveButton").click (
			function (event)
			{
				var newObjective = addObjective (new Objective ());

				// open new objective
				var numPanels = $ ("#ObjectivesAccordion").multiAccordion (
						"getNumPanels");
				$ ("#ObjectivesAccordion").multiAccordion ("openPanelByIndex",
						numPanels);

				// focus in title
				$ ("input[name=objectiveTitle]", newObjective).focus ();
			});

	$ ("Button#Save").click (saveMission);
	$ ("Button#SaveAsDraft").click (saveMissionAsDraft);
	$ ("Button#SaveAsComplete").click (handleSaveAsCompleteButtonPress);
	$ ("Button#Cancel").click (cancelChanges);
	$ ("Button#Discard").click (showDiscardPrompt);
}

// show the buttons appropriate for state
function showButtons ()
{
	var missionEditingInfo = $ ('body').data ('editModeInfo');
	var buttonClassSelector;
	if (missionEditingInfo.editMode == "new")
	{
		buttonClassSelector = ".New";
	}
	else
	{
		if (missionEditingInfo.idType == "current")
		{
			buttonClassSelector = ".Edit";
		}
		else
		{
			buttonClassSelector = ".EditNonDraft";
		}
	}
	$ (".OptionButton").hide ();
	$ (".OptionButton" + buttonClassSelector).show ();
}

function displayAddTopicDialog ()
{
	var currentCategoryId = $ (".MissionDataField[name=categoryId]").val ();
	if (currentCategoryId == 0)
	{
		$ ("#SelectCategoryAlert").dialog ('open');
	}
	else
	{
		$ ("#CreateTopicDialog").dialog ('open');
		$ ("#CreateTopicDialog").dialog ("option", "width", 450);
	}
}

function displayAddCategoryDialog ()
{
	$ ("#CreateCategoryDialog").dialog ('open');
}

function addTopicOrCategory (type)
{
	var categoryId = (type == 'Topic') ? $ (
			".MissionDataField[name=categoryId]").val () : -1;
	var nameDescriptionData = new Object ();

	// TODO: validation here

	$ (".New" + type + "Field").each (function ()
	{
		var attributeName = $ (this).attr ('name');
		var attributeValue = $ (this).val ();
		nameDescriptionData[attributeName] = attributeValue;
	});

	$.ajax (
	{
		url : './Php/addTopicOrCategory.php',
		data :
		{
			categoryId : categoryId,
			data : JSON.stringify (nameDescriptionData)
		},
		success : function (result)
		{
			if (type == 'Topic')
			{
				// just added new topic, id returned in result
				// ensure new topic will show up
				populateTopicsDropdown (categoryId, "#TopicDropdown",
						"Choose Topic");
				// select one just added
				$ ("#TopicDropdown").val (result);
			}
			else
			{
				// just added new category, id returned in result
				// ensure will be listed in category dropdown
				populateCategoryDropdown ("#CategoryDropdown",
						"Choose Category...");
				// make be selected
				$ ("#CategoryDropdown").val (result).change ();
			}
		},
		error : function (jqXHR)
		{
			if (!handleLoginError (jqXHR))
			{
				alert ("There has been an error adding this " + type + ".");
			}
		},
		async : false
	});
}

// in this function, will create arrays with attributes that define a mission
// all attributes for mission must be included in array, since will need to be
// set
// when added to db
// NOTE: for adding mission to database, want following fields in array send to
// php - 1.missionName 2.missionDescription 3.categoryId 4.topicId 5.isPublic
// 6.missionStatusId 7.createdOn 8.createdByUserId 9.estimatedTimeToComplete
// also, isFrozen could be sent along, or handled in php since the value is
// always false
// that means must exclude following fields found in Mission -
// missionId-autoincrement geopodVersion-set by db when add
// lastModifiedByDate & lastModifiedUserId - taken care of in php file (although
// could do here)
// background, objectives, all functions
function addMissionToDatabase ()
{
	var mission = $ ('body').data ('mission');
	// update contents of global mission to match
	// fields on mission editor page
	$ (".MissionDataField").each (function ()
	{
		var attributeName = $ (this).attr ('name');
		var valueInForm = $ (this).val ();
		var oldValue = mission[attributeName];
		if (oldValue != valueInForm)
		{
			mission[attributeName] = valueInForm;
		}
		// alert ("name is " + attributeName + " val is now "
		// + $ ('body').data ('mission')[attributeName]);
	});

	// create array will use to send mission info to database
	var fieldInfoForDatabase =
	{};

	var propertiesToSendToDB = [ 'missionName', 'missionDescription',
			'backgroundText', 'isPublic', 'isFrozenCopy', 'createdOnDate',
			'createdByUserId', 'estimatedTimeToComplete', 'categoryId',
			'topicId', 'missionStatusId' ];
	for ( var element in propertiesToSendToDB)
	{
		var attributeName = propertiesToSendToDB[element];
		fieldInfoForDatabase[attributeName] = mission[attributeName];
	}

	// stringinfy the info for the php file
	fieldInfoForDatabase = JSON.stringify (fieldInfoForDatabase);

	// alert ("adding new mission with given info : " + fieldInfoForDatabase);

	// update objectives
	// get rid of old objectivse - don't care about since don't
	// represent anything, so don;t wory about whether some have
	// been added or removed, just reconstruct from what's on the page
	mission['objectives'] =
	{};
	$ (".Objective").each (function ()
	{
		var objectiveNumber = $ (this).attr ('id');
		var objectiveInfo =
		{};
		$ (".ObjectiveDataField", this).each (function ()
		{
			var attributeName = $ (this).attr ('name');
			objectiveInfo[attributeName] = $ (this).val ();
		});
		// alert("adding " + objectiveNumber + " ==> " +
		// JSON.stringify(objectiveInfo));
		mission['objectives'][objectiveNumber] = objectiveInfo;
	});
	// at this point, contents of Mission.objectives represents lateast to be
	// sent to database

	// update questions
	// get rid of old qustions and reconstruct from what's on page
	// don't care about how what's on the page differes from what's
	// in database (and hence in Mission.questions), since creating
	// new mission anyway
	mission['questions'] =
	{};
	var questionDataForDB =
	{};
	$ (".Question").each (function ()
	{
		var questionNumber = $ (this).attr ('id');
		var questionInfo =
		{};
		questionInfo['questionTypeId'] = $ (this).data ('typeid');
		$ (".QuestionDataField", this).each (function ()
		{
			var attributeName = $ (this).attr ('name');
			questionInfo[attributeName] = $ (this).val ();
		});
		mission['questions'][questionNumber] = questionInfo;

		// send parent objective # in 'objectiveId' field, will switch
		// in real objectiveId in php, when know every objective# will
		// have corrisponding id. At this point, may not.
		var parentObjectiveNum = $ (this).closest (".Objective").attr ('id');
		questionInfo['objectiveId'] = parentObjectiveNum;
		questionDataForDB[questionNumber] = questionInfo;
	});

	// update answers
	// get rid of old answers and reconstruct from what's on page,
	// will be losing things like old answer ids, but don't care about any more
	// anyway
	mission['answers'] =
	{};
	var answerDataForDB =
	{};
	$ (".Answer").each (
			function ()
			{
				var answerNumber = $ (this).attr ('id');
				var answerInfo =
				{};
				answerInfo['placementMarker'] = $ (this).closest (
						".AnswersList").attr ('data-placement-marker');
				$ (".AnswerDataField", this).each (function ()
				{
					var attributeName = $ (this).attr ('name');
					answerInfo[attributeName] = $ (this).val ();
				});
				mission['answers'][answerNumber] = answerInfo;

				// console.log (answerNumber + " --> "
				// + JSON.stringify (answerInfo));

				// as for questions and objectives, send parent question
				// # in 'questionId' field, will switchin real
				// questionId in php, when know everyquestion# will have
				// corrisponding id. At this point, may not.
				var parentQuestionNum = $ (this).closest (".Question").attr (
						'id');
				answerInfo['questionId'] = parentQuestionNum;
				answerDataForDB[answerNumber] = answerInfo;
			});

	// add new mission
	$
			.ajax (
			{
				url : './Php/addNewMission.php',
				data :
				{
					mission : fieldInfoForDatabase,
					objectives : JSON.stringify (mission['objectives']),
					questions : JSON.stringify (questionDataForDB),
					answers : JSON.stringify (answerDataForDB)
				},
				type : "POST",
				// TODO: minimize?
				success : function (result)
				{
					processAddMissionResult (result);
				},
				error : function (jqXHR)
				{
					if (!handleLoginError (jqXHR))
					{
						alert ("There has been an error adding this mission to the database.");
					}
				},
				async : false
			});

}

function processAddMissionResult (result)
{
	var idInfo = JSON.parse (result);
	// alert ("mission successfully added, new id is " + idInfo["missionId"]);

	var missionId = idInfo['missionId'];
	$ ('body').data ('editModeInfo').missionId = missionId;
	$ ('body').data ('editModeInfo').idType = 'current';
	$ ('body').data ('editModeInfo').editMode = 'edit';

	var objectiveIds = idInfo['objectiveIds'];
	var objectives = $ ('body').data ('mission')['objectives'];
	for ( var objectiveNum in objectiveIds)
	{
		// alert ("objective number from database " + objectiveNum);
		// alert ("id sent for this number " + objectiveIds[objectiveNum]);
		objectives[objectiveNum]['objectiveId'] = objectiveIds[objectiveNum];
	}

	var questionIds = idInfo['questionIds'];
	var questions = $ ('body').data ('mission')['questions'];
	for ( var questionNum in questionIds)
	{
		questions[questionNum]['questionId'] = questionIds[questionNum];
	}

	var answerIds = idInfo['answerIds'];
	var answers = $ ('body').data ('mission')['answers'];
	// console.log ("\n\nanswers before " + JSON.stringify (answers));
	for ( var answerNum in answerIds)
	{
		answers[answerNum]['answerId'] = answerIds[answerNum];
	}
	// console.log ("\nanswers after " + JSON.stringify (answers) + "\n\n");

	// alert ("all questions with new ids: \n"
	// + JSON.stringify ($ ('body').data ('mission')['questions']));

	// alert(" processed, " + JSON.stringify($ ('body').data
	// ('mission')['objectives']));
}

// in this function, will construct array of state-value pairs to be changed for
// existing mission
// pass in any state-related variables and their new values, since otherwise
// will
// only look at user-changable variables from form
function updateMissionInDatabase (missionInfo)
{
	var mission = $ ('body').data ('mission');

	/** *Mission** */
	// get mission id php file will need
	var missionId = $ ('body').data ('editModeInfo').missionId;
	// add any column value changes to object to be sent in,
	// ensure reflected in Mission object
	$ (".MissionDataField").each (function ()
	{
		var attributeName = $ (this).attr ('name');
		var valueInForm = $ (this).val ();
		var oldValue = mission[attributeName];
		if (oldValue != valueInForm)
		{
			missionInfo[attributeName] = valueInForm;
			mission[attributeName] = valueInForm;
		}
	});

	/** *Objectives** */
	// go through objectives, determining if any need to be added, deleted or
	// modified
	var missionObjectives = mission['objectives'];

	// before start adding or deleting objectives, get mapping of number to id
	// that php file will need
	var objectiveIds =
	{};
	for ( var objectiveNumber in missionObjectives)
	{
		var objectiveId = missionObjectives[objectiveNumber]['objectiveId'];
		objectiveIds[objectiveNumber] = objectiveId;
	}

	var objectivesToAdd =
	{};
	var objectivesToUpdate =
	{};
	var objectivesToDelete = [];

	// objective numbers of objectives already in database
	var unprocessedObjectives = Object.keys (missionObjectives);

	// go through what's on page, see what to add or update
	$ (".Objective").each (function ()
	{
		var objectiveNumber = $ (this).attr ('id');
		// alert("processing objective with number " + objectiveNumber);
		var index = unprocessedObjectives.indexOf (objectiveNumber);
		if (index == -1)
		// new objective needs to ba added to database
		{
			// get info for objective
			var objectiveInfo =
			{};
			$ (".ObjectiveDataField", this).each (function ()
			{
				var attributeName = $ (this).attr ('name');
				objectiveInfo[attributeName] = $ (this).val ();
			});
			// put in mission object
			missionObjectives[objectiveNumber] = objectiveInfo;
			// put in list to go to database
			objectivesToAdd[objectiveNumber] = objectiveInfo;
		}
		else
		// existing mission - see if needs to be modified
		{
			var objective = missionObjectives[objectiveNumber];
			var changesToObjective =
			{};
			$ (".ObjectiveDataField", this).each (function ()
			{
				var attributeName = $ (this).attr ('name');
				var valueInForm = $ (this).val ();
				var oldValue = objective[attributeName];
				if (oldValue != valueInForm)
				{
					// alert("for attribute " + attributeName + " old value of "
					// + oldValue + " != new value of " + valueInForm);
					objective[attributeName] = valueInForm;
					changesToObjective[attributeName] = valueInForm;
				}
			});

			if (Object.keys (changesToObjective).length > 0)
			{
				var objectiveId = objective["objectiveId"];
				// alert(" objective " + JSON.stringify(objective) + " has
				// changes " + JSON.stringify(changesToObjective));
				objectivesToUpdate[objectiveId] = changesToObjective;
				// alert(" objectiveId " + objectiveId);
				// alert(" objectivesToUpdate " +
				// JSON.stringify(objectivesToUpdate));
			}

			// remove from list of unprocessed missions
			unprocessedObjectives.splice (index, 1);
		}
	});

	// get the ones to delete
	for ( var index in unprocessedObjectives)
	{
		var objectiveNumber = unprocessedObjectives[index];

		// get id of objective to remove
		var objectiveIdToRemove = missionObjectives[objectiveNumber]["objectiveId"];

		// add to list of id's to remove
		objectivesToDelete.push (objectiveIdToRemove);

		// remove from mission object
		delete missionObjectives[objectiveNumber];
	}

	/** *Questions** */
	// go through questions, determining if any need to be added, deleted or
	// modified
	var missionQuestions = mission['questions'];

	// before start adding or deleting objectives or questions, get mapping of
	// number to id
	// that php file will need
	var questionIds =
	{};
	for ( var questionNumber in missionQuestions)
	{
		var questionId = missionQuestions[questionNumber]['questionId'];
		questionIds[questionNumber] = questionId;
	}

	// { [quest#]=>{objectiveId="-" questionTypeId="-" questionText="-"
	// order="-"}, ... }
	var questionsToAdd =
	{};
	// { [questId]=>{questionText and/or order}, ... }
	var questionsToUpdate =
	{};
	// [ questId, questId, ... ]
	var questionsToDelete = [];

	// question numbers of questions already in database
	var unprocessedQuestions = Object.keys (missionQuestions);

	// go through what's on page, see what to add or update
	$ (".Question").each (function ()
	{
		var questionNumber = $ (this).attr ('id');
		var index = unprocessedQuestions.indexOf (questionNumber);
		if (index == -1)
		{
			var questionInfo =
			{};
			questionInfo['questionTypeId'] = $ (this).data ('typeid');
			$ (".QuestionDataField", this).each (function ()
			{
				var attributeName = $ (this).attr ('name');
				questionInfo[attributeName] = $ (this).val ();
			});

			// put in mission object
			missionQuestions[questionNumber] = questionInfo;

			// add in objective number, to be used in php to
			// obtain objective id, put in list to go to database
			var objectiveNumber = $ (this).closest (".Objective").attr ('id');
			questionInfo['objectiveId'] = objectiveNumber;
			questionsToAdd[questionNumber] = questionInfo;
		}
		else
		{
			var question = missionQuestions[questionNumber];
			var changesToQuestion =
			{};
			$ (".QuestionDataField", this).each (function ()
			{
				var attributeName = $ (this).attr ('name');
				var valueInForm = $ (this).val ();
				var oldValue = question[attributeName];
				if (oldValue != valueInForm)
				{
					question[attributeName] = valueInForm;
					changesToQuestion[attributeName] = valueInForm;
				}
			});

			if (Object.keys (changesToQuestion).length > 0)
			{
				// this question is already in database, so questionId
				// was associated with it when came from database or
				// after added previously
				var questionId = question["questionId"];
				questionsToUpdate[questionId] = changesToQuestion;
			}

			// remove from list of unprocessed missions
			unprocessedQuestions.splice (index, 1);
		}
	});

	// get the ones to delete
	for ( var index in unprocessedQuestions)
	{
		var questionNumber = unprocessedQuestions[index];

		// get id of question to remove, will have been sent with when
		// retrieved from database, or updated to after add
		var questionIdToRemove = missionQuestions[questionNumber]["questionId"];

		// add to list of id's to remove
		questionsToDelete.push (questionIdToRemove);

		// remove from mission object
		delete missionQuestions[questionNumber];
	}

	/** *Answers* */
	// go through answers, determining if any need to be added, deleted or
	// modified
	var missionAnswers = mission['answers'];

	// { [answer#]=>{questionId="(questionNumber)" placementMarker="-"
	// answerText="-" isCorrect="-" points="-" order="-"}, ... }
	var answersToAdd =
	{};
	// { [answerId]=>{answerText, order, isCorrect ans/or points}, ... }
	var answersToUpdate =
	{};
	// [ answerId, answerId, ... ]
	var answersToDelete = [];

	// answer numbers of answerss already in database
	var unprocessedAnswers = Object.keys (missionAnswers);

	// go through what's on page, see what to add or update
	$ (".Answer").each (
			function ()
			{
				var answerNumber = $ (this).attr ('id');
				var index = unprocessedAnswers.indexOf (answerNumber);
				if (index == -1)
				{
					// add new answer
					var answerInfo =
					{};
					answerInfo['placementMarker'] = $ (this).closest (
							".AnswersList").attr ('data-placement-marker');

					$ (".AnswerDataField", this).each (function ()
					{
						var attributeName = $ (this).attr ('name');
						answerInfo[attributeName] = $ (this).val ();
					});

					// put in mission object
					missionAnswers[answerNumber] = answerInfo;

					// add in question number, to be used in php to
					// obtain question id, put in list to go to database
					var questionNumber = $ (this).closest (".Question").attr (
							'id');
					answerInfo['questionId'] = questionNumber;
					answersToAdd[answerNumber] = answerInfo;
				}
				else
				{
					// update existing answer
					var answer = missionAnswers[answerNumber];
					var changesToAnswer =
					{};
					$ (".AnswerDataField", this).each (function ()
					{
						var attributeName = $ (this).attr ('name');
						var valueInForm = $ (this).val ();
						var oldValue = answer[attributeName];
						if (oldValue != valueInForm)
						{
							answer[attributeName] = valueInForm;
							changesToAnswer[attributeName] = valueInForm;
						}
					});

					if (Object.keys (changesToAnswer).length > 0)
					{
						var answerId = answer["answerId"];
						answersToUpdate[answerId] = changesToAnswer;
					}

					// remove from list of unprocessed answers
					unprocessedAnswers.splice (index, 1);
				}
			});

	// get the ones to delete
	for ( var index in unprocessedAnswers)
	{
		var answerNumber = unprocessedAnswers[index];

		var answerIdToRemove = missionAnswers[answerNumber]["answerId"];

		// add to list of id's to remove
		answersToDelete.push (answerIdToRemove);

		// remove from mission object
		delete missionAnswers[answerNumber];
	}

	var changesExist = (Object.keys (missionInfo).length > 0)
			|| (Object.keys (objectivesToAdd).length > 0)
			|| (Object.keys (objectivesToUpdate).length > 0)
			|| (objectivesToDelete.length > 0)
			|| (Object.keys (questionsToAdd).length > 0)
			|| (Object.keys (questionsToUpdate).length > 0)
			|| (questionsToDelete.length > 0)
			|| (Object.keys (answersToAdd).length > 0)
			|| (Object.keys (answersToUpdate).length > 0)
			|| (answersToDelete.length > 0);

	if (changesExist)
	{
		missionInfo = JSON.stringify (missionInfo);

		var objectivesInfo =
		{
			"objectivesToAdd" : objectivesToAdd,
			"objectivesToUpdate" : objectivesToUpdate,
			"objectivesToDelete" : objectivesToDelete
		};
		objectivesInfo = JSON.stringify (objectivesInfo);

		var questionsInfo =
		{
			"questionsToAdd" : questionsToAdd,
			"questionsToUpdate" : questionsToUpdate,
			"questionsToDelete" : questionsToDelete
		};
		questionsInfo = JSON.stringify (questionsInfo);

		var answersInfo =
		{
			"answersToAdd" : answersToAdd,
			"answersToUpdate" : answersToUpdate,
			"answersToDelete" : answersToDelete
		};
		answersInfo = JSON.stringify (answersInfo);

		// using missionId in Info and json encoded version of array created as
		// data, call updatemissions php file
		$
				.ajax (
				{
					url : './Php/updateMission.php',
					data :
					{
						// ids
						missionId : missionId,
						objectiveIds : JSON.stringify (objectiveIds),
						questionIds : JSON.stringify (questionIds),
						// info
						mission : missionInfo,
						objectivesInfo : objectivesInfo,
						questionsInfo : questionsInfo,
						answersInfo : answersInfo
					},
					type : "POST",
					success : function (result)
					{
						// TODO: factor out common functionality between this &
						// processAddMissionResult
						// attach objective id's to appropriate objectives
						result = JSON.parse (result);

						var newObjectiveIds = result['newObjectiveIds'];
						for ( var objectiveNumber in newObjectiveIds)
						{
							var objective = mission.objectives[objectiveNumber];
							objective['objectiveId'] = newObjectiveIds[objectiveNumber];
						}

						var newQuestionIds = result['newQuestionIds'];
						for ( var questionNumber in newQuestionIds)
						{
							var question = mission.questions[questionNumber];
							question['questionId'] = newQuestionIds[questionNumber];
						}

						var newAnswerIds = result['newAnswerIds'];
						for ( var answerNumber in newAnswerIds)
						{
							var answer = mission.answers[answerNumber];
							answer['answerId'] = newAnswerIds[answerNumber];
						}
					},
					error : function (jqXHR)
					{
						if (!handleLoginError (jqXHR))
						{
							alert ("There has been an error updating this mission in the database.");
						}
					}
				});
	}
}

// saves mission as is currently to somewhere
// use globla mission object to do this -
// then must ensure all status fields are updated
// may also require additional state info
// if this is on a timer, what happens if it decides to autosave while is doing
// actual save? If uses just mission object, fields may not be in consistant
// state while in midst of doing something
function autosave ()
{
	$ ('#NotificationArea').html ("Autosaving (not really)...");
	$ ('#NotificationArea').show ();
	// TODO: save data to temp location on server
	$ ('#NotificationArea').append ("Done");
	$ ('#NotificationArea').fadeOut (5000);
	resetAutosave ();
}

// get rid of autosave data & return to Mission Control
function discardAutosaveData ()
{
	// TODO: get rid of autosave data here
}

// breaks cycle of autosaving data, so does not try to autosave at bad time
function suspendAutosave ()
{
	clearTimeout ($ ('body').data ('autosaveTimer'));
}

function resetAutosave ()
{
	$ ('body').data ('autosaveTimer', setTimeout ('autosave()', 60000));
}

// gets rid of mission with given id
function removeMissionFromDatabase (missionIdToRemove)
{
	// call discardMission, which will remove specified mission from DB
	$
			.ajax (
			{
				url : './Php/discardMission.php',
				data :
				{
					missionId : missionIdToRemove
				},
				error : function (jqXHR)
				{
					if (!handleLoginError (jqXHR))
					{
						alert ("There has been an error deleting this mission from the database.");
					}
				},
				async : false
			});
}

// function hasUnsavedChanges ()
// {
// /* Check for changes to Mission */
// var hasChanges = false;
//
// var mission = $ ('body').data ('mission');
// $ (".MissionDataField").each (function ()
// {
// if (!hasChanges)
// {
// var attributeName = $ (this).attr ('name');
// var valueInForm = $ (this).val ();
// var oldValue = mission[attributeName];
// hasChanges = oldValue != valueInForm;
// }
// });
//
// if (hasChanges)
// return true;
//
// /* Check for changes to Obejctives */
// var missionObjectives = mission.objectives;
// // to keep track of which missions were processed
// var unprocessedObjectives = Object.keys (missionObjectives);
//
// $ (".Objective").each (function ()
// {
// if (!hasChanges)
// {
// var objectiveNumber = $ (this).attr ('id');
// var index = unprocessedObjectives.indexOf (objectiveNumber);
// if (index == -1)
// {
// // an objective was added that is not in database
// hasChanges = true;
// }
// else
// {
// // the objective exists, see if it was modified
// var objective = missionObjectives[objectiveNumber];
// $ (".ObjectiveDataField", this).each (function ()
// {
// if (!hasChanges)
// {
// var attributeName = $ (this).attr ('name');
// var valueInForm = $ (this).val ();
// var oldValue = objective[attributeName];
// hasChanges = oldValue != valueInForm;
// }
// });
//
// // remove from list of unprocessed missions
// unprocessedObjectives.splice (index, 1);
// }
// }
// });
//
// if (hasChanges || unprocessedObjectives.length > 0)
// {
// // objectives were deleted
// return (true);
// }
//
// /* Check for changes to Questions */
// var missionQuestions = mission['questions'];
// // question numbers of questions already in database
// var unprocessedQuestions = Object.keys (missionQuestions);
//
// // go through what's on page, see what to add or update
// $ (".Question").each (function ()
// {
// if (!hasChanges)
// {
// var questionNumber = $ (this).attr ('id');
// var index = unprocessedQuestions.indexOf (questionNumber);
// if (index == -1)
// {
// // found a question not in database
// hasChanges = true;
// }
// else
// {
// var question = missionQuestions[questionNumber];
// $ (".QuestionDataField", this).each (function ()
// {
// if (!hasChanges)
// {
// var attributeName = $ (this).attr ('name');
// var valueInForm = $ (this).val ();
// var oldValue = question[attributeName];
// hasChanges = oldValue != valueInForm;
// }
// });
// // remove from list of unprocessed missions
// unprocessedQuestions.splice (index, 1);
// }
// }
// });
//
// if (hasChanges || unprocessedQuestions.length > 0)
// {
// // questions were deleted
// return (true);
// }
//
// /* Check for changes to Answers */
// var missionAnswers = mission['answers'];
// // answer numbers of answers already in database
// var unprocessedAnswers = Object.keys (missionAnswers);
//
// // go through what's on page, see what to add or update
// $ (".Answer").each (function ()
// {
// if (!hasChanges)
// {
// var answerNumber = $ (this).attr ('id');
// var index = unprocessedAnswers.indexOf (answerNumber);
// if (index == -1)
// {
// // found a answer not in database
// hasChanges = true;
// }
// else
// {
// var answer = missionAnswers[answerNumber];
// $ (".AnswerDataField", this).each (function ()
// {
// if (!hasChanges)
// {
// var attributeName = $ (this).attr ('name');
// var valueInForm = $ (this).val ();
// var oldValue = answer[attributeName];
// hasChanges = oldValue != valueInForm;
// }
// });
// // remove from list of unprocessed answers
// unprocessedAnswers.splice (index, 1);
// }
// }
// });
//
// if (unprocessedAnswers.length > 0)
// {
// // some answers were deleted
// return (true);
// }
//
// return (hasChanges);
// }

function hasUnsavedChanges ()
{
	// Check for changes to Mission
	var hasChanges = checkForMissionChanges ();

	if (!hasChanges)
		// no changes to mission, check for changes to Objectives
		hasChanges = checkForChanges ('objective');

	if (!hasChanges)
		// no changes found yet, check questions
		hasChanges = checkForChanges ('question');

	if (!hasChanges)
		// no changes found yet, check answers
		hasChanges = checkForChanges ('answer');

	return (hasChanges);
}

/* Check for changes to Mission */
function checkForMissionChanges ()
{
	var hasChanges = false;

	var mission = $ ('body').data ('mission');
	$ (".MissionDataField").each (function ()
	{
		if (!hasChanges)
		{
			var attributeName = $ (this).attr ('name');
			var valueInForm = $ (this).val ();
			var oldValue = mission[attributeName];
			hasChanges = oldValue != valueInForm;
		}
	});

	return (hasChanges);
}

/* check for changes to Objectives, Questions, or Answers */
function checkForChanges (itemType)
{
	var itemSelectorName = itemType.charAt (0).toUpperCase ()
			+ itemType.substring (1);

	var mission = $ ('body').data ('mission');
	var missionItems = mission[itemType + "s"];
	var unprocessedItems = Object.keys (missionItems);

	// go through what's on page, see what to add or update
	var hasChanges = false;
	$ ("." + itemSelectorName).each (
			function ()
			{
				if (!hasChanges)
				{
					var itemNumber = $ (this).attr ('id');
					var index = unprocessedItems.indexOf (itemNumber);
					if (index == -1)
					{
						// found a question not in database
						hasChanges = true;
					}
					else
					{
						var item = missionItems[itemNumber];
						$ ("." + itemSelectorName + "DataField", this).each (
								function ()
								{
									if (!hasChanges)
									{
										var attributeName = $ (this).attr (
												'name');
										var valueInForm = $ (this).val ();
										var oldValue = item[attributeName];
										hasChanges = oldValue != valueInForm;
									}
								});
						// remove from list of unprocessed missions
						unprocessedItems.splice (index, 1);
					}
				}
			});

	return (hasChanges || unprocessedItems.length > 0);
}

// **********************Button Functions**********************\\
// This is the function called when the 'Save' button
// is clicked. 'Save' will only be visible if user is editing
// mission already in database as draft.
// State at this point:
// --editMode - edit
// --missionId - id of mission in db that changes should be made to
// --idType - current (indicates the above)
// State after executes:
// same
function saveMission ()
{
	suspendAutosave ();
	// don't need to change anything state-related --
	// already either a dependent or independent draft
	updateMissionInDatabase (
	{});

	// don't need autosaved data, since have latest in database
	discardAutosaveData ();

	resetAutosave ();
}

// This is the function that is called when 'Save as Draft' button
// is clicked. Will be visible if user is creating new mission, extending
// existing mission, or editing a completed mission.
// State at this point:
// --editMode - new or edit
// --missionId - null or id of base mission
// --idType - base
// State after executes:
// --editMode - edit
// --missionId - id of mission in db that changes should be made to
// --idType - current
function saveMissionAsDraft ()
{
	suspendAutosave ();

	// set global mission's missionstatusid appropriately :
	// if editMode = edit, then will =missionId (dependent draft linked to one
	// at missionId)
	// if editMode = new, then will =1 (independent draft)
	var missionEditingInfo = $ ('body').data ('editModeInfo');
	var statusId = (missionEditingInfo.editMode == 'edit') ? missionEditingInfo.missionId
			: 1;
	$ ('body').data ('mission').missionStatusId = statusId;

	addMissionToDatabase ();

	$ ("#SavedToDraftsAlert").dialog ('open');

	// don't need autosaved data, since have latest in database
	discardAutosaveData ();

	// page will be in different mode now,
	// ensure appropriate header & buttons display
	attachHeader ();
	showButtons ();

	resetAutosave ();
}

// this gets called when button pressed
// if will need public/private status info,
// passes control to prompt box, else,
// goes directly to real function
// necessitated by popup dialog control structure
function handleSaveAsCompleteButtonPress ()
{
	if ($ ("#missionForm").data ('bValidator').validate ())
	{
		var missionEditingInfo = $ ('body').data ('editModeInfo');
		var mission = $ ('body').data ('mission');

		if (missionEditingInfo.editMode == "new"
				|| (missionEditingInfo.idType == "current" && mission.missionStatusId == 1))
		{
			$ ("#PublicPrivatePrompt").dialog ('open');
		}
		else if (missionEditingInfo.idType == "current"
				&& mission.missionStatusId > 1)
		{
			$ ("#CommitChangesPrompt").dialog ('open');
		}
		else
		{
			saveMissionAsComplete ();
		}
	}
	else
	{
		$ ("#ValidateMissionPrompt").dialog ('open');
	}
}

// Gets called when definately want to save. have already gone through any
// sequence of popup warnings
// State at this point:
// --editMode - new or edit
// --missionId - null, id of base mission, or id of current mission
// --idType - base or current
// State after executes:
// irrelevent, since back on mission control
function saveMissionAsComplete ()
{
	suspendAutosave ();

	var missionEditingInfo = $ ('body').data ('editModeInfo');
	var mission = $ ('body').data ('mission');
	var isPublic = $ ('body').data ('isPublic');

	// TODO: validate fields here

	var notificationString = "";

	if (missionEditingInfo.editMode == "new")
	{
		// change values in global mission variable, since this will be
		// used to get properties for adding new mission

		// indicate private/public status chosen by user
		mission.isPublic = isPublic;

		// indicate mission is complete
		mission.missionStatusId = 0;

		addMissionToDatabase ();

		notificationString = "Mission \"" + mission.missionName
				+ "\" has been added to your myMissions folder";
	}
	else
	// editing existing mission, not creating new
	{
		if (missionEditingInfo.idType == "current")
		// 'normal' editing mode - editing a draft mission
		{
			// start status name - value pairs array, to be passed in to
			// updateMission when called (since updating mission only
			// goes through fields in form and sends those that differ
			// from what's in global mission -- if wnat state-related
			// values changes, have to send in to function)
			var statusInfoArray =
			{};

			if (mission.missionStatusId == 1)
			// independent draft
			{
				// add to status array --
				// missionStatusId = 0 (complete), isPublic = userResponse
				statusInfoArray['missionStatusId'] = 0;
				statusInfoArray['isPublic'] = isPublic;

				notificationString = "Mission \"" + mission.missionName
						+ "\" has been moved from your drafts "
						+ "folder to your myMissions folder";
			}
			else
			// dependent draft - saving as complete will replace existing
			// mission
			// id of actual draft - info.missionId
			// id of one will replace - mission.missionStatusId
			{
				var originalMissionId = mission.missionStatusId;
				// remove mission with id = mission.missionStatusId
				// $ ('body').data ('editModeInfo').missionId
				removeMissionFromDatabase (originalMissionId);

				// add to status array missionStatusId=0 (complete) missionId =
				// mission.missionStatusId
				statusInfoArray['missionStatusId'] = 0;
				statusInfoArray['missionId'] = originalMissionId;

				// when update mission is called (below) will
				// push any changes since last save, plus the status changes
				// will update the mission with id in info, which is draft
				// ( which is good since mission with id=mission.missionStatusId
				// doesn't exist anymore) when this update is done, will have
				// changed
				// mission id of draft mission, so will be
				// mission.missionStatusId

				notificationString = "Mission \"" + mission.missionName
						+ "\" has been updated with your changes. ";
			}

			// push any changes since last save, plus the status change
			updateMissionInDatabase (statusInfoArray);
		}
		else
		// idType = base, editing but no draft exists - make changes to
		// already complete mission in db
		{
			// all fields like missionStatusId and isPublic will already be set
			// correctly

			// missionId in info will be used by this function
			updateMissionInDatabase (
			{});

			notificationString = "Mission " + mission.missionName
					+ " has been updated with your changes. ";
		}
	}

	// don't need autosave data anymore, since have latest in db
	discardAutosaveData ();

	$ ("#SavedAsCompleteAlert").html (notificationString);
	$ ("#SavedAsCompleteAlert").dialog ('open');

}

// Cancels changes made since last save. Only available on editing page, since
// on a 'create new' page, would be same as discard.
// State at this point:
// --editMode - edit
// --missionId - id of draft editing, or of base completed if not saved yet
// --idType - current or base, matching above
// State after executes:
// doesn't matter, since will be off page
function cancelChanges ()
{
	suspendAutosave ();

	if (hasUnsavedChanges ())
	{
		$ ("#CancelChangesPrompt").dialog ('open');
	}
	else
	{
		// return to previous page
		window.history.back ();
	}
}

function showDiscardPrompt ()
{
	var missionEditingInfo = $ ('body').data ('editModeInfo');
	var mission = $ ('body').data ('mission');

	var alertString = "";
	// add extra disclamer for removing a completed, public mission
	if (missionEditingInfo.editMode == "edit")
	{
		if (mission.missionStatusId == 0 && mission.isPublic == 1)
		{
			alertString = "This mission is public and may be in use by other members.";
		}
		alertString += " Are you sure you want to completely remove this mission?";
	}
	else
	{
		alertString = "Are you sure you want to discard this new mission?";
	}
	$ ("#DiscardPrompt").html (alertString);

	$ ("#DiscardPrompt").dialog ('open');
}

// Discards the mission currently working on. If is editing draft, will be draft
// mission.
// if opened completed mission for editing, will be the completed mission
// if created new mission or extended mission, in no mission in db to discard,
// will behave same as cancel above
function discardMission ()
{
	suspendAutosave ();

	// get rid of any temporary saved files
	// if in new mode, this is enough
	discardAutosaveData ();

	if ($ ('body').data ('editModeInfo').editMode == "edit")
	// there is a mission in db that we should remove
	// it's id will be missionId in info, since if have opened
	// completedforediting, will want to remove completed, or if editing draft,
	// will be draft want to get rid of
	{
		removeMissionFromDatabase ($ ('body').data ('editModeInfo').missionId);
	}

	// return to mission control (even if we got here by viewing)
	location.href = "./missionControl.html";
}
