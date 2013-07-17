package geopod;

import geopod.eventsystem.events.GeopodEventId;
import geopod.interpolators.RotPosScaleTCBSplinePathInterpolator;
import geopod.interpolators.TCBKeyFrame;
import geopod.utils.TransformGroupControl;
import geopod.utils.math.InterpolatorUtility;

import java.util.Enumeration;

import javax.media.j3d.Alpha;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

/**
 * Autopilot for flying to a location
 */
public class Autopilot
		extends Behavior
{
	private static final long FLIGHT_TIME_IN_MS;
	private static final long TIME_AT_ONE_IN_MS;

	/**
	 * The criteria to wakeup the autopilot on.
	 */
	private WakeupCriterion m_wakeupTrigger;

	/**
	 * The Geopod to control.
	 */
	private Geopod m_geopod;

	/**
	 * The object to control the Geopod's position with.
	 */
	private TransformGroupControl m_transformHelper;

	/**
	 * A node to hold the interpolator. This is needed to add the behavior to a
	 * live scene graph.
	 */
	private BranchGroup m_interpolatorNode;

	/**
	 * An alpha [0,1] defining where along the flight path to place the Geopod.
	 * Based on time from initialization.
	 */
	private Alpha m_alpha;

	/**
	 * The interpolator that calculates the Geopod's position based on the value
	 * of m_alpha.
	 */
	private RotPosScaleTCBSplinePathInterpolator m_autopilotInterpolator;

	/**
	 * This is a value to prevent the autopilot from turning on a point without
	 * appearing to have moved. This distance is in world coordinates.
	 */
	public static final double MINIMUM_FLY_TO_DISTANCE;

	static
	{
		FLIGHT_TIME_IN_MS = 4000;
		TIME_AT_ONE_IN_MS = 100;
		MINIMUM_FLY_TO_DISTANCE = 0.0002;
	}

	/**
	 * Construct an autopilot.
	 * 
	 * @param geopod
	 *            - the Geopod this autopilot belongs to.
	 */
	public Autopilot (Geopod geopod)
	{
		m_geopod = geopod;
		m_interpolatorNode = createInterpolatorBranch ();
		m_geopod.addNodeToMovementGroup (m_interpolatorNode);

		m_wakeupTrigger = new WakeupOnElapsedFrames (0, false);
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void initialize ()
	{
		this.wakeupOn (m_wakeupTrigger);
	}

	/**
	 * Create a branch group to hold the interpolator that does the position
	 * calculations.
	 * 
	 * @return a BranchGroup holding an the autopilot's interpolator.
	 */
	private BranchGroup createInterpolatorBranch ()
	{
		BranchGroup interpolatorNode = new BranchGroup ();

		m_transformHelper = m_geopod.getViewTransformControl ();
		TransformGroup movementGroup = m_transformHelper.getTransformGroup ();

		// Create the interpolator once and reuse later by resetting the alpha,
		// transform axis, and keys.
		m_autopilotInterpolator = new RotPosScaleTCBSplinePathInterpolator (movementGroup);
		m_alpha = new Alpha (1, FLIGHT_TIME_IN_MS);
		m_alpha.setMode (Alpha.INCREASING_ENABLE);

		// Let alpha remain at 1 for a few frames to be sure we reach our
		// destination.
		m_alpha.setAlphaAtOneDuration (TIME_AT_ONE_IN_MS);
		m_autopilotInterpolator.setAlpha (m_alpha);
		interpolatorNode.addChild (m_autopilotInterpolator);

		// Set scheduling bounds. A sphere of radius 1 should ensure it always
		// runs.
		BoundingSphere bounds = new BoundingSphere (new Point3d (), 1);
		m_autopilotInterpolator.setSchedulingBounds (bounds);
		m_autopilotInterpolator.setEnable (false);

		return (interpolatorNode);
	}

	/**
	 * Create a flight path from the starting location to the ending location.
	 * 
	 * This must be called before setEnable().
	 * 
	 * @param startPose
	 *            - the current pose of the Geopod.
	 * @param targetPosition
	 *            - the point in world coordinates to fly to.
	 * @return - returns false if the target is too close to fly to, or true
	 *         otherwise.
	 */
	public boolean calculateFlightPath (Transform3D startPose, Point3d targetPosition)
	{
		// Find a direction vector to the target
		Vector3d forward = new Vector3d (targetPosition);
		forward.sub (m_transformHelper.getPosition ());

		if (forward.length () < MINIMUM_FLY_TO_DISTANCE)
		{
			// Bail out because flight distance is too short
			return (false);
		}

		// set the alpha flight time
		m_alpha.setIncreasingAlphaDuration (FLIGHT_TIME_IN_MS);

		// Create end pose
		Vector3d targetVec = new Vector3d (targetPosition);
		Quat4d endRotation = new Quat4d ();
		startPose.get (endRotation);
		Transform3D endPose = new Transform3D (endRotation, targetVec, 1.0);

		TCBKeyFrame[] keys = InterpolatorUtility.createKeys (startPose, endPose);
		m_autopilotInterpolator.setKeys (keys);

		return (true);
	}

	/**
	 * Create an animation from a starting transform to an ending transform. Use
	 * <tt>setEnable()</tt> to start the animation.
	 * 
	 * @param startPose
	 *            - the current pose of the Geopod.
	 * @param endPose
	 *            - the point in world coordinates to fly to.
	 * @param animationLength
	 *            - the length to run the animation in milliseconds.
	 */
	public void calculateFlightPath (Transform3D startPose, Transform3D endPose, long animationLength)
	{
		m_alpha.setIncreasingAlphaDuration (animationLength);
		TCBKeyFrame[] keys = InterpolatorUtility.createKeys (startPose, endPose);
		m_autopilotInterpolator.setKeys (keys);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnable (boolean doEnable)
	{
		if (doEnable)
		{
			m_geopod.setMovementEnabled (false);
			m_autopilotInterpolator.setEnable (true);
			long currentTime = System.currentTimeMillis ();
			m_alpha.setStartTime (currentTime);

		}
		super.setEnable (doEnable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processStimulus (@SuppressWarnings("rawtypes") Enumeration criteria)
	{
		// if the alpha has finished (reached 1.0), turn off the interpolator
		// and turn the keyboard controls back on
		if (m_alpha.finished ())
		{
			m_autopilotInterpolator.setEnable (false);
			this.setEnable (false);
			m_geopod.setMovementEnabled (true);
			// Activate mouse and keyboard behaviors
			m_geopod.notifyObservers (GeopodEventId.AUTO_PILOT_FINISHED);

		}

		m_geopod.updateAfterMove ();
		m_geopod.updateAfterRotate ();

		this.wakeupOn (m_wakeupTrigger);
	}

}
