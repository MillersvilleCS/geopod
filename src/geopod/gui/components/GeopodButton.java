package geopod.gui.components;

import geopod.eventsystem.IObserver;
import geopod.eventsystem.events.GeopodEventId;
import geopod.utils.FileLoadingUtility;

import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JToolTip;

import org.jdesktop.swingx.JXButton;

/**
 * GeopodButton is a specialized {@link JXButton} that simplifies the creation
 * of buttons with image icons. GeopodButton also creates custom tooltips that
 * remain inside the bounds of the window ancestor.
 * 
 * @author Geopod Team
 * 
 */
public class GeopodButton
		extends JXButton
		implements IObserver
{
	private static final long serialVersionUID = -690830549861217091L;

	/**
	 * Cached tooltip;
	 */
	private JToolTip m_toolTip;

	private boolean m_shouldKeepFocus;

	ImageIcon m_unselectedPressedIcon;
	ImageIcon m_selectedPressedIcon;

	String m_unselectedTooltipText = "";
	String m_selectedTooltipText = "";

	/**
	 * Constructs a GeopodButton with no image icons.
	 */
	public GeopodButton ()
	{
		super ();
	}

	/**
	 * Constructs a GeopodButton with the specified image icons. This is for
	 * buttons that do not show state, and have the same rollover icon as
	 * pressed icon (imageDown) and same unselected icon as selected icon
	 * (imageUp).
	 * 
	 * @param imageUp
	 *            - the path of the up image icon
	 * @param imageDown
	 *            the path of the down image icon
	 */
	public GeopodButton (String imageUp, String imageDown)
	{
		this.setUpButtonIcons (imageUp, imageUp, imageDown, imageDown, imageDown, imageDown);
		this.setUpState ();
	}

	/**
	 * Constructs a GeopodButton with the specified image icons. This is a
	 * simplified constructor provided as a convenience for buttons that don't
	 * need to specify all possible icons. Assumes there is no distinction
	 * between pressed and hover icons, that there is only one hover icon for
	 * selected and unselected states, and that the button shows state.
	 * 
	 * @param imageUnselected
	 *            - visible when button is not selected (state is off)
	 * @param imageSelected
	 *            - visible when button is selected (state is active)
	 * @param imageRollover
	 *            - visible when hovering in either state, and when pressed in
	 *            either state
	 */
	public GeopodButton (String imageUnselected, String imageSelected, String imageRollover)
	{
		this.setUpButtonIcons (imageUnselected, imageSelected, imageRollover, imageRollover, imageRollover,
				imageRollover);
		this.setUpState ();
	}

	/**
	 * Constructs a GeopodButton with the specified image icons.
	 * 
	 * @param imageUnselected
	 *            - default image on button
	 * @param imageSelected
	 *            - this image displays when the button's associated function is
	 *            active (button has been selected).
	 * @param imageUnselectedPressed
	 *            - this image displays when a button that has been unselected
	 *            is pressed (releasing will make it selected)
	 * @param imageSelectedPressed
	 *            - the image displays when a button that has been selected is
	 *            pressed (releasing will make it unselected)
	 * @param imageRolloverUnselected
	 *            - this image displays when the mouse rolls over an unselected
	 *            button
	 * @param imageRolloverSelected
	 *            - this image displays when the mouse rolls over a selected
	 *            button
	 */
	public GeopodButton (String imageUnselected, String imageSelected, String imageUnselectedPressed,
			String imageSelectedPressed, String imageRolloverUnselected, String imageRolloverSelected)
	{
		this.setUpButtonIcons (imageUnselected, imageSelected, imageUnselectedPressed, imageSelectedPressed,
				imageRolloverUnselected, imageRolloverSelected);

		this.setUpState ();
	}

	private void setUpState ()
	{
		// Ensures the GeopodButton looks reasonable when painted
		// this.setFocusable (false);
		this.setContentAreaFilled (false);
		this.setBorderPainted (true);
		this.setFocusPainted (false);
		this.setMargin (new Insets (0, 0, 0, 0));
		this.setBorder (javax.swing.BorderFactory.createEmptyBorder ());
		this.transferFocus ();
		// this.addFocusListener (new ButtonFocusListener (this));
		this.addMouseListener (new ButtonMouseListener (this));

		m_shouldKeepFocus = false;
	}

	private void setUpButtonIcons (String imageUnselected, String imageSelected, String imageUnselectedPressed,
			String imageSelectedPressed, String imageRolloverUnselected, String imageRolloverSelected)
	{
		try
		{
			ImageIcon unselectedIcon = FileLoadingUtility.loadImageIcon (imageUnselected);
			ImageIcon selectedIcon = FileLoadingUtility.loadImageIcon (imageSelected);
			m_selectedPressedIcon = FileLoadingUtility.loadImageIcon (imageSelectedPressed);
			m_unselectedPressedIcon = FileLoadingUtility.loadImageIcon (imageUnselectedPressed);
			ImageIcon rolloverSelectedIcon = FileLoadingUtility.loadImageIcon (imageRolloverSelected);
			ImageIcon rolloverUnselectedIcon = FileLoadingUtility.loadImageIcon (imageRolloverUnselected);

			this.setIcon (unselectedIcon);
			this.setSelectedIcon (selectedIcon);
			this.setPressedIcon (m_unselectedPressedIcon);
			this.setRolloverIcon (rolloverUnselectedIcon);
			this.setRolloverSelectedIcon (rolloverSelectedIcon);

		}
		catch (IOException e)
		{
			e.printStackTrace ();
		}

	}

	private void switchPressedIcons ()
	{
		this.setPressedIcon ((this.isSelected ()) ? m_selectedPressedIcon : m_unselectedPressedIcon);
	}

	private void switchToolTips ()
	{
		super.setToolTipText ((this.isSelected ()) ? m_selectedTooltipText : m_unselectedTooltipText);
	}

	/**
	 * Set the tooltip text for this button.
	 * 
	 * @param unselectedTooltipText
	 * @param selectedTooltipText
	 */
	public void setToolTipTexts (String unselectedTooltipText, String selectedTooltipText)
	{
		m_unselectedTooltipText = unselectedTooltipText;
		m_selectedTooltipText = selectedTooltipText;
		super.setToolTipText (m_unselectedTooltipText);
	}

	@Override
	public void setToolTipText (String text)
	{
		this.setToolTipTexts (text, text);
	}

	private void changeButtonState ()
	{
		this.setSelected (!this.isSelected ());
		this.switchPressedIcons ();
		this.switchToolTips ();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JToolTip createToolTip ()
	{
		return (m_toolTip == null) ? new PaddedToolTip () : m_toolTip;
	}

	public void makeKeepFocus ()
	{
		m_shouldKeepFocus = true;
	}

	public boolean shouldKeepFocus ()
	{
		return m_shouldKeepFocus;
	}

	/*
	 * private class ButtonFocusListener implements FocusListener { private GeopodButton m_button;
	 * 
	 * public ButtonFocusListener (GeopodButton button) { m_button = button; }
	 * 
	 * @Override public void focusGained (FocusEvent e) { m_button.transferFocus (); }
	 * 
	 * @Override public void focusLost (FocusEvent e) {
	 * 
	 * }
	 * 
	 * }
	 */

	private class ButtonMouseListener
			extends MouseAdapter
	{
		private GeopodButton m_button;

		public ButtonMouseListener (GeopodButton button)
		{
			m_button = button;
		}

		@Override
		public void mouseReleased (MouseEvent e)
		{
			if (!m_button.shouldKeepFocus ())
				m_button.transferFocus ();
		}
	}

	@Override
	public void handleNotification (GeopodEventId eventId)
	{
		this.changeButtonState ();
	}
}
