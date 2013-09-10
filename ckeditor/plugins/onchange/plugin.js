/*
 * @file change event plugin for CKEditor
 * Copyright (C) 2011 Alfonso Martínez de Lizarrondo
 *
 * == BEGIN LICENSE ==
 *
 * Licensed under the terms of any of the following licenses at your
 * choice:
 *
 *  - GNU General Public License Version 2 or later (the "GPL")
 *    http://www.gnu.org/licenses/gpl.html
 *
 *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
 *    http://www.gnu.org/licenses/lgpl.html
 *
 *  - Mozilla Public License Version 1.1 or later (the "MPL")
 *    http://www.mozilla.org/MPL/MPL-1.1.html
 *
 * == END LICENSE ==
 *
 */

/*
 * LMY -- Modified (significantly) to remove timer for change events
 */

// Keeps track of changes to the content and fires a "change" event
CKEDITOR.plugins.add ('onchange',
{
	init : function (editor)
	{

		function fireChangeEvent ()
		{
			editor.fire ('change');
		}

		// Set several listeners to watch for changes to the content
		editor.on ('saveSnapshot', function (evt)
		{
			if (!evt.data || !evt.data.contentOnly)
				fireChangeEvent ();
		});

		editor.getCommand ('undo').on ('afterUndo', fireChangeEvent);
		editor.getCommand ('redo').on ('afterRedo', fireChangeEvent);

		// Changes in WYSIWYG mode
		editor.on ('contentDom', function ()
		{
			editor.document.on ('keydown', function (event)
			{
				// Do not capture CTRL hotkeys.
				if (!event.data.$.ctrlKey && !event.data.$.metaKey)
					fireChangeEvent ();
			});

			// Firefox OK
			editor.document.on ('drop', fireChangeEvent);
			// IE OK
			editor.document.getBody ().on ('drop', fireChangeEvent);
		});

		// Detect changes in source mode
		editor.on ('mode', function (e)
		{
			if (editor.mode != 'source')
				return;

			editor.textarea.on ('keydown', function (event)
			{
				// Do not capture CTRL hotkeys.
				if (!event.data.$.ctrlKey && !event.data.$.metaKey)
					fireChangeEvent ();
			});

			editor.textarea.on ('drop', fireChangeEvent);
			editor.textarea.on ('input', fireChangeEvent);
		});

		editor.on ('afterCommandExec', function (event)
		{
			if (event.data.name == 'source')
				return;

			if (event.data.command.canUndo !== false)
				fireChangeEvent();
		});

	} // Init
});