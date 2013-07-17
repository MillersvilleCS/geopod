package geopod;

import geopod.gui.Hud;
import geopod.utils.idv.SceneGraphControl;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.media.j3d.Behavior;

import visad.MouseBehavior;
import visad.java3d.DefaultDisplayRendererJ3D;
import visad.java3d.KeyboardBehaviorJ3D;

/**
 * Window listener to turn the geopod mouse behaviors on and off.
 * 
 */
public class GeopodWindowListener
		extends WindowAdapter
{
	private final Hud m_hud;

	/**
	 * Constructs a GeopodWindowListiner that gets IDV and geopod mouse
	 * behaviors from the specified {@link Hud}.
	 */
	public GeopodWindowListener (Hud hud)
	{
		m_hud = hud;
	}

	/**************************************************************************/
	// Window events

	/**
	 * Displays the shutdown dialogue when window is closed
	 */
	@Override
	public void windowClosing (WindowEvent event)
	{
		m_hud.displayShutdownDialogue ();
	}

	/**************************************************************************/

	@Override
	/**
	 * Turns the IDV controls off and the Geopod controls on.
	 * 
	 * {@inheritDoc}
	 */
	public void windowGainedFocus (WindowEvent e)
	{
		// Turn off the IDV mouse behavior
		DefaultDisplayRendererJ3D displayRenderer = SceneGraphControl.getDisplayRenderer ();
		MouseBehavior idvMouseBehavior = displayRenderer.getMouseBehavior ();

		((Behavior) idvMouseBehavior).setEnable (false);

		// Turn off the IDV keyboard behavior
		List<KeyboardBehaviorJ3D> keyBehaviorList = SceneGraphControl.findNodesOfType (KeyboardBehaviorJ3D.class);
		for (KeyboardBehaviorJ3D kbd : keyBehaviorList)
		{
			kbd.setEnable (false);
		}
		KeyboardBehaviorJ3D kb = (KeyboardBehaviorJ3D) displayRenderer.getKeyboardBehavior ();
		kb.setEnable (false);

		// Remove any transforms above the data volume and reset to the default top view.
		m_hud.setTopView ();
	}

	@Override
	/**
	 * Turns the IDV controls on and the Geopod controls off.
	 * 
	 * {@inheritDoc}
	 */
	public void windowLostFocus (WindowEvent e)
	{
		// Turn on the IDV mouse behavior
		DefaultDisplayRendererJ3D displayRenderer = SceneGraphControl.getDisplayRenderer ();
		MouseBehavior idvMouseBehavior = displayRenderer.getMouseBehavior ();
		((Behavior) idvMouseBehavior).setEnable (true);

		// Turn on the IDV keyboard behavior.
		List<KeyboardBehaviorJ3D> keyBehaviorList = SceneGraphControl.findNodesOfType (KeyboardBehaviorJ3D.class);
		for (KeyboardBehaviorJ3D kbd : keyBehaviorList)
		{
			kbd.setEnable (true);
		}
		KeyboardBehaviorJ3D kb = (KeyboardBehaviorJ3D) displayRenderer.getKeyboardBehavior ();
		kb.setEnable (true);
	}

}
