package geopod.eventsystem.events;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import geopod.eventsystem.events.GeopodEventId;

public abstract class FlightEvent
{
	@XStreamAlias("time")
	protected long m_time;

	public long getTime ()
	{
		return (m_time);
	}

	public abstract GeopodEventId getEventType ();
}