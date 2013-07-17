package geopod.eventsystem.events;

import visad.georef.EarthLocation;
import visad.georef.EarthLocationLite;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ParticleImageChangedEvent")
public class ParticleImageChangedEvent
		extends FlightEvent
{
	@XStreamAlias("lat")
	private double m_latitude;

	@XStreamAlias("lon")
	private double m_longitude;

	@XStreamAlias("alt")
	private double m_altitude;

	@XStreamAlias("ParticleImageCategory")
	private String m_particleCategory;

	/**
	 * No-args constructor for XStream serialization. Do not use.
	 */
	public ParticleImageChangedEvent ()
	{

	}

	public ParticleImageChangedEvent (long time, double lat, double lon, double alt, String categoryName)
	{
		m_time = time;
		m_latitude = lat;
		m_longitude = lon;
		m_altitude = alt;
		m_particleCategory = categoryName;
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

	public String getParticleCategory ()
	{
		return (m_particleCategory);
	}

	public EarthLocation getEarthLocation ()
	{
		EarthLocation earthLocation = null;
		try
		{
			earthLocation = new EarthLocationLite (m_latitude, m_longitude, m_altitude);
		}
		catch (Exception e)
		{
			e.printStackTrace ();
		}
		return (earthLocation);
	}

	@Override
	public GeopodEventId getEventType ()
	{
		return (GeopodEventId.PARTICLE_IMAGED);
	}

}
