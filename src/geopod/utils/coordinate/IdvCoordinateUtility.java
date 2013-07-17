package geopod.utils.coordinate;

import java.rmi.RemoteException;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import ucar.unidata.idv.control.DisplayControlImpl;
import ucar.unidata.view.geoloc.NavigatedDisplay;
import visad.VisADException;
import visad.georef.EarthLocation;
import visad.georef.LatLonPoint;

public class IdvCoordinateUtility
{
	private static DisplayControlImpl m_displayControl;
	private static NavigatedDisplay m_navigatedDisplay;

	private IdvCoordinateUtility ()
	{
		// Static class
	}

	/**
	 * Constructor.
	 * 
	 * @param displayControl
	 *            - the display control for the coordinate utility to use.
	 */
	public static void setGeopodPlugin (DisplayControlImpl displayControl)
	{
		m_displayControl = displayControl;
		m_navigatedDisplay = m_displayControl.getNavigatedDisplay ();
	}

	/**
	 * Convert from box (data volume) coordinates to an earth location
	 * 
	 * @param boxPoint
	 *            - a point in box coordinates
	 * @return an earth location (latitude, longitude, altitude)
	 */
	public static EarthLocation convertBoxToEarth (Point3d boxPoint)
	{
		// Use "false" as last argument to prevent altitude from getting set to
		// zero.
		EarthLocation el = m_navigatedDisplay.getEarthLocation (boxPoint.x, boxPoint.y, boxPoint.z, false);

		return (el);
	}

	public static EarthLocation convertBoxToEarth (Point3f boxPoint)
	{
		// Use "false" as last argument to prevent altitude from getting set to
		// zero.
		EarthLocation el = m_navigatedDisplay.getEarthLocation (boxPoint.x, boxPoint.y, boxPoint.z, false);

		return (el);
	}

	/**
	 * Convert from box (data volume) to world coordinates
	 * 
	 * @param boxPoint
	 *            - a point in box coordinates
	 * @return a point in world coordinates
	 */
	public static Point3d convertBoxToWorld (Point3d boxPoint)
	{
		final double[] boxToWorldArray = m_navigatedDisplay.getProjectionMatrix ();
		final Matrix4d boxToWorld = new Matrix4d (boxToWorldArray);

		Point3d worldPoint = new Point3d ();
		boxToWorld.transform (boxPoint, worldPoint);

		return (worldPoint);
	}

	/**
	 * Convert an earth location to box (data volume) coordinates.
	 * 
	 * @param el
	 *            - an earth location (latitude, longitude, altitude)
	 * @return a point in box coordinates
	 */
	public static Point3d convertEarthToBox (EarthLocation el)
	{
		double[] boxPointArray = null;
		try
		{
			boxPointArray = m_displayControl.earthToBox (el);
		}
		catch (final RemoteException e)
		{
			e.printStackTrace ();
		}
		catch (final VisADException e)
		{
			e.printStackTrace ();
		}
		final Point3d boxPoint = new Point3d (boxPointArray);

		return (boxPoint);
	}

	/**
	 * Convert an earth location to box (data volume) coordinates.
	 * 
	 * @param el
	 *            - an earth location (latitude, longitude, altitude)
	 * @return a point in box coordinates
	 */
	public static Point3f convertEarthToBoxFloat (EarthLocation el)
	{
		Point3d boxPointDouble = convertEarthToBox (el);
		final Point3f boxPoint = new Point3f (boxPointDouble);

		return (boxPoint);
	}

	/**
	 * Convert an earth location to an earth location.
	 * 
	 * @param el
	 *            - an earth location (latitude, longitude, altitude)
	 * @return a point in world coordinates
	 */
	public static Point3d convertEarthToWorld (EarthLocation el)
	{
		final Point3d boxPoint = IdvCoordinateUtility.convertEarthToBox (el);
		final Point3d worldPoint = IdvCoordinateUtility.convertBoxToWorld (boxPoint);

		return (worldPoint);
	}

	/**
	 * Convert from world to box (data volume) coordinates
	 * 
	 * @param worldPoint
	 *            - a point in world coordinates
	 * @return a point in IDV box coordinates
	 */
	public static Point3d convertWorldToBox (Point3d worldPoint)
	{
		final double[] boxToWorldArray = m_navigatedDisplay.getProjectionMatrix ();
		final Matrix4d worldToBox = new Matrix4d (boxToWorldArray);
		worldToBox.invert ();

		final Point3d boxPoint = new Point3d ();
		worldToBox.transform (worldPoint, boxPoint);

		return (boxPoint);
	}

	/**
	 * Convert from world coordinates to an earth location (lat, lon, alt)
	 * coordinates
	 * 
	 * @param worldPoint
	 *            - a point in world coordinates
	 * @return an earth location (latitude, longitude, altitude)
	 */
	public static EarthLocation convertWorldToEarth (Point3d worldPoint)
	{
		// FIXME: convertWorldToBox is not working if the world is rotated
		// The current workaround it to always reset to a top view when the plugin gains focus.
		final Point3d boxPoint = IdvCoordinateUtility.convertWorldToBox (worldPoint);
		final EarthLocation el = IdvCoordinateUtility.convertBoxToEarth (boxPoint);

		return (el);
	}

	/**
	 * Return a string representing a lat/lon point with the lat restricted to
	 * [-90, 90) and the lon restricted to [-180, 180].
	 * 
	 * @param point
	 *            - the {@link LatLonPoint} to create a string for.
	 * @return - a string containing the normalized name.
	 */
	public static String getNormalizedString (LatLonPoint point)
	{
		double lat = point.getLatitude ().getValue ();
		double lon = point.getLongitude ().getValue ();

		lat = (lat <= 90) ? lat : lat - 90.0;
		lon = (lon <= 180) ? lon : lon - 360.0;

		return String.format ("Lat: %.3f Lon: %.3f", lat, lon);
	}
}
