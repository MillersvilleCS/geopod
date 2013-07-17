package geopod.eventsystem.events;

import java.util.Map;

import visad.Real;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("LocationNotedEvent")
public class LocationNotedEvent
		extends FlightEvent
{
	@XStreamAlias("lat")
	private double m_latitude;

	@XStreamAlias("lon")
	private double m_longitude;

	@XStreamAlias("alt")
	private double m_altitude;

	@XStreamAlias("parameters")
	private Map<String, Real> m_parameters;

	/**
	 * No-args constructor for XStream serialization. Do not use.
	 */
	public LocationNotedEvent ()
	{

	}

	public LocationNotedEvent (long time, double lat, double lon, double alt, Map<String, Real> dataMap)
	{
		m_time = time;
		m_latitude = lat;
		m_longitude = lon;
		m_altitude = alt;
		m_parameters = dataMap;
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

	public Map<String, Real> getParameters ()
	{
		return (m_parameters);
	}

	@Override
	public GeopodEventId getEventType ()
	{
		return (GeopodEventId.LOCATION_NOTED);
	}

}
