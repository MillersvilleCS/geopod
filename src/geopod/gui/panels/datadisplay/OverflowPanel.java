package geopod.gui.panels.datadisplay;

import geopod.eventsystem.IObserver;
import geopod.eventsystem.events.GeopodEventId;

import java.awt.BorderLayout;

import org.jdesktop.swingx.JXCollapsiblePane;

/**
 * A specialized panel that will automatically collapse or expand when the
 * associated {@link SensorDisplayPanel} becomes active or empty
 * 
 * @author Geopod Team
 * 
 */
public class OverflowPanel
		extends JXCollapsiblePane
		implements IObserver
{

	private static final long serialVersionUID = -3308476793403252053L;

	/**
	 * Initializes a newly created OverflowPanel object.
	 */
	public OverflowPanel ()
	{
		super ();

		setupLayout ();
	}

	/**
	 * Sets the layout of this OverflowPanel
	 */
	private void setupLayout ()
	{
		super.setLayout (new BorderLayout (0, 0));
	}

	@Override
	public void handleNotification (GeopodEventId eventId)
	{
		if (eventId.equals (GeopodEventId.DISPLAY_PANEL_EMPTY))
		{
			super.setCollapsed (true);
		}
		else if (eventId.equals (GeopodEventId.DISPLAY_PANEL_ACTIVE))
		{
			super.setCollapsed (false);
		}
	}
}
