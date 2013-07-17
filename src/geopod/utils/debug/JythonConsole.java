package geopod.utils.debug;

import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;

import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 * JythonConsole provides access to an interactive Jython console.
 * 
 * @author Geopod Team
 * 
 */
public class JythonConsole
{
	private final static String JYTHON_CONSOLE_DIR;
	private static JFrame m_console;
	private static PythonInterpreter m_interpreter;

	static
	{
		// TODO GZ: Check if this works when run through plugin manager
		JYTHON_CONSOLE_DIR = "./JythonConsole";
		m_console = null;
		m_interpreter = null;
	}

	private JythonConsole ()
	{
		// Static class
	}

	/**
	 * Returns a {@link PythonInterpreter}
	 * 
	 * @return the PythonIntrepreter
	 */
	public static PythonInterpreter create ()
	{
		if (m_interpreter == null)
		{
			m_interpreter = createInterpreter ();
		}
		return (m_interpreter);
	}

	private static PythonInterpreter createInterpreter ()
	{
		PySystemState.initialize ();

		m_interpreter = new PythonInterpreter ();
		m_interpreter.exec ("import sys");
		m_interpreter.exec ("sys.path.append ('" + JYTHON_CONSOLE_DIR + "')");
		m_interpreter.exec ("from console import main");

		PyObject main = m_interpreter.get ("main");
		PyObject frame = main.__call__ (m_interpreter.getLocals ());
		m_console = (JFrame) frame.__tojava__ (JFrame.class);

		return (m_interpreter);
	}

	/**
	 * Sets the visibility of the JythonConsole to the specified value
	 * 
	 * @param isVisible
	 *            true to make it visible; false to make it invisible
	 */
	public static void setVisible (boolean isVisible)
	{
		m_console.setVisible (isVisible);
	}

	/**
	 * A convenience method for toggling the visibility of this console. If the
	 * console is currently invisible, a call to this method will make it
	 * visible.
	 */
	public static void toggleVisibility ()
	{
		boolean isVisible = m_console.isVisible ();
		m_console.setVisible (!isVisible);
	}

	/**
	 * Binds the specified name to a java object for use in the JythonConsole.
	 * 
	 * @param name
	 *            a user defined identifier for javaObject
	 * @param javaObject
	 *            the object to be bound
	 */
	public static void addNameBinding (String name, Object javaObject)
	{
		m_interpreter.set (name, javaObject);
	}

	/**
	 * Binds the specified names to the java objects to which they are mapped.
	 * 
	 * @param namesToExport
	 *            a map containing name identifiers to objects
	 */
	public static void addNameBindings (Map<String, Object> namesToExport)
	{
		for (Entry<String, Object> entry : namesToExport.entrySet ())
		{
			m_interpreter.set (entry.getKey (), entry.getValue ());
		}
	}

	/**
	 * Shuts down the interpreter
	 */
	public static void shutdown ()
	{
		if (m_interpreter != null)
		{
			m_console.dispose ();
			m_interpreter.cleanup ();
		}
	}
}
