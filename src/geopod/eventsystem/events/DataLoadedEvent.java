package geopod.eventsystem.events;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("DataLoadedEvent")
public class DataLoadedEvent
		extends FlightEvent
{
	@XStreamAlias("DataSourceName")
	private String m_dataSourceName;

	/**
	 * No-args constructor for XStream serialization. Do not use.
	 */
	public DataLoadedEvent ()
	{

	}

	public DataLoadedEvent (long time, String dataSourceName)
	{
		m_time = time;
		m_dataSourceName = dataSourceName;
	}

	public String getDataSourceName ()
	{
		return (m_dataSourceName);
	}

	@Override
	public GeopodEventId getEventType ()
	{
		return (GeopodEventId.ALL_CHOICES_LOADING_FINISHED);
	}
}
