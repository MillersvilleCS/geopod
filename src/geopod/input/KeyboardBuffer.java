package geopod.input;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.KeyStroke;

/**
 * This class is used to transition from a event-based key system to a polling
 * system.
 * 
 * It stores key events and maintains a list of which keys are currently down.
 * This is used to smooth out the keyboard repeat rate.
 * 
 * @author Geopod Team
 * 
 */
class KeyboardBuffer
{
	private static boolean KEY_UP;

	/**
	 * Mask storing which masks to maintain. This allows us to exclude unwanted
	 * mouse masks.
	 */
	private static int MASKS_TO_STORE = KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK;

	/**
	 * Mask for the JDK 1.3 key modifiers.
	 */
	private static int JDK_1_3_MODIFIERS = InputEvent.SHIFT_DOWN_MASK - 1;

	static
	{
		KEY_UP = false;
	}

	private static final boolean[] m_keysDown;

	private static int m_modifiers;

	static
	{
		// Allocate buffer sufficient for all ASCI keys
		final int maxNumKeys = 256;
		m_keysDown = new boolean[maxNumKeys];
	}

	private KeyboardBuffer ()
	{
		// Static class, only has a private constructor.
	}

	/**
	 * Update the keyboard buffer state based on a KeyEvent
	 * 
	 * @param event
	 *            - the key event to process
	 */
	public static synchronized void recordKeyEvent (KeyEvent event)
	{
		int eventID = event.getID ();
		if (eventID != KeyEvent.KEY_TYPED)
		{
			// Set the key state (up or down).
			boolean keyDown = (eventID == KeyEvent.KEY_PRESSED);
			int keyCode = event.getKeyCode ();
			setKeyDown (keyCode, keyDown);

			// Save the state of the current modifier keys.
			m_modifiers = event.getModifiersEx () & MASKS_TO_STORE;
		}
	}

	/**
	 * Check if the given key is pressed.
	 * 
	 * @param keyCode
	 * @return true if the specified key is down.
	 */
	public static synchronized boolean isKeyDown (int keyCode)
	{
		boolean keyDown = m_keysDown[keyCode];
		return keyDown;
	}

	/**
	 * Check if the given {@link KeyStroke} matches with the current state of
	 * the keyboard buffer.
	 * 
	 * @param keyState
	 * @return true if the state of the letter key and all control keys match
	 *         the desired pattern.
	 */
	public static synchronized boolean isKeyDown (KeyStroke keyState)
	{
		// Check if the correct key is down.
		boolean keysMatch = m_keysDown[keyState.getKeyCode ()];

		// Check if the correct combination of modifier keys is down.
		int mods = keyState.getModifiers () & ~JDK_1_3_MODIFIERS;
		boolean modsMatch = (mods == m_modifiers);

		return keysMatch && modsMatch;
	}

	public static synchronized void setKeyDown (int keyCode, boolean isDown)
	{
		if (keyCode < m_keysDown.length)
		{
			m_keysDown[keyCode] = isDown;
		}
	}

	public static synchronized void resetKeyBuffer ()
	{
		Arrays.fill (m_keysDown, KEY_UP);
	}
}