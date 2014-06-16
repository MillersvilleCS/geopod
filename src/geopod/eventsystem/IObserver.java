package geopod.eventsystem;

import geopod.eventsystem.events.GeopodEventId;

/**
 * Interface for the observer side of the subject/observer pattern.
 * 
 * @author Geopod Team
 * 
 */
public interface IObserver
{
	/**
	 * Handle an event from an observed object.
	 * 
	 * @param eventId
	 *            - the event that occurred.
	 */
	public void handleNotification (GeopodEventId eventId);
}