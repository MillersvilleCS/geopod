package geopod.utils.comparators;

import geopod.utils.math.MathUtility;

import java.io.Serializable;
import java.util.Comparator;

import javax.vecmath.Point3f;

/**
 * A comparator for sorting grid points. Orders first by <tt>Y</tt>, then
 * <tt>Z</tt>, then <tt>X</tt>. In Earth coordinates, this corresponds to
 * <tt>lat, lot, alt</tt>.
 * 
 * @author Geopod Team
 */
public class GridPointComparator
		implements Comparator<Point3f>, Serializable
{
	private static final long serialVersionUID = -5495653667729354255L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare (Point3f p1, Point3f p2)
	{
		// Order by y, z, then x
		if (MathUtility.lessEpsilon (p1.y, p2.y))
		{
			return (-1);
		}
		if (MathUtility.greaterEpsilon (p1.y, p2.y))
		{
			return (1);
		}
		if (MathUtility.lessEpsilon (p1.z, p2.z))
		{
			return (-1);
		}
		if (MathUtility.greaterEpsilon (p1.z, p2.z))
		{
			return (1);
		}
		if (MathUtility.lessEpsilon (p1.x, p2.x))
		{
			return (-1);
		}
		if (MathUtility.greaterEpsilon (p1.x, p2.x))
		{
			return (1);
		}

		return (0);
	}
}
