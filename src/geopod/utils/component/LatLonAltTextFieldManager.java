package geopod.utils.component;

import geopod.constants.UIConstants;
import geopod.utils.math.LatLonAltValidator;
import geopod.utils.math.MathUtility;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;

import javax.swing.JTextField;

import visad.georef.EarthLocation;
import visad.georef.LatLonPoint;

public class LatLonAltTextFieldManager
{
	private static final String FORMAT_PATTERN;

	private Color m_normalColor;
	private Color m_errorColor;

	private JTextField m_latitudeInput;
	private JTextField m_longitudeInput;
	private JTextField m_altitudeInput;

	private Font m_latitudeFont;
	private Font m_longitudeFont;
	private Font m_altitudeFont;

	private DecimalFormat m_formatter;

	static
	{
		FORMAT_PATTERN = "0.##";
	}

	public LatLonAltTextFieldManager (JTextField latitudeInput, JTextField longitudeInput, JTextField altitudeInput)
	{
		m_latitudeInput = latitudeInput;
		m_longitudeInput = longitudeInput;
		m_altitudeInput = altitudeInput;

		m_latitudeFont = defaultFont ();
		m_longitudeFont = defaultFont ();
		m_altitudeFont = defaultFont ();

		m_normalColor = UIConstants.GEOPOD_GREEN;
		m_errorColor = UIConstants.GEOPOD_RED;
		m_formatter = new DecimalFormat (FORMAT_PATTERN);

		m_latitudeInput.setFont (m_latitudeFont);
		m_latitudeInput.setBackground (m_normalColor);
		m_latitudeInput.addFocusListener (new FocusListener ());

		m_longitudeInput.setFont (m_latitudeFont);
		m_longitudeInput.setBackground (m_normalColor);
		m_longitudeInput.addFocusListener (new FocusListener ());

		altitudeInput.setFont (m_latitudeFont);
		altitudeInput.setBackground (m_normalColor);
		altitudeInput.addFocusListener (new FocusListener ());		
	}

	/**
	 * Set all lat/lon/alt input field fonts to the specified font.
	 * @param font
	 */
	public void setFieldFonts (Font font)
	{
		setLatitudeFont (font);
		setLongitudeFont (font);
		setAltitudeFont (font);
	}
	
	/**
	 * Set only the latitude input field font.
	 * @param font
	 */
	public void setLatitudeFont (Font font)
	{
		m_latitudeFont = font;
		m_latitudeInput.setFont (m_latitudeFont);
	}
	
	/**
	 * Set only the longitude input field font,
	 * @param font
	 */
	public void setLongitudeFont (Font font)
	{
		m_longitudeFont = font;
		m_longitudeInput.setFont (m_longitudeFont);
	}
	
	/**
	 * Set only the altitude input field font.
	 * @param font
	 */
	public void setAltitudeFont (Font font)
	{
		m_altitudeFont = font;
		m_altitudeInput.setFont (m_altitudeFont);
	}
	
	/**
	 * Set the normal background color of all lat/lon/alt input fields to 
	 * the specified color. 
	 * @param c - the specified color.
	 */
	public void resetBackgroundColors (Color c)
	{
		m_normalColor = c;
		resetColors ();
	}
	
	/**
	 * Set the error indication color for all fields to the specified color.
	 * @param c - the specified color
	 */
	public void resetErrorColor (Color c)
	{
		m_errorColor = c;
	}
	
	/**
	 * Add a key listener to the lat/lon/alt input fields
	 * @param changeListener - the KeyListener to add to each component
	 */
	public void addKeyListener (KeyListener changeListener)
	{
		m_latitudeInput.addKeyListener (changeListener);
		m_longitudeInput.addKeyListener (changeListener);
		m_altitudeInput.addKeyListener (changeListener);
	}
	
	/**
	 * All lat/lon/alt fields have a key listener that indicates if inputed data is correct.
	 */
	public void addDefaultKeyListener ()
	{
		m_latitudeInput.addKeyListener (new ChangeListener ());
		m_longitudeInput.addKeyListener (new ChangeListener ());
		m_altitudeInput.addKeyListener (new ChangeListener ());
	}

	/**
	 * Sets the latitude, longitude, and altitude fields to the values specified
	 * by a given {@link EarthLocation}
	 * 
	 * @param el
	 *            the specified EarthLocation
	 */
	public void setInputEarthLocation (EarthLocation el)
	{
		double lat = LatLonAltValidator.getLatitudeValue (el);
		double lon = LatLonAltValidator.getLongitudeValue (el);
		double alt = LatLonAltValidator.getAltitudeValue (el);

		setInputText (m_latitudeInput, m_formatter.format (lat));
		setInputText (m_longitudeInput, m_formatter.format (lon));
		setInputText (m_altitudeInput, m_formatter.format (alt));

		// Make sure lat/lon/alt are valid
		validateLatLonAlt ();
	}

	/**
	 * Sets the latitude, longitude, and altitude fields to the values specified
	 * individually
	 * 
	 * @param latitude
	 *            the specified latitude
	 * @param longitude
	 *            the specified longitude
	 * @param altitude
	 *            the specified altitude
	 */
	public void setInputEarthLocation (double latitude, double longitude, double altitude)
	{
		longitude = LatLonAltValidator.getLongitudeValue (longitude);

		setInputText (m_latitudeInput, m_formatter.format (latitude));
		setInputText (m_longitudeInput, m_formatter.format (longitude));
		setInputText (m_altitudeInput, m_formatter.format (altitude));

		// values changed without any typing, so let's check it
		validateLatLonAlt ();
	}

	/**
	 * Sets only the Latitude and Longitude of the lat/lon/alt display to the
	 * specified {@link LatLonPoint}
	 * 
	 * @param llp
	 *            the specified Latitude and Longitude
	 */
	public void setInputLatLon (LatLonPoint llp, EarthLocation el)
	{
		double lat = llp.getLatitude ().getValue ();
		double lon = llp.getLongitude ().getValue ();

		// Method "setText" is thread safe so these calls needn't be on the EDT
		m_latitudeInput.setText (m_formatter.format (lat));
		m_longitudeInput.setText (m_formatter.format (lon));

		m_latitudeInput.setCaretPosition (0);
		m_longitudeInput.setCaretPosition (0);

		// the altitude might be an error - what do we do with it?
		// the answer is to set it to the current altitude (only if it's NaN)
		if (Double.isNaN (MathUtility.parseDouble (m_altitudeInput.getText ())))
		{
			double alt = LatLonAltValidator.getAltitudeValue (el);
			m_altitudeInput.setText (m_formatter.format (alt));
		}

		// values changed without any typing, so let's check it
		validateLatLonAlt ();
	}

	/**
	 * Returns an {@link EarthLocation} that represents the current values in
	 * the lat/lon/alt display.
	 * 
	 * @return the currently displayed EarthLocation, null if the current values
	 *         cannot be parsed.
	 */
	public EarthLocation parseInputtedEarthLocation ()
	{
		String lat = m_latitudeInput.getText ();
		String lon = m_longitudeInput.getText ();
		String alt = m_altitudeInput.getText ();
		return (LatLonAltValidator.parseInputtedEarthLocation (lat, lon, alt));
	}

	/**
	 * Returns an {@link EarthLocation} that represents the current values in
	 * the lat/lon/alt display.
	 * 
	 * @return the currently displayed EarthLocation, null if the current values
	 *         cannot be parsed.
	 */
	public EarthLocation parseAndValidateInputtedEarthLocation ()
	{
		EarthLocation el = null;

		if (attemptValidLatLonAlt ())
		{
			el = parseInputtedEarthLocation ();
		}
		return (el);
	}

	/**
	 * Checks whether lat/lon/alt are valid to the application
	 */
	public boolean validateLatLonAlt ()
	{
		boolean isValid = true;

		if (!latIsValid ())
		{
			isValid = false;
		}

		if (!lonIsValid ())
		{
			isValid = false;
		}

		if (!altIsValid ())
		{
			isValid = false;
		}

		return (isValid);
	}

	/**
	 * Checks whether lat is valid to the application
	 */
	public boolean latIsValid ()
	{
		boolean isValid = LatLonAltValidator.validLatitudeText (m_latitudeInput.getText ());

		if (isValid)
		{
			m_latitudeInput.setBackground (m_normalColor);
		}
		else
		{
			m_latitudeInput.setBackground (m_errorColor);
		}

		return (isValid);
	}

	/**
	 * Checks whether lon is valid to the application
	 */
	public boolean lonIsValid ()
	{
		boolean isValid = LatLonAltValidator.validLongitudeText (m_longitudeInput.getText ());

		if (isValid)
		{
			m_longitudeInput.setBackground (m_normalColor);
		}
		else
		{
			m_longitudeInput.setBackground (m_errorColor);
		}

		return (isValid);
	}

	/**
	 * Checks whether alt is valid to the application
	 */
	public boolean altIsValid ()
	{
		boolean isValid = LatLonAltValidator.validAltitudeText (m_altitudeInput.getText ());

		if (isValid)
		{
			m_altitudeInput.setBackground (m_normalColor);
		}
		else
		{
			m_altitudeInput.setBackground (m_errorColor);
		}

		return (isValid);
	}

	/**
	 * Makes sure the values for lat/lon/alt are valid to the application
	 */
	public boolean attemptValidLatLonAlt ()
	{
		resetColors ();

		boolean isGood = true;

		double lat = MathUtility.parseDouble (m_latitudeInput.getText ());
		double lon = MathUtility.parseDouble (m_longitudeInput.getText ());
		double alt = MathUtility.parseDouble (m_altitudeInput.getText ());

		lat = LatLonAltValidator.clampLatitude (lat);
		if (Double.isNaN (lat))
		{
			m_latitudeInput.setBackground (m_errorColor);
			isGood = false;
		}

		lon = LatLonAltValidator.clampLongitude (lon);
		if (Double.isNaN (lon))
		{
			m_longitudeInput.setBackground (m_errorColor);
			isGood = false;
		}

		alt = LatLonAltValidator.clampLongitude (alt);
		if (Double.isNaN (alt))
		{
			m_altitudeInput.setBackground (m_errorColor);
			isGood = false;
		}

		if (isGood)
		{
			// Set the new lat/lon/alt - we were successful
			setInputEarthLocation (lat, lon, alt);
		}

		return (isGood);
	}

	/**
	 * Resets the background colors of the lat/lon/alt display
	 */
	public void resetColors ()
	{
		this.setInputAreaBackgroundColor (m_normalColor);
	}

	/**
	 * Sets background color of the lat/lon/alt display to the specified
	 * {@link Color}
	 * 
	 * @param c
	 *            the specified color to be set
	 */
	private void setInputAreaBackgroundColor (Color c)
	{
		m_altitudeInput.setBackground (c);
		m_latitudeInput.setBackground (c);
		m_longitudeInput.setBackground (c);
	}

	private static class FocusListener
			extends FocusAdapter
	{
		@Override
		public void focusGained (FocusEvent e)
		{
			if (e.getComponent () instanceof JTextField)
			{
				JTextField field = (JTextField) e.getComponent ();
				field.selectAll ();
			}
		}

	}

	private Font defaultFont ()
	{
		Font textFieldFont = UIConstants.GEOPOD_VERDANA.deriveFont (Font.BOLD, 15.0f);
		
		return textFieldFont;
	}

	private void setInputText (JTextField field, String text)
	{
		// Method "setText" is thread safe so these calls needn't be on the EDT
		field.setText (text);
		field.setCaretPosition (0);
	}

	/**
	 * A default keylistener that checks to see if the lat/lon/alt values are valid
	 * on the release of each key.
	 * 
	 * @author Geopod team
	 *
	 */
	private class ChangeListener
			extends KeyAdapter
	{
		@Override
		public void keyReleased (KeyEvent e)
		{
			validateLatLonAlt ();
		}
	}
}
