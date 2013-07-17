package geopod.gui.panels;

import geopod.constants.UIConstants;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.events.GeopodEventId;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXCollapsiblePane;

public class StatusPanel
		extends JXCollapsiblePane
		implements IObserver
{
	private static final long serialVersionUID = 131554834527102637L;

	private JTextField m_statusField;

	public StatusPanel (String defaultMessage)
	{
		m_statusField = new JTextField (defaultMessage);
		m_statusField.setFont (UIConstants.GEOPOD_VERDANA.deriveFont (UIConstants.CONTENT_FONT_SIZE));
		m_statusField.setBackground (UIConstants.GEOPOD_GREEN);

		super.setDirection (Direction.LEFT);
		super.setCollapsed (false);
		super.setLayout (new BorderLayout (0, 0));

		JPanel panel = new JPanel (new BorderLayout ());
		panel.add (m_statusField, BorderLayout.CENTER);
		super.add (panel, BorderLayout.CENTER);
	}

	public void showStatusMessage (String message)
	{
		m_statusField.setText (message);
		super.setCollapsed (false);
	}

	public void hideStatus ()
	{
		super.setCollapsed (true);
	}

	public void setStatus (String message)
	{
		m_statusField.setText (message);
	}

	/**
	 * Toggles the collapsed state of this StatusPanel
	 */
	public void toggleCollapsedState ()
	{
		super.setCollapsed (!super.isCollapsed ());
	}

	@Override
	public void handleNotification (GeopodEventId eventId)
	{
		if (eventId.equals (GeopodEventId.ISOSURFACE_LOCKED))
		{
			this.showStatusMessage ("Locked to Isosurface");
		}
		else if (eventId.equals (GeopodEventId.ISOSURFACE_UNLOCKED))
		{
			this.hideStatus ();
		}
	}
}
