package geopod.mission;

import java.awt.Desktop;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class MissionHyperlinkListener
		implements HyperlinkListener
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void hyperlinkUpdate (HyperlinkEvent event)
	{
		if (event.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
		{
			try
			{
				Desktop.getDesktop ().browse (event.getURL ().toURI ());
			}
			catch (Exception e)
			{
				// Do nothing.
			}
		}
	}
}
