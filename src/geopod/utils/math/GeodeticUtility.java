package geopod.utils.math;

import javax.vecmath.Point3d;

import visad.SI;
import visad.georef.EarthLocation;

public class GeodeticUtility
{
	/**
	 * The radius of the earth in kilometers.
	 */
	public static final double EARTH_RADIUS_IN_KM;
	/**
	 * The semi-major radius of the earth in kilometers.
	 */
	public static final double EARTH_SEMIMAJOR_RADIUS_IN_KM;
	private static final double EARTH_ELLIPSOID_FLATTENING;
	/**
	 * The semi-minor radius of the earth in kilometers.
	 */
	public static final double EARTH_SEMIMINOR_RADIUS_IN_KM;
	private static final double EARTH_FIRST_ECCENTRICITY;

	@SuppressWarnings("unused")
	private static final double EARTH_SECOND_ECCENTRICITY;
	@SuppressWarnings("unused")
	private static final double EARTH_MINOR_MAJOR_RATIO_SQ;

	static
	{
		EARTH_RADIUS_IN_KM = 6370;
		EARTH_SEMIMAJOR_RADIUS_IN_KM = 6378.137;
		double a = EARTH_SEMIMAJOR_RADIUS_IN_KM;
		EARTH_ELLIPSOID_FLATTENING = 1 / 298.257223563;
		double f = EARTH_ELLIPSOID_FLATTENING;
		EARTH_SEMIMINOR_RADIUS_IN_KM = EARTH_SEMIMAJOR_RADIUS_IN_KM * (1 - f);
		double b = EARTH_SEMIMINOR_RADIUS_IN_KM;
		EARTH_FIRST_ECCENTRICITY = Math.sqrt ((a * a - b * b) / (a * a));
		EARTH_SECOND_ECCENTRICITY = Math.sqrt ((a * a - b * b) / (b * b));
		EARTH_MINOR_MAJOR_RATIO_SQ = (b * b) / (a * a);
	}

	private GeodeticUtility ()
	{
		// Static class
	}

	/**
	 * Compute the great circle distance between two earth locations. The Earth
	 * is treated as a sphere. The average of the two altitudes is used. This
	 * calculation uses the haversine formula.
	 * 
	 * @return the computed distance.
	 * 
	 */
	public static double computeGreatCircleDistanceHaversine (EarthLocation source, EarthLocation destination)
	{
		double sourceLat = Math.toRadians (source.getLatitude ().getValue ());
		double destLat = Math.toRadians (destination.getLatitude ().getValue ());
		double deltaLat = destLat - sourceLat;

		double sourceLon = Math.toRadians (source.getLongitude ().getValue ());
		double destLon = Math.toRadians (destination.getLongitude ().getValue ());
		double deltaLon = destLon - sourceLon;

		double sinLatSq = Math.pow (Math.sin (deltaLat / 2), 2);
		double sinLonSq = Math.pow (Math.sin (deltaLon / 2), 2);
		double a = sinLatSq + Math.cos (sourceLat) * Math.cos (destLat) * sinLonSq;
		double c = 2 * Math.atan2 (Math.sqrt (a), Math.sqrt (1 - a));

		double sourceAlt = getAltitudeInKilometers (source);
		double destAlt = getAltitudeInKilometers (destination);
		double averageAlt = (sourceAlt + destAlt) * 0.5;
		double distance = c * (EARTH_RADIUS_IN_KM + averageAlt);

		return (distance);
	}

	/**
	 * Compute the great circle distance between two earth locations. The Earth
	 * is treated as a sphere. The average of the two altitudes is used. This
	 * calculation uses the spherical law of cosines.
	 * 
	 * @param source
	 * @param destination
	 * @return the computed distance.
	 * 
	 */
	public static double computeGreatCircleDistanceLawCos (EarthLocation source, EarthLocation destination)
	{
		// Dr. Clark's notes
		// dy = (a + z) * dPhi; 
		// dx = (a + z) * cos (phi) * dLambda;

		double sourceAlt = getAltitudeInKilometers (source);
		double destAlt = getAltitudeInKilometers (destination);
		double averageAlt = (sourceAlt + destAlt) * 0.5;

		double sourceLat = Math.toRadians (source.getLatitude ().getValue ());
		double destLat = Math.toRadians (destination.getLatitude ().getValue ());
		double sinProd = Math.sin (sourceLat) * Math.sin (destLat);

		double sourceLon = Math.toRadians (source.getLongitude ().getValue ());
		double destLon = Math.toRadians (destination.getLongitude ().getValue ());
		double deltaLon = destLon - sourceLon;
		double cosProd = Math.cos (sourceLat) * Math.cos (destLat) * Math.cos (deltaLon);
		double arcCos = Math.acos (sinProd + cosProd);

		double distance = arcCos * (EARTH_RADIUS_IN_KM + averageAlt);

		return (distance);
	}

	public static double computeLinearDistance (EarthLocation source, EarthLocation destination)
	{
		Point3d sourceXyz = convertSphericalToCartesian (source);
		Point3d destXyz = convertSphericalToCartesian (destination);
		double distance = sourceXyz.distance (destXyz);

		return (distance);
	}

	public static double computeLinearDistanceUsingEcef (EarthLocation source, EarthLocation destination)
	{
		Point3d sourceXyz = convertEarthLocationToEcef (source);
		Point3d destXyz = convertEarthLocationToEcef (destination);
		double distance = sourceXyz.distance (destXyz);

		return (distance);
	}

	public static Point3d convertSphericalToCartesian (EarthLocation location)
	{
		double latitude = Math.toRadians (location.getLatitude ().getValue ());
		double longitude = Math.toRadians (location.getLongitude ().getValue ());
		Point3d xyzPoint = new Point3d ();
		double altitude = getAltitudeInKilometers (location);
		xyzPoint.x = (EARTH_RADIUS_IN_KM + altitude) * Math.cos (latitude) * Math.cos (longitude);
		xyzPoint.y = (EARTH_RADIUS_IN_KM + altitude) * Math.cos (latitude) * Math.sin (longitude);
		xyzPoint.z = (EARTH_RADIUS_IN_KM + altitude) * Math.sin (latitude);

		return (xyzPoint);
	}

	/**
	 * Convert earth location (lat/lon/alt) to earth-centered, earth-fixed
	 * (ECEF) coordinate system. The WGS84 reference ellipsoid is used.
	 * 
	 * @param location
	 *            - the earth location to convert
	 */
	public static Point3d convertEarthLocationToEcef (EarthLocation location)
	{
		Point3d ecefPoint = new Point3d ();
		double altitude = getAltitudeInKilometers (location);
		double latitude = Math.toRadians (location.getLatitude ().getValue ());
		double longitude = Math.toRadians (location.getLongitude ().getValue ());

		double denom = Math.sqrt (1 - Math.pow (EARTH_FIRST_ECCENTRICITY, 2) * Math.pow (Math.sin (latitude), 2));
		double radiusOfCurvature = EARTH_SEMIMAJOR_RADIUS_IN_KM / denom;

		ecefPoint.x = (radiusOfCurvature + altitude) * Math.cos (latitude) * Math.cos (longitude);
		ecefPoint.y = (radiusOfCurvature + altitude) * Math.cos (latitude) * Math.sin (longitude);
		ecefPoint.z = (radiusOfCurvature + altitude) * Math.sin (latitude);

		return (ecefPoint);
	}

	public static double getAltitudeInKilometers (EarthLocation location)
	{
		double altitude = 0;
		try
		{
			altitude = location.getAltitude ().getValue (SI.meter.scale (1000));
		}
		catch (Exception e)
		{
			throw new RuntimeException ("Shouldn't occur");
		}
		return (altitude);
	}
}