package geopod.devices;

import geopod.ConfigurationManager;
import geopod.constants.parameters.IDV4ParameterConstants;
import geopod.constants.parameters.ParameterUtil;
import geopod.utils.coordinate.IdvCoordinateUtility;
import geopod.utils.idv.VerticalProfileUtility;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

import ucar.unidata.data.grid.GridDataInstance;
import ucar.unidata.data.grid.GridUtil;
import visad.FieldImpl;
import visad.FlatField;
import visad.Real;
import visad.VisADException;
import visad.georef.EarthLocation;
import visad.georef.LatLonPoint;

/**
 * A class to imitate a physical dropsonde and create a vertical profile of the
 * atmosphere.
 * 
 * @author Geopod Team
 * 
 */
public class Dropsonde
{

	//private static final java.util.Set<String> DEFAULT_DROPSONDE_PARAMETERS;
	/*
		static
		{
			// GRIB 1 names
			DEFAULT_DROPSONDE_PARAMETERS = ParameterListConstants.getDefaultDropsondeParameters (); // new java.util.HashSet<String> ();
			/*
			DEFAULT_DROPSONDE_PARAMETERS.add ("Temperature @ isobaric");
			DEFAULT_DROPSONDE_PARAMETERS.add ("Dewpoint (from Temperature & Relative_humidity)");
			DEFAULT_DROPSONDE_PARAMETERS.add ("u_wind @ isobaric");
			DEFAULT_DROPSONDE_PARAMETERS.add ("v_wind @ isobaric");
			DEFAULT_DROPSONDE_PARAMETERS.add ("Relative_humidity @ isobaric");
			// theta-e == Equiv. Potential Temperature
			DEFAULT_DROPSONDE_PARAMETERS.add ("Equiv. Potential Temperature (from Temperature & Relative_humidity)");

			// GRIB 2 names
			DEFAULT_DROPSONDE_PARAMETERS.add ("Temperature @ pressure");
			DEFAULT_DROPSONDE_PARAMETERS.add ("Dew_point_temperature @ pressure");
			DEFAULT_DROPSONDE_PARAMETERS.add ("U-component_of_wind @ pressure");
			DEFAULT_DROPSONDE_PARAMETERS.add ("V-component_of_wind @ pressure");
			DEFAULT_DROPSONDE_PARAMETERS.add ("Relative_humidity @ pressure");
			DEFAULT_DROPSONDE_PARAMETERS.add ("Potential_temperature @ height_above_ground");
			*/
	/*
	DEFAULT_DROPSONDE_PARAMETERS.addAll (ParameterListConstants.TEMPERATURE);
	DEFAULT_DROPSONDE_PARAMETERS.addAll (ParameterListConstants.DEWPOINT_D);
	DEFAULT_DROPSONDE_PARAMETERS.addAll (ParameterListConstants.DEWPOINT_TEMPERATURE);
	DEFAULT_DROPSONDE_PARAMETERS.addAll (ParameterListConstants.U_WIND);
	DEFAULT_DROPSONDE_PARAMETERS.addAll (ParameterListConstants.V_WIND);
	DEFAULT_DROPSONDE_PARAMETERS.addAll (ParameterListConstants.RELATIVE_HUMIDITY);
	DEFAULT_DROPSONDE_PARAMETERS.addAll (ParameterListConstants.RELATIVE_HUMIDITY_D);
	DEFAULT_DROPSONDE_PARAMETERS.addAll (ParameterListConstants.POTENTIAL_TEMPERATURE);
	DEFAULT_DROPSONDE_PARAMETERS.addAll (ParameterListConstants.POTENTIAL_TEMPERATURE_D);
	DEFAULT_DROPSONDE_PARAMETERS.addAll (ParameterListConstants.EQUIVALENT_POTENTIAL_TEMPERATURE_D);
	
	}
	*/

	private LatLonPoint m_launchLocation;
	private Real m_launchTime;

	/**
	 * The sensors the dropsonde gets samples with.
	 */
	private Map<String, Sensor> m_sensorMap;

	private Map<String, FlatField> m_dataMap;

	// Initialization block (copied to all constructors).
	{
		m_sensorMap = new java.util.HashMap<String, Sensor> ();
		m_dataMap = new java.util.HashMap<String, FlatField> ();

		// These are set in the launch method.
		m_launchLocation = null;
		m_launchTime = null;
	}

	/**
	 * Create a Dropsonde that will collect data with the given {@link Sensor
	 * sensor}.
	 * 
	 * @param sensor
	 *            - the sensor used to collect data.
	 */
	public Dropsonde (Sensor sensor)
	{
		// Store the sensor
		m_sensorMap.put (sensor.getSimpleName (), sensor);
	}

	/**
	 * Create a Dropsonde that will collect data with several {@link Sensor
	 * sensors}.
	 * 
	 * @param sensors
	 *            - the array of sensors to collect data from.
	 */
	public Dropsonde (Sensor[] sensors)
	{
		for (Sensor sensor : sensors)
		{
			m_sensorMap.put (sensor.getSimpleName (), sensor);
		}
	}

	/**
	 * Create a Dropsonde that will collect data with several {@link Sensor
	 * sensors}.
	 * 
	 * @param sensors
	 *            - a map of sensors to use
	 */
	public Dropsonde (Map<String, Sensor> sensors)
	{
		m_sensorMap.putAll (sensors);
	}

	public boolean isUsingPressureForDomain ()
	{
		// Use a log axis when using pressure as the domain.
		String configString = ConfigurationManager.getProperty (ConfigurationManager.ChartDomainUnit);
		return (configString.equals ("Pressure"));
	}

	/**
	 * Launch the Dropsonde.
	 * 
	 * @param elt
	 *            - the earth location to launch the dropsonde at.
	 * @param time
	 *            - the time to sample at.
	 * @throws VisADException
	 * @throws RemoteException
	 */
	public void launch (EarthLocation elt, Real time)
			throws VisADException, RemoteException
	{
		// Discard altitude, as we are taking a vertical profile.
		m_launchLocation = elt.getLatLonPoint ();
		m_launchTime = time;

		collectData (m_launchLocation, m_launchTime);
	}

	/**
	 * Collect data at the specified point.
	 * 
	 * @param llp
	 *            - the lat/lon point to sample at.
	 * @param time
	 *            - the time to sample at.
	 * 
	 * @throws RemoteException
	 * @throws VisADException
	 */
	private void collectData (LatLonPoint llp, Real time)
			throws RemoteException, VisADException
	{
		// Populate the data map.
		for (Map.Entry<String, Sensor> entry : m_sensorMap.entrySet ())
		{
			FlatField data = createVerticalProfile (entry.getValue (), llp, time);
			m_dataMap.put (entry.getKey (), data);
		}
	}

	/**
	 * Create a vertical profile with a sensor at the given point and time.
	 * 
	 * @param sensor
	 *            - the sensor to sample with.
	 * @param llp
	 *            - the lat/lon point to sample at.
	 * @param time
	 *            - the time to sample at.
	 * 
	 * @return a FlatField containing a vertical profile.
	 * 
	 * @throws VisADException
	 * @throws RemoteException
	 */
	private FlatField createVerticalProfile (Sensor sensor, LatLonPoint llp, Real time)
			throws VisADException, RemoteException
	{
		FieldImpl grid = ((GridDataInstance) sensor.getDataInstance ()).getGrid ();
		FieldImpl profileData = GridUtil.getProfileAtLatLonPoint (grid, llp);

		FlatField oneTime = null;
		FieldImpl profile;
		if (profileData != null)
		{
			// Don't convert the units if
			boolean convertUnits = !isUsingPressureForDomain ();
			profile = VerticalProfileUtility.makeProfile (profileData, convertUnits);

			boolean isTimeSequence = GridUtil.isTimeSequence (profile);

			if (isTimeSequence)
			{
				if (time == null)
				{
					// There is no specified time, so get the first sample.
					oneTime = (FlatField) profile.getSample (0);
				}
				else
				{
					// Get the sample from the specified time.
					oneTime = (FlatField) profile.evaluate (time);
				}
			}
			else
			{
				// The profile is not a time sequence, so cast it to a FlatField.
				oneTime = (FlatField) profile;
			}

		}
		return (oneTime);
	}

	/**
	 * Get the data collected by the dropsonde.
	 * 
	 * @param parameterName
	 * 
	 * @return a flat field containing the dropsonde data.
	 */
	public FlatField getData (String parameterName)
	{
		return (m_dataMap.get (parameterName));
	}

	/**
	 * 
	 * @return the names of the sensors used by the dropsonde.
	 */
	public Set<String> getNames ()
	{
		return (m_sensorMap.keySet ());
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public String toString ()
	{
		return (IdvCoordinateUtility.getNormalizedString (m_launchLocation));
	}

	/**
	 * @return true if the dropsonde has collected data.
	 */
	public boolean hasData ()
	{
		try
		{
			// Check each FlatField to check if at least one has usable data.
			for (FlatField flatField : m_dataMap.values ())
			{
				// If the FlatField exists and contains something other then NaNs, assume it
				// is usable data.
				if (flatField != null && !Double.isNaN (flatField.getFloats (false)[0][0]))
				{
					return (true);
				}
			}
		}
		catch (VisADException exp)
		{
			// Fall through and return false.
		}

		return (false);
	}

	/**
	 * @return a set of parameter names used by a default dropsonde.
	 */
	public static java.util.Set<String> getDefaultParameterNames ()
	{
		//return (DEFAULT_DROPSONDE_PARAMETERS);
		//return ParameterListConstants.getDefaultDropsondeParameters ();
		//return IDV4ParameterConstants.getDefaultDropsondeParameters ();
		return ParameterUtil.getDefaultDropsondeParameters ();
	}

	public LatLonPoint getLauchLocation ()
	{
		return m_launchLocation;
	}

	public boolean equals (Dropsonde other)
	{
		return m_launchLocation.equals (other.getLauchLocation ());
	}
}
