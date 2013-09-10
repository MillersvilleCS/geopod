// customPager.js
//
// Provides a simple paginator for paginating a series of already-loaded elements with a high degree of control over what is paginated and when.
// Designed to be dynamic and accomidate changes to the content being paginated. Includes methods to update paginator when items are added or 
// removed, or when item order changes. Can also programatically get and set page and itemsPerPage at any time.
//
// options: 
// 	itemsPerPage --------- number of items to display per page. default is 10.
//	pageItemsSelector --- selector used to determine which decendents of the pager element are items that should be shown in pages
//	pageDisplaySelector - selector used to locate the decendent of the pager element which should contain the pager info display
//  currentPage --------- page set to when pager first loads. default is 0.
//  loadOnCreate -------- whether or not the pager should paginate the element when it is created. Set to true if the element the pager 
//				   		  is initialized on already contains the items to paginate. If set to false, call "paginateContent" when you 
//				          are ready to paginate. default is true.
//
// methods:
//	paginateContent - call this to 'redo' pagination when there have been changes to the number of items being displayed, also to paginate 
// 					  for the first time if 'loadOnCreate' was set to false. Note: if no items are found to paginate, the pager will not be displayed
// 	refresh --------- call this when the order if the items have changed but the items, and thus the number of items, have not
//	setItemsPerPage - changes the number of items displayed per page. 'paginateContent' will be called and paginator will return to the first page
//	getItemsPerPage - returns the currently set items per page
//  setCurrentPage -- changes the page currently displayed. If content was not yet paginated or the element contains no items, will be stored and used 
//                    when the content is paginated. 0-based page numbering.
//	getCurrentPage -- returns the page currently being displayed, or, if not yet paginated, the starting page passed in as an option. 0-based page numbering.
//
// examples:
// 	$ (".paginate").customPager (
// 	{
//		itemsPerPage : 15,
//		loadOnCreate : false
//	});
//
// $ (".paginate").customPager ("setCurrentPage", 2);

;
(function ($, undefined)
{
	// Default plugin options
	var defaultOptions =
	{
		itemsPerPage : 10,
		pageItemsSelector : "tbody > tr",
		pageDisplaySelector : "#pagerDisplay",
		currentPage : 0,
		loadOnCreate : true
	};

	// fill in structure for pager display
	function createPagerDisplay ()
	{
		var $pagerContainer = this;
		var pagerOptions = $pagerContainer.data ('pager-options');

		// put content in the element that has been set aside to hold the pager
		// display
		var $pagerDisplay = $ (this).find (pagerOptions.pageDisplaySelector);
		$pagerDisplay.html ("<button id='PagerPrevLink'>Prev</button> "
				+ "page <span id='PagerCurrentPage'></span> "
				+ "of <span id='PagerTotalPages'></span> "
				+ "<button id='PagerNextLink'>Next</button>");

		// set up the prev link
		$ ("#PagerPrevLink", $pagerDisplay).click (function ()
		{
			var previousPage = parseInt (pagerOptions.currentPage) - 1;
			methods.setCurrentPage.call ($pagerContainer, previousPage);
		});

		// set up the next link
		$ ("#PagerNextLink", $pagerDisplay).click (function ()
		{
			var nextPage = parseInt (pagerOptions.currentPage) + 1;
			methods.setCurrentPage.call ($pagerContainer, nextPage);
		});

		// TODO: add classes & CSS to make things look nice

		// hide it since it's not complete, will show when content is loaded
		$pagerDisplay.hide ();

	}

	// returns the highest page number (0 based) that will be produced for this
	// pager, or false if there are no items at all.
	function calculateHighestPageNum ()
	{
		var $pagerContainer = this;
		var pagerOptions = $pagerContainer.data ('pager-options');

		// find the total number of items we will be paginating
		var numPageItems = $pagerContainer
				.find (pagerOptions.pageItemsSelector).size () - 1;

		// floor to simulate integer division
		var highestPage = Math.floor (numPageItems / pagerOptions.itemsPerPage);
		return (highestPage);
	}

	// return true of there are items for us to paginate
	function hasContent ()
	{
		var $pagerContainer = this;
		var pagerOptions = $pagerContainer.data ('pager-options');

		// find the total number of items we will be paginating
		var numPageItems = $pagerContainer
				.find (pagerOptions.pageItemsSelector).size ();
		// if have none, then have no content; won't display pager
		return (numPageItems != 0);
	}

	// verify 'current page' is within range [0 -
	// highest page]
	function clampCurrentPage (highestPageNumber)
	{
		var $pagerContainer = this;
		var pagerOptions = $pagerContainer.data ('pager-options');

		pagerOptions.currentPage = Math.min (pagerOptions.currentPage,
				highestPageNumber);
		pagerOptions.currentPage = Math.max (pagerOptions.currentPage, 0);
	}

	function updatePagerDisplay (highestPageNumber)
	{
		var $pagerContainer = this;
		var pagerOptions = $pagerContainer.data ('pager-options');
		var $pagerDisplay = $ (this).find (pagerOptions.pageDisplaySelector);

		// disable/enable prev button
		var prevLink = $ ("#PagerPrevLink", $pagerDisplay);
		if (pagerOptions.currentPage == 0)
		{
			prevLink.attr ("disabled", "true");
		}
		else
		{
			prevLink.removeAttr ("disabled");
		}

		// disable/enable next button
		var nextLink = $ ("#PagerNextLink", $pagerDisplay);
		if (pagerOptions.currentPage == highestPageNumber)
		{
			nextLink.attr ("disabled", "true");
		}
		else
		{
			nextLink.removeAttr ("disabled");
		}

		// set the "page __" in the pager display
		$ ("#PagerCurrentPage", $pagerDisplay).html (
				parseInt (pagerOptions.currentPage) + 1);

		// set the "of __" in the pager display
		$ ("#PagerTotalPages", $pagerDisplay).html (highestPageNumber + 1);

		return ($pagerDisplay);
	}

	function refreshPage ()
	{
		var $pagerContainer = $ (this);
		var pagerOptions = $pagerContainer.data ('pager-options');

		var $pageItems = $pagerContainer.find (pagerOptions.pageItemsSelector);
		$pageItems.hide ();

		var firstShow = pagerOptions.itemsPerPage * pagerOptions.currentPage;
		var lastShow = pagerOptions.itemsPerPage
				* (pagerOptions.currentPage + 1);
		$pageItems.slice (firstShow, lastShow).show ();
	}

	var methods =
	{
		init : function (options)
		{
			return this.each (function ()
			{
				var $pagerContainer = $ (this);
				// check if initialized
				if (!$pagerContainer.data ('pager-options'))
				{
					// get any changes made to options
					$pagerContainer.data ('pager-options', $.extend (
					{}, defaultOptions, options));

					// create framework for pager display
					createPagerDisplay.call ($pagerContainer);

					// element we initialized the pager on already
					// contains the items we want to paginate, so
					// paginate them
					if ($pagerContainer.data ('pager-options').loadOnCreate)
					{
						methods.paginateContent.call ($pagerContainer);
					}
				}
				else
				{
					$.error ("jQuery.customPager already initialized");
				}
			});
		},
		paginateContent : function ()
		{
			return this.each (function ()
			{
				var $pagerContainer = $ (this);
				var pagerOptions = $pagerContainer.data ('pager-options');

				// check if element this method has been called on is
				// truly a pager
				if (pagerOptions)
				{
					// check to make sure there are some items for us
					// to divide into pages
					if (hasContent.call ($pagerContainer))
					{
						// determine the highest page number the pager will have
						var highestPage = calculateHighestPageNum
								.call ($pagerContainer);

						// make sure our current page is in a valid range
						clampCurrentPage.call ($pagerContainer, highestPage);

						// update pager display for current page and make it
						// visible
						updatePagerDisplay.call ($pagerContainer, highestPage)
								.show ();

						// show items on this page & hide rest
						refreshPage.call ($pagerContainer);
					}
					else
					{
						// there are no items to divide into pages, so
						// hide the pager display
						$ (this).find (pagerOptions.pageDisplaySelector)
								.hide ();
					}
				}
			});

		},
		refresh : function ()
		{
			return this.each (function ()
			{
				var $pagerContainer = $ (this);
				var pagerOptions = $pagerContainer.data ('pager-options');
				if (pagerOptions && hasContent.call ($pagerContainer))
				{
					refreshPage.call ($pagerContainer);
				}
			});
		},
		setItemsPerPage : function (perPage)
		{
			return this.each (function ()
			{
				var $pagerContainer = $ (this);
				var pagerOptions = $pagerContainer.data ('pager-options');
				if (pagerOptions)
				{
					pagerOptions.itemsPerPage = parseInt (perPage);
					pagerOptions.currentPage = 0;

					methods.paginateContent.call ($pagerContainer);
				}
			});

		},
		getItemsPerPage : function ()
		{
			var $pagerContainer = this;
			var pagerOptions = $pagerContainer.data ('pager-options');
			if (pagerOptions)
			{
				return (pagerOptions.itemsPerPage);
			}
			else
			{
				$.error ("jQuery.customPager not initialized");
			}
		},
		setCurrentPage : function (page)
		{
			return this.each (function ()
			{
				var $pagerContainer = $ (this);
				var pagerOptions = $pagerContainer.data ('pager-options');
				pagerOptions.currentPage = page;

				// verify 'current page' is within range 0 - last page

				// make sure we have content, if not, no pager displayed
				// and none of the subsequent change need to be made
				if (hasContent.call ($pagerContainer))
				{
					var highestPage = calculateHighestPageNum
							.call ($pagerContainer);

					// make sure our current page is in a valid range
					clampCurrentPage.call ($pagerContainer, highestPage);

					// update pager display for current page and make it
					// visible
					updatePagerDisplay.call ($pagerContainer, highestPage);

					// show items on this page & hide rest
					refreshPage.call ($pagerContainer);
				}

			});

		},
		getCurrentPage : function ()
		{
			var $pagerContainer = this;
			var pagerOptions = $pagerContainer.data ('pager-options');
			if (pagerOptions)
			{
				return (pagerOptions.currentPage);
			}
			else
			{
				$.error ("jQuery.customPager not initialized");
			}
		}
	};

	$.fn.customPager = function (method)
	{
		var methodToCall = methods[method];
		if (methodToCall)
		{
			var remainingArgs = Array.prototype.slice.call (arguments, 1);
			return methodToCall.apply (this, remainingArgs);
		}
		else if (typeof (method) === "object" || !method)
		{
			return methods.init.apply (this, arguments);
		}
		else
		{
			$.error ("Method " + method
					+ " does not exist on jQuery.customPager");
		}
	};
}) (jQuery);