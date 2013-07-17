package geopod.devices;

import geopod.ConfigurationManager;
import geopod.Geopod;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.events.DataLoadedEvent;
import geopod.eventsystem.events.DropsondeEvent;
import geopod.eventsystem.events.FlightEvent;
import geopod.eventsystem.events.GeopodEventId;
import geopod.eventsystem.events.LocationNotedEvent;
import geopod.eventsystem.events.MovementEvent;
import geopod.eventsystem.events.ParticleImageChangedEvent;
import geopod.eventsystem.events.ParticleImagerDisplayEvent;
import geopod.utils.debug.Debug;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Quat4d;

import visad.Real;
import visad.georef.EarthLocation;

import com.google.common.collect.ArrayListMultimap;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Records certain flight events which may be of interest to instructors. This
 * record can later be used to verify that assessment conditions have been met
 * or provide necessary information to recreate flight path. Currently, a
 * FlightDataRecorder records the loading of a data set, movement of the Geopod,
 * opening and closing of the particle imager, particle categories imaged,
 * deployment of dropsondes, and locations noted.
 * 
 * @author Geopod Team
 * 
 */
@XStreamAlias("FlightRecord")
public class FlightDataRecorder
		implements IObserver

{
	private static final int NUM_EVENT_TYPES_RECORDED;
	static
	{
		NUM_EVENT_TYPES_RECORDED = GeopodEventId.values ().length;
	}

	@XStreamOmitField
	private long m_startTime;

	@XStreamOmitField
	private Geopod m_geopod;

	@XStreamAlias("FlightEvents")
	private List<FlightEvent> m_flightEvents;

	@XStreamAlias("IndicesList")
	private ArrayListMultimap<GeopodEventId, Integer> m_eventsByType;

	@XStreamOmitField
	private boolean m_recordingEnabled;

	@XStreamOmitField
	private boolean m_recordingLockedOff;

	/**
	 * For serialization Don't use in code
	 */
	public FlightDataRecorder ()
	{

	}

	/**
	 * Creates a new FlightDataRecorder, which uses <b>geopod</b> to get
	 * information associated with an event
	 * 
	 * @param geopod
	 *            - The geopod used to get event information
	 */
	public FlightDataRecorder (Geopod geopod)
	{
		initilizeFlightRecorder (geopod);
		resetLog ();
	}

	/**
	 * Initialize fields if using default constructor
	 * 
	 * @param geopod
	 */
	public void initilizeFlightRecorder (Geopod geopod)
	{
		m_geopod = geopod;
		m_recordingLockedOff = false;
		boolean recordingEnabled = ConfigurationManager.isEnabled (ConfigurationManager.RecordFlightPath);
		setRecordingEnabled (recordingEnabled);
		ConfigurationManager.addPropertyChangeListener (ConfigurationManager.RecordFlightPath,
				new PropertyChangeListener ()
				{

					@Override
					public void propertyChange (PropertyChangeEvent evt)
					{
						setRecordingEnabled (ConfigurationManager.isEnabled (ConfigurationManager.RecordFlightPath));
					}

				});
		Debug.println ("Flight Log Recording? " + m_recordingEnabled);
		m_startTime = System.currentTimeMillis ();
	}

	@Override
	public void handleNotification (GeopodEventId eventType)
	{
		if (m_recordingEnabled && !m_recordingLockedOff)
		{
			long time = System.currentTimeMillis () - m_startTime;

			FlightEvent flightEvent = null;

			EarthLocation currentLocation = m_geopod.getEarthLocation ();
			double lat = currentLocation.getLatitude ().getValue ();
			double lon = currentLocation.getLongitude ().getValue ();
			double alt = currentLocation.getAltitude ().getValue ();

			switch (eventType)
			{
			case GEOPOD_TRANSLATED:
				Quat4d rotation = m_geopod.getWorldPose ().getRotation ();
				flightEvent = new MovementEvent (time, lat, lon, alt, rotation);
				break;
			case PARTICLE_IMAGED:
				String categoryName = m_geopod.getCurrentCategoryFromParticleImager ();
				flightEvent = new ParticleImageChangedEvent (time, lat, lon, alt, categoryName);
				break;
			case PARTICLE_IMAGER_OPENED:
			case PARTICLE_IMAGER_CLOSED:
				flightEvent = new ParticleImagerDisplayEvent (time, eventType);
				break;
			case DROPSONDE_LAUNCHED:
				flightEvent = new DropsondeEvent (time, lat, lon);
				break;
			case ALL_CHOICES_LOADING_FINISHED:
				String dataSource = m_geopod.extractCurrentDataSourceName ();
				flightEvent = new DataLoadedEvent (time, dataSource);
				break;
			case LOCATION_NOTED:
				Map<String, Real> parameters = m_geopod.getCurrentSensorValues ();
				flightEvent = new LocationNotedEvent (time, lat, lon, alt, parameters);
				break;
			}

			if (flightEvent != null)
			{
				m_flightEvents.add (flightEvent); // Add to the end of the list
				GeopodEventId eventId = flightEvent.getEventType ();
				m_eventsByType.get (eventId).add (m_flightEvents.size () - 1);
			}
			else
			{
				throw new RuntimeException ("flightEvent was null.");
			}
		}

	}

	/**
	 * Prints a flight log to console. Useful for debugging purposes. Call to
	 * this method must be Debug protected.
	 */
	public void printLog ()
	{
		System.out.println ("*********************************** flight log ***********************************");
		for (int i = 0; i < m_flightEvents.size (); i++)
		{
			FlightEvent flightEvent = m_flightEvents.get (i);
			GeopodEventId eventType = flightEvent.getEventType ();
			if (eventType == GeopodEventId.GEOPOD_TRANSLATED)
			{
				MovementEvent event = (MovementEvent) flightEvent;
				System.out.println ("Movement Event recorded at time: " + event.getTime () + " lat: "
						+ event.getLatitude () + " lon: " + event.getLongitude () + " alt: " + event.getAltitude ());
			}
			else if (eventType == GeopodEventId.PARTICLE_IMAGED)
			{
				ParticleImageChangedEvent event = (ParticleImageChangedEvent) flightEvent;
				System.out.println ("Particle imaged at time: " + event.getTime () + " lat: " + event.getLatitude ()
						+ " lon: " + event.getLongitude () + " alt: " + event.getAltitude () + " category:  "
						+ event.getParticleCategory ());
			}
			else if (eventType == GeopodEventId.PARTICLE_IMAGER_OPENED
					|| eventType == GeopodEventId.PARTICLE_IMAGER_CLOSED)
			{
				ParticleImagerDisplayEvent event = (ParticleImagerDisplayEvent) flightEvent;
				System.out.println ("Particle Display event recorded at time: " + event.getTime ()
						+ " display event type: " + event.getEventType ());
			}
			else if (eventType == GeopodEventId.DROPSONDE_LAUNCHED)
			{
				DropsondeEvent event = (DropsondeEvent) flightEvent;
				System.out.println ("Dropsonde recorded at time: " + event.getTime () + " lat: " + event.getLatitude ()
						+ " lon: " + event.getLongitude ());
			}
			else if (eventType == GeopodEventId.ALL_CHOICES_LOADING_FINISHED)
			{
				DataLoadedEvent event = (DataLoadedEvent) flightEvent;
				System.out.println ("Dataset Loaded at time:" + event.getTime () + " data Source is "
						+ event.getDataSourceName ());
			}
			else if (eventType == GeopodEventId.LOCATION_NOTED)
			{
				LocationNotedEvent event = (LocationNotedEvent) flightEvent;
				System.out.println ("Location Noted at time: " + event.getTime () + " lat " + event.getLatitude ()
						+ " lon " + event.getLongitude () + " alt " + event.getAltitude ());
			}
		}
		System.out.println ("*********************************** end flight log ***********************************");

	}

	/**
	 * Returns the number of times a specific type of flight event has occurred,
	 * given a GeopodEventId which identifies the type of the event
	 * 
	 * @param eventId
	 *            - GeopodEventId which identifies the type of the event
	 * @return the number of times the event has occurred
	 */
	public int getNumberOfOccurrences (GeopodEventId eventId)
	{
		return (m_eventsByType.get (eventId).size ());
	}

	/**
	 * Returns a list of all events in the record of a specific type
	 * (<b>eventType</b>).
	 * 
	 * @param eventType
	 *            - the type of the event
	 * @return A list of all events of type <b>eventType</b>
	 */
	public List<FlightEvent> findOccurencesOfEvent (GeopodEventId eventType)
	{
		List<Integer> locations = getLocations (eventType);
		List<FlightEvent> events = new ArrayList<FlightEvent> ();
		for (int i = 0; i < locations.size (); i++)
		{
			FlightEvent flightEvent = m_flightEvents.get (locations.get (i));
			events.add (flightEvent);
		}
		return (events);
	}

	/**
	 * Returns a list of all events that the FlightDataRecorder has recorded.
	 * 
	 * @return list of all recorded events
	 */
	public List<FlightEvent> getAllEvents ()
	{
		return (m_flightEvents);
	}

	/**
	 * Turns flight recording on or off.
	 * 
	 * @param record
	 *            - whether or not the FlightDataRecorder should record events
	 */
	public void setRecordingEnabled (boolean record)
	{
		m_recordingEnabled = record;
		Debug.println ("Recording Enabled? " + isRecordingEnabled ());
	}

	/**
	 * Prevents flight recording until <code>unlockRecording</code> is called.
	 * Independent of whether or not flight recording has been enabled/disabled
	 * by user.
	 */
	public void lockRecordingOff ()
	{
		m_recordingLockedOff = true;
	}

	/**
	 * Turns off flight recording lock enabled with
	 * <code>lockRecordingOff</code> so that whether record or not now depends
	 * on if user enabled/disabled flight record. If recording has not been
	 * locked off, the call has no effect.
	 */
	public void unlockRecording ()
	{
		m_recordingLockedOff = false;
	}

	/**
	 * Whether or not the FlightDataRecorder is currently recording events.
	 * 
	 * @return true if recording, false if not
	 */
	public boolean isRecordingEnabled ()
	{
		return (m_recordingEnabled);
	}

	/**
	 * Returns the most recent occurrence of an event of type <b>eventType</b>.
	 * 
	 * @param eventType
	 *            - GeopodEventId specifying the type of event
	 * @return A FlightEvent (which will be of type <b>eventType</b>) which is
	 *         the most recent event of that type to occur.
	 */
	public FlightEvent getLatestEvent (GeopodEventId eventType)
	{
		List<Integer> locations = getLocations (eventType);
		Integer lastLocation = locations.get (locations.size () - 1);
		FlightEvent latestEvent = m_flightEvents.get (lastLocation);
		return (latestEvent);
	}

	public void resetLog ()
	{
		m_flightEvents = new ArrayList<FlightEvent> ();
		m_eventsByType = ArrayListMultimap.create (NUM_EVENT_TYPES_RECORDED, 20);
	}

	private List<Integer> getLocations (GeopodEventId eventType)
	{
		return (m_eventsByType.get (eventType));
	}
}
