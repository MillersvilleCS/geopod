package geopod.gui.panels;

import geopod.GeopodPlugin;
import geopod.constants.UIConstants;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.components.ButtonFactory;
import geopod.gui.components.PainterFactory;
import geopod.utils.collections.SortedListModel;
import geopod.utils.comparators.DataChoiceComparator;
import geopod.utils.debug.Debug;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;

import ucar.unidata.data.DataChoice;

public class ParameterChooserPanel
		extends JXPanel
		implements ActionListener, ISubject
{
	private static final long serialVersionUID = 1L;

	private SubjectImpl m_subjectImpl;

	private GeopodPlugin m_plugin;

	private JScrollPane m_possibleDataScrollPane;
	private JScrollPane m_selectedDataScrollPane;

	private JXList m_possibleDataList;
	private JXList m_selectedDataList;

	private JButton m_removeButton;
	private JButton m_addButton;

	private SortedListModel<DataChoice> m_possibleDataListModel;
	private SortedListModel<DataChoice> m_selectedDataListModel;

	// Maximum number of selected parameters
	private static boolean m_limitMaxSelectedParameters = true;
	private static int m_limitMaxSelectedParametersTo = 19;

	// Remember that minimum for selected parameters = 1
	private static boolean m_limitMinSelectedParameters = true;
	private static int m_limitMinSelectedParametersTo = 1;

	/**
	 * Constuctor.
	 * 
	 * @param plugin
	 *            - the plugin to set the parameters on.
	 * @param possibleChoices
	 *            - the possible choices to display.
	 * @param currentlySelected
	 *            - the currently selected choices to display.
	 */
	public ParameterChooserPanel (GeopodPlugin plugin, List<DataChoice> possibleChoices,
			List<DataChoice> currentlySelected)
	{
		m_subjectImpl = new SubjectImpl ();

		m_plugin = plugin;

		setupLayout ();
		setupPanelBackground ();

		addLabels ();
		addScrollPanes ();
		addSouthButtonPanel ();

		setupListModels (possibleChoices, currentlySelected);
	}

	private void setupPanelBackground ()
	{
		MattePainter gradient = PainterFactory.createStandardGradientBackground ();
		this.setBackgroundPainter (gradient);
	}

	private SortedListModel<DataChoice> buildSortedListModel (List<DataChoice> choices)
	{
		SortedListModel<DataChoice> sortedListModel = new SortedListModel<DataChoice> (new DataChoiceComparator ());
		sortedListModel.addAll (choices);

		return (sortedListModel);
	}

	private void setupLayout ()
	{
		MigLayout migLayout = new MigLayout ("", "[grow, center][grow, center][grow, center]", "[][]1[][]");
		super.setLayout (migLayout);
	}

	private void addScrollPanes ()
	{
		//PreferredSizes are set to ensure the MigLayout makes these components consume their entire cell
		m_possibleDataScrollPane = new JScrollPane ();
		m_possibleDataScrollPane.setPreferredSize (new Dimension (2000, 2000));
		super.add (m_possibleDataScrollPane, "pushy, growpriox 200");

		JPanel addRemoveButtons = buildAddRemovePanel ();
		super.add (addRemoveButtons);

		m_selectedDataScrollPane = new JScrollPane ();
		m_selectedDataScrollPane.setPreferredSize (new Dimension (2000, 2000));
		super.add (m_selectedDataScrollPane, "wrap, pushy, growpriox 200");
	}

	private void setupListModels (List<DataChoice> possibleChoices, List<DataChoice> currentlySelected)
	{
		Font font = UIConstants.GEOPOD_VERDANA;
		font = font.deriveFont (12.0f);

		m_possibleDataListModel = buildSortedListModel (possibleChoices);
		m_possibleDataList = new JXList (m_possibleDataListModel);
		m_possibleDataScrollPane.setViewportView (m_possibleDataList);

		m_possibleDataList.setDragEnabled (true);
		m_possibleDataList.setBackground (UIConstants.GEOPOD_GREEN);
		m_possibleDataList.setFont (font);

		m_selectedDataListModel = buildSortedListModel (currentlySelected);
		m_selectedDataList = new JXList (m_selectedDataListModel);
		m_selectedDataScrollPane.setViewportView (m_selectedDataList);

		m_selectedDataList.setDragEnabled (true);
		m_selectedDataList.setBackground (UIConstants.GEOPOD_GREEN);
		m_selectedDataList.setFont (font);
	}

	private void addLabels ()
	{
		JLabel instructionLabel = new JLabel ();

		Font font = UIConstants.GEOPOD_BANDY;
		instructionLabel.setText ("SELECT PARAMETERS BELOW");
		instructionLabel.setFont (font.deriveFont (UIConstants.TITLE_SIZE));
		super.add (instructionLabel, "pushx, wrap, spanx 3");

		JLabel selectedDataLabel = new JLabel ();
		selectedDataLabel.setText ("AVAILABLE PARAMETERS");
		selectedDataLabel.setFont (font.deriveFont (UIConstants.SUBTITLE_SIZE));
		super.add (selectedDataLabel);

		JLabel availableDataChoicesLabel = new JLabel ();
		availableDataChoicesLabel.setText ("SELECTED PARAMETERS");
		availableDataChoicesLabel.setFont (font.deriveFont (UIConstants.SUBTITLE_SIZE));
		super.add (availableDataChoicesLabel, "skip, wrap");
	}

	private JPanel buildButtonPanel ()
	{
		JPanel buttonPanel = new JPanel ();
		FlowLayout buttonPanelLayout = new FlowLayout ();
		buttonPanel.setLayout (buttonPanelLayout);
		buttonPanel.setOpaque (false);

		float buttonFontSize = UIConstants.BUTTON_FONT_SIZE;

		JButton saveButton = ButtonFactory.createGradientButton (buttonFontSize, UIConstants.GEOPOD_GREEN, false);
		saveButton.setText ("SAVE CHANGES");
		saveButton.setActionCommand ("save");
		saveButton.addActionListener (this);
		buttonPanel.add (saveButton);

		JButton saveBundleButton = ButtonFactory.createGradientButton (buttonFontSize, UIConstants.GEOPOD_GREEN, false);
		saveBundleButton.setText ("SAVE AS BUNDLE");
		saveBundleButton.setActionCommand ("saveBundle");
		saveBundleButton.addActionListener (this);
		buttonPanel.add (saveBundleButton);

		JButton removeBundleButton = ButtonFactory.createGradientButton (buttonFontSize, UIConstants.GEOPOD_GREEN,
				false);
		removeBundleButton.setText ("REMOVE BUNDLE");
		removeBundleButton.setActionCommand ("removeBundle");
		removeBundleButton.addActionListener (this);
		buttonPanel.add (removeBundleButton);

		JButton cancelButton = ButtonFactory.createGradientButton (buttonFontSize, UIConstants.GEOPOD_GREEN, false);
		cancelButton.setText ("CANCEL");
		cancelButton.setActionCommand ("cancel");
		cancelButton.addActionListener (this);
		buttonPanel.add (cancelButton);

		return (buttonPanel);
	}

	private JPanel buildAddRemovePanel ()
	{
		JPanel addRemovePanel = new JPanel ();
		GridLayout addRemovePanelLayout = new GridLayout (0, 1);
		addRemovePanelLayout.setColumns (1);
		addRemovePanelLayout.setHgap (5);
		addRemovePanelLayout.setVgap (10);
		addRemovePanelLayout.setRows (0);
		addRemovePanel.setLayout (addRemovePanelLayout);
		addRemovePanel.setOpaque (false);

		m_addButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE, UIConstants.GEOPOD_NOTE_COLOR,
				false);
		m_addButton.setText (">>");
		m_addButton.setActionCommand ("add");
		m_addButton.addActionListener (this);
		m_addButton.setFont (UIConstants.GEOPOD_BANDY.deriveFont (18.0f));

		m_removeButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
				UIConstants.GEOPOD_NOTE_COLOR, false);
		m_removeButton.setText ("<<");
		m_removeButton.setActionCommand ("remove");
		m_removeButton.addActionListener (this);
		m_removeButton.setFont (UIConstants.GEOPOD_BANDY.deriveFont (18.0f));

		addRemovePanel.add (m_addButton);
		addRemovePanel.add (m_removeButton);

		return (addRemovePanel);
	}

	private void addSouthButtonPanel ()
	{
		JPanel buttonPanel = buildButtonPanel ();
		super.add (buttonPanel, "spanx 3");
	}

	public List<String> getSelected ()
	{
		List<String> choicesList = new ArrayList<String> (m_possibleDataListModel.getSize ());
		for (int i = 0; i < m_possibleDataListModel.getSize (); ++i)
		{
			choicesList.add (m_possibleDataListModel.getElementAt (i).toString ());
		}
		return (choicesList);
	}

	private void saveParameterChoices ()
	{
		Debug.println ("Saved parameters");
		// Build a list from the data model
		List<DataChoice> newChoices = new ArrayList<DataChoice> ();

		for (int i = 0; i < m_selectedDataListModel.getSize (); ++i)
		{
			DataChoice dc = m_selectedDataListModel.getElementAt (i);
			newChoices.add (dc);
		}

		m_plugin.resetDataChoices (newChoices);
	}

	public void updateDataChoices ()
	{
		List<DataChoice> allDataChoices = m_plugin.getPossibleDataChoices ();
		List<DataChoice> selectedDataChoices = m_plugin.getDataChoices ();

		allDataChoices.removeAll (selectedDataChoices);

		m_possibleDataListModel = buildSortedListModel (allDataChoices);
		m_possibleDataList.setModel (m_possibleDataListModel);

		m_selectedDataListModel = buildSortedListModel (selectedDataChoices);
		m_selectedDataList.setModel (m_selectedDataListModel);

		// Make sure the buttons are well
		updateAddRemoveButtonState ();
	}

	/**
	 * Enables or disables (as necessary) the add/remove buttons
	 */
	public void updateAddRemoveButtonState ()
	{
		int totalPossible = m_possibleDataListModel.getSize ();
		int totalSelected = m_selectedDataListModel.getSize ();

		boolean addEnabled = !(totalPossible == 0 || (m_limitMaxSelectedParameters && totalSelected >= m_limitMaxSelectedParametersTo));
		boolean removeEnabled = !(m_limitMinSelectedParameters && totalSelected <= m_limitMinSelectedParametersTo);

		m_addButton.setEnabled (addEnabled);
		m_removeButton.setEnabled (removeEnabled);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed (ActionEvent e)
	{
		String actionCommand = e.getActionCommand ();

		if (actionCommand.equals ("save"))
		{
			saveParameterChoices ();
			this.setVisible (false);
			notifyObservers (GeopodEventId.PARAMETER_BUTTON_STATE_CHANGED);
		}
		else if (actionCommand.equals ("saveBundle"))
		{
			saveParameterChoices ();
			this.setVisible (false);
			notifyObservers (GeopodEventId.PARAMETER_BUTTON_STATE_CHANGED);
			m_plugin.saveAsDefaultBundle ();
		}
		else if (actionCommand.equals ("removeBundle"))
		{
			Debug.println ("Removed default bundle");
			m_plugin.removeDefaultBundle ();
			this.setVisible (false);
			notifyObservers (GeopodEventId.PARAMETER_BUTTON_STATE_CHANGED);
		}
		else if (actionCommand.equals ("cancel"))
		{
			// Hide window and discard changes
			Debug.println ("Canceled parameter selection");
			this.setVisible (false);
			notifyObservers (GeopodEventId.PARAMETER_BUTTON_STATE_CHANGED);
		}
		else if (actionCommand.equals ("add"))
		{
			// Add selected parameters to the chosen parameters list
			Object[] dcs = m_possibleDataList.getSelectedValues ();

			// Number of available spots, and how many we should go through
			int availableSpots = m_limitMaxSelectedParametersTo - m_selectedDataListModel.getSize ();
			int addValues = dcs.length;
			if (availableSpots <= addValues)
			{
				addValues = availableSpots;
			}

			for (int i = 0; i < addValues; i++)
			{
				DataChoice dc = (DataChoice) dcs[i];
				m_selectedDataListModel.add (dc);
				m_possibleDataListModel.removeElement (dc);
				Debug.println ("added " + dc.toString ());
			}

			int index = m_possibleDataList.getSelectedIndex ();
			int size = m_possibleDataListModel.getSize ();

			if (size > 0)
			{
				if (index >= size)
				{
					// Removed item was in last row
					index = size - 1;
				}
				m_possibleDataList.setSelectedIndex (index);
			}
		}
		else if (actionCommand.equals ("remove"))
		{
			// Remove parameter from selected list
			Object[] selectedObjects = m_selectedDataList.getSelectedValues ();

			// How many to go through
			int process = selectedObjects.length;
			if (m_limitMinSelectedParameters
					&& (m_selectedDataListModel.getSize () - selectedObjects.length) <= m_limitMinSelectedParametersTo)
			{
				process = m_selectedDataListModel.getSize () - m_limitMinSelectedParametersTo;
			}

			for (int i = 0; i < process; i++)
			{
				DataChoice dc = (DataChoice) selectedObjects[i];
				m_selectedDataListModel.removeElement (dc);
				m_possibleDataListModel.add (dc);
			}

			int index = m_selectedDataList.getSelectedIndex ();
			int size = m_selectedDataListModel.getSize ();

			if (size > 0)
			{
				if (index >= size)
				{
					// Removed item was in last row
					index = size - 1;
				}
				m_selectedDataList.setSelectedIndex (index);
			}
		}

		// Just to be safe the buttons are well
		updateAddRemoveButtonState ();
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
}
