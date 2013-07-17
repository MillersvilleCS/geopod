package geopod.input;

import geopod.ConfigurationManager;
import geopod.Geopod;
import geopod.constants.DirectionConstants;
import geopod.input.Keys.GeopodKeys;
import geopod.utils.TransformGroupControl.RotationDirection;

import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.media.j3d.WakeupOr;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

/**
 * Behavior to handle keyboard input and move the geopod.
 * 
 * @author Geopod Team
 * 
 */
public class KeyBehavior
		extends Behavior
{
	private WakeupCondition m_awtTrigger;
	private Geopod m_geopod;
	private boolean m_limitRoll;

	private boolean m_ignoreKeyEvents;

	/**
	 * Maintain the combined translation vector for the movement keys
	 */
	private Vector3d m_translation;

	/**
	 * Constructor.
	 * 
	 * @param geopod
	 *            - the {@link Geopod} to control.
	 */
	public KeyBehavior (Geopod geopod)
	{
		m_ignoreKeyEvents = false;
		m_geopod = geopod;
		m_limitRoll = ConfigurationManager.isEnabled (ConfigurationManager.DisableRoll);
		//KeyboardBuffer = new KeyboardBuffer ();
		m_translation = new Vector3d ();

		// Create wake-up conditions
		WakeupCriterion[] conditions = new WakeupCriterion[2];
		// Ensure behavior is active so we get frame events continuously
		conditions[0] = new WakeupOnElapsedFrames (0, false);
		conditions[1] = new WakeupOnAWTEvent (AWTEvent.KEY_EVENT_MASK);
		m_awtTrigger = new WakeupOr (conditions);

		ConfigurationManager.addPropertyChangeListener (ConfigurationManager.DisableRoll, new PropertyChangeListener ()
		{

			@Override
			public void propertyChange (PropertyChangeEvent evt)
			{
				KeyBehavior.this.m_limitRoll = ConfigurationManager.isEnabled (ConfigurationManager.DisableRoll);
			}

		});

	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void initialize ()
	{
		this.wakeupOn (m_awtTrigger);
	}

	/**
	 * Tell the key behavior to ignore key events or not. This is used to ensure
	 * keys are still flushed from the AWT buffer when we don't wish to process
	 * key events (e.g., during autopilot). If the behavior is disabled
	 * processStimulus won't be called and events will accumulate.
	 * 
	 * @param ignoreKeys
	 */
	public void setIgnoreKeys (boolean ignoreKeys)
	{
		m_ignoreKeyEvents = ignoreKeys;
	}

	/**
	 * Mark all keys as up. This can be used to correct focus issues where the
	 * KeyBehavior never gets the keyUp events.
	 */
	public void resetKeyBuffer ()
	{
		KeyboardBuffer.resetKeyBuffer ();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	/**
	 * {@inheritDoc}
	 */
	public void processStimulus (Enumeration criteria)
	{
		Enumeration<WakeupCriterion> wakeupCriteria = criteria;
		boolean isFrameElapsed = false;

		while (wakeupCriteria.hasMoreElements ())
		{
			WakeupCriterion wakeupCriterion = wakeupCriteria.nextElement ();

			if (wakeupCriterion instanceof WakeupOnAWTEvent)
			{

				WakeupOnAWTEvent awtEvent = (WakeupOnAWTEvent) wakeupCriterion;
				AWTEvent[] events = awtEvent.getAWTEvent ();

				if (!m_ignoreKeyEvents)
				{
					this.recordKeyEvents (events);
				}
			}
			else
			{
				// Defer frame update until after all key actions are handled.
				isFrameElapsed = true;
			}
		}

		if (isFrameElapsed)
		{
			this.performKeyActions ();
		}

		// Set wake-up criteria for next time
		wakeupOn (m_awtTrigger);
	}

	private void recordKeyEvents (AWTEvent[] events)
	{
		for (AWTEvent e : events)
		{
			KeyboardBuffer.recordKeyEvent ((KeyEvent) e);
		}
	}

	/**
	 * Handle movement on an elapsed frame event
	 */
	private void performKeyActions ()
	{
		// To determine if the Geopod's position needs to be updated, re-sampling needs to be performed, etc.
		boolean hasMoved = false;
		boolean hasRotated = false;
		m_translation.set (0, 0, 0);
		// If the Geopod is not active ignore control input
		if (m_geopod.isMovementEnabled ())
		{
			// Move forward/backward
			if (KeyboardBuffer.isKeyDown (Keys.getBinding (Keys.GeopodKeys.MoveForward)))
			{
				hasMoved = true;
				m_translation.add (DirectionConstants.FORWARD);
			}
			else if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.MoveBackward)))
			{
				hasMoved = true;
				m_translation.add (DirectionConstants.BACKWARD);
			}

			// Move left/right
			if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.MoveLeft)))
			{
				hasMoved = true;
				m_translation.add (DirectionConstants.LEFT);
			}
			else if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.MoveRight)))
			{
				hasMoved = true;
				m_translation.add (DirectionConstants.RIGHT);
			}

			// Move up/down
			if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.MoveUp)))
			{
				hasMoved = true;
				m_translation.add (DirectionConstants.UP);
			}
			else if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.MoveDown)))
			{

				hasMoved = true;
				m_translation.add (DirectionConstants.DOWN);
			}

			// Yaw left/right
			if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.TurnLeft)))
			{
				hasRotated = true;
				m_geopod.yaw (RotationDirection.CLOCKWISE);
			}
			else if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.TurnRight)))
			{
				hasRotated = true;
				m_geopod.yaw (RotationDirection.COUNTERCLOCKWISE);
			}

			// Roll CW/CCW
			if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.RollClockwise)) && !m_limitRoll)
			{
				hasRotated = true;
				m_geopod.roll (RotationDirection.CLOCKWISE);
			}
			else if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.RollCounterclockwise)) && !m_limitRoll)
			{
				hasRotated = true;
				m_geopod.roll (RotationDirection.COUNTERCLOCKWISE);
			}

			// Align local Y axis with Earth up (using an animation)
			if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.MakeUpright)))
			{
				m_geopod.alignWithEarthUp (true);
			}

			int keyCode = Keys.getBinding (GeopodKeys.LockToSurface).getKeyCode ();
			if (KeyboardBuffer.isKeyDown (keyCode))
			{
				m_geopod.toggleSurfaceLock ();
				KeyboardBuffer.setKeyDown (keyCode, false);
			}

			// Set position above view box's center, align with world down
			if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.SetPoseAboveCenterOfViewBox)))
			{
				m_geopod.setPose (new Quat4d (), new Point3d (0, 0, 1.25));
				unlockFromIsosurface ();
			}

			// Reset pose to starting pose
			if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.ResetPose)))
			{
				m_geopod.resetPose ();
				unlockFromIsosurface ();
			}

			// Linear acceleration
			if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.Accelerate)))
			{
				m_geopod.accelerate ();
			}
			else if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.Decelerate)))
			{
				m_geopod.decelerate ();
			}

			// Angular acceleration
			if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.AngularAccelerate)))
			{
				m_geopod.increaseAngularSpeed ();
			}
			else if (KeyboardBuffer.isKeyDown (Keys.getBinding (GeopodKeys.AngularDecelerate)))
			{
				m_geopod.decreaseAngularSpeed ();
			}

		}

		if (hasMoved)
		{
			m_translation.normalize ();
			m_geopod.moveLocal (m_translation);
		}

		if (hasRotated)
		{
			m_geopod.updateAfterRotate ();
		}
	}

	private void unlockFromIsosurface ()
	{
		if (m_geopod.getIsosurfaceLockEnabled ())
		{
			m_geopod.lockToSurface (false);
		}
	}
}
