package geopod.utils.math;

import geopod.utils.TransformGroupControl;

import javax.vecmath.Matrix3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

/**
 * Class providing matrix functions.
 * 
 * @author Geopod Team
 * 
 */
public class MatrixUtility
{
	private static final Vector3d ZERO_VECTOR = new Vector3d (0, 0, 0);

	private MatrixUtility ()
	{
		// Static class
	}

	/**
	 * Create a rotation matrix with up and forward vectors.
	 * 
	 * @param forward
	 *            - the forward vector
	 * @param up
	 *            - the up vector
	 * @return the constructed rotation matrix.
	 */
	public static Matrix3d createRotationMatrix (Vector3d forward, Vector3d up)
	{
		Vector3d rightNorm = new Vector3d ();
		rightNorm.cross (forward, up);
		boolean isZero = rightNorm.epsilonEquals (ZERO_VECTOR, TransformGroupControl.EPSILON);
		if (isZero)
		{
			// Cross product is zero vector
			Matrix3d identity = new Matrix3d ();
			identity.setIdentity ();
			return (identity);
		}
		rightNorm.normalize ();
		Vector3d forwardNorm = new Vector3d ();
		forwardNorm.normalize (forward);
		Vector3d upNorm = new Vector3d ();
		upNorm.cross (rightNorm, forwardNorm);

		Matrix3d rotation = new Matrix3d ();
		rotation.setColumn (0, rightNorm);
		rotation.setColumn (1, upNorm);
		// Need to store backward b/c of RHS
		forwardNorm.negate ();
		rotation.setColumn (2, forwardNorm);

		return (rotation);
	}

	/**
	 * Create a quaternion with up and forward vectors.
	 * 
	 * @param forward
	 *            - the forward vector
	 * @param up
	 *            - the up vector
	 * @return the constructed quaternion.
	 */
	public static Quat4d createRotation (Vector3d forward, Vector3d up)
	{
		Matrix3d rotation = MatrixUtility.createRotationMatrix (forward, up);
		Quat4d rotationQuat = new Quat4d ();
		rotationQuat.set (rotation);

		return (rotationQuat);
	}

	/**
	 * Return the angle between two {@link Quat4d quaternions}.
	 * 
	 * @param quat1
	 *            - the source quaternion
	 * @param quat2
	 *            - the destination quaternion.
	 * @return the angle in radians
	 */
	public static double computeAngleDifference (Quat4d quat1, Quat4d quat2)
	{
		Quat4d delta = computeRotationDelta (quat1, quat2);

		return (2.0 * Math.acos (delta.w));
	}

	/**
	 * <p>
	 * Compute the quaternion that will rotate "srcQuat" to "destQuat".
	 * </p>
	 * <p>
	 * This will return a delta quaternion such that
	 * 
	 * <pre>
	 * srcQuat * deltaQuat = destQuat
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param srcQuat
	 *            - the quaternion representing the starting rotation.
	 * @param destQuat
	 *            - the quaternion representing the ending rotation.
	 * @return the delta quaternion.
	 */
	public static Quat4d computeRotationDelta (Quat4d srcQuat, Quat4d destQuat)
	{
		Quat4d delta = new Quat4d ();
		delta.inverse (srcQuat);
		delta.mul (destQuat);
		delta.normalize ();
		// Ensure rotation angle is always positive for consistency
		if (delta.w < 0.0)
		{
			delta.w = -delta.w;
			delta.x = -delta.x;
			delta.y = -delta.y;
			delta.z = -delta.z;
		}

		return (delta);
	}

}
