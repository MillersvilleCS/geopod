package geopod.utils;

import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;

import javax.media.j3d.BadTransformException;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3d;

/**
 * A helper class to facilitate manipulation of a {@link TransformGroup}.
 */
public class TransformGroupControl
{
	private static Matrix3d INVERSE_EARTH_ROTATION_MATRIX;

	/**
	 * A list of possible directions to rotate in.
	 */
	public static enum RotationDirection
	{
		/**
		 * Clockwise.
		 */
		CLOCKWISE,
		/**
		 * Counterclockwise.
		 */
		COUNTERCLOCKWISE
	}

	/**
	 * A small value used in floating point comparisons.
	 */
	public static final double EPSILON;

	static
	{
		EPSILON = 1e-4;
		INVERSE_EARTH_ROTATION_MATRIX = new Matrix3d ();
		INVERSE_EARTH_ROTATION_MATRIX.rotX (-Math.PI / 2.0);
	}

	private TransformGroup m_transformGroup;
	private Transform3D m_currentTransform;
	// Avoid reallocations when obtaining the position
	private Vector3d m_cachedTranslation;
	private TransformGroup m_topTransformGroup;

	/**
	 * Construct a transfromGroupControl with the transformGroups is meant to control.
	 * 
	 * @param target
	 *            - the transfromGroup to control.
	 * @param topViewTransform
	 *            - the transform group above top view (mini-map).
	 */
	public TransformGroupControl (TransformGroup target, TransformGroup topViewTransform)
	{
		m_transformGroup = target;
		m_topTransformGroup = topViewTransform;
		m_currentTransform = new Transform3D ();
		m_cachedTranslation = new Vector3d ();
	}

	/**
	 * Set the translation and rotation using the given pose.
	 * 
	 * @param pose
	 */
	public synchronized void setPose (Pose pose)
	{
		m_currentTransform.set (pose.getRotation (), pose.getPosition (), 1);
		m_transformGroup.setTransform (m_currentTransform);

		this.updateTopDownView ();
	}

	// 
	/**
	 * Ensure axes are orthonormal. This corrects for drift over repeated transformations.
	 */
	public synchronized void orthonormalize ()
	{
		m_transformGroup.getTransform (m_currentTransform);

		Matrix3d rotation = new Matrix3d ();
		Vector3d translation = new Vector3d ();
		m_currentTransform.get (rotation, translation);

		Vector3d backward = new Vector3d ();
		rotation.getColumn (2, backward);
		Vector3d right = new Vector3d ();
		rotation.getColumn (0, right);

		backward.normalize ();
		Vector3d up = new Vector3d ();
		right.cross (up, backward);
		right.normalize ();
		up.cross (backward, right);

		rotation.setColumn (0, right);
		rotation.setColumn (1, up);
		rotation.setColumn (2, backward);

		m_currentTransform.set (rotation, translation, 1.0);
		m_transformGroup.setTransform (m_currentTransform);
	}

	/**
	 * Set the transform using the provided basis vectors and translation point.
	 * 
	 * @param forward
	 * @param up
	 * @param translation
	 */
	public synchronized void setMatrix (Vector3d forward, Vector3d up, Point3d translation)
	{
		Vector3d scale = new Vector3d (1.0, 1.0, 1.0);
		this.setMatrix (forward, up, new Vector3d (translation), scale);
	}

	/**
	 * Set the transform using the provided basis vectors and translation vector.
	 * 
	 * @param forward
	 * @param up
	 * @param translation
	 */
	public synchronized void setMatrix (Vector3d forward, Vector3d up, Vector3d translation)
	{
		Vector3d scale = new Vector3d (1.0, 1.0, 1.0);
		this.setMatrix (forward, up, translation, scale);
	}

	/**
	 * Set the transform using the provided basis vectors and LOCAL scale Wrapper function to handle Point3d
	 * translations.
	 * 
	 * @param forward
	 * @param up
	 * @param translation
	 * @param scale
	 */
	public synchronized void setMatrix (Vector3d forward, Vector3d up, Point3d translation, Vector3d scale)
	{
		this.setMatrix (forward, up, new Vector3d (translation), scale);
	}

	/**
	 * Set the transform using the provided basis vectors and LOCAL scale
	 * 
	 * @param forward
	 * @param up
	 * @param translation
	 * @param scale
	 */
	public synchronized void setMatrix (Vector3d forward, Vector3d up, Vector3d translation, Vector3d scale)
	{
		Vector3d rightNorm = new Vector3d ();
		rightNorm.cross (forward, up);
		rightNorm.normalize ();
		Vector3d forwardNorm = new Vector3d ();
		forwardNorm.normalize (forward);
		Vector3d upNorm = new Vector3d ();
		upNorm.cross (rightNorm, forwardNorm);

		Matrix3d m3d = new Matrix3d ();
		rightNorm.scale (scale.x);
		m3d.setColumn (0, rightNorm);
		upNorm.scale (scale.y);
		m3d.setColumn (1, upNorm);
		forwardNorm.scale (-scale.z);
		m3d.setColumn (2, forwardNorm);

		m_currentTransform.set (m3d, translation, 1.0);
		m_transformGroup.setTransform (m_currentTransform);
	}

	/**
	 * Set the rotation component using the provided forward and up basis vectors.
	 * 
	 * @param forward
	 * @param up
	 * @param preserveScale
	 */
	public synchronized void setRotation (Vector3d forward, Vector3d up, boolean preserveScale)
	{
		Vector3d scale = new Vector3d ();
		if (preserveScale)
		{
			m_transformGroup.getTransform (m_currentTransform);
			m_currentTransform.getScale (scale);
		}
		Point3d position = this.getPosition ();
		Vector3d translation = new Vector3d (position);
		if (preserveScale)
		{
			this.setMatrix (forward, up, translation, scale);
		}
		else
		{
			this.setMatrix (forward, up, translation);
		}
	}

	/**
	 * @param scaleFactor
	 *            - the scale factor in local coordinates.
	 */
	public synchronized void scaleLocal (double scaleFactor)
	{
		m_transformGroup.getTransform (m_currentTransform);
		Transform3D scaleTransform = new Transform3D ();
		scaleTransform.set (scaleFactor);
		m_currentTransform.mul (scaleTransform);
		m_transformGroup.setTransform (m_currentTransform);
	}

	/**
	 * @param scaleFactor
	 *            - the scale factor in world coordinates.
	 */
	public synchronized void scaleWorld (double scaleFactor)
	{
		m_transformGroup.getTransform (m_currentTransform);
		Transform3D scaleTransform = new Transform3D ();
		scaleTransform.set (scaleFactor);
		scaleTransform.mul (m_currentTransform);
		m_transformGroup.setTransform (scaleTransform);
	}

	/**
	 * Yaw counterclockwise about the local Y-axis.
	 * 
	 * @param angleDegrees
	 *            - the angle to rotate (in degrees).
	 */
	public synchronized void yaw (double angleDegrees)
	{
		m_transformGroup.getTransform (m_currentTransform);
		Transform3D rotation = new Transform3D ();
		double angleRadians = Math.toRadians (angleDegrees);
		rotation.rotY (angleRadians);
		m_currentTransform.mul (rotation);
		m_transformGroup.setTransform (m_currentTransform);
	}

	/**
	 * Pitch counterclockwise about the local X-axis.
	 * 
	 * @param angleDegrees
	 *            - the angle to rotate (in degrees).
	 */
	public synchronized void pitch (double angleDegrees)
	{
		m_transformGroup.getTransform (m_currentTransform);
		Transform3D rotation = new Transform3D ();
		double angleRadians = Math.toRadians (angleDegrees);
		rotation.rotX (angleRadians);
		m_currentTransform.mul (rotation);
		m_transformGroup.setTransform (m_currentTransform);
	}

	/**
	 * Roll counterclockwise about the local Z-axis.
	 * 
	 * @param angleDegrees
	 *            - the angle to rotate (in degrees).
	 */
	public synchronized void roll (double angleDegrees)
	{
		m_transformGroup.getTransform (m_currentTransform);
		Transform3D rotation = new Transform3D ();
		double angleRadians = Math.toRadians (angleDegrees);
		rotation.rotZ (angleRadians);
		m_currentTransform.mul (rotation);
		m_transformGroup.setTransform (m_currentTransform);
	}

	/**
	 * Rotate about an arbitrary axis defined in local coordinates.
	 * 
	 * @param axis
	 *            - the axis to rotate about
	 * @param angleDegrees
	 *            - the angle to rotate in degrees.
	 */
	public synchronized void rotateLocalAxis (Vector3d axis, double angleDegrees)
	{
		m_transformGroup.getTransform (m_currentTransform);
		Transform3D rotation = new Transform3D ();
		double angleRadians = Math.toRadians (angleDegrees);
		AxisAngle4d axisAngle = new AxisAngle4d (axis, angleRadians);
		rotation.set (axisAngle);
		m_currentTransform.mul (rotation);
		m_transformGroup.setTransform (m_currentTransform);
	}

	/**
	 * Rotate about an arbitrary axis defined in world coordinates.
	 * 
	 * @param axis
	 *            - the axis to rotate about
	 * @param angleDegrees
	 *            - the angle to rotate in degrees.
	 */
	public synchronized void rotateWorldAxis (Vector3d axis, double angleDegrees)
	{
		m_transformGroup.getTransform (m_currentTransform);
		Transform3D rotation = new Transform3D ();
		double angleRadians = Math.toRadians (angleDegrees);
		AxisAngle4d axisAngle = new AxisAngle4d (axis, angleRadians);
		rotation.set (axisAngle);
		rotation.mul (m_currentTransform);
		m_transformGroup.setTransform (rotation);
	}

	/**
	 * 
	 * @param axis
	 *            - the axis to rotate about
	 * @param point
	 *            - the point to rotate around
	 * @param angleDegrees
	 *            - the number of degrees to rotate
	 */
	public synchronized void rotateWorldAxisAboutPoint (Vector3d axis, Point3d point, double angleDegrees)
	{
		m_transformGroup.getTransform (m_currentTransform);
		Transform3D rotate = new Transform3D ();
		double angleRadians = Math.toRadians (angleDegrees);
		AxisAngle4d axisAngle = new AxisAngle4d (axis, angleRadians);
		rotate.set (axisAngle);
		Vector3d translation = new Vector3d (point);
		translation.scale (-1.0);
		rotate.transform (translation);
		translation.add (point);
		rotate.setTranslation (translation);
		rotate.mul (m_currentTransform);
		m_transformGroup.setTransform (rotate);
	}

	/**
	 * Align the local Y axis with a vector in world coordinates.
	 * 
	 * @param worldVector
	 *            - the vector to align with.
	 */
	public synchronized void alignLocalYWithWorldVector (Vector3d worldVector)
	{
		m_currentTransform = createAlignedTransform (worldVector);
		m_transformGroup.setTransform (m_currentTransform);
	}

	/**
	 * Get a copy of the current transform with the pitch and roll removed relative to a given vector. This aligns the
	 * local up vector with the given vector, keeping the right vector constant.
	 * 
	 * @param worldVector
	 *            - the world up vector to be relative to.
	 * 
	 * @return - a {@link Transform3D} that is level with respect to the given up vector.
	 */
	public Transform3D createAlignedTransform (Vector3d worldVector)
	{
		m_transformGroup.getTransform (m_currentTransform);
		Matrix3d rotation = new Matrix3d ();
		Vector3d translation = new Vector3d ();
		m_currentTransform.get (rotation, translation);

		alignRotationWithUpVector (rotation, worldVector);
		Transform3D transform = new Transform3D (rotation, translation, 1.0);

		return transform;
	}

	/**
	 * Align the local up vector with a given world up vector, keeping the right vector constant.
	 * 
	 * @param rotation
	 *            - the {@link Matrix3d} to realign.
	 * @param worldUpVector
	 *            - the up vector in world coordinates to align with.
	 */
	private void alignRotationWithUpVector (Matrix3d rotation, Vector3d worldUpVector)
	{
		Vector3d up = new Vector3d (worldUpVector);
		up.normalize ();
		Vector3d backward = new Vector3d ();
		rotation.getColumn (2, backward);
		backward.normalize ();
		Vector3d right = new Vector3d ();

		// Guard against a cross product that results in a zero vector.
		if (!up.epsilonEquals (backward, EPSILON))
		{
			right.cross (up, backward);
			right.normalize ();
			backward.cross (right, up);
			backward.normalize ();
		}
		else
		{
			// "up" and "backward" coincide in this case.
			rotation.getColumn (0, right);
			backward.cross (right, up);
			backward.normalize ();
			right.cross (up, backward);
			right.normalize ();
		}

		// Set the new rotational components.
		rotation.setColumn (0, right);
		rotation.setColumn (1, up);
		rotation.setColumn (2, backward);
	}

	/**
	 * Move in a direction relative to the local coordinate system.
	 * 
	 * @param direction
	 *            - the direction to move in local coordinates.
	 * @param distance
	 *            - the distance to move.
	 */
	public synchronized void moveLocal (Vector3d direction, double distance)
	{
		m_transformGroup.getTransform (m_currentTransform);
		Vector3d worldDirection = new Vector3d ();
		m_currentTransform.transform (direction, worldDirection);
		this.moveWorld (worldDirection, distance);
	}

	/**
	 * Move in a direction relative to the world coordinate system.
	 * 
	 * @param direction
	 *            - the direction to move in world coordinates.
	 * @param distance
	 *            - the distance to move.
	 */
	public synchronized void moveWorld (Vector3d direction, double distance)
	{
		m_transformGroup.getTransform (m_currentTransform);
		Vector3d translation = new Vector3d ();
		Vector3d scaledDirection = new Vector3d (direction);
		scaledDirection.scale (distance);
		m_currentTransform.get (translation);
		translation.add (scaledDirection);
		m_currentTransform.setTranslation (translation);
		m_transformGroup.setTransform (m_currentTransform);
	}

	/**
	 * Update the top-down(mini-map) view.
	 */
	public void updateTopDownView ()
	{
		Vector3d translation = new Vector3d ();
		m_currentTransform.get (translation);

		Transform3D topTransform = new Transform3D ();
		m_topTransformGroup.getTransform (topTransform);
		topTransform.setTranslation (translation);
		m_topTransformGroup.setTransform (topTransform);
	}

	/**
	 * @return the right basis vector in world coordinates.
	 */
	public synchronized Vector3d getRight ()
	{
		m_transformGroup.getTransform (m_currentTransform);
		double[] matrix = new double[16];
		m_currentTransform.get (matrix);
		Vector3d right = new Vector3d (matrix[0], matrix[4], matrix[8]);

		return (right);
	}

	/**
	 * @return the up basis vector in world coordinates.
	 */
	public synchronized Vector3d getUp ()
	{
		m_transformGroup.getTransform (m_currentTransform);
		double[] matrix = new double[16];
		m_currentTransform.get (matrix);
		Vector3d up = new Vector3d (matrix[1], matrix[5], matrix[9]);

		return (up);
	}

	/**
	 * @return the backward basis vector in world coordinates.
	 */
	public synchronized Vector3d getBackward ()
	{
		m_transformGroup.getTransform (m_currentTransform);
		double[] matrix = new double[16];
		m_currentTransform.get (matrix);
		Vector3d backward = new Vector3d (matrix[2], matrix[6], matrix[10]);

		return (backward);
	}

	/**
	 * @return the forward unit vector in world coordinates.
	 */
	public synchronized Vector3d getForward ()
	{
		m_transformGroup.getTransform (m_currentTransform);
		double[] matrix = new double[16];
		m_currentTransform.get (matrix);
		Vector3d backward = new Vector3d (matrix[2], matrix[6], matrix[10]);
		backward.negate ();

		return (backward);
	}

	/**
	 * @return the translation point in world coordinates.
	 */
	public synchronized Point3d getPosition ()
	{
		m_transformGroup.getTransform (m_currentTransform);
		m_currentTransform.get (m_cachedTranslation);
		Point3d position = new Point3d (m_cachedTranslation);

		return (position);
	}

	/**
	 * Set the given point to the current translation.
	 * 
	 * @param position
	 *            - the point to set.
	 */
	public synchronized void getPosition (Point3d position)
	{
		m_transformGroup.getTransform (m_currentTransform);
		m_currentTransform.get (m_cachedTranslation);
		position.set (m_cachedTranslation);
	}

	/**
	 * Get the {@link Pose} of the controlled transform group. NOTE: Scale is disregarded.
	 * 
	 * @return the pose.
	 */
	public synchronized Pose getPose ()
	{
		m_transformGroup.getTransform (m_currentTransform);
		Pose currentPose = Pose.valueOf (m_currentTransform);

		return (currentPose);
	}

	/**
	 * 
	 * @param transform
	 *            - the {@link Transform3D} representing the current transformation.
	 */
	public synchronized void getTransform (Transform3D transform)
	{
		m_transformGroup.getTransform (transform);
	}

	/**
	 * @return the {@link TransformGroup} controlled by this TransformGroupControl.
	 */
	public synchronized TransformGroup getTransformGroup ()
	{
		return (m_transformGroup);
	}

	/**
	 * Change the {@link TransformGroup} to control.
	 * 
	 * @param transform
	 *            - the transform group to control.
	 */
	public synchronized void setTransformGroup (TransformGroup transform)
	{
		m_transformGroup = transform;
	}

	/**
	 * Set the position. Argument is cast to a {@link Vector3d}.
	 * 
	 * @param point
	 */
	public synchronized void setPosition (Tuple3d point)
	{
		m_currentTransform.setTranslation (new Vector3d (point));
		m_transformGroup.setTransform (m_currentTransform);
	}

	/**
	 * Set the position. Argument is cast to a {@link Vector3d}.
	 * 
	 * @param point
	 */
	public synchronized void setPosition (Tuple3f point)
	{
		m_currentTransform.setTranslation (new Vector3d (point));
		m_transformGroup.setTransform (m_currentTransform);
	}

	/**
	 * Set the position.
	 * 
	 * @param vector
	 */
	public synchronized void setTranslation (Vector3d vector)
	{
		m_currentTransform.setTranslation (vector);
		m_transformGroup.setTransform (m_currentTransform);
	}

	/**
	 * Turn to face a point.
	 * 
	 * @param target
	 *            - the target to look at
	 * @param upVector
	 *            - the desired up vector in world coordinates.
	 */
	public synchronized void lookAt (Point3d target, Vector3d upVector)
	{
		Vector3d forward = new Vector3d ();
		Point3d position = this.getPosition ();
		forward.sub (target, position);

		this.setRotation (forward, upVector, false);
	}

	/**
	 * 
	 * @return the current rotation as a {@link Quat4d quaternion}.
	 */
	public synchronized Quat4d getRotation ()
	{
		m_transformGroup.getTransform (m_currentTransform);
		Quat4d rotation = new Quat4d ();
		m_currentTransform.get (rotation);

		return (rotation);
	}

	/**
	 * 
	 * @param rotation
	 *            - a {@link Quat4d quaternion} rotation value.
	 */
	public synchronized void setRotation (Quat4d rotation)
	{
		try
		{
			m_transformGroup.getTransform (m_currentTransform);

			Vector3d translation = new Vector3d ();
			m_currentTransform.get (translation);
			m_currentTransform.set (rotation, translation, 1.0);

			m_transformGroup.setTransform (m_currentTransform);
		}
		catch (BadTransformException exp)
		{
			/*
			 * Normalize non-congruent matrix using cross products. Standard normalize does not appear to work in this
			 * case, possibly because of an incorrectly set ROTATION_BIT in the transform3D that Java3D dose not handle
			 * correctly. Setting the scale is also necessary before normalization, although I am not sure why. -KPW
			 */
			m_currentTransform.setScale (1.0);
			m_currentTransform.normalizeCP ();
			m_transformGroup.setTransform (m_currentTransform);

			if (Debug.levelAtLeast (DebugLevel.LOW))
			{
				System.err.println ("Bad rotation normalized.");
			}
		}

	}

	/**
	 * Get a vector containing the yaw, pitch, and roll components in radians.
	 * 
	 * Based on examples from http://www.geometrictools.com/Documentation/EulerAngles.pdf
	 * 
	 * @return a vector3d consisting of yaw (x component), pitch (y component), roll (z component);
	 */
	public Vector3d getYawPitchRoll ()
	{
		m_transformGroup.getTransform (m_currentTransform);
		Matrix3d r = new Matrix3d ();
		m_currentTransform.get (r);

		r.mul (INVERSE_EARTH_ROTATION_MATRIX);

		double thetaX, thetaY, thetaZ;

		if (r.m21 < 1)
		{
			if (r.m21 > -1)
			{
				thetaX = Math.asin (r.m21);
				thetaZ = Math.atan2 (-r.m01, r.m11);
				thetaY = Math.atan2 (-r.m20, r.m22);
			}
			else
			// r21 = -1
			{
				// Not a unique solution: thetaY - thetaZ = atan2(r02,r00)
				thetaX = -Math.PI / 2.0;
				thetaZ = -Math.atan2 (r.m02, r.m00);
				thetaY = 0;
			}
		}
		else
		// r21 = +1
		{
			// Not a unique solution: thetaY + thetaZ = atan2(r02,r00)
			thetaX = Math.PI / 2;
			thetaZ = Math.atan2 (r.m02, r.m00);
			thetaY = 0;
		}

		return new Vector3d (thetaX, thetaY, thetaZ);
	}

}
