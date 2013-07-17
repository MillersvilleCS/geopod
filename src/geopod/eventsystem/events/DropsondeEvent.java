package geopod.eventsystem.events;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("DropsondeLaunchedEvent")
public class DropsondeEvent
		extends FlightEvent
{
	@XStreamAlias("lat")
	private double m_latitude;

	@XStreamAlias("lon")
	private double m_longitude;

	/**
	 * No-args constructor for XStream serialization. Do not use.
	 */
	public DropsondeEvent ()
	{

	}

	public DropsondeEvent (long time, double lat, double lon)
	{
		m_time = time;
		m_latitude = lat;
		m_longitude = lon;
	}

	public double getLatitude ()
	{
		return (m_latitude);
	}

	public double getLongitude ()
	{
		return (m_longitude);
	}

	@Override
	public GeopodEventId getEventType ()
	{
		return (GeopodEventId.DROPSONDE_LAUNCHED);
	}
}
