// TODO: Encapsulate these
// Global variables
var MissionControl =
{
	TAB_AREA_SELECTOR : "#tabs",
	EXPORT_UPDATE_TIMEOUT : 1000,
	DASHBOARD_TAB_INDEX : 0,
	FAVORITES_TAB_INDEX : 1,
	MY_MISSIONS_TAB_INDEX : 2,
	SEARCH_TAB_INDEX : 3
};

// code to be executed the instant the js is loaded, perhaps before the document
// is ready.
if (!localStorage)
{
	alert ("Your browser does not support local storage. This page will not function properly.");
}

if (!localStorage["validLogin"])
{
	alert ("Your login is invalid or has expired. Please try again.");
	handleAuthError ();
}

// document ready for missionControl.html
$ (document).ready (
		function ()
		{

			attachWelcomeText ();

			$ ("#LogOut").click (logOut);

			activateTabs ();

			$ ('.createNewMission').click (function ()
			{
				extendMission (null);
			});

			$ ('#searchMissions').click (
					function ()
					{
						goToTab (MissionControl.TAB_AREA_SELECTOR,
								MissionControl.SEARCH_TAB_INDEX);
					});

			displayAccountStatus ();

			$ (window).unload (saveDataBeforeLeave);

			initPagination ();

			initPopups ();

			// set up forms - do this once
			bindSearchForm ();
			bindPasswordChangeForm ();

			// prepare mission lists on tab that will be displayed
			refreshSelectedTab ();
		});

function activateTabs ()
{
	var tabToSelect = localStorage["ReturnToTab"];
	tabToSelect = (tabToSelect) ? parseInt (tabToSelect) : 0;

	$ (MissionControl.TAB_AREA_SELECTOR).tabs (
	{
		selected : tabToSelect
	});

	$ (MissionControl.TAB_AREA_SELECTOR).bind ('tabsselect',
			function (event, ui)
			{
				// calls specific refresh content routines
				// depending on which tab gets selected
				refreshTabContent (ui.index);
			});
}

function displayAccountStatus ()
{
	$ ("#accountStatus").html (
			localStorage['userStatus'] == 0 ? "Instructor" : "Student");
}

function initPagination ()
{
	// set up pagers (don't 'initialize' them since we don't have
	// content yet)
	$ (".PaginatedContent").each (function ()
	{
		// parent Id uniquely identifies table
		var id = $ (this).attr ("id");
		var pageToBeOn = localStorage[id + "PageNum"];
		if (!pageToBeOn || isNaN (pageToBeOn))
		{
			pageToBeOn = 0;
		}
		$ (this).customPager (
		{
			currentPage : pageToBeOn,
			loadOnCreate : false
		});
	});
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

	$ ("#AddFavoritePrompt").dialog ("option", "buttons",
	{
		"Add" : function ()
		{
			$ (this).dialog ("close");
			var freezeCopy = $ ('input:checkbox', this).attr ('checked');
			var action = (freezeCopy) ? 'freezeCopy' : 'add';
			specificChangeFavorites (action);
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");
		}
	});

	$ ("#AddOwnFavoritePrompt").dialog ("option", "buttons",
	{
		"Add" : function ()
		{
			$ (this).dialog ("close");
			specificChangeFavorites ('add');
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");
		}
	});

	$ ("#RemoveFavoritePrompt").dialog ("option", "buttons",
	{
		"Remove" : function ()
		{
			$ (this).dialog ("close");
			specificChangeFavorites ('remove');
		},
		"Cancel" : function ()
		{
			$ (this).dialog ("close");
		}
	});

	$ ("#DiscardPrompt").dialog ("option", "buttons",
	{
		"Discard" : function ()
		{
			deleteSpecificMission ();
			$ (this).dialog ("close");
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

// If necessary use jQuery.getScript to dynamically load JS code

function handleAuthError ()
{
	// The server session has expired, so redirect to the login page.
	localStorage.clear ();
	window.location = "./missionSystemOverview.html";
}

function attachWelcomeText ()
{
	var firstName = localStorage["firstName"];
	var userName = localStorage["userName"];
	$ ("#WelcomeText").html ("Welcome, " + firstName + " (" + userName + ")!");
}

function logOut ()
{
	$.post ("./Php/logout.php", function ()
	{
		localStorage.clear ();
		alert ("Goodbye!");
		location.href = "./missionSystemOverview.html";
	});

}

function loadDashboard ()
{
	// use ajax to get top 10 most recently used favorites and put them on page
	loadTopFavorites ();

	// use ajax to get top 10 recently used of my missions
	loadTopMissions ();

	// use ajax to get up to 10 draft missions
	loadTopDraftMissions ();

}

function changeFavorites ()
{
	$.ajax (
	{
		url : './Php/favoritesControl.php',
		type : "POST",
		data :
		{
			missionId : arguments[0],
			action : arguments[1]
		},
		success : function (data)
		{
			// Refresh top favorites list
			refreshSelectedTab ();
		},
		error : function (jqXHR, textStatus, errorThrown)
		{
			if (jqXHR.status == '401')
			{
				handleAuthError ();
			}

			var responseObject = JSON.parse (jqXHR.responseText);
			$ ("#serverError")
					.html (
							'<span class="error">' + responseObject.message
									+ '</span>');
		}
	});
}

function exportAndUpdate (missionId)
{
	// export the mission
	exportAsXml (missionId);

	// only thing that could change with exporting is arrangement of top
	// favorite missions, so if we're on the dashboard tab, refresh just that
	// table
	var selectedTab = $ (MissionControl.TAB_AREA_SELECTOR).tabs ("option",
			"selected");
	if (selectedTab == MissionControl.DASHBOARD_TAB_INDEX)
	{
		// let the database get updated before refreshing
		setTimeout (loadTopFavorites, MissionControl.EXPORT_UPDATE_TIMEOUT);
	}
}

// function to do operations from the "options" dropdown menu
function handleMissionOptions ()
{
	// Find the first parent in the DOM chain that has a custom HTML5
	// "data-missionid" attribute.
	var row = $ (this).parents ("[data-missionid]");
	var missionId = row.data ("missionid");
	// Get the "name" attribute of the selected option.
	var action = $ (this).children ("option:selected").attr ("name");

	// Reset select list to first option to preserve the illusion of it being a
	// menu.
	$ (this).val (jQuery ('options:first', $ (this)).val ());

	switch (action)
	{
	case 'view':
		viewMission (missionId);
		break;
	case 'export':
		exportAndUpdate (missionId);
		break;
	case 'addOwnFavorite':
		$ ("#AddOwnFavoritePrompt").dialog ('open');
		specificChangeFavorites = changeFavorites.bind (undefined, missionId);
		break;
	case 'addFavorite':
		$ ("#AddFavoritePrompt").dialog ('open');
		specificChangeFavorites = changeFavorites.bind (undefined, missionId);
		break;
	case 'removeFavorite':
		$ ("#RemoveFavoritePrompt").dialog ('open');
		specificChangeFavorites = changeFavorites.bind (undefined, missionId);
		break;
	case 'extend':
		extendMission (missionId);
		break;
	case 'editDraft':
		// Edit the mission as a draft
		editMission (missionId, true);
		break;
	case 'editPublic':
		// Edit a public mission
		editMission (missionId, false);
		break;
	case 'makePublic':
		showChangeMissionVisibilityPrompt ("#MakePublicPrompt", true,
				missionId, refreshSelectedTab);
		break;
	case 'makePrivate':
		showChangeMissionVisibilityPrompt ("#MakePrivatePrompt", false,
				missionId, refreshSelectedTab);
		break;
	case 'deleteMission':
		var wasCompleteAndPublic = $ (this).children (
				"option[name=makePrivate]").length;
		showDeleteMissionPrompt ("#DiscardPrompt", wasCompleteAndPublic,
				missionId, refreshSelectedTab);
		break;
	}
}

// ** Utility function to load missions ** //
/*
 * Arguments: -- target: the selector to hold the template target, header, and
 * loading icon -- constraints : constraints to pass to ajax call.
 */
function loadMissions (options)
{
	var loadContainerSelector = options['target'];
	var constraints = options['constraints'];
	var additionalData = options['templateData'];

	var templateTarget = ' .templateTarget';
	var template = $ ("#missionTemplate");
	var loadingIcon = $ (loadContainerSelector + ' .loadingIcon');

	var sortableTableSelector = loadContainerSelector + " .SortableTable table";

	var defaultSortConfig = (loadContainerSelector == "#favoritesTable") ? [ [
			2, 0 ] ] : [ [ 6, 1 ] ];
	var savedSortConfig = localStorage[loadContainerSelector + "SortConfig"];
	// default sort on first column using ascending
	savedSortConfig = savedSortConfig ? JSON.parse (savedSortConfig)
			: defaultSortConfig;

	$
			.ajax (
			{
				url : './Php/getMissions.php',
				type : 'POST',
				data : constraints,
				beforeSend : function ()
				{
					loadingIcon.show ();
				},
				success : function (data)
				{
					// Output data using the standard mission table template
					$ (loadContainerSelector + templateTarget).empty ();
					template.tmpl (data, additionalData).appendTo (
							loadContainerSelector + templateTarget);

					// Add dropdowns to option menus.
					var missionOptionsMenu = $ (loadContainerSelector
							+ " .missionOptionsMenu");
					missionOptionsMenu.change (handleMissionOptions);

					// allow first option in list to act like button
					$ ("option:first", missionOptionsMenu).click (function ()
					{
						$ (this).parent ().change ();
					});
				},
				complete : function ()
				{
					loadingIcon.hide ();

					// Sort newly appended rows; skip options column
					$ (sortableTableSelector).tablesorter (
					{
						// Sort on first column using ascending
						sortList : savedSortConfig,
						headers :
						{
							8 :
							{
								sorter : false
							}
						}
					}).bind (
							"sortEnd",
							function (sorter)
							{
								// store sort configuration so doesn't get
								// changed
								// every time reload, which means when switching
								// between tabs, or doing modifying operations
								// within
								// tab
								currentSort = sorter.target.config.sortList;
								localStorage[loadContainerSelector
										+ "SortConfig"] = JSON
										.stringify (currentSort);

								$ (loadContainerSelector).customPager (
										"refresh");

							});

					$ (loadContainerSelector).customPager ("paginateContent");

					var numMissions = $ (loadContainerSelector + " tbody > tr")
							.size ();
					$ (loadContainerSelector + " #TotalMissions").html (
							numMissions);
				},
				error : function (jqXHR, textStatus, errorThrown)
				{
					if (jqXHR.status == '401')
					{
						handleAuthError ();
					}

					var responseObject = JSON.parse (jqXHR.responseText);
					$ ("#serverError").html (
							'<span class="error">' + responseObject.message
									+ '</span>');
				}
			});
}

function bindSearchForm ()
{
	// Setup toggle boxes
	$ ('#searchForm #dateRanges input[type="checkbox"]').change (function ()
	{
		if ($ (this).attr ("checked"))
		{
			$ (this).parent ().removeClass ("disabled");
			$ (this).siblings ("input,select").removeAttr ('disabled');
		}
		else
		{
			$ (this).parent ().addClass ("disabled");
			$ (this).siblings ("input,select").attr ('disabled', 'disabled');
		}
	});

	// set up timepicker and data pickers
	$ ("#completionTime").timepicker (
	{
		showPeriodLabels : false
	});
	$ ("#createdLower").datepicker ();
	$ ("#createdUpper").datepicker ();
	$ ("#modifiedLower").datepicker ();
	$ ("#modifiedUpper").datepicker ();

	// Populate categories
	populateCategoryDropdown ('#category', "Select a category", false);

	// Populate topics based on the category
	linkTopicToCategory ('#category', '#topic', "Select a category first",
			"Select a topic", false);

	// link results per page adjuster
	$ ("#perPageSelector").change (function ()
	{
		var itemsPerPage = $ (this).val ();
		$ ("#searchResults").customPager ("setItemsPerPage", itemsPerPage);
		localStorage["SearchResultsPerPage"] = itemsPerPage;
	});

	// if have stored items per page from before, set that here
	var searchResultsPerPage = localStorage["SearchResultsPerPage"];
	if (searchResultsPerPage)
	{
		// save the current page set from storage when initiated, since changing
		// results per page resets to first page
		var currentPage = $ ("#searchResults").customPager ("getCurrentPage");
		$ ("#perPageSelector").val (searchResultsPerPage).change ();
		$ ("#searchResults").customPager ("setCurrentPage", currentPage);
	}

	// Setup AJAX form submit
	$ ('#searchForm').submit (function (event)
	{
		// Collect data from form
		var data =
		{};
		$.each ($ (this).serializeArray (), function (i, field)
		{
			if (field.value != '')
			{
				data[field.name] = field.value;
			}
		});

		// store search data (will be accessed from loadSearch)
		localStorage["SearchData"] = JSON.stringify (data);

		// this will get missions via php & load them in table
		loadSearch ();

		event.preventDefault ();
	});

	// create the "Clear Options" callback
	$ ('#searchForm #formReset').click (function ()
	{
		// do manual reset
		$ ('#searchForm')[0].reset ();

		// make sure the checkboxes fire change events so content is disabled
		$ ('#searchForm #dateRanges input[type="checkbox"]').change ();

		// Disable the topic dropdown
		$ ('#searchForm #topic').attr ("disabled", "disabled");

		// at this point, resetting the per-page selector with 'reset' hasn't
		// caused a change event to be fired, so no modification has been made
		// to itemsPerPage in the paginator. So can restore the per-page
		// selector to the correct value.
		var searchResultsPerPage = localStorage["SearchResultsPerPage"];
		if (searchResultsPerPage)
		{
			// don't fire a change event here, since paginator was never
			// modified from what is was originally
			$ ("#perPageSelector").val (localStorage["SearchResultsPerPage"]);
		}
	});

	// restore search terms if are returning to search page from another page
	reconstructStoredSearch ();
}

// fill in terms in search page so that it corresponds to the search results
// displayed
function reconstructStoredSearch ()
{
	// loadSearch method also uses "SearchData" so
	// date filled in using it automatically corresponds
	// to loaded search results
	var storedSearch = localStorage["SearchData"];
	if (storedSearch)
	{
		storedSearch = JSON.parse (storedSearch);
		$.each (storedSearch, function (name, value)
		{
			var element = $ ("#searchForm [name=" + name + "]");
			if (element.attr ('type') == 'checkbox')
			{
				// a checkbox, handle differently
				element.prop ("checked", true).change ();
			}
			else
			{
				// set value
				element.val (value).change ();
			}
		});
	}
}

// Load the user's first 10 favorite missions on the home tab
function loadTopFavorites ()
{
	loadMissions (
	{
		target : "#topFavoritesTable",
		constraints :
		{
			isFavorite : 1,
			sort : [
			{
				column : 'lastExportedOnDate',
				direction : 'DESC'
			},
			{
				column : 'favoritedOnDate',
				direction : 'DESC'
			} ],
			limit : 5
		},
		templateData :
		{
			haveLink : true,
			linkPath : "goToTab (MissionControl.TAB_AREA_SELECTOR, MissionControl.FAVORITES_TAB_INDEX)"
		}
	});
}

// Load the user's first 10 missions on the home tab.
function loadTopMissions ()
{
	loadMissions (
	{
		target : "#myMissionsTable",
		constraints :
		{
			lastModifiedByUserId : localStorage["userId"],
			isFrozenCopy :
			{
				operator : '=',
				value : 0
			},
			missionStatusId :
			{
				operator : '=',
				value : 0
			},
			sort : [
			{
				column : 'lastModifiedOnDate',
				direction : 'DESC'
			} ],
			limit : 5
		},
		templateData :
		{
			haveLink : true,
			linkPath : "goToTab (MissionControl.TAB_AREA_SELECTOR, MissionControl.MY_MISSIONS_TAB_INDEX)"
		}
	});
}

// Load the user's first 10 draft missions on the home tab
function loadTopDraftMissions ()
{
	loadMissions (
	{
		target : '#myDraftMissionsTable',
		constraints :
		{
			lastModifiedByUserId : localStorage["userId"],
			missionStatusId :
			{
				operator : '!=',
				value : 0
			},
			sort : [
			{
				column : 'lastModifiedOnDate',
				direction : 'DESC'
			} ],
			limit : 5
		},
		templateData :
		{
			haveLink : true,
			linkPath : "goToTab (MissionControl.TAB_AREA_SELECTOR, MissionControl.MY_MISSIONS_TAB_INDEX, '#MyDraftMissionsTable')"
		}
	});
}

function loadMyMissionsTab ()
{
	var userId = localStorage["userId"];

	// Load original missions
	loadMissions (
	{
		target : "#MyOriginalMissionsTable",
		constraints :
		{
			lastModifiedByUserId : userId,
			createdByUserId : userId,
			missionStatusId :
			{
				operator : '=',
				value : 0
			},
			isFrozenCopy :
			{
				operator : '=',
				value : 0
			},
			sort : [
			{
				column : 'lastModifiedOnDate',
				direction : 'DESC'
			} ],
			limit : 50
		}
	});

	// Load adapted missions
	loadMissions (
	{
		target : "#MyAdaptedMissionsTable",
		constraints :
		{
			lastModifiedByUserId : userId,
			createdByUserId :
			{
				operator : '!=',
				value : userId
			},
			missionStatusId :
			{
				operator : '=',
				value : 0
			},
			isFrozenCopy :
			{
				operator : '=',
				value : 0
			},
			sort : [
			{
				column : 'missionName',
				direction : 'ASC',
			} ],
			limit : 50
		}
	});

	// Load draft missions
	loadMissions (
	{
		target : "#MyDraftMissionsTable",
		constraints :
		{
			lastModifiedByUserId : userId,
			missionStatusId :
			{
				operator : '!=',
				value : 0
			},
			sort : [
			{
				column : 'missionName',
				direction : 'ASC',
			} ],
			limit : 50
		}
	});
}

// Load all favorite missions on the favorites tab
function loadFavorites ()
{
	loadMissions (
	{
		target : "#favoritesTable",
		constraints :
		{
			isFavorite : 1,
			sort : [
			{
				column : 'missionName',
				direction : 'ASC'
			} ]
		}
	});
}

// If a search was submitted, load results in table in search tab
function loadSearch ()
{
	var searchData = localStorage["SearchData"];
	if (searchData)
	{
		searchData = JSON.parse (searchData);

		// Add custom data not visible in the form
		var customData =
		{
			isFrozenCopy :
			{
				operator : '=',
				value : 0
			},
			missionStatusId :
			{
				operator : '=',
				value : 0
			},
			sort : [
			{
				column : 'missionName',
				direction : 'ASC'
			} ],
			limit : 5000
		};
		$.extend (searchData, customData);

		// Load missions from server into the results table
		loadMissions (
		{
			target : "#searchResults",
			constraints : searchData
		});
	}
}

function searchAuthor (authorName)
{
	// store author search
	localStorage["SearchData"] = JSON.stringify (
	{
		author : authorName
	});
	// clear search fields
	$ ('#formReset').click ();
	// fill in options for author search
	reconstructStoredSearch ();
	// load the search
	loadSearch ();
	// open the search tab
	goToTab (MissionControl.TAB_AREA_SELECTOR, MissionControl.SEARCH_TAB_INDEX);
}

function bindPasswordChangeForm ()
{
	var passwordChangeOptions =
	{
		url : "./Php/changePassword.php",
		type : "POST",
		resetForm : true,
		beforeSubmit : function ()
		{
			return $ ('#passwordChangeForm').validate ().form ();
		},
		success : function (responseText)
		{
			alert ('Password changed.');
		},
		error : function (xhr, status, error)
		{
			alert (xhr.responseText);
			var responseObject = JSON.parse (xhr.responseText);
			$ ("#serverError").html (
					'<p class="error">' + responseObject.message + '</p>');
		}
	};
	$ ('#passwordChangeForm').ajaxForm (passwordChangeOptions);

	// custom password rule for validation
	$.validator
			.addMethod (
					"strongPassword",
					function (password, element)
					{
						var optional = this.optional (element);
						var lowercase = (/[a-z]/).test (password);
						var uppercase = (/[A-Z]/).test (password);
						var digit = (/[0-9]/).test (password);
						var special = (/[^A-Za-z0-9]/).test (password);
						var space = (/\s/).test (password);

						return optional
								|| (lowercase && uppercase && digit && special && !space);
					},
					"Must contain at least one lowercase letter, one capital letter, one digit, and one special character\n");

	// Define the client-side password validation rules.
	$ ('#passwordChangeForm').validate (
	{
		rules :
		{
			oldPassword :
			{
				required : true,
				remote : "./Php/confirmOldPassword.php"
			},
			newPassword :
			{
				required : true,
				minlength : 6,
				strongPassword : true
			},
			newPasswordConfirm :
			{
				required : true,
				equalTo : "#newPassword"
			}
		},
		messages :
		{
			oldPassword : " Ensure old password is typed correctly.",
			newPassword :
			{
				required : " Please provide a password.",
				minlength : " Password must be at least {0} characters long."
			},
			newPasswordConfirm :
			{
				required : " Please re-type new password.",
				equalTo : " Passwords do not match.",
				minlength : " Password must be at least {0} characters long."
			}
		}
	});

	// End validation rules
}

// refresh the content of the currently selected tab
// call this when have done something from the individual
// mission's options list that should change the way the
// mission is displayed in the list, or potentially, its
// order in the list
// don't need to refresh everything at this point, since content
// of other tabs will be refreshed when go to them later, if ever do
function refreshSelectedTab ()
{
	var selectedTab = $ (MissionControl.TAB_AREA_SELECTOR).tabs ("option",
			"selected");
	refreshTabContent (selectedTab);
}

// refreshes the content of mission lists contained in the
// tab specified by tabIndex
function refreshTabContent (tabIndex)
{
	switch (tabIndex)
	{
	case MissionControl.DASHBOARD_TAB_INDEX:
		loadDashboard ();
		break;
	case MissionControl.FAVORITES_TAB_INDEX:
		loadFavorites ();
		break;
	case MissionControl.MY_MISSIONS_TAB_INDEX:
		loadMyMissionsTab ();
		break;
	case MissionControl.SEARCH_TAB_INDEX:
		loadSearch ();
		break;
	default:
		break;
	}
}

function goToTab (tabSelector, index)
{
	$ (tabSelector).tabs ('select', index);
	if (!arguments[2])
		arguments[2] = "html";
	$ ('html').animate (
	{
		scrollTop : $ (arguments[2]).offset ().top
	}, 0);
}

function viewMission (missionId)
{
	localStorage["ViewMissionId"] = missionId;
	location.href = "./viewMission.html";
}

function saveDataBeforeLeave ()
{
	// don't save if we're logging out or being forced off because of login
	// expired
	if (localStorage["validLogin"])
	{
		var selectedTabIndex = $ (MissionControl.TAB_AREA_SELECTOR).tabs (
				"option", "selected");
		localStorage["ReturnToTab"] = selectedTabIndex;

		$ (".PaginatedContent").each (function ()
		{
			var pageOn = $ (this).customPager ("getCurrentPage");
			var id = $ (this).attr ("id");
			localStorage[id + "PageNum"] = pageOn;
		});
	}
}
