package geopod.interpolators;

import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;

import com.sun.j3d.internal.J3dUtilsI18N;

public class TCBKeyFrame
{
	// Position, Rotation and Scale
	public Point3f position;
	public Quat4f quat;
	public Point3f scale;

	// Tension, Continuity & Bias
	public float tension;
	public float continuity;
	public float bias;

	// Sample Time
	public float knot;

	// Interpolation type (linear = 0 -> spline interpolation)
	public int linear;

	// default constructor
	TCBKeyFrame ()
	{
		tension = continuity = bias = 0.0f;
	}

	public TCBKeyFrame (TCBKeyFrame kf)
	{
		this (kf.knot, kf.linear, kf.position, kf.quat, kf.scale, kf.tension, kf.continuity, kf.bias);

	}

	/**
	 * Creates a key frame using the given inputs.
	 * 
	 * @param k
	 *            knot value for this key frame
	 * @param l
	 *            the linear flag (0 - Spline Interp, 1, Linear Interp
	 * @param pos
	 *            the position at the key frame
	 * @param q
	 *            the rotations at the key frame
	 * @param s
	 *            the scales at the key frame
	 * @param t
	 *            tension (-1.0 < t < 1.0)
	 * @param c
	 *            continuity (-1.0 < c < 1.0)
	 * @param b
	 *            bias (-1.0 < b < 1.0)
	 */
	public TCBKeyFrame (float k, int l, Point3f pos, Quat4f q, Point3f s, float t, float c, float b)
	{

		knot = k;
		linear = l;
		position = new Point3f (pos);
		quat = new Quat4f (q);
		scale = new Point3f (s);

		// Check for valid tension continuity and bias values
		if (t < -1.0f || t > 1.0f)
		{
			throw new IllegalArgumentException (J3dUtilsI18N.getString ("TCBKeyFrame0"));
		}

		if (b < -1.0f || b > 1.0f)
		{
			throw new IllegalArgumentException (J3dUtilsI18N.getString ("TCBKeyFrame1"));
		}

		if (c < -1.0f || c > 1.0f)
		{
			throw new IllegalArgumentException (J3dUtilsI18N.getString ("TCBKeyFrame2"));
		}

		// copy valid tension, continuity and bias values
		tension = t;
		continuity = c;
		bias = b;
	}

	/**
	 * Prints information contained in this key frame
	 * 
	 * @param tag
	 *            string tag for identifying debug message
	 */
	public void debugPrint (String tag)
	{
		System.out.println ("\n" + tag);
		System.out.println (" knot = " + knot);
		System.out.println (" linear = " + linear);
		System.out.println (" position(x,y,z) = " + position.x + " " + position.y + " " + position.z);

		System.out.println (" tension = " + tension);
		System.out.println (" continuity = " + continuity);
		System.out.println (" bias = " + bias);
	}
}
