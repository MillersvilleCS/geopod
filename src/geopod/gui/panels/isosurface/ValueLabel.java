package geopod.gui.panels.isosurface;

import geopod.eventsystem.IObserver;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.components.GeopodLabel;

public class ValueLabel
		extends GeopodLabel
		implements IObserver
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1604789699123766515L;

	private static final String FORMAT_STRING = "%1.2f";
	private Isosurface m_surface;

	public ValueLabel (Isosurface surface)
	{
		m_surface = surface;
		super.setText (String.format (FORMAT_STRING, m_surface.getValue ()));
		super.setToolTipText (" Current isovalue ");
	}

	@Override
	public void handleNotification (GeopodEventId eventId)
	{
		super.setText (String.format (FORMAT_STRING, m_surface.getValue ()));
	}

}
