package geopod.constants.parameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WRFParameterUtil
{
	private WRFParameterUtil ()
	{
		// Static class. No instantiation.
	}

	public static final String DEWPOINT = "Dewpoint (from Temperature & Relative_humidity)";
	public static final String DEWPOINT_TEMPERATURE = "Dew_point_temperature @ isobaric";
	public static final String SPEED = "Speed (from u_wind & v_wind)";
	public static final String GEOPOTENTIAL_HEIGHT = "Geopotential_height @ isobaric";
	public static final String TEMPERATURE = "Temperature @ isobaric";
	public static final String RELATIVE_HUMIDITY = "Relative Humidity (from Temperature & mixingratio)";
	public static final String MIXING_RATIO = "Mixing ratio (from Temperature & Relative_humidity)";
	public static final String U_WIND = "u_wind @ isobaric";
	public static final String V_WIND = "v_wind @ isobaric";
	public static final String EQUIVALENT_POTENTIAL_TEMPERATURE = "Equiv. Potential Temperature (from Temperature & Relative_humidity)";
	public static final String POTENTIAL_TEMPERATURE = "Potential Temperature (from Temperature)";

	/**
	 * Return a List of parameter names from a WRF dataset that Geopod should
	 * load on default.
	 * 
	 * @return
	 */
	public static final List<String> getDefaultGeopodParameters ()
	{
		List<String> params = new ArrayList<String> ();
		{
			params.add (SPEED);
			params.add (GEOPOTENTIAL_HEIGHT);

			params.add (MIXING_RATIO);

			// RELATIVE_HUMIDITY
			params.addAll (getDropsondeRHParameters ());

			// TEMPERATURE, DEWPOINT, & DEWPOINT_TEMPERATURE
			params.addAll (getDropsondeTDewParameters ());
		}
		return Collections.unmodifiableList (params);
	}

	/**
	 * Return a List of parameter names from a WRF dataset that Geopod should
	 * load on default.
	 * 
	 * @return
	 */
	public static final List<String> getDefaultDropsondeParameters ()
	{
		List<String> params = new ArrayList<String> ();
		{
			// TEMPERATURE, DEWPOINT, & DEWPOINT_TEMPERATURE
			params.addAll (getDropsondeTDewParameters ());

			// U_WIND & V_WIND
			params.addAll (getDropsondeWindParameters ());

			// RELATIVE_HUMIDITY
			params.addAll (getDropsondeRHParameters ());

			// EQUIVALENT_POTENTIAL_TEMPERATURE & POTENTIAL_TEMPERATURE);
			params.addAll (getDropsondeThetaEParameters ());
		}
		return Collections.unmodifiableList (params);
	}

	public static final List<String> getDropsondeTDewParameters ()
	{
		List<String> params = new ArrayList<String> ();
		{
			params.add (TEMPERATURE);
			params.add (DEWPOINT);
			params.add (DEWPOINT_TEMPERATURE);
		}
		return Collections.unmodifiableList (params);
	}

	public static final List<String> getDropsondeWindParameters ()
	{
		List<String> params = new ArrayList<String> ();
		{
			params.add (U_WIND);
			params.add (V_WIND);
		}
		return Collections.unmodifiableList (params);
	}

	public static final List<String> getDropsondeRHParameters ()
	{
		List<String> params = new ArrayList<String> ();
		{
			params.add (RELATIVE_HUMIDITY);
		}
		return Collections.unmodifiableList (params);
	}

	public static final List<String> getDropsondeThetaEParameters ()
	{
		List<String> params = new ArrayList<String> ();
		{
			params.add (EQUIVALENT_POTENTIAL_TEMPERATURE);
			params.add (POTENTIAL_TEMPERATURE);
		}
		return Collections.unmodifiableList (params);
	}

}
