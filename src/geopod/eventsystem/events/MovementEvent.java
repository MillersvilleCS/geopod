package geopod.eventsystem.events;

import javax.vecmath.Quat4d;

import visad.VisADException;
import visad.georef.EarthLocation;
import visad.georef.EarthLocationLite;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("MovementEvent")
public class MovementEvent
		extends FlightEvent
{
	@XStreamAlias("lat")
	private double m_latitude;

	@XStreamAlias("lon")
	private double m_longitude;

	@XStreamAlias("alt")
	private double m_altitude;

	@XStreamAlias("quaternion")
	private Quat4d m_quat;

	/**
	 * No-args constructor for XStream serialization. Do not use.
	 */
	public MovementEvent ()
	{
		
	}

	public MovementEvent (long time, double lat, double lon, double alt, Quat4d quat)
	{
		m_time = time;
		m_latitude = lat;
		m_longitude = lon;
		m_altitude = alt;
		m_quat = quat;
	}

	public double getLatitude ()
	{
		return (m_latitude);
	}

	public double getLongitude ()
	{
		return (m_longitude);
	}

	public double getAltitude ()
	{
		return (m_altitude);
	}

	public Quat4d getRotation ()
	{
		return (m_quat);
	}

	public EarthLocation getEarthLocation ()
	{
		EarthLocation earthLocation = null;
		try
		{
			earthLocation = new EarthLocationLite (m_latitude, m_longitude, m_altitude);
		}
		catch (VisADException e)
		{
			e.printStackTrace ();
		}
		return (earthLocation);
	}

	@Override
	public GeopodEventId getEventType ()
	{
		return (GeopodEventId.GEOPOD_TRANSLATED);
	}
}
