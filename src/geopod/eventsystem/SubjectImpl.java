package geopod.eventsystem;

import geopod.eventsystem.events.GeopodEventId;

import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Implementation of the ISubject interface.
 */
public class SubjectImpl
		implements ISubject
{
	private EnumMap<GeopodEventId, ArrayList<IObserver>> m_observerMap;

	/**
	 * Constructor.
	 */
	public SubjectImpl ()
	{
		m_observerMap = new EnumMap<GeopodEventId, ArrayList<IObserver>> (GeopodEventId.class);

		// Initialize all event IDs with empty lists
		for (GeopodEventId geid : GeopodEventId.values ())
		{
			m_observerMap.put (geid, new ArrayList<IObserver> ());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addObserver (IObserver observer, GeopodEventId eventId)
	{
		ArrayList<IObserver> listeners = m_observerMap.get (eventId);
		listeners.add (observer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeObserver (IObserver observer, GeopodEventId eventId)
	{
		ArrayList<IObserver> observerList = m_observerMap.get (eventId);
		observerList.remove (observer);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeObservers ()
	{
		// remove all event, observer pairs
		m_observerMap.clear ();

		// Reinitialize all event IDs with empty lists
		for (GeopodEventId geid : GeopodEventId.values ())
		{
			m_observerMap.put (geid, new ArrayList<IObserver> ());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyObservers (GeopodEventId eventId)
	{
		ArrayList<IObserver> observerList = m_observerMap.get (eventId);
		for (IObserver observer : observerList)
		{
			observer.handleNotification (eventId);
		}
	}
}
