package geopod.utils.geometry;

import geopod.utils.coordinate.Java3dCoordinateUtility;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.PickConeRay;
import javax.media.j3d.PickCylinderRay;
import javax.media.j3d.PickRay;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * PickUtility facilitates in the creation of various
 * {@link javax.media.j3d.PickShape PickShape} used for three dimensional
 * picking. It is necessary to associate a {@link Canvas3D} prior to
 * 
 * @author Geopod Team
 * 
 */
public class PickUtility
{
	private static Canvas3D m_canvas;

	static
	{
		m_canvas = null;
	}

	/**
	 * Sets the {@link Canvas3D} associated with this utility. It is necessary
	 * to call this method prior to using other methods in this utility.
	 * 
	 * @param canvas
	 *            the canvas that this Utility will use
	 */
	public static void setCanvas (Canvas3D canvas)
	{
		m_canvas = canvas;
	}

	/**
	 * Returns a {@link PickConeRay} at the given screen coordinates with the
	 * specified tolerance
	 * 
	 * @param screenX
	 *            the x value in screen coordinates
	 * @param screenY
	 *            the y value in screen coordinates
	 * @param tolerance
	 *            the tolerance value in number of pixels
	 * @return an infinite cone pick shape
	 */
	public static PickConeRay computePickConeRay (int screenX, int screenY, int tolerance)
	{
		Point3d mousePointInPlate = new Point3d ();
		m_canvas.getPixelLocationInImagePlate (screenX, screenY, mousePointInPlate);
		Point3d mouseDeltaXPointInPlate = new Point3d ();
		// Adding tolerance to screenX so we can set the angle for picking
		m_canvas.getPixelLocationInImagePlate (screenX + tolerance, screenY, mouseDeltaXPointInPlate);
		Point3d eyePointInPlate = new Point3d ();
		m_canvas.getCenterEyeInImagePlate (eyePointInPlate);

		// Compute spread angle
		Vector3d eyeToMouseInPlate = new Vector3d ();
		eyeToMouseInPlate.sub (mousePointInPlate, eyePointInPlate);

		Vector3d eyeToMouseDelta = new Vector3d ();
		eyeToMouseDelta.sub (mouseDeltaXPointInPlate, eyePointInPlate);
		double spreadAngle = eyeToMouseInPlate.angle (eyeToMouseDelta);

		// Better to use dot product than atan (not right triangle)
		// double mouseToDeltaLength = mouseToDelta.length () * tolerance;
		// double eyeToMouseLength = eyeToMouseInPlate.length ();
		// double spreadAngle = Math.atan (mouseToDeltaLength /
		// eyeToMouseLength);

		// Compute eye to mouse point ray in world space
		Point3d eyePoint = Java3dCoordinateUtility.convertImagePlatePointToWorld (eyePointInPlate);
		Point3d mousePoint = Java3dCoordinateUtility.convertImagePlatePointToWorld (mousePointInPlate);
		Vector3d eyeToMouseDir = new Vector3d ();
		eyeToMouseDir.sub (mousePoint, eyePoint);
		eyeToMouseDir.normalize ();
		PickConeRay pickRay = new PickConeRay (eyePoint, eyeToMouseDir, spreadAngle);

		return (pickRay);
	}

	/**
	 * Returns a {@link PickRay} at the specified screen coordinates
	 * 
	 * @param screenX
	 *            the x value in screen coordinates
	 * @param screenY
	 *            the y value in screen coordinates
	 * @return an infinite ray pick shape
	 */
	public static PickRay computePickRay (int screenX, int screenY)
	{
		Point2d mouseScreen = new Point2d (screenX, screenY);
		Point3d mouseWorld = Java3dCoordinateUtility.convertScreenPointToWorld (mouseScreen);
		Point3d eyeWorld = Java3dCoordinateUtility.getEyePointInWorld ();

		Vector3d eyeToMouseDir = new Vector3d ();
		eyeToMouseDir.sub (mouseWorld, eyeWorld);
		eyeToMouseDir.normalize ();
		PickRay pickRay = new PickRay (eyeWorld, eyeToMouseDir);

		return (pickRay);
	}

	/**
	 * Returns a {@link PickCylinderRay} from the specified parameters
	 * 
	 * @param screenX
	 *            the x component of the cylinder's origin in screen coordinates
	 * @param screenY
	 *            the y component of the cylinder's origin in screen coordinates
	 * @param radius
	 *            the radius of the cylinder
	 * @return an infinite cylindrical ray pick shape
	 */
	public static PickCylinderRay computePickCylinderRay (int screenX, int screenY, double radius)
	{
		PickRay pickRay = computePickRay (screenX, screenY);
		Point3d origin = new Point3d ();
		Vector3d dir = new Vector3d ();
		pickRay.get (origin, dir);
		PickCylinderRay pickCylRay = new PickCylinderRay (origin, dir, radius);

		return (pickCylRay);
	}

}
