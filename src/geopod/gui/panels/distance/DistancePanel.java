package geopod.gui.panels.distance;

import geopod.Geopod;
import geopod.constants.UIConstants;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.Hud;
import geopod.gui.components.BorderFactory;
import geopod.gui.components.ButtonFactory;
import geopod.gui.components.GeopodButton;
import geopod.gui.components.GeopodLabel;
import geopod.gui.components.PainterFactory;
import geopod.utils.math.GeodeticUtility;
import geopod.utils.math.LatLonAltValidator;
import geopod.utils.math.VisadUtility;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXPanel;

import visad.georef.EarthLocation;

/**
 * The distance panel
 * 
 * @author geopod team
 * 
 */

public class DistancePanel
		extends JXPanel
		implements ISubject
{

	private static final long serialVersionUID = 3675180947835866753L;

	private static final String FORMAT_PATTERN;
	private static String CLOSE_BUTTON_TOOL_TIP;
	private static String CLOSE_BUTTON_TEXT;
	private static String RECORD_BUTTON_TOOL_TIP;
	private static String RECORD_BUTTON_TEXT;
	private static String SET_BUTTON_TEXT;
	private static String SET_BUTTON_TOOL_TIP;
	private static Color m_backgroundColor;

	private JPanel m_innerViewPanel;
	private GeopodButton m_closeButton;
	private GeopodButton m_recordButton;
	private GeopodButton m_setButton;
	// This unitLabel should probably always be in km, because we also convert to km before calculations
	private JLabel m_distanceValue;
	private JLabel m_distanceUnit;
	private JLabel m_latitudeValue;
	private JLabel m_longitudeValue;
	private JLabel m_altitudeValue;

	private Geopod m_geopod;
	private Hud m_hud;
	private SubjectImpl m_subjectImpl;
	private EarthLocation m_startPosition;
	private EarthLocation m_endPosition;
	private DecimalFormat m_formatter;
	private double m_distance;

	static
	{
		CLOSE_BUTTON_TOOL_TIP = " Close the distance panel ";
		CLOSE_BUTTON_TEXT = "CLOSE";
		RECORD_BUTTON_TOOL_TIP = " Set Start to Current Location ";
		RECORD_BUTTON_TEXT = " SET TO CURRENT ";
		SET_BUTTON_TOOL_TIP = " Set Start Location ";
		SET_BUTTON_TEXT = " SET ";
		FORMAT_PATTERN = "0.00";
		m_backgroundColor = UIConstants.GEOPOD_GREEN;
	}

	/**
	 * 
	 * @param geopod
	 */
	public DistancePanel (Geopod geopod, Hud hud)
	{
		m_geopod = geopod;
		m_hud = hud;
		m_subjectImpl = new SubjectImpl ();

		m_startPosition = m_geopod.getEarthLocation ();
		m_endPosition = m_startPosition;
		m_distance = 0.0;
		m_formatter = new DecimalFormat (FORMAT_PATTERN);
		setUpPanelBackground ();
		//addHeader ();
		setUpButtons ();
		setUpInnerViewPanel ();
		addInnerComponents ();
		displayEarthLocation (m_startPosition);
		this.setFocusable (false);
	}

	private void setUpPanelBackground ()
	{	
		// the layered JPanels on the Geopod Frame resize. To prevent it, we add our own here
		this.setLayout (new MigLayout ("wrap 1, fillx", "0[align center, grow]2", "5[] 0[align center, grow]0"));
		this.setBorder (BorderFactory.createStandardBorder ());
		this.setBackgroundPainter (PainterFactory.createStandardMattePainter (410, 350));
	}

	//	private void addHeader ()
	//	{
	//		JLabel headerLabel = new JLabel (PANEL_TITLE, SwingConstants.CENTER);
	//		Font font = UIConstants.GEOPOD_BANDY.deriveFont (Font.BOLD, UIConstants.TITLE_SIZE-3);
	//		headerLabel.setFont (font);
	//		this.add (headerLabel, "gaptop 1, gapbottom 1");
	//	}

	private void setUpInnerViewPanel ()
	{

		// Initialize the panel
		m_innerViewPanel = new JPanel ();
		m_innerViewPanel.setLayout (new MigLayout ("", "[30:30:30]15[60:60:60]15[60:60:60]10[100:100, right, grow]", ""));
		m_innerViewPanel.setBackground (m_backgroundColor);

		setUpDisplayFields ();
	}

	private void addInnerComponents ()
	{
		this.add (m_innerViewPanel);
		this.add (m_closeButton);
	}
	
	/**
	 * 
	 */
	private void setUpDisplayFields ()
	{
		Font textFieldFont = UIConstants.GEOPOD_VERDANA;
		textFieldFont = textFieldFont.deriveFont (12.0f);
		Font labelFont = textFieldFont.deriveFont (Font.BOLD);

		JLabel distanceLabel = makeLabel ("Distance: ", labelFont);
		// Default test valueLabel, shows the maximum number of supported digits
		m_distanceValue = makeLabel ("1000000.01", textFieldFont);
		m_distanceUnit = makeLabel ("km", textFieldFont);

		GeopodLabel latitudeLabel = makeLabel ("LAT:", labelFont);
		m_latitudeValue = makeLabel ("", textFieldFont);
		m_latitudeValue.setToolTipText (latToolTipText ());

		JLabel longitudeLabel = makeLabel ("LON:", labelFont);
		m_longitudeValue = makeLabel ("", textFieldFont);
		m_longitudeValue.setToolTipText (lonToolTipText ());

		JLabel altitudeLabel = makeLabel ("ALT:", labelFont);
		m_altitudeValue = makeLabel ("", textFieldFont);
		m_altitudeValue.setToolTipText (altToolTipText ());

		// Row 1
		m_innerViewPanel.add (latitudeLabel, "left, pushy");
		m_innerViewPanel.add (m_latitudeValue, "right");
		m_innerViewPanel.add (m_setButton, "span 2, center, pushx, growpriox 200, wrap");

		// Row 2
		m_innerViewPanel.add (longitudeLabel, "left, pushy");
		m_innerViewPanel.add (m_longitudeValue, "right");
		m_innerViewPanel.add (m_recordButton, "span 2, center, pushx, growpriox 200, wrap");

		// Row 3
		m_innerViewPanel.add (altitudeLabel, "left, pushy");
		m_innerViewPanel.add (m_altitudeValue, "right");
		m_innerViewPanel.add (distanceLabel, "left");
		m_innerViewPanel.add (m_distanceValue, "split 2, pushx, growpriox 200");
		m_innerViewPanel.add (m_distanceUnit);

	}

	private GeopodLabel makeLabel (String text, Font labelFont)
	{
		GeopodLabel label = new GeopodLabel (text);
		label.setFont (labelFont);
		return (label);
	}

	// Units will be converted into the units of the start location
	private String latToolTipText ()
	{
		return (VisadUtility.latitudeToolTipText ("Latitude", m_startPosition));
	}

	private String lonToolTipText ()
	{
		return (VisadUtility.longitudeToolTipText ("Longitude", m_startPosition));
	}

	private String altToolTipText ()
	{
		return (VisadUtility.altitudeToolTipText ("Altitude", m_startPosition));
	}

	private void setUpButtons ()
	{
		float innerButtonSize = 14.0f;
		float outerButtonSize = UIConstants.BUTTON_FONT_SIZE;
		Color buttonColor = UIConstants.GEOPOD_GREEN;
		boolean boldInnerButtons = false;
		boolean boldOuterButton = true;

		m_recordButton = ButtonFactory.createGradientButton (innerButtonSize, buttonColor, boldInnerButtons);
		m_recordButton.setText (RECORD_BUTTON_TEXT);
		m_recordButton.setToolTipText (RECORD_BUTTON_TOOL_TIP);
		m_recordButton.setActionCommand ("Record");
		m_recordButton.addActionListener (new ActionListener ()
		{

			@Override
			public void actionPerformed (ActionEvent e)
			{

				EarthLocation el = m_geopod.getEarthLocation ();
				if (el != null)
				{
					setStartPosition (el);
				}
			}

		});
		m_recordButton.setFocusable (false);

		m_setButton = ButtonFactory.createGradientButton (innerButtonSize, buttonColor, boldInnerButtons);
		m_setButton.setText (SET_BUTTON_TEXT);
		m_setButton.setToolTipText (SET_BUTTON_TOOL_TIP);
		m_setButton.addActionListener (new DistanceModalWindow (this));

		m_closeButton = ButtonFactory.createGradientButton (outerButtonSize, buttonColor, boldOuterButton);
		m_closeButton.setText (CLOSE_BUTTON_TEXT);
		m_closeButton.setToolTipText (CLOSE_BUTTON_TOOL_TIP);
		m_closeButton.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				toggleVisibility ();
			}

		});
		m_setButton.setFocusable (false);
	}

	public void calculateDistance ()
	{
		// Computes linear distances using approx. the Earth as a sphere
		setDistanceIndicator (GeodeticUtility.computeLinearDistance (m_startPosition, m_endPosition));
	}

	private void setDistanceIndicator (double distance)
	{
		// Set and format the distance between two earth locations
		m_distance = distance;

		if (Double.isNaN (m_distance))
		{
			// We should not ever have to execute this code, but just in case
			m_distanceValue.setText ("NaN");
			m_distanceUnit.setText ("");
		}
		else
		{
			m_distanceValue.setText (m_formatter.format (m_distance));
			m_distanceUnit.setText ("km");
		}
	}

	public void setStartPosition (EarthLocation el)
	{
		m_startPosition = el;
		calculateDistance ();
		displayEarthLocation (m_startPosition);
	}
	
	public EarthLocation getDisplayLocation ()
	{
		return m_startPosition;
	}
	
	/**
	 * Request that the main canvas gets focus
	 */
	public void requestFocusOnCanvas ()
	{
		m_hud.requestFocusOnCanvas ();
	}

	/**
	 * Sets the end position to the given {@link EarthLocation} and calculates
	 * the distance from start position to new end position.
	 * 
	 * @param el
	 */
	public void setEndPosition (EarthLocation el)
	{
		m_endPosition = el;
		calculateDistance ();
	}

	/**
	 * Sets the latitude, longitude, and altitude fields to the values specified
	 * by a given {@link EarthLocation}
	 * 
	 * @param el
	 *            the specified EarthLocation
	 */
	private void displayEarthLocation (EarthLocation el)
	{
		double lat = LatLonAltValidator.getLatitudeValue (el);
		double lon = LatLonAltValidator.getLongitudeValue (el);
		double alt = LatLonAltValidator.getAltitudeValue (el);

		m_latitudeValue.setText (m_formatter.format (lat));
		m_longitudeValue.setText (m_formatter.format (lon));
		m_altitudeValue.setText (m_formatter.format (alt));
	}

	/**
	 * Opens and closes the display via toggling.
	 */
	public void toggleVisibility ()
	{
		this.setVisible (!this.isVisible ());
		notifyObservers (GeopodEventId.DISTANCE_BUTTON_STATE_CHANGED);
	}

	/****************************************************************************************************************************
	 * ISubject methods
	 */

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
