package geopod.gui.panels;

import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.Hud;
import geopod.gui.components.GeopodButton;
import geopod.gui.components.GeopodSpinner;
import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * The PrimaryButtonPanel is a specialized {@link JPanel} that is responsible
 * for the creation and addition of multiple {@link GeopodButton}
 * 
 * @author Geopod Team
 * 
 */
public class PrimaryButtonPanel
		extends JPanel
{
	private static final long serialVersionUID = -5360340898962809818L;
	private Hud m_hud;

	/**
	 * This constructor builds a transparent PrimaryButtonPanel filled with
	 * buttons. The specified {@link Hud} is added as an ActionListener to all
	 * buttons.
	 * 
	 * @param hud
	 */
	public PrimaryButtonPanel (Hud hud)
	{
		m_hud = hud;

		setPanelProperties ();
		createAndAddComponents ();
	}

	/**
	 * Creates and adds a widget that allows users to adjust grid point density
	 * values.
	 */
	private GeopodSpinner createGridPointDensitySelector ()
	{
		final int defaultValue = 4;
		final int minValue = 0;
		final int maxValue = 25;
		final int incrementValue = 1;

		GeopodSpinner gridPointSpinner = new GeopodSpinner (defaultValue, minValue, maxValue, incrementValue);
		gridPointSpinner.addPropertyChangeListener ("value", new PropertyChangeListener ()
		{
			@Override
			public void propertyChange (PropertyChangeEvent evt)
			{
				int value = (Integer) evt.getNewValue ();

				m_hud.adjustGridPointDensity (value);
				m_hud.requestFocusOnCanvas ();
			}
		});

		// ToolTips
		String upButtonTip = "Increment by " + incrementValue + " Points";
		String downButtonTip = "Decrement by " + incrementValue + " Points";
		// Using HTML in tooltips will require extra work to avoid double padding. -KPW
		String textFieldTip = "Show Every k Points";
		gridPointSpinner.setToolTips (upButtonTip, downButtonTip, textFieldTip);

		return (gridPointSpinner);
	}

	/**
	 * Makes the PrimaryButtonPanel transparent and adds a {@link MigLayout}
	 * layout manager.
	 */
	private void setPanelProperties ()
	{
		super.setOpaque (false);
		if (Debug.levelAtLeast (DebugLevel.HIGH))
		{
			super.setBorder (BorderFactory.createLineBorder (Color.green));
		}
		super.setLayout (new MigLayout ("ins 0, aligny center", "[]4"));
	}

	/**
	 * Creates and adds buttons, adding the {@link Hud} as an
	 * {@link ActionListener} to each button.
	 */
	private void createAndAddComponents ()
	{
		String imageUp = "//Resources/Images/User Interface/Buttons/CalculatorButton.png";
		String imageDown = "//Resources/Images/User Interface/Buttons/CalculatorButtonDown.png";
		String imageHover = "//Resources/Images/User Interface/Buttons/CalculatorButtonHover.png";
		// GeopodButton calcButton = new GeopodButton (imageUp, imageUp, imageDown, imageDown, imageHover, imageHover);
		GeopodButton calcButton = new GeopodButton (imageUp, imageDown, imageHover);
		calcButton.setToolTipText (" Calculator ");
		calcButton.setActionCommand ("calculator");
		calcButton.addActionListener (m_hud);
		m_hud.addObserver (calcButton, GeopodEventId.CALC_BUTTON_STATE_CHANGED);
		super.add (calcButton, "gapleft 10, gapright 1");

		imageUp = "//Resources/Images/User Interface/Buttons/NotepadButton.png";
		imageDown = "//Resources/Images/User Interface/Buttons/NotepadButtonDown.png";
		imageHover = "//Resources/Images/User Interface/Buttons/NotepadButtonHover.png";
		GeopodButton notepadButton = new GeopodButton (imageUp, imageDown, imageHover);
		notepadButton.setToolTipTexts (" View Notepad ", " Hide Notepad ");
		notepadButton.setActionCommand ("notepad");
		notepadButton.addActionListener (m_hud);
		m_hud.addObserver (notepadButton, GeopodEventId.NOTEPAD_BUTTON_STATE_CHANGED);
		super.add (notepadButton, "gapright 1");

		imageUp = "//Resources/Images/User Interface/Buttons/AddNoteButton.png";
		imageDown = "//Resources/Images/User Interface/Buttons/AddNoteButtonDown.png";
		imageHover = "//Resources/Images/User Interface/Buttons/AddNoteButtonHover.png";
		GeopodButton addNoteButton = new GeopodButton (imageUp, imageDown, imageHover);
		addNoteButton.setToolTipTexts (" Note a Location ", " Cancel ");
		addNoteButton.setActionCommand ("note_location");
		addNoteButton.addActionListener (m_hud);
		m_hud.addObserver (addNoteButton, GeopodEventId.ADDNOTE_BUTTON_STATE_CHANGED);
		addNoteButton.makeKeepFocus ();
		super.add (addNoteButton, "gapright 1");

		imageUp = "//Resources/Images/User Interface/Buttons/DropSondeButton.png";
		imageDown = "//Resources/Images/User Interface/Buttons/DropSondeButtonDown.png";
		imageHover = "//Resources/Images/User Interface/Buttons/DropSondeButtonHover.png";
		GeopodButton dropSondeButton = new GeopodButton (imageUp, imageDown, imageHover);
		dropSondeButton.setToolTipTexts (" Dropsonde ", " Hide Dropsonde Panel ");
		dropSondeButton.setActionCommand ("dropsonde");
		dropSondeButton.addActionListener (m_hud);
		m_hud.addObserver (dropSondeButton, GeopodEventId.DROPSONDE_BUTTON_STATE_CHANGED);
		super.add (dropSondeButton, "gapright 1");

		imageUp = "//Resources/Images/User Interface/Buttons/ParticleButton.png";
		imageDown = "//Resources/Images/User Interface/Buttons/ParticleButtonDown.png";
		imageHover = "//Resources/Images/User Interface/Buttons/ParticleButtonHover.png";
		GeopodButton particleImagerButton = new GeopodButton (imageUp, imageDown, imageHover);
		particleImagerButton.setToolTipTexts (" Open Particle Imager ", " Close Particle Imager ");
		particleImagerButton.setActionCommand ("particle");
		particleImagerButton.addActionListener (m_hud);
		m_hud.addObserver (particleImagerButton, GeopodEventId.PARTICLE_BUTTON_STATE_CHANGED);
		super.add (particleImagerButton, "gapright 1");

		imageUp = "//Resources/Images/User Interface/Buttons/GridButton.png";
		imageDown = "//Resources/Images/User Interface/Buttons/GridButtonDown.png";
		imageHover = "//Resources/Images/User Interface/Buttons/GridButtonHover.png";
		GeopodButton displayGridPointsButton = new GeopodButton (imageUp, imageDown, imageHover);
		displayGridPointsButton.setToolTipTexts (" Display Grid Points ", " Hide Grid Points ");
		displayGridPointsButton.setActionCommand ("displayPoints");
		displayGridPointsButton.addActionListener (m_hud);
		m_hud.addObserver (displayGridPointsButton, GeopodEventId.GRIDPOINTS_BUTTON_STATE_CHANGED);
		super.add (displayGridPointsButton);

		GeopodSpinner gridPointSpinner = createGridPointDensitySelector ();
		super.add (gridPointSpinner, "push");

		imageUp = "//Resources/Images/User Interface/Buttons/ParameterButton.png";
		imageDown = "//Resources/Images/User Interface/Buttons/ParameterButtonDown.png";
		imageHover = "//Resources/Images/User Interface/Buttons/ParameterButtonHover.png";
		GeopodButton parameterChooserButton = new GeopodButton (imageUp, imageDown, imageHover);
		parameterChooserButton.setToolTipText (" Parameter Chooser ");
		parameterChooserButton.setActionCommand ("parameterChooser");
		parameterChooserButton.addActionListener (m_hud);
		m_hud.addObserver (parameterChooserButton, GeopodEventId.PARAMETER_BUTTON_STATE_CHANGED);
		super.add (parameterChooserButton);
	}
}
