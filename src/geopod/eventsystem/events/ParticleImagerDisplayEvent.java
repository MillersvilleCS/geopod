package geopod.eventsystem.events;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ParticleImagerDisplayEvent")
public class ParticleImagerDisplayEvent
		extends FlightEvent
{
	@XStreamAlias("DisplayEventType")
	private GeopodEventId m_particleDisplayEventType;

	/**
	 * No-args constructor for XStream serialization. Do not use.
	 */
	public ParticleImagerDisplayEvent ()
	{

	}

	public ParticleImagerDisplayEvent (long time, GeopodEventId eventType)
	{
		m_time = time;
		m_particleDisplayEventType = eventType;
	}

	@Override
	public GeopodEventId getEventType ()
	{
		return (m_particleDisplayEventType);
	}
}
