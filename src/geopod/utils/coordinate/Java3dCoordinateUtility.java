package geopod.utils.coordinate;

import java.awt.Point;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.View;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import com.sun.j3d.utils.universe.ViewInfo;

/**
 * Provides methods to convert between Java3D coordinate systems.
 * 
 * @author Geopod Team
 * 
 */
public class Java3dCoordinateUtility
{

	private static Canvas3D m_canvas;
	private static View m_view;
	private static ViewInfo m_viewInfo;

	static
	{
		m_canvas = null;
		m_view = null;
		m_viewInfo = null;
		//		m_transform = new Transform3D ();
	}

	private Java3dCoordinateUtility ()
	{
		// Static class, no constructor.
	}

	/**
	 * Set the canvas for this coordinate utility to use. This must be called
	 * before any other method in this class.
	 */
	public static void setCanvas (Canvas3D canvas)
	{
		m_canvas = canvas;
		// Each canvas has a single view
		m_view = canvas.getView ();
		m_viewInfo = new ViewInfo (m_view);
	}

	/**
	 * Get the aspect ratio (width / height) of the canvas.
	 * 
	 * @throws NullPointerException
	 *             if the canvas has not been set.
	 */
	private static double getCanvasAspectRatio ()
	{
		int canvasWidth = m_canvas.getWidth ();
		int canvasHeight = m_canvas.getHeight ();

		return ((double) canvasWidth / canvasHeight);
	}

	/**
	 * Convert a point in eye coordinates to screen coordinates.
	 * 
	 * @param eyePoint
	 *            - the point in eye coordinates to convert.
	 * 
	 * @return the equivalent point in screen coordinates.
	 */
	public static Point2d convertEyePointToScreen (Point3d eyePoint)
	{
		Transform3D eyeToPlate = new Transform3D ();
		m_viewInfo.getEyeToImagePlate (m_canvas, eyeToPlate, null);
		Point3d platePoint = new Point3d ();
		eyeToPlate.transform (eyePoint, platePoint);
		Point2d screenPoint = convertImagePlatePointToScreen (platePoint);

		return (screenPoint);
	}

	/**
	 * Convert a point in screen coordinates to eye coordinates.
	 * 
	 * @param screenPoint
	 *            - the screen point to convert.
	 * @return the equivalent point in eye coordinates.
	 */
	public static Point3d convertScreenPointToWorld (Point2d screenPoint)
	{
		Point3d imagePlatePoint = new Point3d ();
		m_canvas.getPixelLocationInImagePlate (screenPoint, imagePlatePoint);
		Point3d worldPoint = convertImagePlatePointToWorld (imagePlatePoint);

		return (worldPoint);
	}

	/**
	 * Convert from screen to eye (VPC) space for a given depth (screenPoint.z)
	 * Depth is given in eye coordinates (negative values are in front of the
	 * eye)
	 * 
	 * @param screenPoint
	 *            - the screen point to convert
	 * @return the equivalent eye point
	 */
	public static Point3d convertScreenPointToEye (Point3d screenPoint)
	{
		int screenX = (int) screenPoint.x;
		int screenY = (int) screenPoint.y;
		Point2d ndcPoint = convertScreenPointToNdc (screenX, screenY);
		double xn = ndcPoint.x;
		double yn = ndcPoint.y;
		double fov = m_view.getFieldOfView ();
		double tanOfFovOver2 = Math.tan (fov / 2.0);
		double viewWidthScale = -screenPoint.z * tanOfFovOver2;
		double viewHeightScale = viewWidthScale / getCanvasAspectRatio ();
		double xv = xn * viewWidthScale;
		double yv = yn * viewHeightScale;
		double zv = screenPoint.z;
		Point3d eyePoint = new Point3d (xv, yv, zv);

		return (eyePoint);
	}

	/**
	 * 
	 * @param worldPoint
	 *            - the world point to convert
	 * @return the equivalent screen point.
	 */
	public static Point2d convertWorldPointToScreen (Point3d worldPoint)
	{
		Transform3D worldToPlate = new Transform3D ();
		m_canvas.getVworldToImagePlate (worldToPlate);
		Point3d platePoint = new Point3d ();
		worldToPlate.transform (worldPoint, platePoint);
		Point2d pixelPoint = convertImagePlatePointToScreen (platePoint);

		return (pixelPoint);
	}

	/**
	 * Converts from image plate to screen coordinates.
	 * 
	 * @param platePoint
	 *            - the image plate point to convert.
	 * @return - the equivalent point in screen coordinates.
	 */
	public static Point2d convertImagePlatePointToScreen (Point3d platePoint)
	{
		Point2d screenPoint = new Point2d ();
		m_canvas.getPixelLocationFromImagePlate (platePoint, screenPoint);

		return (screenPoint);
	}

	/**
	 * Converts from screen to world coordinates.
	 * 
	 * @param platePoint
	 *            - the image plate point to convert.
	 * @return - the equivalent point in world coordinates.
	 */
	public static Point3d convertImagePlatePointToWorld (Point3d platePoint)
	{
		Transform3D plateToWorld = new Transform3D ();
		m_canvas.getImagePlateToVworld (plateToWorld);
		Point3d worldPoint = new Point3d ();
		plateToWorld.transform (platePoint, worldPoint);

		return (worldPoint);
	}

	/**
	 * Convert from local to world coordinates.
	 * 
	 * @param localPoint
	 *            - the local point to convert.
	 * @param nodeDefiningLocalFrame
	 *            - the scene graph {@link Node node} defining the local
	 *            reference frame.
	 * @return - the equivalent world point.
	 */
	public static Point3d convertLocalPointToWorld (Point3d localPoint, Node nodeDefiningLocalFrame)
	{
		Transform3D localToWorld = new Transform3D ();
		nodeDefiningLocalFrame.getLocalToVworld (localToWorld);
		Point3d worldPoint = new Point3d ();
		localToWorld.transform (localPoint, worldPoint);

		return (worldPoint);
	}

	/**
	 * @return the center eye point in world coordinates
	 */
	public static Point3d getEyePointInWorld ()
	{
		Point3d eyePoint = new Point3d ();
		m_canvas.getCenterEyeInImagePlate (eyePoint);
		Point3d eyePointWorld = convertImagePlatePointToWorld (eyePoint);

		return (eyePointWorld);
	}

	/**
	 * Convert a point from screen coordinates to eye (VPC) coordinates. Use
	 * "vpcZ" as the depth of the point relative to the eye This method strictly
	 * uses J3D conversion routines and should yield the same results as
	 * convertScreenPointToEye, but will not be as efficient Use this as a
	 * verification routine only
	 * 
	 * @param screenPoint
	 *            - the screen point to convert
	 * @param vpcZ
	 *            -
	 * 
	 * @return the point in view platform coordinates.
	 */
	public static Point3d convertScreenPointToVpc (Point screenPoint, double vpcZ)
	{
		Point3d platePoint = new Point3d ();
		m_canvas.getPixelLocationInImagePlate (screenPoint.x, screenPoint.y, platePoint);

		Transform3D plateToVpc = new Transform3D ();
		m_viewInfo.updateViewPlatform ();
		m_viewInfo.getImagePlateToViewPlatform (m_canvas, plateToVpc, null);
		Point3d vpcPoint = new Point3d ();
		plateToVpc.transform (platePoint, vpcPoint);
		// View volume is in the -z half-space
		// Compute projected values for x and y
		// J3D uses an eye depth relative to the image plate (canvas) to match the field of view
		// Code below compensates for this
		vpcPoint.x *= vpcZ / vpcPoint.z;
		vpcPoint.y *= vpcZ / vpcPoint.z;
		vpcPoint.z = vpcZ;

		return (vpcPoint);
	}

	/**
	 * convert physical units to virtual units.
	 * 
	 * @param physicalUnits
	 *            - the distance in physical units to convert
	 * @return - the equivalent distance in virtual units.
	 */
	public static double convertPhysicalToVirtual (double physicalUnits)
	{
		double physicalScale = m_viewInfo.getPhysicalToVirtualScale (m_canvas);

		return (physicalUnits * physicalScale);
	}

	// Map screen point into [-1, +1] in both x and y
	private static Point2d convertScreenPointToNdc (int screenX, int screenY)
	{
		int canvasWidth = m_canvas.getWidth ();
		int canvasHeight = m_canvas.getHeight ();
		double ndcX = -1 + 2 * (double) screenX / canvasWidth;
		// Match Java 3D by using (screenY + 1)
		double ndcY = +1 - 2 * (double) (screenY + 1) / canvasHeight;
		Point2d ndcPoint = new Point2d (ndcX, ndcY);

		return (ndcPoint);
	}

}
