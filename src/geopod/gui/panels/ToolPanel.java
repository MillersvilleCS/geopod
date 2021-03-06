package geopod.gui.panels;

import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.Hud;
import geopod.gui.components.GeopodButton;
import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class ToolPanel
		extends JPanel
{

	private static final long serialVersionUID = 135073955721768291L;
	private Hud m_hud;

	public ToolPanel (Hud hud)
	{
		m_hud = hud;
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
		String isoSurfaceImage = "//Resources/Images/User Interface/Buttons/IsoSurfaceButton.png";
		String isoSurfaceImageDown = "//Resources/Images/User Interface/Buttons/IsoSurfaceButtonDown.png";
		String isoSurfaceImageHover = "//Resources/Images/User Interface/Buttons/IsoSurfaceButtonHover.png";
		GeopodButton isosurfaceViewButton = new GeopodButton (isoSurfaceImage, isoSurfaceImageDown, isoSurfaceImageHover);
		isosurfaceViewButton.setToolTipTexts (" Open Isosurface Display ", " Close Isosurface Display ");
		isosurfaceViewButton.setActionCommand ("isosurface");
		isosurfaceViewButton.addActionListener (m_hud);
		m_hud.addObserver (isosurfaceViewButton, GeopodEventId.ISOSURFACE_BUTTON_STATE_CHANGED);
		super.add (isosurfaceViewButton, "wrap");
		
		String distanceImage = "//Resources/Images/User Interface/Buttons/DistanceButton.png";
		String distanceImageDown = "//Resources/Images/User Interface/Buttons/DistanceButtonDown.png";
		String distanceImageHover = "//Resources/Images/User Interface/Buttons/DistanceButtonHover.png";
		GeopodButton distanceButton = new GeopodButton (distanceImage, distanceImageDown, distanceImageHover);
		distanceButton.setToolTipTexts (" Open Distance Measurer ", " Close Distance Measurer ");
		distanceButton.setActionCommand ("distance");
		distanceButton.addActionListener (m_hud);
		m_hud.addObserver (distanceButton, GeopodEventId.DISTANCE_BUTTON_STATE_CHANGED);
		super.add (distanceButton, "wrap");
	}
}
