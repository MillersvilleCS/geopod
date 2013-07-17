package geopod.utils.geometry;

import geopod.utils.idv.SceneGraphControl;

import java.awt.Color;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.PickInfo;
import javax.media.j3d.PickRay;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

public class IsosurfaceRayCaster
{
	/**
	 * The group to cast rays into.
	 */
	private BranchGroup m_volumeGroup;

	/**
	 * Used to change the color of the intersection point on the isosurface.
	 */
	private ColorUpdater m_colorUpdater;

	/**
	 * Cache the points of the intersected triangle
	 */
	private Point3d[] m_triangleCache;

	/**
	 * Construct an IsosurfaceRayCaster.
	 */
	public IsosurfaceRayCaster ()
	{
		m_volumeGroup = SceneGraphControl.getIdvContentBranch ();

		m_colorUpdater = new ColorUpdater (Color.YELLOW);

		m_triangleCache = new Point3d[3];
		m_triangleCache[0] = new Point3d ();
		m_triangleCache[1] = new Point3d ();
		m_triangleCache[2] = new Point3d ();
	}

	/**
	 * Find the nearest isosurface collision point by casting two rays and
	 * taking the nearest collision point. To follow an isosurface, the two rays
	 * are cast parallel to the surface normal from the previous collision.
	 * 
	 * @param currentLocation
	 *            - the location to test from.
	 * @param previousSurfaceNormal
	 *            - the previous surface normal to cast parallel to.
	 * @return the collision point (index 0) and surface normal (index 1).
	 */
	public Tuple3d[] computeNearestCollision (Point3d currentLocation, Vector3d previousSurfaceNormal)
	{
		TransformGroup volumeTransform = SceneGraphControl.getDataVolumeTransform ();
		Transform3D dataTransform = new Transform3D ();
		volumeTransform.getTransform (dataTransform);

		// Create the up ray.
		Vector3d rayDirection = new Vector3d (previousSurfaceNormal);
		PickRay rayUp = new PickRay (currentLocation, rayDirection);

		// Create the down ray.
		rayDirection.negate ();
		PickRay rayDown = new PickRay (currentLocation, rayDirection);

		// Cast the rays.
		PickInfo pickInfoUp = m_volumeGroup.pickClosest (PickInfo.PICK_GEOMETRY, PickInfo.ALL_GEOM_INFO, rayUp);
		PickInfo pickInfoDown = m_volumeGroup.pickClosest (PickInfo.PICK_GEOMETRY, PickInfo.ALL_GEOM_INFO, rayDown);

		// Find the closest non-null pick info.
		PickInfo pickInfoClosest = null;
		if (pickInfoUp != null && pickInfoDown != null)
		{
			double upDistance = pickInfoUp.getClosestDistance ();
			double downDistance = pickInfoDown.getClosestDistance ();

			// Select the closer of the two pickInfos.
			pickInfoClosest = (upDistance < downDistance) ? pickInfoUp : pickInfoDown;
		}
		else if (pickInfoUp != null)
		{
			pickInfoClosest = pickInfoUp;
		}
		else if (pickInfoDown != null)
		{
			pickInfoClosest = pickInfoDown;
		}

		// If we have found a pickInfo, calculate the intersection point and
		// surface normal from it.
		if (pickInfoClosest != null)
		{
			PickInfo.IntersectionInfo[] intersectionInfo = pickInfoClosest.getIntersectionInfos ();
			Point3d intersectionPoint = intersectionInfo[0].getIntersectionPoint ();
			TriangleStripArray triStrip = (TriangleStripArray) intersectionInfo[0].getGeometry ();

			// Update vertex colors
			int[] indices = intersectionInfo[0].getVertexIndices ();
			m_colorUpdater.setColorIndices (indices);
			triStrip.updateData (m_colorUpdater);

			// Get triangle vertices as Point3ds.
			float[] vertices = triStrip.getCoordRefFloat ();
			Point3d point1 = this.createPoint3d (vertices, indices[0]);
			Point3d point2 = this.createPoint3d (vertices, indices[1]);
			Point3d point3 = this.createPoint3d (vertices, indices[2]);

			Vector3d surfaceNormal = this.computeNormal (point1, point2, point3);
			dataTransform.transform (intersectionPoint);

			Tuple3d[] intersectionPtAndNormal = { intersectionPoint, surfaceNormal };

			return (intersectionPtAndNormal);
		}

		// No intersection found.
		return (null);
	}

	/**
	 * Create a Point3d with values from an array of floats.
	 * 
	 * @param vertices
	 *            - the array of vertices to use.
	 * @param index
	 *            - the starting index of the point. This method will use the
	 *            value at this index and the two after it to construct the
	 *            point.
	 * @return
	 */
	private Point3d createPoint3d (float[] vertices, int index)
	{
		final int COMPONENTS_PER_VERTEX = 3;
		int pointIndex = index * COMPONENTS_PER_VERTEX;
		Point3d coord = new Point3d (vertices[pointIndex + 0], vertices[pointIndex + 1], vertices[pointIndex + 2]);

		return (coord);
	}

	/**
	 * Calculate the surface normal of a triangle defined by three points.
	 * 
	 * @param point1
	 * @param point2
	 * @param point3
	 * @return
	 */
	private Vector3d computeNormal (Point3d point1, Point3d point2, Point3d point3)
	{
		Vector3d sideA = new Vector3d ();
		sideA.sub (point2, point1);
		Vector3d sideB = new Vector3d ();
		sideB.sub (point3, point1);
		Vector3d normal = new Vector3d ();
		normal.cross (sideA, sideB);
		normal.normalize ();

		return (normal);
	}

}
