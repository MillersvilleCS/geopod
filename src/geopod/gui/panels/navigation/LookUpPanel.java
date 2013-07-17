package geopod.gui.panels.navigation;

import geopod.constants.UIConstants;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.components.ButtonFactory;
import geopod.utils.geocoding.GeocoderUtility;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXCollapsiblePane;

import visad.georef.EarthLocation;
import visad.georef.LatLonPoint;

/**
 * The LookUpPanel provides both forward and reverse geocoding functionality.
 * Once a successful lookup has been performed, the associated
 * {@link NavigationPanel} is updated to reflect any changes.
 * 
 * @author Geopod Team
 */
public class LookUpPanel
		extends JXCollapsiblePane
		implements IObserver, ISubject
{
	private static final long serialVersionUID = -3640424355944819878L;

	private NavigationPanel m_navigationPanel;
	private JTextField m_addressField;

	private SubjectImpl m_subjectImpl;

	/**
	 * Initializes the LookUpPanel and associates it with the specified
	 * {@link NavigationPanel}
	 * 
	 * @param navPanel
	 *            the specified NavigationPanel
	 */
	public LookUpPanel (NavigationPanel navPanel)
	{
		m_subjectImpl = new SubjectImpl ();

		m_navigationPanel = navPanel;
		super.setDirection (Direction.DOWN);
		super.setCollapsed (true);
		super.setLayout (new BorderLayout (0, 0));

		JPanel lookUpWidget = buildLookUpWidget ();
		super.add (lookUpWidget, BorderLayout.CENTER);
	}

	/**
	 * Returns a container that holds both an address field and button
	 * 
	 * @return the look up widget panel
	 */
	private JPanel buildLookUpWidget ()
	{
		JPanel panel = new JPanel (new BorderLayout ());

		m_addressField = new JTextField ("Enter Address Here");
		m_addressField.setBackground (UIConstants.GEOPOD_GREEN);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (UIConstants.SUBTITLE_SIZE);
		m_addressField.setFont (font);
		m_addressField.addKeyListener (new KeyAdapter ()
		{
			@Override
			public void keyPressed (KeyEvent e)
			{
				if (e.getKeyChar () == KeyEvent.VK_ENTER)
				{
					performLookUp ();
				}
			}
		});

		panel.add (m_addressField, BorderLayout.CENTER);

		JButton lookUpButton = ButtonFactory.createGradientButton (18.0f, UIConstants.GEOPOD_GREEN, false);
		lookUpButton.setText ("LOOK UP");
		lookUpButton.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				performLookUp ();
			}

		});

		panel.add (lookUpButton, BorderLayout.EAST);

		return (panel);
	}

	/**
	 * Performs a forward geocoding of the address currently contained in the
	 * address field. The associated {@link NavigationPanel} is then populated
	 * with the results.
	 */
	private void performLookUp ()
	{
		LatLonPoint llp = GeocoderUtility.geocode (m_addressField.getText ());
		if (llp != null)
		{
			m_navigationPanel.setInputLatLon (llp);
			m_navigationPanel.indicateValueChange ();
		}
		else
		{
			m_addressField.setText ("No Results Found");
		}
	}

	/**
	 * Toggles the collapsed state of this LookUpPanel
	 */
	public void toggleCollapsedState ()
	{
		if (super.isCollapsed ())
		{
			this.updateLocation ();
		}

		super.setCollapsed (!super.isCollapsed ());

		this.notifyObservers (GeopodEventId.LOOKUP_BUTTON_STATE_CHANGED);
	}

	/**
	 * Performs a reverse geocoding of the currently displayed lat/lon/alt in
	 * the associated {@link NavigationPanel}. The resulting address is placed
	 * inside the LookUpPanel address field.
	 */
	public void updateLocation ()
	{
		EarthLocation el = m_navigationPanel.parseInputtedEarthLocation ();
		String currentLocation = "No Address Found";

		if (el != null)
		{
			currentLocation = GeocoderUtility.geocode (el.getLatitude ().getValue (), el.getLongitude ().getValue ());

			if (currentLocation == null)
			{
				currentLocation = "No Address Found";
			}
			m_addressField.setText (currentLocation);
		}
		else
		{
			m_addressField.setText (currentLocation);
		}
	}

	@Override
	public void handleNotification (GeopodEventId eventId)
	{
		if (!super.isCollapsed ())
		{
			super.setCollapsed (true);
			this.notifyObservers (GeopodEventId.LOOKUP_BUTTON_STATE_CHANGED);
		}
		m_navigationPanel.resetColors ();
	}

	@Override
	public void addObserver (IObserver observer, GeopodEventId eventId)
	{
		m_subjectImpl.addObserver (observer, eventId);
	}

	@Override
	public void removeObserver (IObserver observer, GeopodEventId eventId)
	{
		m_subjectImpl.removeObserver (observer, eventId);
	}

	@Override
	public void notifyObservers (GeopodEventId eventId)
	{
		m_subjectImpl.notifyObservers (eventId);
	}

	@Override
	public void removeObservers ()
	{
		m_subjectImpl.removeObservers ();
	}
}
