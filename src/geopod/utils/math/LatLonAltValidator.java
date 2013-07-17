package geopod.utils.math;

import visad.VisADException;
import visad.georef.EarthLocation;
import visad.georef.EarthLocationLite;

public class LatLonAltValidator
{
	private static final boolean HAVE_LATITUDEMIN = true;
	private static final double LATITUDEMIN = -89.99;
	private static final boolean HAVE_LATITUDEMAX = true;
	private static final double LATITUDEMAX = 89.99;

	private static final boolean HAVE_LONGITUDEMIN = false;
	private static final double LONGITUDEMIN = 0;
	private static final boolean HAVE_LONGITUDEMAX = false;
	private static final double LONGITUDEMAX = 0;

	private static final boolean HAVE_ALTITUDEMIN = true;
	private static final double ALTITUDEMIN = -25000;
	private static final boolean HAVE_ALTITUDEMAX = false;
	private static final double ALTITUDEMAX = 0;

	private LatLonAltValidator ()
	{
		// static class, no public ctor
	}

	public static double getLatitudeValue (EarthLocation el)
	{
		return (VisadUtility.getLatitudeValue (el));
	}

	public static double getLongitudeValue (EarthLocation el)
	{
		double lon = VisadUtility.getLongitudeValue (el);

		return (getLongitudeValue (lon));
	}

	public static double getLongitudeValue (double longitude)
	{
		if (longitude > 180)
		{
			// Make sure longitude is within [-180, 180)
			double inRange = (longitude + 180) / 360;
			inRange -= Math.floor (inRange);
			longitude = inRange * 360 - 180;
		}
		return (longitude);
	}

	public static double getAltitudeValue (EarthLocation el)
	{
		return (VisadUtility.getAltitudeInMeter (el));
	}

	/**
	 * Returns an {@link EarthLocation} that represents the current values in
	 * the lat/lon/alt display.
	 * 
	 * @return the currently displayed EarthLocation, null if the current values
	 *         cannot be parsed.
	 */
	public static EarthLocation parseInputtedEarthLocation (String latitude, String longitude, String altitude)
	{
		EarthLocationLite location = null;

		double lat = MathUtility.parseDouble (latitude);
		double lon = MathUtility.parseDouble (longitude);
		double alt = MathUtility.parseDouble (altitude);

		try
		{
			location = new EarthLocationLite (lat, lon, alt);
		}
		catch (VisADException e)
		{
			location = null;
		}

		return (location);
	}

	/**
	 * Checks whether text representing is valid to the application
	 */
	public static boolean validLatitudeText (String latitudeText)
	{
		boolean isValid = true;

		double lat = MathUtility.parseDouble (latitudeText);

		if (Double.isNaN (lat) || (HAVE_LATITUDEMIN && lat < LATITUDEMIN) || (HAVE_LATITUDEMAX && lat > LATITUDEMAX))
		{
			isValid = false;
		}

		return (isValid);
	}

	/**
	 * Checks whether text representing is valid to the application
	 */
	public static boolean validLongitudeText (String longitudeText)
	{
		boolean isValid = true;

		double lon = MathUtility.parseDouble (longitudeText);

		isValid = validValue (lon, HAVE_LONGITUDEMIN, HAVE_LONGITUDEMAX, LONGITUDEMAX, LONGITUDEMIN);

		return (isValid);
	}

	/**
	 * Checks whether text representing altitude is valid to the application
	 */
	public static boolean validAltitudeText (String longitudeText)
	{
		boolean isValid = true;

		double alt = MathUtility.parseDouble (longitudeText);

		isValid = validValue (alt, HAVE_ALTITUDEMIN, HAVE_ALTITUDEMAX, ALTITUDEMAX, ALTITUDEMIN);

		return (isValid);
	}

	private static boolean validValue (double value, boolean haveMin, boolean haveMax, double min, double max)
	{
		// negate invalid values
		return !(Double.isNaN (value) || (haveMin && value < min) || (haveMax && value > max));
	}

	/**
	 * Clamps the latitude value if it is out of range. Returns the clamped
	 * value.
	 * 
	 * @param lat
	 *            - the latitude value to be clamped. If lat is NaN or inside
	 *            the range, This method returns lat.
	 */
	public static double clampLatitude (double lat)
	{
		return (clamp (lat, HAVE_LATITUDEMIN, HAVE_LATITUDEMAX, LATITUDEMIN, LATITUDEMAX));
	}

	/**
	 * Clamps the longitude value if it is out of range. Returns the clamped
	 * value.
	 * 
	 * @param lat
	 *            - the latitude value to be clamped. If lat is NaN or inside
	 *            the range, This method returns lat.
	 */
	public static double clampLongitude (double lat)
	{
		return (clamp (lat, HAVE_LONGITUDEMIN, HAVE_LONGITUDEMAX, LONGITUDEMIN, LONGITUDEMAX));
	}

	/**
	 * Clamps the altitude value if it is out of range. Returns the clamped
	 * value.
	 * 
	 * @param alt
	 *            - the altitude value to be clamped. If alt is NaN or inside
	 *            the range, This method returns alt.
	 */
	public static double clampAltitude (double alt)
	{
		return (clamp (alt, HAVE_ALTITUDEMIN, HAVE_ALTITUDEMAX, ALTITUDEMAX, ALTITUDEMIN));
	}

	private static double clamp (double value, boolean haveMin, boolean haveMax, double min, double max)
	{
		if (haveMin && value < min)
		{
			value = LATITUDEMIN;
		}
		else if (haveMax && value > max)
		{
			value = LATITUDEMAX;
		}
		return (value);
	}

	/**
	 * Checks whether lat is valid to the application
	 */
	public boolean validAltitude (String longitudeText)
	{
		boolean isValid = true;

		double lon = MathUtility.parseDouble (longitudeText);

		if (Double.isNaN (lon) || (HAVE_ALTITUDEMIN && lon < ALTITUDEMIN) || (HAVE_ALTITUDEMAX && lon > ALTITUDEMAX))
		{
			isValid = false;
		}

		return (isValid);
	}
}
