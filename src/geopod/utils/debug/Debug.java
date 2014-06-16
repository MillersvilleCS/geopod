package geopod.utils.debug;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import geopod.ConfigurationManager;

/**
 * A class to hold debugging functions.
 * 
 */
public class Debug
{
	/**
	 * An enumeration of the possible debug levels.
	 */
	public enum DebugLevel
	{
		/**
		 * Debugging is turned off.
		 */
		NONE,
		/**
		 * TODO: Figure out what LOW debugging should include.
		 */
		LOW,
		/**
		 * TODO: Figure out what MEDIUM debugging should include.
		 */
		MEDIUM,
		/**
		 * All debugging messages turned on.
		 */
		HIGH
	}

	private static DebugLevel ms_debugLevel;
	private static boolean ms_createConsole;

	static
	{
		ms_debugLevel = DebugLevel.NONE;
		ms_createConsole = false;
		ConfigurationManager.addPropertyChangeListener (ConfigurationManager.Debug, new PropertyChangeListener ()
		{

			@Override
			public void propertyChange (PropertyChangeEvent evt)
			{
				Debug.setDebugLevel (DebugLevel.NONE);
				if (ConfigurationManager.isEnabled (ConfigurationManager.Debug))
				{
					Debug.setDebugLevel (Debug.DebugLevel.MEDIUM);
				}
			}
		});
	}

	private Debug ()
	{
		// Private constructor for static class.
	}

	public static void setDebugLevel (DebugLevel level)
	{
		ms_debugLevel = level;
	}

	/**
	 * @return true if debugging is turned on.
	 */
	public static boolean isDebuggingOn ()
	{
		return (ms_debugLevel != DebugLevel.NONE);
	}

	/**
	 * Check if the given debugging level is turned on.
	 * 
	 * @param level
	 * @return true if the debugging level is the same as the one specified.
	 */
	public static boolean levelIs (DebugLevel level)
	{
		return (ms_debugLevel == level);
	}

	/**
	 * @return true if Jython console support is enabled.
	 */
	public static boolean consoleEnabled ()
	{
		return (ms_createConsole);
	}

	/**
	 * @param level
	 * @return true if the debugging level is equal to or greater then the one
	 *         specified.
	 */
	public static boolean levelAtLeast (DebugLevel level)
	{
		return (ms_debugLevel.compareTo (level) >= 0);
	}

	public static void print (String output)
	{
		print (DebugLevel.LOW, output);
	}

	public static void print (DebugLevel level, String output)
	{
		if (levelAtLeast (level))
		{
			System.out.print (output);
		}
	}

	public static void println (String output)
	{
		println (DebugLevel.LOW, output);
	}

	public static void println (DebugLevel level, String output)
	{
		if (levelAtLeast (level))
		{
			System.out.println (output);
		}
	}

	public static void printf (String format, Object... args)
	{
		printf (DebugLevel.LOW, format, args);
	}

	public static void printf (DebugLevel level, String format, Object... args)
	{
		if (levelAtLeast (level))
		{
			System.out.printf (format, args);
		}
	}

	/**
	 * Get the name of the calling function.
	 * <p>
	 * Code adapted from example on <a href=
	 * "http://helpdesk.objects.com.au/java/can-i-get-details-of-the-call-stack
	 * -in-java-such-as-to-find-where-a-method-was-called-from
	 * ">helpdesk.objects.com.au</a>
	 * 
	 * @return the name of the calling method and line number of the call.
	 */
	public static String whoCalledMe ()
	{
		StackTraceElement[] stackTraceElements = Thread.currentThread ().getStackTrace ();
		StackTraceElement caller = stackTraceElements[4];
		String classname = caller.getClassName ();
		String methodName = caller.getMethodName ();
		int lineNumber = caller.getLineNumber ();
		return classname + "." + methodName + ":" + lineNumber;
	}

	/**
	 * Print the call stack at the current point.
	 * <p>
	 * Code adapted from example on <a href=
	 * "http://helpdesk.objects.com.au/java/can-i-get-details-of-the-call-stack
	 * -in-java-such-as-to-find-where-a-method-was-called-from
	 * ">helpdesk.objects.com.au</a>
	 * 
	 * </p>
	 */
	public static void showCallStack ()
	{
		System.out.println ("=====DEBUG show call stack =====");
		StackTraceElement[] stackTraceElements = Thread.currentThread ().getStackTrace ();
		for (int i = 2; i < stackTraceElements.length; i++)
		{
			StackTraceElement ste = stackTraceElements[i];
			String classname = ste.getClassName ();
			String methodName = ste.getMethodName ();
			int lineNumber = ste.getLineNumber ();
			System.out.println (classname + "." + methodName + ":" + lineNumber);
		}
		System.out.println ();
	}
}