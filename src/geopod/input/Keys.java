package geopod.input;

import java.awt.event.KeyEvent;
import java.util.EnumMap;

import javax.swing.KeyStroke;

/**
 * A static class defining key bindings for the geopod.
 * 
 * @author Geopod Team
 * 
 */
public class Keys
{
	/**
	 * An enumeration of the possible geopod key bindings.
	 * 
	 * @author Geopod Team
	 * 
	 */
	public static enum GeopodKeys
	{
		MoveForward,
		MoveBackward,
		MoveUp,
		MoveDown,
		MoveRight,
		MoveLeft,
		TurnLeft,
		TurnRight,
		RollClockwise,
		RollCounterclockwise,
		Accelerate,
		Decelerate,
		AngularAccelerate,
		AngularDecelerate,
		LockToSurface,
		SetPoseAboveCenterOfViewBox,
		ResetPose,
		MakeUpright,
		ParameterDisplay,
		OverflowDisplay,
		NoteLocation,
		ResetIsosurfaceColors,
		RecreateFlightPath,
		PauseFlightPlayback,
		StopFlightPlayback,
		ToggleGridpoints,
		ToggleCalculator,
		ToggleSettings,
		ToggleHelp,
		/**
		 * Bring up the escape key menu.
		 */
		Menu
	}

	/**
	 * A map of the current key bindings, in the form <tt>Action->keyCode</tt>.
	 */
	public static final EnumMap<GeopodKeys, KeyStroke> Bindings;

	static
	{
		// NOTE: you must use single quotes here, otherwise getKeyStroke returns null.
		Bindings = new EnumMap<GeopodKeys, KeyStroke> (GeopodKeys.class);

		setBinding (GeopodKeys.MoveForward, KeyStroke.getKeyStroke (KeyEvent.VK_W, 0));
		setBinding (GeopodKeys.MoveBackward, KeyStroke.getKeyStroke (KeyEvent.VK_S, 0));

		setBinding (GeopodKeys.MoveLeft, KeyStroke.getKeyStroke (KeyEvent.VK_A, 0));
		setBinding (GeopodKeys.MoveRight, KeyStroke.getKeyStroke (KeyEvent.VK_D, 0));

		setBinding (GeopodKeys.MoveUp, KeyStroke.getKeyStroke (KeyEvent.VK_F, 0));
		setBinding (GeopodKeys.MoveDown, KeyStroke.getKeyStroke (KeyEvent.VK_C, 0));

		setBinding (GeopodKeys.TurnLeft, KeyStroke.getKeyStroke (KeyEvent.VK_Q, 0));
		setBinding (GeopodKeys.TurnRight, KeyStroke.getKeyStroke (KeyEvent.VK_E, 0));

		setBinding (GeopodKeys.RollClockwise, KeyStroke.getKeyStroke (KeyEvent.VK_Q, KeyEvent.SHIFT_DOWN_MASK));
		setBinding (GeopodKeys.RollCounterclockwise, KeyStroke.getKeyStroke (KeyEvent.VK_E, KeyEvent.SHIFT_DOWN_MASK));

		setBinding (GeopodKeys.MakeUpright, KeyStroke.getKeyStroke (KeyEvent.VK_SPACE, 0));

		setBinding (GeopodKeys.ResetPose, KeyStroke.getKeyStroke (KeyEvent.VK_R, 0));
		setBinding (GeopodKeys.SetPoseAboveCenterOfViewBox, KeyStroke.getKeyStroke (KeyEvent.VK_T, 0));
		setBinding (GeopodKeys.LockToSurface, KeyStroke.getKeyStroke (KeyEvent.VK_L, 0));

		// Using '=' instead of '+' to avoid needing to press SHIFT
		setBinding (GeopodKeys.Accelerate, KeyStroke.getKeyStroke (KeyEvent.VK_EQUALS, 0));
		setBinding (GeopodKeys.Decelerate, KeyStroke.getKeyStroke (KeyEvent.VK_MINUS, 0));

		setBinding (GeopodKeys.AngularAccelerate, KeyStroke.getKeyStroke (KeyEvent.VK_PERIOD, 0));
		setBinding (GeopodKeys.AngularDecelerate, KeyStroke.getKeyStroke (KeyEvent.VK_COMMA, 0));

		setBinding (GeopodKeys.ParameterDisplay, KeyStroke.getKeyStroke (KeyEvent.VK_P, 0));
		setBinding (GeopodKeys.OverflowDisplay, KeyStroke.getKeyStroke (KeyEvent.VK_O, 0));
		setBinding (GeopodKeys.NoteLocation, KeyStroke.getKeyStroke (KeyEvent.VK_N, 0));

		setBinding (GeopodKeys.ResetIsosurfaceColors, KeyStroke.getKeyStroke (KeyEvent.VK_I, 0));

		setBinding (GeopodKeys.RecreateFlightPath, KeyStroke.getKeyStroke (KeyEvent.VK_H, 0));
		setBinding (GeopodKeys.PauseFlightPlayback, KeyStroke.getKeyStroke (KeyEvent.VK_J, 0));
		setBinding (GeopodKeys.StopFlightPlayback, KeyStroke.getKeyStroke (KeyEvent.VK_K, 0));

		setBinding (GeopodKeys.ToggleGridpoints, KeyStroke.getKeyStroke (KeyEvent.VK_G, 0));
		setBinding (GeopodKeys.ToggleCalculator, KeyStroke.getKeyStroke (KeyEvent.VK_C, 0));
		setBinding (GeopodKeys.ToggleSettings, KeyStroke.getKeyStroke (KeyEvent.VK_S, 0));
		// '/' is used in lieu of '?' since the location of the '?' character on international 
		// keyboards varies. Java also does not provide a KeyEvent for '?'.
		setBinding (GeopodKeys.ToggleHelp, KeyStroke.getKeyStroke (KeyEvent.VK_SLASH, 0));

		setBinding (GeopodKeys.Menu, KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE, 0));
	}

	/**
	 * Set the key binding associated with a given command name.
	 * 
	 * @param commandName
	 * @param keySequence
	 */
	public static void setBinding (GeopodKeys commandName, KeyStroke keySequence)
	{
		Bindings.put (commandName, keySequence);
	}

	/**
	 * Get the key binding associated with a given command name.
	 * 
	 * @param commandName
	 * @return
	 */
	public static KeyStroke getBinding (GeopodKeys commandName)
	{
		return Bindings.get (commandName);
	}

	private Keys ()
	{
		// Static class, no constructor.
	}
}