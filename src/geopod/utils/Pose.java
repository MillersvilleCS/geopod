package geopod.utils;

import javax.media.j3d.Transform3D;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

/**
 * A structure defining a position and rotation.
 */
public class Pose
{
	private final static Quat4d QUATERNION_IDENTITY;

	static
	{
		QUATERNION_IDENTITY = new Quat4d (0, 0, 0, 1);
	}

	private Quat4d m_rotation;
	private Vector3d m_position;

	/**
	 * Construct a pose with default values.
	 */
	public Pose ()
	{
		m_rotation = QUATERNION_IDENTITY;
		m_position = new Vector3d ();
	}

	/**
	 * Construct a pose with an initial position and default rotation.
	 * 
	 * @param position
	 *            - the initial position.
	 */
	public Pose (Tuple3d position)
	{
		this (QUATERNION_IDENTITY, position);
	}

	/**
	 * Construct a pose with an initial position and rotation.
	 * 
	 * @param rotation
	 *            - the initial rotation as a {@link Quat4d quaternion}.
	 * @param position
	 *            - the initial position.
	 */
	public Pose (Quat4d rotation, Tuple3d position)
	{
		setRotation (rotation);
		setPosition (position);
	}

	/**
	 * Construct a pose with an initial position and rotation.
	 * 
	 * @param rotation
	 *            - the initial rotation as an {@link AxisAngle4d}
	 *            representation.
	 * @param position
	 *            - the initial position.
	 */
	public Pose (AxisAngle4d rotation, Tuple3d position)
	{
		setRotation (rotation);
		setPosition (position);
	}

	/**
	 * Set the rotation.
	 * 
	 * @param rotation
	 *            - the new rotation as a {@link Quat4d quaternion}.
	 */
	public void setRotation (Quat4d rotation)
	{
		m_rotation = new Quat4d (rotation);
	}

	/**
	 * Set the rotation
	 * 
	 * @param rotation
	 *            - the new rotation as an {@link AxisAngle4d} representation.
	 */
	public void setRotation (AxisAngle4d rotation)
	{
		m_rotation = new Quat4d ();
		m_rotation.set (rotation);
	}

	/**
	 * @return the rotation as a {@link Quat4d quaternion}.
	 */
	public Quat4d getRotation ()
	{
		return (m_rotation);
	}

	/**
	 * Set the position.
	 * 
	 * @param position
	 *            - the new position as a {@link Tuple3d}.
	 */
	public void setPosition (Tuple3d position)
	{
		m_position = new Vector3d (position);
	}

	/**
	 * @return - the position as a {@link Vector3d}.
	 */
	public Vector3d getPosition ()
	{
		return (m_position);
	}

	/**
	 * Obtain the {@link Pose} equivalent to the specified {@link Transform3D}.
	 * Scale is disregarded.
	 * 
	 * @param transform
	 *            - the transform to copy from.
	 * @return - the equivalent pose.
	 */
	public static Pose valueOf (Transform3D transform)
	{
		Pose pose = new Pose ();
		Quat4d rotation = pose.getRotation ();
		transform.get (rotation);
		Vector3d translation = pose.getPosition ();
		transform.get (translation);

		return (pose);
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public String toString ()
	{
		StringBuilder sb = new StringBuilder ();
		sb.append ("Rot: ");
		AxisAngle4d rotation = new AxisAngle4d ();
		rotation.set (m_rotation);
		sb.append (rotation.toString ());
		sb.append ("; Pos: ");
		sb.append (m_position.toString ());

		return (sb.toString ());
	}

}
