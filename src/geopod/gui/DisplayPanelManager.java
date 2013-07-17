package geopod.gui;

import geopod.gui.panels.datadisplay.SensorDisplayPanel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import ucar.unidata.data.DataChoice;

import com.sun.java.swing.plaf.windows.WindowsBorders.DashedBorder;

/**
 * A DisplayPanelManager manages multiple SensorDisplayPanels. The first
 * SensorDisplayPanel added is considered "Primary" and all additions of
 * parameters will be added there first. When the primary
 * {@link SensorDisplayPanel} reaches maximum capacity, the overflow is added to
 * other secondary display panels.
 * 
 * @author Geopod Team
 * 
 */
public class DisplayPanelManager
{
	private List<SensorDisplayPanel> m_displayPanels;

	/**
	 * Constructs a DisplayPanelManager
	 */
	public DisplayPanelManager ()
	{
		m_displayPanels = new ArrayList<SensorDisplayPanel> ();
	}

	/**
	 * Adds the specified {@link SensorDisplayPanel} to this
	 * DisplayPanelManager.
	 * 
	 * @param sensorDisplay
	 *            the SensorDisplayPanel to be managed
	 */
	public void addDisplayPanel (SensorDisplayPanel sensorDisplay)
	{
		sensorDisplay.setDisplayManager (this);
		m_displayPanels.add (sensorDisplay);
	}

	/**
	 * Shows a border for all managed {@link SensorDisplayPanel}
	 */
	public void showBorders ()
	{
		DashedBorder dashedBorder = new DashedBorder (Color.black);

		for (SensorDisplayPanel dp : m_displayPanels)
		{
			dp.setGridCellBorder (dashedBorder);
		}
	}

	/**
	 * Hides border for all managed {@link SensorDisplayPanel}
	 */
	public void hideBorders ()
	{
		Border emptyBorder = BorderFactory.createEmptyBorder ();

		for (SensorDisplayPanel dp : m_displayPanels)
		{
			dp.setGridCellBorder (emptyBorder);
		}
	}

	/**
	 * Updates all managed {@link SensorDisplayPanel} to reflect the specified
	 * list of new data choices.
	 * 
	 * @param newChoices
	 *            an updated list of {@link DataChoice}
	 */
	public void updateDisplayMappings (List<DataChoice> newChoices)
	{
		Set<String> newChoicesStringSet = new HashSet<String> ();

		for (DataChoice choice : newChoices)
		{
			String formalParamName = choice.getDescription ();
			newChoicesStringSet.add (formalParamName);
		}

		Set<String> currentlyMappedParameters = new HashSet<String> ();

		// Build the set of all currently mapped parameter names
		for (SensorDisplayPanel dp : m_displayPanels)
		{
			currentlyMappedParameters.addAll (dp.getAllMappedParameterNames ());
		}

		// Build the set of adds
		Set<String> adds = new HashSet<String> (newChoicesStringSet);
		adds.removeAll (currentlyMappedParameters);

		// Build the set of removes
		Set<String> removes = new HashSet<String> (currentlyMappedParameters);
		removes.removeAll (newChoicesStringSet);

		// Remove from every display panel under this manager
		for (SensorDisplayPanel dp : m_displayPanels)
		{
			dp.removeParameters (removes);
		}

		// Starting with the Primary Display Panel, add parameters and catch overflows
		Set<String> overFlow = new HashSet<String> ();
		for (SensorDisplayPanel dp : m_displayPanels)
		{
			// The first DisplayPanel is considered the "Primary DisplayPanel"
			if (dp == m_displayPanels.get (0))
			{
				overFlow = dp.addParameters (adds);
			}
			else if (overFlow.size () > 0)
			{
				overFlow = dp.addParameters (overFlow);
			}
		}
	}

	/**
	 * Updates all managed {@link SensorDisplayPanel} to reflect their current
	 * values
	 */
	public void updateDisplayPanels ()
	{
		for (SensorDisplayPanel dp : m_displayPanels)
		{
			dp.updateDisplay ();
		}
	}
}
