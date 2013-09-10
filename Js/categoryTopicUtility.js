function populateCategoryDropdown (categoryDropdownIdentifier,
		defaultOptionString, defaultHasValue)
{
	$.ajax (
	{
		url : './Php/getCategories.php',
		type : "POST", 
		dataType : 'json',
		success : function (data)
		{
			var optionHeader = (defaultHasValue) ? '<option value="0">' : '<option value="">';
			var categoryList = optionHeader + defaultOptionString
					+ '</option>';
			for (var i = 0; i < data.length; i++)
			{
				categoryList += '<option value="' + data[i].categoryId + '">'
						+ data[i].categoryName + '</option>';
			}

			$ (categoryDropdownIdentifier).html (categoryList);
		},
		error : function (jqXHR, textStatus, errorThrown)
		{
			if (jqXHR.status == '401')
			{
				handleAuthError ();
			}
		},
		async : false
	});
}

function linkTopicToCategory (categoryDropdownIdentifier,
		topicDropdownIdentifier, disabledTopicString, defaultTopicString, defaultHasValue)
{
	// Populate topics based on the cateorgy
	$ (categoryDropdownIdentifier)
			.change (
					function ()
					{
						var currentCategoryId = $ (this).val ();

						if (currentCategoryId == 0)
						{
							// Clear topic list
							var optionHeader = (defaultHasValue) ? '<option value="0">' : '<option value="">';
							$ (topicDropdownIdentifier).html (
									optionHeader + disabledTopicString
											+ '</option>');
							$ (topicDropdownIdentifier).attr ("disabled",
									"disabled");
							$ (topicDropdownIdentifier).parent ().addClass (
									"disabled");
							return;
						}

						populateTopicsDropdown (currentCategoryId,
								topicDropdownIdentifier, defaultTopicString, defaultHasValue);
					});
}

function populateTopicsDropdown (currentCategoryId, topicDropdownIdentifier,
		defaultTopicString, defaultHasValue)
{
	// Populate topics
	$.ajax (
	{
		url : './Php/getTopics.php',
		data :
		{
			categoryId : currentCategoryId
		},
		dataType : 'json',
		success : function (data)
		{
			var optionHeader = (defaultHasValue) ? '<option value="0">' : '<option value="">';
			var topicList = optionHeader + defaultTopicString
					+ '</option>';
			for ( var i = 0; i < data.length; i++)
			{
				topicList += '<option value="' + data[i].topicId + '">'
						+ data[i].topicName + '</option>';
			}

			$ (topicDropdownIdentifier).html (topicList);
			$ (topicDropdownIdentifier).removeAttr ("disabled");
			$ (topicDropdownIdentifier).parent ().removeClass ("disabled");
		},
		error : function (jqXHR, textStatus, errorThrown)
		{
			if (jqXHR.status == '401')
			{
				handleAuthError ();
			}
		},
		async : false
	});
}
