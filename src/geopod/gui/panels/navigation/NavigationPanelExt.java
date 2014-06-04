package geopod.gui.panels.navigation;

import geopod.Geopod;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.Hud;
import geopod.gui.components.GeopodButton;
import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class NavigationPanelExt
		extends JPanel
{
	private static final long serialVersionUID = 135073955721768291L;
	private Hud m_hud;
	// Testing for slider
	private Geopod m_geopod;

	public NavigationPanelExt (Hud hud, /* Testing for slider */ Geopod geopod)
	{
		m_hud = hud;
		// Testing for slider
		m_geopod = geopod;
		setPanelProperties ();
		createAndAddButtons ();
	}

	/**
	 * Makes the transparent and adds a {@link MigLayout} layout manager.
	 */
	private void setPanelProperties ()
	{
		super.setOpaque (false);
		if (Debug.levelAtLeast (DebugLevel.HIGH))
		{
			super.setBorder (BorderFactory.createLineBorder (Color.green));
		}
		super.setLayout (new MigLayout ("btt, ins 0, alignx center"));
	}

	private void createAndAddButtons ()
	{
		String imageUp = "//Resources/Images/User Interface/Buttons/GeocodeButton.png";
		String imageDown = "//Resources/Images/User Interface/Buttons/GeocodeButtonDown.png";
		String imageHover = "//Resources/Images/User Interface/Buttons/GeocodeButtonHover.png";
		GeopodButton lookUpButton = new GeopodButton (imageUp, imageDown, imageHover);
		lookUpButton.addActionListener (m_hud);
		lookUpButton.setActionCommand ("lookUp");
		lookUpButton.setToolTipTexts (" Geocode (look up location) ", " Hide Lookup Panel ");
		m_hud.addObserver (lookUpButton, GeopodEventId.LOOKUP_BUTTON_STATE_CHANGED);
		
		super.add (lookUpButton, "wrap");
		
		//Adding the components bottom-to-top 
		String imageUnlocked = "//Resources/Images/User Interface/Buttons/LockButton.png";
		String imageUnlockedHover = "//Resources/Images/User Interface/Buttons/LockButtonUpHover.png";
		String imageLockedHover = "//Resources/Images/User Interface/Buttons/LockButtonDownHover.png";
		String imageLocked = "//Resources/Images/User Interface/Buttons/LockButtonDown.png";
		GeopodButton isosurfaceLockButton = new GeopodButton (imageUnlocked, imageLocked, imageLockedHover, imageUnlockedHover,
				imageUnlockedHover, imageLockedHover);
		isosurfaceLockButton.setToolTipTexts (" Lock to Isosurface ", " Unlock from Isosurface ");
		isosurfaceLockButton.setActionCommand ("lock");
		isosurfaceLockButton.addActionListener (m_hud);
		
		super.add (isosurfaceLockButton, "wrap");
		
		m_hud.addObserver (isosurfaceLockButton, GeopodEventId.LOCK_BUTTON_STATE_CHANGED);
	}
}
