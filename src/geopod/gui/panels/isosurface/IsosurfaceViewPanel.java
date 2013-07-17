package geopod.gui.panels.isosurface;

import geopod.GeopodPlugin;
import geopod.constants.UIConstants;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.Hud;
import geopod.gui.components.BorderFactory;
import geopod.gui.components.ButtonFactory;
import geopod.gui.components.GeopodLabel;
import geopod.gui.components.PainterFactory;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXPanel;

import ucar.unidata.data.DataChoice;
import ucar.unidata.idv.ViewManager;
import ucar.unidata.idv.control.ThreeDSurfaceControl;
import visad.VisADException;

/**
 * 
 * @author geopod
 * 
 */
public class IsosurfaceViewPanel
		extends JXPanel
		implements ISubject
{
	private static final long serialVersionUID = 1645965063401099203L;

	private static final String PANEL_TITLE;
	private static final String CHANGE_BUTTON_TEXT;
	private static final String REMOVE_BUTTON_TEXT;
	private static final String ADD_SURFACE_BUTTON_TEXT;
	private static final String ADD_SURFACE_BUTTON_TOOLTIP;
	private static final String CLOSE_BUTTON_TOOLTIP;
	static
	{
		PANEL_TITLE = "ISOSURFACES DISPLAYED";
		CHANGE_BUTTON_TEXT = "CHANGE";
		REMOVE_BUTTON_TEXT = "REMOVE";
		ADD_SURFACE_BUTTON_TEXT = "ADD NEW ISOSURFACE";
		ADD_SURFACE_BUTTON_TOOLTIP = "Add a new isosurface via IDV";
		CLOSE_BUTTON_TOOLTIP = "Close the isosurface view display";
	}

	private JPanel m_isosurfaceList;

	private JScrollPane m_scrollPane;
	private JPanel m_southButtonPanel;

	private SubjectImpl m_subjectImpl;
	private Hud m_hud;
	private ViewManager m_viewManager;
	private List<ThreeDSurfaceControl> m_surfaceList;

	/**
	 * Constructs a GUI that lets users view isosurfaces, change isovalues, and
	 * remove isosurfaces.
	 * 
	 * @param Hud
	 *            hud
	 */
	public IsosurfaceViewPanel (Hud hud)
	{
		m_hud = hud;
		m_viewManager = m_hud.getIdvViewManager ();
		m_surfaceList = new ArrayList<ThreeDSurfaceControl> ();
		m_subjectImpl = new SubjectImpl ();
		setupPanel ();
	}

	/************************************************************************************************************************
	 * Set up GUI
	 */

	private void setupPanel ()
	{
		setUpPanelBackground ();
		setUpScrollPane ();
		setUpIsosurfaceList ();
		addHeader ();
		addIsosurfaceListToScrollPane ();
		addScrollPane ();
		addSouthButtonPanel ();
	}

	
	private void setUpPanelBackground ()
	{
		this.setLayout (new MigLayout ("wrap 1, fill", "[align center]", ""));
		this.setBorder (BorderFactory.createStandardBorder ());

		super.setBackgroundPainter (PainterFactory.createStandardMattePainter (1045, 670));
	}
	
	/**
	 * Create and format the scroll pane.
	 */
	private void setUpScrollPane ()
	{
		m_scrollPane = new JScrollPane ();
		m_scrollPane.setPreferredSize (new Dimension (1000, 1000));
		// The scroll pane's view port width should also be the same as its contents; therefore, we 
		// do not need to scroll horizontally.
		m_scrollPane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		m_scrollPane.addComponentListener (new ComponentAdapter ()
		{
			@Override
			public void componentResized (ComponentEvent arg0)
			{
				trimSurfaceNames ();
			}
		});
	}
	
	//TODO: Only call this method once
	/**
	 * Recreate the main contents of the Isosurface View Panel (the list of
	 * isosurface names, parameter values, and related buttons). Called when
	 * there have been changes to the surface list, that is, surfaces have been
	 * added or removed
	 */
	private void setUpIsosurfaceList ()
	{
		m_isosurfaceList = new JPanel ();
		m_isosurfaceList.setLayout (new MigLayout ("wrap 5, fillx",
				"[align left][align right][align left][align center][align center]", ""));

		for (ThreeDSurfaceControl control : m_surfaceList)
		{
			addNewIsosurfaceControlPanel (control);
		}

		m_isosurfaceList.setBackground (UIConstants.GEOPOD_GREEN);
	}
	
	/**
	 * Add the title and define the gaps above and below it.
	 */
	private void addHeader ()
	{
		JLabel headerLabel = new JLabel (PANEL_TITLE, SwingConstants.CENTER);
		Font font = UIConstants.GEOPOD_BANDY.deriveFont (Font.BOLD, UIConstants.TITLE_SIZE);
		headerLabel.setFont (font);
		this.add (headerLabel, "gaptop 8, gapbottom 5");
	}
	
	/**
	 * Add the scroll scroll pane to the {@link IsosurfaceViewPanel}. Also updates the surface list.
	 */
	private void addScrollPane ()
	{
		super.add (m_scrollPane, "gapright 15, gapleft 15, growpriox 200, growprioy 200");
	}

	/**
	 * Put isosurface list panel in place. Do this after it has been recreated
	 * when isosurfaces are added or removed
	 */
	private void addIsosurfaceListToScrollPane ()
	{
		m_scrollPane.setViewportView (m_isosurfaceList);
		Dimension isosurfaceListDimension = new Dimension (m_scrollPane.getViewportBorderBounds ().width,
				m_isosurfaceList.getPreferredSize ().height);
		m_isosurfaceList.setPreferredSize (isosurfaceListDimension);
	}
	
	private void addSouthButtonPanel ()
	{
		m_southButtonPanel = buildButtonPanel ();
		super.add (m_southButtonPanel, "spanx 3");
	}

	private JPanel buildButtonPanel ()
	{
		JPanel buttonPanel = new JPanel ();
		FlowLayout buttonPanelLayout = new FlowLayout ();
		buttonPanel.setLayout (buttonPanelLayout);
		buttonPanel.setOpaque (false);

		JButton addIsosurfaceButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
				UIConstants.GEOPOD_GREEN, false);
		addIsosurfaceButton.setText (ADD_SURFACE_BUTTON_TEXT);
		addIsosurfaceButton.setToolTipText (ADD_SURFACE_BUTTON_TOOLTIP);
		addIsosurfaceButton.addActionListener (new AddSurfaceActionListener ());
		buttonPanel.add (addIsosurfaceButton);

		JButton closeButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
				UIConstants.GEOPOD_GREEN, false);
		closeButton.setText ("CLOSE");
		closeButton.setToolTipText (CLOSE_BUTTON_TOOLTIP);
		closeButton.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				setVisible (false);
				notifyObservers (GeopodEventId.ISOSURFACE_BUTTON_STATE_CHANGED);
			}
		});
		buttonPanel.add (closeButton);

		return (buttonPanel);
	}

	/***********************************************************************************************************************
	 * Update GUI methods
	 */

	

	/**
	 * Correct spacing of isosurface list in main panel. Avoids complications in
	 * MigLayout.
	 */
	private void trimSurfaceNames ()
	{
		int maxValRowWidth = 0, maxUnitRowWidth = 0;
		for (int i = 0; i < m_isosurfaceList.getComponentCount (); i += 5)
		{
			Component valRowComponent = m_isosurfaceList.getComponent (i + 1);
			maxValRowWidth = Math.max (maxValRowWidth, valRowComponent.getPreferredSize ().width);

			Component unitRowComponent = m_isosurfaceList.getComponent (i + 2);
			maxUnitRowWidth = Math.max (maxUnitRowWidth, unitRowComponent.getPreferredSize ().width);
		}
		int changeButtonWidth = 0, removeButtonWidth = 0;
		if (m_isosurfaceList.getComponentCount () > 0)
		{
			changeButtonWidth = m_isosurfaceList.getComponent (3).getPreferredSize ().width;
			removeButtonWidth = m_isosurfaceList.getComponent (4).getPreferredSize ().width;
		}
		int maxRightHandSideWidth = maxValRowWidth + maxUnitRowWidth + changeButtonWidth + removeButtonWidth;

		int viewportWidth = m_scrollPane.getViewportBorderBounds ().width;
		Dimension isosurfaceListDimension = new Dimension (viewportWidth, m_isosurfaceList.getPreferredSize ().height);
		m_isosurfaceList.setPreferredSize (isosurfaceListDimension);
		int maxSurfaceNameWidth = viewportWidth - (maxRightHandSideWidth + 50);
		for (int i = 0; i < m_isosurfaceList.getComponentCount (); i += 5)
		{
			JLabel surfaceNameLabel = (JLabel) m_isosurfaceList.getComponent (i);
			surfaceNameLabel.setMinimumSize (new Dimension (maxSurfaceNameWidth, 18));
			surfaceNameLabel.revalidate ();
		}
	}

	// TODO: remove the entire method below and use separate row objects so we do not add multiple 
	// listener to 3Dsurface controls. We also should not ever create the panel on refresh. 
	// We only do these things to ensure we have the correct layout. Need to look into layouts for row objects.
	/**
	 * Constructs listing for one isosurface, including parameter name, surface
	 * value, units, change & remove buttons.
	 * 
	 * @param control
	 *            {@link ThreeDSurfaceControl} representing isosurface
	 */
	private void addNewIsosurfaceControlPanel (ThreeDSurfaceControl control)
	{
		// Get data associated with parameter
		DataChoice dataChoice = control.getDataChoice ();

		if (dataChoice == null)
		{
			System.err.println ("Error: null data choice");
			return;
		}

		control.removeObservers ();
		Isosurface surface = new Isosurface (control);
		String parameterName = dataChoice.toString ();

		/*
		 * Create Labels
		 */

		Font labelFont = UIConstants.GEOPOD_VERDANA;
		labelFont = labelFont.deriveFont (UIConstants.CONTENT_FONT_SIZE);

		//JLabel parameterLabel = new GeopodLabel (displayParameterName);
		JLabel parameterLabel = new GeopodLabel (parameterName);
		parameterLabel.setBackground (UIConstants.GEOPOD_GREEN);
		parameterLabel.setFont (labelFont);
		parameterLabel.setToolTipText (parameterName);

		ValueLabel valueLabel = new ValueLabel (surface);
		valueLabel.setBackground (UIConstants.GEOPOD_GREEN);
		valueLabel.setFont (labelFont);
		// Link isovalue label with an instance of ThreeDControl
		control.addObserver (valueLabel, GeopodEventId.ISOSURFACE_LEVEL_CHANGED);

		UnitLabel unitLabel = new UnitLabel (surface);
		unitLabel.setBackground (UIConstants.GEOPOD_GREEN);
		unitLabel.setFont (labelFont);
		// TODO: NTO change make an ISOSURFACE UNIT CHANGED event.
		control.addObserver (unitLabel, GeopodEventId.ISOSURFACE_LEVEL_CHANGED);

		/*
		 * Create Buttons
		 */

		JButton changeIsosurfaceButton = ButtonFactory.createGradientButton (14, UIConstants.GEOPOD_GREEN, false);
		changeIsosurfaceButton.setText (CHANGE_BUTTON_TEXT);
		changeIsosurfaceButton.addActionListener (new IsovalueModalWindow (this, control));
		changeIsosurfaceButton.setToolTipText ("Change " + parameterName.toLowerCase () + " isovalue");

		JButton removeIsosurfaceButton = ButtonFactory.createGradientButton (14, UIConstants.GEOPOD_GREEN, false);
		removeIsosurfaceButton.setText (REMOVE_BUTTON_TEXT);
		removeIsosurfaceButton.addActionListener (new RemoveSurfaceActionListener (control));
		removeIsosurfaceButton.setToolTipText ("Remove " + parameterName.toLowerCase () + " isosurface");

		/*
		 * Add components
		 */

		m_isosurfaceList.add (parameterLabel);
		m_isosurfaceList.add (valueLabel, "gapleft 10");
		m_isosurfaceList.add (unitLabel);
		m_isosurfaceList.add (changeIsosurfaceButton, "gapleft 3");
		m_isosurfaceList.add (removeIsosurfaceButton);
	}

	

	/**
	 * Update isosurface list to reflect added/removed surfaces.
	 * 
	 * @return whether or not there are changes in the isosurfaces displayed
	 */
	public boolean updateSurfaceList ()
	{
		boolean addedSurfaceControl = false;
		List<ThreeDSurfaceControl> surfaceControls = GeopodPlugin.findIsosurfaceDisplayControls (m_viewManager);
		for (ThreeDSurfaceControl control : surfaceControls)
		{
			if (!m_surfaceList.contains (control))
			{
				m_surfaceList.add (control);
				addedSurfaceControl = true;
			}
		}

		//      boolean removedSurfaceControl = false;
		//		Iterator<ThreeDSurfaceControl> controlIterator = m_surfaceList.iterator();
		//		while (controlIterator.hasNext()) {
		//			ThreeDSurfaceControl control = controlIterator.next ();
		//			if(control == null || control.getDataChoice () == null)
		//			{
		//				removedSurfaceControl = true;
		//				controlIterator.remove ();
		//			}
		//		}

		boolean removedSurfaceControl = false;
		List<ThreeDSurfaceControl> controlsToRemove = new ArrayList<ThreeDSurfaceControl> ();
		for (ThreeDSurfaceControl control : m_surfaceList)
		{
			if (control == null || control.getDataChoice () == null)
			{
				controlsToRemove.add (control);
				removedSurfaceControl = true;
			}
		}
		m_surfaceList.removeAll (controlsToRemove);

		return (addedSurfaceControl || removedSurfaceControl);
	}

	public void refreshComponents ()
	{
		if (updateSurfaceList ())
		{
			// TODO: find better way to do this, or fire event rather than holding hud
			// Something has changed in the list - reset gridpoints
			m_hud.resetGridPoints ();

			setUpIsosurfaceList ();
			addIsosurfaceListToScrollPane ();
		}
		trimSurfaceNames ();
	}

	public void toggleVisibility ()
	{
		this.setVisible (!this.isVisible ());
		notifyObservers (GeopodEventId.ISOSURFACE_BUTTON_STATE_CHANGED);
	}

	/******************************************************************************************************************************
	 * Custom ActionListeners
	 */

	private class RemoveSurfaceActionListener
			implements ActionListener
	{

		ThreeDSurfaceControl m_control;

		RemoveSurfaceActionListener (ThreeDSurfaceControl control)
		{
			m_control = control;
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			if (m_control != null && m_control.getDataChoice () != null)
			{
				try
				{
					m_control.doRemove ();
					// m_control.dataChanged ();
				}
				catch (RemoteException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace ();
				}
				catch (VisADException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace ();
				}
				refreshComponents ();
			}
		}
	}

	private class AddSurfaceActionListener
			implements ActionListener
	{
		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_viewManager.getIdvUIManager ().dataSelectorToFront ();
		}
	}

	/****************************************************************************************************************************
	 * ISubject methods
	 */

	@Override
	public void addObserver (IObserver observer, GeopodEventId eventId)
	{
		m_subjectImpl.addObserver (observer, eventId);
	}

	@Override
	public void removeObserver (IObserver observer, GeopodEventId eventId)
	{
		m_subjectImpl.removeObserver (observer, eventId);
	}

	@Override
	public void notifyObservers (GeopodEventId eventId)
	{
		m_subjectImpl.notifyObservers (eventId);
	}

	@Override
	public void removeObservers ()
	{
		m_subjectImpl.removeObservers ();
	}

}
