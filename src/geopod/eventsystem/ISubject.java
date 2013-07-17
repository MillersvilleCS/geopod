package geopod.eventsystem;

import geopod.eventsystem.events.GeopodEventId;

/**
 * Interface to abstract away the handling of observers.
 * 
 * @author Geopod Team
 * 
 */
public interface ISubject
{
	/**
	 * Add an observer to this subject.
	 * 
	 * @param observer
	 *            - the observer to add
	 * @param eventId
	 *            - the event to wait for
	 */
	public void addObserver (IObserver observer, GeopodEventId eventId);

	/**
	 * Remove an observer from this subject.
	 * 
	 * @param observer
	 *            - the observer to remove
	 * @param eventId
	 *            - the event to stop waiting for
	 */
	public void removeObserver (IObserver observer, GeopodEventId eventId);
	
	/**
	 * Remove all observers from this subject.
	 * 
	 */
	public void removeObservers ();

	/**
	 * Notify all observers that an event has occurred.
	 * 
	 * @param eventId
	 *            - the event that has occurred.
	 */
	public void notifyObservers (GeopodEventId eventId);
}
