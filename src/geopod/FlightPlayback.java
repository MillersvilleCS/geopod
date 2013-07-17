package geopod;

import geopod.devices.FlightDataRecorder;
import geopod.eventsystem.events.FlightEvent;
import geopod.eventsystem.events.MovementEvent;
import geopod.eventsystem.events.ParticleImageChangedEvent;
import geopod.eventsystem.events.ParticleImagerDisplayEvent;
import geopod.gui.panels.NotificationPanel;
import geopod.utils.coordinate.IdvCoordinateUtility;

import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;

import visad.georef.EarthLocation;

public class FlightPlayback
		implements Runnable
{

	private FlightDataRecorder m_dataRecord;
	private Geopod m_geopod;
	private boolean m_isPaused;
	private boolean m_wasStopped;
	private NotificationPanel m_notificationPanel;
	private int m_direction;

	public FlightPlayback (FlightDataRecorder blackbox, Geopod geopod, NotificationPanel notificationPanel)
	{
		m_dataRecord = blackbox;
		m_geopod = geopod;
		m_isPaused = false;
		m_wasStopped = false;
		m_notificationPanel = notificationPanel;
		m_direction = 1;
	}

	public synchronized void stopPlayback ()
	{
		m_wasStopped = true;
		notify ();
	}

	public synchronized void pause ()
	{
		m_isPaused = true;
		notify ();
	}

	public synchronized void resume ()
	{
		m_isPaused = false;
		notify ();
	}

	public void reverseDirection ()
	{
		m_direction *= -1;
	}

	public boolean isPaused ()
	{
		return (m_isPaused);
	}

	@Override
	public void run ()
	{
		prepareToBeginFlightPlayback ();

		List<FlightEvent> allFlightEvents = m_dataRecord.getAllEvents ();

		for (int i = 0; i < allFlightEvents.size () && i > -1 && !m_wasStopped; i += m_direction)
		{
			synchronized (this)
			{
				while (m_isPaused && !m_wasStopped)
				{
					try
					{
						wait ();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace ();
					}
				}
			}

			int millisecondsToWait = Geopod.PLAYBACK_FRAME_SPEED_IN_MS;
			if (m_notificationPanel.isVisible ())
			{
				millisecondsToWait = Geopod.TIME_TO_DISPLAY_EVENT_NOTIFICATION_IN_MS;
			}
			waitPlayback (millisecondsToWait);

			FlightEvent currentEvent = allFlightEvents.get (i);

			if (currentEvent instanceof MovementEvent)
			{
				m_notificationPanel.setVisible (false);
				MovementEvent movementEvent = (MovementEvent) allFlightEvents.get (i);
				moveGeopod (movementEvent);
			}
			else if (currentEvent instanceof ParticleImagerDisplayEvent)
			{
				m_geopod.toggleParticleImager ();
			}
			else if (!(currentEvent instanceof ParticleImageChangedEvent))
			{
				String eventType = currentEvent.getEventType ().toString ();
				m_notificationPanel.setNotificationText (eventType);
				m_notificationPanel.setVisible (true);
			}
		}

		prepareToEndFlightPlayback ();

	}

	private void moveGeopod (MovementEvent movementEvent)
	{
		EarthLocation earthLoc = movementEvent.getEarthLocation ();
		Point3d worldLoc = IdvCoordinateUtility.convertEarthToWorld (earthLoc);
		Quat4d rotation = movementEvent.getRotation ();

		m_geopod.setPose (rotation, worldLoc);
	}

	private void waitPlayback (int millisecondsToWait)
	{
		long time = System.currentTimeMillis ();
		long timeNow = System.currentTimeMillis ();
		while (timeNow < time + millisecondsToWait)
		{
			timeNow = System.currentTimeMillis ();
		}
	}

	private void prepareToBeginFlightPlayback ()
	{
		m_dataRecord.lockRecordingOff ();
		m_geopod.resetPose ();
		m_geopod.setFlightPlaybackStatus (true);
		//Ensures future calls to deployParticleImager 
		//will not create reverse behavior during playback
		if (m_geopod.particleImagerActive ())
		{
			m_geopod.toggleParticleImager ();
		}

		// ensures will no have conflicts if playback was started 
		// while isosurface lock was enabled
		if (m_geopod.getIsosurfaceLockEnabled ())
		{
			m_geopod.toggleSurfaceLock ();
		}

		m_notificationPanel.setNotificationText ("Beginning Flight Playback (press 'k' to stop)");
		m_notificationPanel.setVisible (true);
		waitPlayback (2 * Geopod.TIME_TO_DISPLAY_EVENT_NOTIFICATION_IN_MS);
	}

	private void prepareToEndFlightPlayback ()
	{
		if (m_notificationPanel.isVisible ())
		{
			waitPlayback (Geopod.TIME_TO_DISPLAY_EVENT_NOTIFICATION_IN_MS);
		}

		m_geopod.resetPose ();

		m_notificationPanel.setNotificationText ("Flight Playback Finished");
		m_notificationPanel.setVisible (true);
		waitPlayback (3 * Geopod.TIME_TO_DISPLAY_EVENT_NOTIFICATION_IN_MS);
		m_notificationPanel.setVisible (false);

		m_dataRecord.unlockRecording ();
		m_geopod.setFlightPlaybackStatus (false);
	}

}
