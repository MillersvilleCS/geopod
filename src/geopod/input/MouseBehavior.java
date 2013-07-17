package geopod.input;

import geopod.ConfigurationManager;
import geopod.Geopod;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.media.j3d.WakeupOr;
import javax.vecmath.Vector2f;

/**
 * Behavior to handle mouse input and rotate the geopod.
 * 
 * @author Geopod Team
 * 
 */
public class MouseBehavior
		extends Behavior
{
	private static final float WHEEL_ROTATION_MULTIPLIER;
	private static final float ROTATION_DRAG_MULTIPLIER;
	private static final float ROTATION_SLIDE_MULTIPLIER;

	/**
	 * Wake up on mouse events.
	 */
	private static final WakeupCondition MOUSE_TRIGGER;
	/**
	 * Wake up on mouse events and on every elapsed frame.
	 */
	private static final WakeupCondition MOUSE_AND_FRAME_TRIGGER;

	static
	{
		WHEEL_ROTATION_MULTIPLIER = 5.0f;
		ROTATION_DRAG_MULTIPLIER = 100f;
		ROTATION_SLIDE_MULTIPLIER = 1f / 300f;

		WakeupCriterion[] conditions = new WakeupCriterion[3];
		conditions[0] = new WakeupOnAWTEvent (AWTEvent.MOUSE_EVENT_MASK);
		conditions[1] = new WakeupOnAWTEvent (AWTEvent.MOUSE_MOTION_EVENT_MASK);
		conditions[2] = new WakeupOnAWTEvent (AWTEvent.MOUSE_WHEEL_EVENT_MASK);
		MOUSE_TRIGGER = new WakeupOr (conditions);

		conditions = new WakeupCriterion[4];
		conditions[0] = new WakeupOnAWTEvent (AWTEvent.MOUSE_EVENT_MASK);
		conditions[1] = new WakeupOnAWTEvent (AWTEvent.MOUSE_MOTION_EVENT_MASK);
		conditions[2] = new WakeupOnElapsedFrames (0, true);
		conditions[3] = new WakeupOnAWTEvent (AWTEvent.MOUSE_WHEEL_EVENT_MASK);

		MOUSE_AND_FRAME_TRIGGER = new WakeupOr (conditions);
	}

	/**
	 * The wakeup to reset this behavior with. Reset to false on a mouseReleased
	 * event.
	 */
	private WakeupCondition m_nextWakeup;

	/**
	 * True if a mouse button is pressed.
	 */
	private boolean m_mousePressed;

	/**
	 * The button that was pressed on the last event. Do not store
	 * MouseEvent.BUTTON0 (the index of "no button") in this variable.
	 */
	private int m_lastButtonPressed;

	/**
	 * The position of the mouse the last time a button was pressed.
	 */
	private Point m_initialClickPoint;

	/**
	 * Stores the calculated rotation delta in degrees for use on the next frame
	 * update.
	 */
	private Vector2f m_rotationDelta;

	private Geopod m_geopod;

	private boolean m_limitRoll;

	/**
	 * Constructor.
	 * 
	 * @param geopod
	 *            - the {@link Geopod} to control.
	 */
	public MouseBehavior (Geopod geopod)
	{
		m_geopod = geopod;
		m_mousePressed = false;
		m_initialClickPoint = new Point ();
		m_rotationDelta = new Vector2f ();
		m_limitRoll = ConfigurationManager.isEnabled (ConfigurationManager.DisableRoll);

		m_nextWakeup = MOUSE_TRIGGER;

		// Listen if the limitRoll property is changed.
		ConfigurationManager.addPropertyChangeListener (ConfigurationManager.DisableRoll, new PropertyChangeListener ()
		{
			@Override
			public void propertyChange (PropertyChangeEvent evt)
			{
				MouseBehavior.this.m_limitRoll = ConfigurationManager.isEnabled (ConfigurationManager.DisableRoll);
			}
		});
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void initialize ()
	{
		this.wakeupOn (m_nextWakeup);
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void processStimulus (@SuppressWarnings("rawtypes") Enumeration criteria)
	{
		boolean movementEnabled = m_geopod.isMovementEnabled ();
		if (movementEnabled)
		{
			@SuppressWarnings("unchecked")
			Enumeration<WakeupCriterion> wakeupCriteria = criteria;

			boolean isFrameElapsed = false;
			while (wakeupCriteria.hasMoreElements ())
			{

				WakeupCriterion wakeupCriterion = wakeupCriteria.nextElement ();

				if (wakeupCriterion instanceof WakeupOnAWTEvent)
				{
					AWTEvent awtEvent = ((WakeupOnAWTEvent) wakeupCriterion).getAWTEvent ()[0];
					MouseEvent mouseEvent = (MouseEvent) awtEvent;

					int buttonId = mouseEvent.getButton ();

					switch (buttonId)
					{
					case MouseEvent.NOBUTTON:
						handleNoButton (mouseEvent);
						break;
					case MouseEvent.BUTTON1:
						handleRotationDrag (mouseEvent);
						m_lastButtonPressed = buttonId;
						break;
					case MouseEvent.BUTTON2:
						m_lastButtonPressed = buttonId;
						break;
					case MouseEvent.BUTTON3:
						handleRotationSlide (mouseEvent);
						m_lastButtonPressed = buttonId;
						break;

					default:
						break;
					}
				}
				else
				{
					isFrameElapsed = true;
				}
			}

			// Add rotations to the controlled object on frame events.
			if (isFrameElapsed)
			{
				updateOnFrameElapsed ();
			}
		}

		this.wakeupOn (m_nextWakeup);
	}

	/**
	 * Rotate the Geopod on frame events.
	 */
	private void updateOnFrameElapsed ()
	{
		if (!m_mousePressed)
		{
			// If a mouse button is not pressed, we no longer need to listen for frame events.
			m_nextWakeup = MOUSE_TRIGGER;
		}
		else
		{
			m_geopod.yaw (m_rotationDelta.x);
			m_geopod.pitch (m_rotationDelta.y);

			m_geopod.updateAfterRotate ();

			// Clear rotate deltas at end of mouse drag.
			if (m_lastButtonPressed == MouseEvent.BUTTON1)
			{
				m_rotationDelta.x = 0;
				m_rotationDelta.y = 0;
			}

			m_nextWakeup = MOUSE_AND_FRAME_TRIGGER;
		}
	}

	/**
	 * Handle rotation with a drag motion. Currently mapped to the left mouse
	 * button.
	 * 
	 * @param mouesEvent
	 *            - the mouse event from the mouse button mapped to this action.
	 */
	private void handleRotationDrag (MouseEvent mouseEvent)
	{
		int eventId = mouseEvent.getID ();
		m_initialClickPoint.x = mouseEvent.getX ();
		m_initialClickPoint.y = mouseEvent.getY ();

		if (eventId == MouseEvent.MOUSE_PRESSED)
		{
			m_mousePressed = true;
			m_nextWakeup = MOUSE_AND_FRAME_TRIGGER;
		}
		else if (eventId == MouseEvent.MOUSE_RELEASED)
		{
			m_mousePressed = false;
			m_nextWakeup = MOUSE_TRIGGER;
		}
	}

	/**
	 * Handle rotation with a sliding motion. Currently mapped to the right
	 * mouse button.
	 */
	private void handleRotationSlide (MouseEvent mouseEvent)
	{
		int eventId = mouseEvent.getID ();

		if (eventId == MouseEvent.MOUSE_PRESSED)
		{
			m_mousePressed = true;

			// Get the start position
			m_initialClickPoint.x = mouseEvent.getX ();
			m_initialClickPoint.y = mouseEvent.getY ();

			// Wakeup on the next mouse event or the next frame.
			m_nextWakeup = MOUSE_AND_FRAME_TRIGGER;
		}
		else if (eventId == MouseEvent.MOUSE_RELEASED)
		{
			m_mousePressed = false;

			// Reset mouse movement
			m_rotationDelta.x = 0;
			m_rotationDelta.y = 0;

			// Only wakeup on the next mouse pressed event.
			m_nextWakeup = MOUSE_TRIGGER;
		}
	}

	/**
	 * Handle mouse events other then button presses.
	 */
	private void handleNoButton (MouseEvent mouseEvent)
	{
		int buttonId = mouseEvent.getID ();
		int eventId = mouseEvent.getID ();

		if (eventId == MouseEvent.MOUSE_WHEEL && !m_limitRoll)
		{
			// Handle wheel events immediately, as there is no state to maintain.
			MouseWheelEvent wheel = (MouseWheelEvent) mouseEvent;
			int wheelRotation = wheel.getWheelRotation ();
			double degreesZ = wheelRotation * WHEEL_ROTATION_MULTIPLIER;
			m_geopod.roll (degreesZ);
			m_geopod.updateAfterRotate ();
		}
		else if (eventId == MouseEvent.MOUSE_DRAGGED)
		{
			// Handle bugs with the drag starting during a window select.
			if (m_lastButtonPressed == MouseEvent.BUTTON1)
			{
				Dimension d = mouseEvent.getComponent ().getSize ();

				// Handle jumping camera bugs if the drag started during a window select.
				if (!m_mousePressed)
				{
					m_initialClickPoint.x = mouseEvent.getX ();
					m_initialClickPoint.y = mouseEvent.getY ();
				}

				int current_x = mouseEvent.getX ();
				int current_y = mouseEvent.getY ();

				// angle is based on delta from initial point and weighted by a constant and the screen size.
				double anglex = -(current_x - m_initialClickPoint.x) * ROTATION_DRAG_MULTIPLIER / d.width;
				double angley = -(current_y - m_initialClickPoint.y) * ROTATION_DRAG_MULTIPLIER / d.height;

				// Set the amount to rotate on the next frame.
				m_rotationDelta.x = (float) anglex;
				m_rotationDelta.y = (float) angley;

				m_geopod.updateAfterRotate ();

				m_initialClickPoint.x = current_x;
				m_initialClickPoint.y = current_y;

			}
			else if (m_lastButtonPressed == MouseEvent.BUTTON3)
			{
				if (!m_mousePressed && buttonId == MouseEvent.BUTTON3)
				{
					m_mousePressed = true;

					m_initialClickPoint.x = mouseEvent.getX ();
					m_initialClickPoint.y = mouseEvent.getY ();

				}

				if (m_mousePressed)
				{
					// Delta = currentPosition - initial position.
					float deltaX = mouseEvent.getX () - m_initialClickPoint.x;
					float deltaY = mouseEvent.getY () - m_initialClickPoint.y;

					// Adjust delta by constant speed multiplier.
					m_rotationDelta.x = -deltaX * ROTATION_SLIDE_MULTIPLIER;
					m_rotationDelta.y = -deltaY * ROTATION_SLIDE_MULTIPLIER;

					m_nextWakeup = MOUSE_AND_FRAME_TRIGGER;
				}
			}
		}
	}

}
