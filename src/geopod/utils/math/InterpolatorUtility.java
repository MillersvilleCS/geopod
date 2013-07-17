package geopod.utils.math;

import geopod.interpolators.TCBKeyFrame;

import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class InterpolatorUtility
{
	private static final int SPLINE;
	
	@SuppressWarnings("unused")
	private static final int LINEAR;

	static
	{
		SPLINE = 0;
		LINEAR = 1;
	}
	
	public static TCBKeyFrame[] createKeys (Transform3D start, Transform3D end)
	{
		TCBKeyFrame[] keyFrames = new TCBKeyFrame[2];
		keyFrames[0] = InterpolatorUtility.createTcbKeyFrame (0.0f, start);
		keyFrames[1] = InterpolatorUtility.createTcbKeyFrame (1.0f, end);

		return (keyFrames);
	}

	public static TCBKeyFrame createTcbKeyFrame (float knotValue, Transform3D pose)
	{
		float tension = 0.0f;
		float continuity = 0.0f;
		float bias = 0.0f;

		Vector3f translation = new Vector3f ();
		pose.get (translation);
		Point3f position = new Point3f (translation);

		Quat4f rotation = new Quat4f ();
		pose.get (rotation);

		Vector3d scaleVec = new Vector3d ();
		pose.getScale (scaleVec);
		Point3f scale = new Point3f (scaleVec);

		TCBKeyFrame key = new TCBKeyFrame (knotValue, SPLINE, position, rotation, scale, tension, continuity, bias);

		return (key);
	}
}
