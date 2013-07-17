package geopod.gui.panels.dropsonde;

import geopod.constants.UIConstants;
import geopod.devices.Dropsonde;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.Hud;
import geopod.gui.components.BorderFactory;
import geopod.gui.components.ButtonFactory;
import geopod.gui.components.GeopodButton;
import geopod.gui.components.PainterFactory;
import geopod.utils.idv.SceneGraphControl;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.Animator.EndBehavior;
import org.jdesktop.animation.timing.interpolation.KeyFrames;
import org.jdesktop.animation.timing.interpolation.KeyTimes;
import org.jdesktop.animation.timing.interpolation.KeyValues;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.jdesktop.swingx.JXPanel;

/**
 * Panel to display chart data from multiple dropsonde panels. The overall
 * structure uses a card layout to switch between dropsonde panels. Each
 * dropsonde panel is a JTabbedPane that displays the dropsonde data charts.
 * 
 * Switching between dropsondes is handed by a cardLayout, while displaying
 * parameters is handled by the DropsondePanel.
 * 
 * When switching between dropsonde panels, the selected parameter tab is
 * maintained and stays the same even as the displayed data changes.
 */
public class MultipleDropsondeChartPanel
		extends JXPanel
		implements ListDataListener, ActionListener, AncestorListener, ChangeListener, ISubject
{
	private static final long serialVersionUID = -661385581141097483L;

	/**
	 * Dropsonde launch animation length in milliseconds;
	 */
	private static int LAUNCH_ANIMATION_LENGTH = 1000;

	private MutableComboBoxModel m_dropsondes;
	private JComboBox m_dropdown;

	private JPanel m_cardPanel;

	private DropsondePanel m_currentDropsondePanel;
	private int m_currentTab;

	private GeopodButton m_previousDropsondeButton;

	private GeopodButton m_nextDropsondeButton;

	private GeopodButton m_removeButton;

	private Hud m_hud;

	private SubjectImpl m_subjectImpl;

	// Animation values.
	private Animator m_launchIndicationAnimator;

	//	private ColorCellRenderer m_colorRenderer;

	/**
	 * Constructor.
	 * 
	 * @param dropsondeModel
	 *            - the model containing the dropsondes to display.
	 * @param hud
	 *            - needed to launch new dropsondes.
	 */
	public MultipleDropsondeChartPanel (MutableComboBoxModel dropsondeModel, Hud hud)
	{
		super (new BorderLayout ());
		super.setBorder (BorderFactory.createStandardBorder ());
		super.setBackgroundPainter (PainterFactory.createStandardMattePainter (406, 505));
		this.setLayout (new MigLayout ("", "1[align center]1", "2[]2[]3[]3"));

		m_subjectImpl = new SubjectImpl ();

		m_hud = hud;

		m_currentTab = 0;

		m_dropsondes = dropsondeModel;

		m_dropsondes.addListDataListener (this);

		m_dropdown = new JComboBox (dropsondeModel);

		//m_dropdown.setForeground (Color.BLUE);
		m_dropdown.setPreferredSize (new Dimension (200, 17));
		m_dropdown.addActionListener (this);
		this.add (m_dropdown, "split 3, gapright 5, gapleft 30");

		// Create animations for dropsonde launch
		createPulseAnimation ();

		createAndAddButtons ();
	}

	private void createPulseAnimation ()
	{
		KeyValues<Color> vals = KeyValues.create (Color.white, UIConstants.GEOPOD_DARK_GREEN, Color.white);
		KeyTimes times = new KeyTimes (0.0f, 0.5f, 1.0f);
		KeyFrames frames = new KeyFrames (vals, times);
		m_launchIndicationAnimator = PropertySetter.createAnimator (LAUNCH_ANIMATION_LENGTH, m_dropdown, "background",
				frames);
		m_launchIndicationAnimator.setEndBehavior (EndBehavior.RESET);
	}

	private void createAndAddButtons ()
	{
		m_previousDropsondeButton = ButtonFactory.createGradientButton (14, UIConstants.GEOPOD_GREEN, true);
		m_previousDropsondeButton.setMargin (new Insets (0, 1, 0, 1));
		m_previousDropsondeButton.setText ("<<");
		m_previousDropsondeButton.setActionCommand ("previous");
		m_previousDropsondeButton.setToolTipText ("Previous Dropsonde");
		m_previousDropsondeButton.addActionListener (this);
		m_previousDropsondeButton.setEnabled (false);
		this.add (m_previousDropsondeButton, "split 3, gapleft 3, gapright 3");

		m_nextDropsondeButton = ButtonFactory.createGradientButton (14, UIConstants.GEOPOD_GREEN, true);
		m_nextDropsondeButton.setMargin (new Insets (0, 1, 0, 1));
		m_nextDropsondeButton.setText (">>");
		m_nextDropsondeButton.setActionCommand ("next");
		m_nextDropsondeButton.setToolTipText ("Next Dropsonde");
		m_nextDropsondeButton.addActionListener (this);
		m_nextDropsondeButton.setEnabled (false);
		this.add (m_nextDropsondeButton, "split 3, gapleft 3, gapright 30, wrap");

		m_cardPanel = new JPanel (new CardLayout ());
		m_cardPanel.setOpaque (false);
		m_cardPanel.setPreferredSize (new Dimension (2000, 2000));
		this.add (m_cardPanel, "wrap");

		JButton launchButton = ButtonFactory.createGradientButton (16, UIConstants.GEOPOD_GREEN, false);
		launchButton.setText ("LAUNCH");
		launchButton.setActionCommand ("launch");
		launchButton.setToolTipText ("Launch Dropsonde");
		launchButton.addActionListener (this);
		launchButton.setEnabled (true);
		this.add (launchButton, "split 3, align center, gapright 20, height 29::, width 82::");

		m_removeButton = ButtonFactory.createGradientButton (16, UIConstants.GEOPOD_GREEN, false);
		m_removeButton.setText ("REMOVE");
		m_removeButton.setActionCommand ("remove");
		m_removeButton.setToolTipText ("Remove Dropsonde");
		m_removeButton.addActionListener (this);
		m_removeButton.setEnabled (false);
		this.add (m_removeButton, "split 3, align center, gapright 20, height 29::, width 86::");

		JButton closeButton = ButtonFactory.createGradientButton (16, UIConstants.GEOPOD_GREEN, false);
		closeButton.setText ("CLOSE");
		closeButton.setActionCommand ("close");
		closeButton.setToolTipText ("Close");
		closeButton.addActionListener (this);
		closeButton.setEnabled (true);
		this.add (closeButton, "split 3, align center, height 29::, width 69::");
	}

	/**
	 * When a new dropsonde is added to the model.
	 */
	@Override
	public void intervalAdded (ListDataEvent e)
	{
		int index = e.getIndex0 ();

		Dropsonde ds = (Dropsonde) m_dropsondes.getElementAt (index);

		DropsondePanel dp = new DropsondePanel (ds);
		dp.addChangeListener (this);
		dp.addAncestorListener (this);

		String dropsondeNumber = ds.toString ();

		m_cardPanel.add (dp, dropsondeNumber);

		// Set the visible dropsonde to the one that was just added
		m_dropsondes.setSelectedItem (ds);
		CardLayout cl = (CardLayout) m_cardPanel.getLayout ();
		cl.show (m_cardPanel, ds.toString ());

		// Disable the next button, as we are already on the last item.
		m_nextDropsondeButton.setEnabled (false);

		updateWithNewIndex (index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void intervalRemoved (ListDataEvent e)
	{
		int index = e.getIndex0 ();
		m_cardPanel.remove (index);

		updateWithNewIndex (index);
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void contentsChanged (ListDataEvent e)
	{
		// Do nothing.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed (ActionEvent e)
	{
		// Next and previous buttons will only modify the drop-down list. The list will then update 
		// the card display
		if (e.getActionCommand ().equals ("previous"))
		{
			int nextIndex = m_dropdown.getSelectedIndex () - 1;
			updateWithNewIndex (nextIndex);
		}
		else if (e.getActionCommand ().equals ("next"))
		{
			int nextIndex = m_dropdown.getSelectedIndex () + 1;
			updateWithNewIndex (nextIndex);
		}
		else if (e.getActionCommand ().equals ("launch"))
		{
			m_hud.launchDropsonde ();
		}
		else if (e.getActionCommand ().equals ("remove"))
		{
			Dropsonde ds = (Dropsonde) m_dropdown.getSelectedItem ();
			m_hud.removeDropsonde (ds);
			MutableComboBoxModel model = (MutableComboBoxModel) m_dropdown.getModel ();
			model.removeElement (ds);
		}
		else if (e.getActionCommand ().equals ("close"))
		{
			this.setVisible (false);
			notifyObservers (GeopodEventId.DROPSONDE_BUTTON_STATE_CHANGED);
		}
		else if (e.getActionCommand ().equals ("comboBoxChanged"))
		{
			// switch to the graph panel for the currently selected dropsonde.
			CardLayout cl = (CardLayout) m_cardPanel.getLayout ();
			Dropsonde ds = (Dropsonde) m_dropdown.getSelectedItem ();
			int index = m_dropdown.getSelectedIndex ();
			if (ds != null)
			{
				String dropsondeName = ds.toString ();
				cl.show (m_cardPanel, dropsondeName);
				// this done so works (should be done differently)
				boolean enableNext = true;
				boolean enablePrevious = true;
				int items = m_dropdown.getModel ().getSize ();
				int lastValidIndex = items - 1;
				if (index == 0 || items <= 1)
				{
					enablePrevious = false;
				}

				if (index == lastValidIndex || items <= 1)
				{
					enableNext = false;
				}

				m_nextDropsondeButton.setEnabled (enableNext);
				m_previousDropsondeButton.setEnabled (enablePrevious);
			}
		}
		updateSelectedSonde();
	}

	private void updateWithNewIndex (int newIndex)
	{
		boolean enableNext = true;
		boolean enablePrevious = true;

		int items = m_dropdown.getModel ().getSize ();
		int lastValidIndex = items - 1;

		if (newIndex < 0)
		{
			newIndex = 0;
		}

		if (newIndex > lastValidIndex)
		{
			newIndex = lastValidIndex;
		}

		if (newIndex == 0 || items <= 1)
		{
			enablePrevious = false;
		}

		if (newIndex == lastValidIndex || items <= 1)
		{
			enableNext = false;
		}

		// set the buttons
		m_nextDropsondeButton.setEnabled (enableNext);
		m_previousDropsondeButton.setEnabled (enablePrevious);

		// set the selected model
		m_dropdown.setSelectedIndex (newIndex);

		// make sure remove button is enables or disabled, as appropriate
		if (items > 0)
		{
			m_removeButton.setEnabled (true);
		}
		else
		{
			m_removeButton.setEnabled (false);
		}

	}

	@Override
	public void ancestorAdded (AncestorEvent event)
	{
		// handle a new dropsonde being added
		m_currentDropsondePanel = (DropsondePanel) event.getComponent ();
		//m_currentDropsondePanel.setSelectedIndex (m_currentTab);

		if (!m_launchIndicationAnimator.isRunning ())
		{
			m_launchIndicationAnimator.start ();
		}
	}

	@Override
	public void ancestorRemoved (AncestorEvent event)
	{
		// Do nothing.
	}

	@Override
	public void ancestorMoved (AncestorEvent event)
	{
		// Do nothing.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stateChanged (ChangeEvent e)
	{
		// update the selected tab
		if (e.getSource ().getClass ().equals (DropsondePanel.class))
		{
			DropsondePanel dp = (DropsondePanel) e.getSource ();
			m_currentTab = dp.getSelectedIndex ();
		}
	}

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
	public void updateSelectedSonde()
	{
		m_hud.updateSelectedSonde ((Dropsonde) m_dropdown.getSelectedItem ());
	}
}
