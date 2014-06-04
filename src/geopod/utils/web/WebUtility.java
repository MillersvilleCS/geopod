package geopod.utils.web;

import geopod.utils.debug.Debug;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;

import javax.swing.event.HyperlinkEvent;

/**
 * A utility class for web functionality
 * 
 * @author Geopod Team
 * 
 */
public class WebUtility
{

	private WebUtility ()
	{
		//Static Class
	}

	/**
	 * Attempts to use the system's native web browser to browse to the
	 * specified address
	 * 
	 * @param address
	 *            the address to browse to
	 */
	public static void browse (String address)
	{
		try
		{
			URL url = new URL (address);
			Desktop.getDesktop ().browse (url.toURI ());
		}
		catch (Exception e)
		{
			System.err.println ("Could not browse");
			if (Debug.isDebuggingOn ())
			{
				e.printStackTrace ();
			}
		}
	}

	/**
	 * Attempts to use the system's native web browser to browse to the
	 * specified address contained in the {@link HyperlinkEvent}
	 * 
	 * @param linkEvent
	 *            the HyperlinkEvent that contains a {@link URL}
	 */
	public static void browse (HyperlinkEvent linkEvent)
	{
		try
		{
			URI uri = linkEvent.getURL ().toURI ();
			Desktop.getDesktop ().browse (uri);
		}
		catch (Exception e)
		{
			System.err.println ("Could not browse");
			if (Debug.isDebuggingOn ())
			{
				e.printStackTrace ();
			}
		}
	}
}
