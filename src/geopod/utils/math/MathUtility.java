package geopod.utils.math;

import visad.Real;
import visad.Unit;

/**
 * Class to provide general math functions.
 * 
 * @author Geopod Team
 * 
 */
public class MathUtility
{
	/**
	 * A small value used for calculating floating point equality. Equal to
	 * 1E-6.
	 */
	public static final float EPSILON;

	static
	{
		EPSILON = 1e-6f;
	}

	private MathUtility ()
	{
		// Static class, no constructor.
	}

	/**
	 * check if two floating point numbers are close to each other.
	 * 
	 * @param a
	 * @param b
	 * @return true if a and b are within an epsilon of each other.
	 */
	public static boolean equalsEpsilon (float a, float b)
	{
		return (equalsEpsilon (a, b, EPSILON));
	}

	/**
	 * Check if two floating point numbers are close to each other.
	 * 
	 * @param a
	 * @param b
	 * @param epsilon
	 *            - both numbers must be within this value of each other.
	 * @return true if a and b are within an epsilon of each other.
	 */
	public static boolean equalsEpsilon (float a, float b, float epsilon)
	{
		float delta = Math.abs (a - b);
		return (delta <= epsilon);
	}

	/**
	 * @param a
	 * @param b
	 * @return true if a is more then an epsilon less then b.
	 */
	public static boolean lessEpsilon (float a, float b)
	{
		return (lessEpsilon (a, b, EPSILON));
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param epsilon
	 *            - the epsilon value to use.
	 * @return true if a is more then an epsilon less then b.
	 */
	public static boolean lessEpsilon (float a, float b, float epsilon)
	{
		return (a < b - epsilon);
	}

	/**
	 * @param a
	 * @param b
	 * @return true if a is more then an epsilon grater then b.
	 */
	public static boolean greaterEpsilon (float a, float b)
	{
		return (greaterEpsilon (a, b, EPSILON));
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param epsilon
	 *            - the epsilon to use.
	 * @return true if a is more then an epsilon grater then b.
	 */
	public static boolean greaterEpsilon (float a, float b, float epsilon)
	{
		return (a > b + epsilon);
	}
	
	/**
	 * Parse a double from a string with error handling.
	 * 
	 * @param s
	 *            - the string to parse.
	 * @return - the double value of the string, or NaN if a double can not be
	 *         parsed.
	 */
	public static double parseDouble (String s)
	{
		try
		{
			return (Double.parseDouble (s));
		}
		catch (NumberFormatException nfe)
		{
			return (Double.NaN);
		}
	}
}
