// missionModificationUtility.js
//
// Contains functions for mission operations done both from 
// the missionControl page (via dropdown menus) and the 
// viewMission page. Provides a common location to avoid 
// duplicated code. 

/*
 * 			How To Use 
 * the edit and extend functions
 * 
 *  // create new mission
 *	extendMission (null);
 *
 *  // create mission based on other mission (extend)
 *  extendMission (117);
 *
 *  // edit completed mission (not a draft)
 *  editMission (111, false);
 *
 *  // edit mission from drafts
 *  editMission (111, true);
 * 
 */
function extendMission (missionId)
{
	var missionEditorInfo =
	{
		editMode : "new",
		missionId : missionId,
		idType : "base"
	};
	localStorage["MissionEditorInfo"] = JSON.stringify (missionEditorInfo);
	location.href = "./missionEditor.html";
}

function editMission (missionId, isDraft)
{
	var missionEditorInfo =
	{
		editMode : "edit",
		missionId : missionId,
		idType : (isDraft ? "current" : "base")
	};
	localStorage["MissionEditorInfo"] = JSON.stringify (missionEditorInfo);
	location.href = "./missionEditor.html";
}

function exportAsXml (missionId)
{
	location.href = "./Php/exportXml.php?missionId=" + missionId;
}

function showDeleteMissionPrompt (promptSelector, wasCompleteAndPublic,
		missionId, finalFunction)
{
	var alertString = "";
	// add extra disclamer for removing a completed, public mission
	if (wasCompleteAndPublic)
	{
		alertString = "This mission is public and may be in use by other members. ";
	}
	alertString += "Are you sure you want to completely remove this mission?";

	$ (promptSelector).html (alertString);

	$ (promptSelector).dialog ('open');

	deleteSpecificMission = deleteMission.bind (undefined, missionId,
			finalFunction);
}

function deleteMission ()
{
	$.ajax (
	{
		url : './Php/discardMission.php',
		data :
		{
			missionId : arguments[0]
		},
		error : function (jqXHR)
		{
			if (!handleLoginError (jqXHR))
			{
				alert ("There has been an error deleting"
						+ " this mission from the database.");
			}
		},
		async : false,
		complete : arguments[1]
	});
}

function showChangeMissionVisibilityPrompt (promptSelector, isPublic,
		missionId, refreshFunction)
{
	$ (promptSelector).dialog ('open');

	changeMissionVisibility = changeVisibility.bind (undefined,
	{
		isPublic : isPublic,
		missionId : missionId,
		refreshFunction : refreshFunction
	});
}

function getPublicPopUpText ()
{
	return "Are you sure you want to make this private mission be public?";
}

function getPrivatePopUpText ()
{
	return "Are you sure you want to make this mission private? Other users will no longer be able to view, export or extend it.";
}

function changeVisibility (data)
{
	$.post ('./Php/changeVisibility.php',
	{
		isPublic : data['isPublic'],
		missionId : data['missionId']
	}, data['refreshFunction']);
}
