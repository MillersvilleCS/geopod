package geopod.constants;

import javax.vecmath.Vector3d;

/**
 * A class containing axis and direction unit vector constants.
 * 
 */
public class DirectionConstants
{
	/**
	 * A unit vector pointing up from the surface of the earth.
	 */
	public static final Vector3d EARTH_UP;

	/**
	 * A unit vector pointing to the right.
	 */
	public static final Vector3d RIGHT;
	/**
	 * A unit vector pointing to the left.
	 */
	public static final Vector3d LEFT;
	/**
	 * A unit vector pointing up.
	 */
	public static final Vector3d UP;
	/**
	 * A unit vector pointing down.
	 */
	public static final Vector3d DOWN;
	/**
	 * A unit vector pointing backwards.
	 */
	public static final Vector3d BACKWARD;
	/**
	 * A unit vector pointing forwards.
	 */
	public static final Vector3d FORWARD;
	/**
	 * A vector with zero length.
	 */
	public static final Vector3d ZERO;

	/**
	 * A unit vector along the X-axis.
	 */
	public static final Vector3d UNIT_X;
	/**
	 * A unit vector along the Y-axis.
	 */
	public static final Vector3d UNIT_Y;
	/**
	 * A unit vector along the Z-axis.
	 */
	public static final Vector3d UNIT_Z;

	static
	{
		// Vector constants
		UNIT_X = new Vector3d (1, 0, 0);
		UNIT_Y = new Vector3d (0, 1, 0);
		UNIT_Z = new Vector3d (0, 0, 1);

		ZERO = new Vector3d (0, 0, 0);

		RIGHT = UNIT_X;
		UP = UNIT_Y;
		BACKWARD = UNIT_Z;

		LEFT = new Vector3d (-1, 0, 0);
		DOWN = new Vector3d (0, -1, 0);
		FORWARD = new Vector3d (0, 0, -1);

		// Because of the way the IDV data volume is rotated, the z-axis points up from the earth
		EARTH_UP = UNIT_Z;
	}

	private DirectionConstants ()
	{
		// Static class, no public constructor.
	}
}
