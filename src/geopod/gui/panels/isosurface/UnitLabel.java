package geopod.gui.panels.isosurface;

import geopod.eventsystem.IObserver;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.components.GeopodLabel;

public class UnitLabel
		extends GeopodLabel
		implements IObserver
{

	private static final long serialVersionUID = -1280368621507276137L;

	private Isosurface m_surface;

	public UnitLabel (Isosurface surface)
	{
		m_surface = surface;
		super.setText (m_surface.getUnitIdentifier ());
	}

	@Override
	public void handleNotification (GeopodEventId eventId)
	{
		super.setText (m_surface.getUnitIdentifier ());
	}

}
