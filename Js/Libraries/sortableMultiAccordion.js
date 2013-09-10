// JQuery sortable multi-accordion
//   Assumes following structure for now:
//   <div id="myAccordion">
//     <!-- Panel 1 -->
//     <div> 
//       <h3 class="accordionHeader">
//         <a href="#">Header 1</a>
//       </h3>
//       <div>
//         Content 1
//       </div>
//     </div>
//     <!-- Panel 2 -->
//     <div> 
//       <h3 class="accordionHeader">
//         <a href="#">Header 2</a>
//       </h3>
//       <div>
//         Content 2
//       </div>
//     </div>
//     <!-- Panel 3 -->
//	   ...
//   </div>

(function ($, undefined)
{
	// Default plugin options
	var defaultOptions =
	{
		sortable : true,
		scrollToTop : true
	};

	// Private methods
	function isClosed (panel)
	{
		var $content = $ (panel.lastElementChild);
		return $content.css ("display") === "none";
	}

	function accordionize ($headers)
	{
		// Add style to header
		$headers
				.addClass ('ui-accordion-header ui-helper-reset ui-state-default ui-corner-all')
		$headers.prepend ('<span class="ui-icon ui-icon-triangle-1-e"></span>');

		// set up toggle open/close on click behavior
		$headers.click (function ()
		{
			var panel = this.parentNode;
			var isOpening = isClosed (panel);
			var $header = $ (this);
			if (isOpening)
			{
				methods.openPanel ($header);
			}
			else
			{
				methods.closePanel ($header);
			}

			return false;
		});

		// Add hover style
		$headers.hover (function ()
		{
			$ (this).addClass ('ui-state-hover');
		}, function ()
		{
			$ (this).removeClass ('ui-state-hover');
		});

		// Add style to content
		var content = $headers.next ();
		content
				.addClass ('ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom');
		content.hide ();
	}

	// Public methods
	var methods =
	{
		init : function (options)
		{
			return this
					.each (function ()
					{
						var $accordion = $ (this);
						if (!$accordion.data ('accordion-options'))
						{
							// get any changes made to options
							$accordion.data ('accordion-options', $.extend (
							{}, defaultOptions, options));

							// Add default list style to accordion widget
							$accordion
									.addClass ('ui-accordion ui-widget ui-helper-reset ui-accordion-icons');

							// add default style to headers & content
							var headers = $accordion.children ().children (
									":first-child");
							accordionize (headers);

							// make sortable
							if ($accordion.data ('accordion-options')['sortable'])
							{
								$accordion.sortable ();
							}

							$accordion.triggerHandler ("onCreate");
						}
						else
						{
							$
									.error ("jQuery.multiAccordion already initialized");
						}
					});
		},

		getNumPanels : function ()
		{
			var numPanels = this.children ().length;
			return numPanels;
		},

		openPanelByIndex : function (panelNum)
		{
			return this.each (function ()
			{
				var $accordion = $ (this);

				// Get nth panel
				var nthPanel = $accordion.children (":nth-child(" + panelNum
						+ ")");
				var nthPanelElem = nthPanel.get (0);

				// Get child header
				var nthHeader = nthPanel.children (":first-child");

				// Do nothing if already open
				if (isClosed (nthPanelElem))
				{
					methods.openPanel (nthHeader);
				}
			});
		},

		openPanel : function ($header)
		{
			var $accordion = $header.parent ().parent ();
			var panel = $header.parent ().get (0);
			$accordion.triggerHandler ("onChange", [ panel, true ]);

			// Change style for open panels
			$header.removeClass ('ui-state-default ui-corner-all').addClass (
					'ui-state-active ui-corner-top');
			var $span = $header.children ('span.ui-icon');
			$span.removeClass ('ui-icon-triangle-1-e').addClass (
					'ui-icon-triangle-1-s');
			var $content = $header.next ();
			$content.slideDown ('fast', function ()
			{
				$content.addClass ('ui-accordion-content-active');

				if ($accordion.data ('accordion-options')['scrollToTop'])
				{
					$ ('html').animate (
							{
								scrollTop : $ (".ui-accordion-header", panel)
										.offset ().top
							}, 'slow');
				}
			});
		},

		openAllPanels : function ()
		{
			return this.each (function ()
			{
				var $accordion = $ (this);
				var panels = $accordion.children ();
				var headers = panels.children ("* > .ui-accordion-header");
				headers.each (function ()
				{
					var panel = this.parentNode;
					if (isClosed (panel))
					{
						var $this = $ (this);
						methods.openPanel ($this);
					}
				});
			});
		},

		closePanelByIndex : function (panelNum)
		{
			return this.each (function ()
			{
				var $accordion = $ (this);

				// Get nth panel
				var nthPanel = $accordion.children (":nth-child(" + panelNum
						+ ")");
				var nthPanelElem = nthPanel.get (0);
				// Get child header
				var nthHeader = nthPanel.children (":first-child");
				// Do nothing if already closed
				if (!isClosed (nthPanelElem))
				{
					methods.closePanel (nthHeader);
				}
			});
		},

		closePanel : function ($header)
		{
			var $accordion = $header.parent ().parent ();
			var panel = $header.parent ().get (0);
			$accordion.triggerHandler ("onChange", [ panel, false ]);

			$header.removeClass ('ui-state-active ui-corner-top').addClass (
					'ui-state-default ui-corner-all');
			var $span = $header.children ('span.ui-icon');
			$span.removeClass ('ui-icon-triangle-1-s').addClass (
					'ui-icon-triangle-1-e');
			var $content = $header.next ();
			$content.slideUp ('fast', function ()
			{
				$content.removeClass ('ui-accordion-content-active');
			});
		},

		closeAllPanels : function ()
		{
			return this.each (function ()
			{
				var $accordion = $ (this);
				var panels = $accordion.children ();
				var headers = panels.children ("* > .ui-accordion-header");
				headers.each (function ()
				{
					var panel = this.parentNode;
					if (!isClosed (panel))
					{
						var $this = $ (this);
						methods.closePanel ($this);
					}
				});
			});
		},

		append : function (panelToAppend)
		{
			return this.each (function ()
			{
				var $accordion = $ (this);

				// DOM tree to append is passed as jQuery object
				$accordion.append (panelToAppend);

				var $header = panelToAppend.children (":first-child");
				accordionize ($header);

				$accordion.triggerHandler ("onAppend");
			});
		},

		remove : function ()
		{
			return this.each (function ()
			{
				var $accordion = $ (this).parent ();
				$ (this).remove ();
				$accordion.triggerHandler ("onRemove");
			});
		}

	};

	$.fn.multiAccordion = function (method)
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
					+ " does not exist on jQuery.multiAccordion");
		}
	};
}) (jQuery);
