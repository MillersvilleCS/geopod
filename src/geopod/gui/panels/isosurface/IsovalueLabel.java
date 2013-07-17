package geopod.gui.panels.isosurface;

import geopod.eventsystem.IObserver;
import geopod.eventsystem.events.GeopodEventId;

import javax.swing.JLabel;

public class IsovalueLabel extends JLabel implements IObserver
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1604789699123766515L;

	
	private static final String m_format = "%4.1f";
	private Isosurface m_surface;
	
	
	IsovalueLabel (Isosurface surface)
	{
		m_surface = surface;
		super.setText (String.format (m_format, m_surface.getValue ()) + " " + m_surface.getUnitIdentifier ());
	}
	
	@Override
	public void handleNotification (GeopodEventId eventId)
	{
		super.setText (String.format (m_format, m_surface.getValue ()) + " " + m_surface.getUnitIdentifier ());	
	}

}
