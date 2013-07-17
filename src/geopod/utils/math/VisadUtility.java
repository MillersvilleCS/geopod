package geopod.utils.math;

import ucar.visad.quantities.CommonUnits;
import visad.Real;
import visad.RealType;
import visad.SI;
import visad.Unit;
import visad.VisADException;
import visad.georef.EarthLocation;

/**
 * A collection of convenience methods for doing routine calculations with
 * {@link Visad} measurement utilities. This class provides methods the do most
 * of the heavy lifting when we need to interface with {@link Visad}
 * 
 * @author Millersville Geopod Team
 * 
 */
public class VisadUtility
{
	private VisadUtility ()
	{
		// static class
	}

	/**
	 * Convert a {@link Real} value to the specified units. If conversion cannot
	 * take plane, NaN is returned.
	 * 
	 * @param realIn
	 *            - the input {@link Real}
	 * @param unitOut
	 *            - convert the Real's value to this unit
	 * @return
	 */
	public static double getValue (Real realIn, Unit unitOut)
	{
		double value = Double.NaN;
		try
		{
			value = realIn.getValue (unitOut);
		}
		catch (VisADException e)
		{
			System.err.println ("Bad unit conversion in VisadUtility, getValue (Real, Unit) method");
			e.printStackTrace ();
		}
		return (value);

	}

	/**
	 * Convert a value interpreted to have the specified units into the desired
	 * output units. The converted input value is returned. If conversion cannot
	 * take plane, NaN is returned.
	 * 
	 * @param real
	 *            - the real value
	 * @param unitOut
	 *            - convert the Real's value to this unit
	 * @return the value of the converted input value as a double
	 */
	public static double getValue (double valueIn, Unit unitIn, Unit unitOut)
	{
		double value = Double.NaN;
		try
		{
			Real tmp = new Real (RealType.Generic, unitIn.toThat (valueIn, unitOut), unitOut);
			value = tmp.getValue ();
		}
		catch (VisADException e)
		{
			System.err.println ("Bad unit conversion in VisadUtility, getValue (Real, Unit) method");
			e.printStackTrace ();
		}

		return (value);
	}

	/**
	 * Convert a value interpreted to have the specified units into the desired
	 * output units. The converted value is returned as a {@link Real}. If
	 * conversion cannot take plane, NaN is returned.
	 * 
	 * @param real
	 *            - the real value
	 * @param unitOut
	 *            - convert the Real's value to this unit
	 * @return the real value of the converted input value as a double
	 */
	public static Real getReal (double valueIn, Unit unitIn, Unit unitOut)
	{
		Real realValue = new Real (Double.NaN);
		try
		{
			realValue = new Real (RealType.Generic, unitIn.toThat (valueIn, unitOut), unitOut);
		}
		catch (VisADException e)
		{
			System.err.println ("Bad unit conversion in VisadUtility, getValue (Real, Unit) method");
			e.printStackTrace ();
		}
		return (realValue);
	}

	/**
	 * Converts the input {@link Real} to the specified output {@link Unit} and
	 * returns the converted real. If the input {@link Real} cannot be
	 * converted, it is returned as an undefined {@link Real}, aka NaN.
	 * 
	 * @param real
	 *            - the {@link Real} to be converted
	 * @param unitOut
	 *            - the output unit
	 * @return the converted {@line Real}
	 */
	public static Real convert (Real real, Unit unitOut)
	{
		Real value = new Real (Double.NaN);
		try
		{
			value = new Real ((RealType) real.getType (), real.getValue (unitOut), unitOut);
		}
		catch (VisADException e)
		{
			System.err.println ("Bad unit conversion in VisadUtility, convert (Real, Unit) method.");
			e.printStackTrace ();
		}
		return value;
	}

	public static Real constructReal (double value, Unit unit)
	{
		Real real = new Real (Double.NaN);
		try
		{
			real = new Real (RealType.Generic, value, unit);
		}
		catch (VisADException e)
		{
			System.err.println ("Could not construct real with specified value and units.");
			e.printStackTrace ();
		}
		return (real);
	}

	/**
	 * A helper method used to convert the units of specified parameters.
	 * 
	 * @param parameterName
	 * @param real
	 * @return - the Converted real value of the parameter
	 */
	public static Real convertParameterValue (String parameterName, Real real)
	{
		String lowerParameterName = parameterName.toLowerCase ();
		if (lowerParameterName.startsWith ("temperature"))
		{
			real = convert (real, CommonUnits.CELSIUS);
		}
		return real;
	}

	/**
	 * Gets the value of the latitude portion of given {@link EarthLocation} in
	 * degrees.
	 * 
	 * @param el
	 * @return the value of the latitude portion. Returns NaN if an exception is
	 *         thrown.
	 */
	public static double getLatitudeInDegrees (EarthLocation el)
	{
		Unit degree = CommonUnits.DEGREE;
		Real latitude = el.getLatitude ();
		return getValue (latitude, degree);
	}

	public static double getLatitudeValue (EarthLocation el)
	{
		return el.getLatitude ().getValue ();
	}

	/**
	 * Gets the String representation of the given EarthLocation's latitude
	 * part.
	 * 
	 * @param el
	 *            - the given EarthLocation
	 * @return the latitude unit as a String.
	 */
	public static String getLatitudeUnitIdentifier (EarthLocation el)
	{
		return (el.getLatitude ().getUnit ().toString ());
	}

	/**
	 * Gets the value of the longitude portion of given {@link EarthLocation} in
	 * degrees.
	 * 
	 * @param el
	 * @return the value of the longitude portion. Returns NaN if an exception
	 *         is thrown.
	 */
	public static double getLongitudeInDegrees (EarthLocation el)
	{
		Unit degree = CommonUnits.DEGREE;
		Real altitude = el.getAltitude ();
		return (getValue (altitude, degree));
	}

	public static double getLongitudeValue (EarthLocation el)
	{
		return el.getLongitude ().getValue ();
	}

	/**
	 * 
	 * @param el
	 * @return
	 */
	public static String getLongitudeValueString (EarthLocation el)
	{
		return (el.getLongitude ().toValueString ());
	}

	/**
	 * Gets the String representation of the given EarthLocation's longitude
	 * part.
	 * 
	 * @param el
	 *            - the given EarthLocation
	 * @return the altitude unit as a String.
	 */
	public static String getLongitudeUnitIdentifier (EarthLocation el)
	{
		return (el.getLongitude ().getUnit ().toString ());
	}

	/**
	 * Gets the value of the altitude portion of given {@link EarthLocation} in
	 * km. If the altitude cannot be converted, NaN is returned.
	 * 
	 * @param el
	 *            - the given EarthLocation
	 * @return the value of the altitude portion in km.
	 */
	public static double getAltitudeInKm (EarthLocation el)
	{
		Real altitude = el.getAltitude ();
		return getValue (altitude, CommonUnits.KILOMETER);
	}

	/**
	 * Gets the value
	 * 
	 * @param el
	 * @return
	 */
	public static double getAltitudeValue (EarthLocation el)
	{
		return (el.getAltitude ().getValue ());
	}

	public static double getAltitudeInMeter (EarthLocation el)
	{
		Real altitude = el.getAltitude ();
		return getValue (altitude, SI.meter);
	}

	/**
	 * Gets the String representation of the given EarthLocation's altitude
	 * part.
	 * 
	 * @param el
	 *            - the given {@link EarthLocation}
	 * @return the altitude unit as a String.
	 */
	public static String getAltitudeUnitIdentifier (EarthLocation el)
	{
		return (el.getAltitude ().getUnit ().toString ());
	}

	/**
	 * Gets the String representation of the value and unit semantics for
	 * altitude portion of the given {@link EarthLocation}.
	 * 
	 * @param el
	 *            -the given EarthLocation
	 * @return the value and unit as a String
	 */
	public static String getAltitudeValueString (EarthLocation el)
	{
		return (el.getAltitude ().toValueString ());
	}

	/**************************************************************************************/
	// Component utilities related to Visad quantities and Java components, such 
	// as text fields.

	public static String latitudeToolTipText (String header, EarthLocation el)
	{
		return (header + " (" + getLatitudeUnitIdentifier (el) + ")");
	}

	public static String longitudeToolTipText (String header, EarthLocation el)
	{
		return (header + " (" + getLongitudeUnitIdentifier (el) + ")");
	}

	public static String altitudeToolTipText (String header, EarthLocation el)
	{
		return (header + " (" + getAltitudeUnitIdentifier (el) + ")");
	}
}
