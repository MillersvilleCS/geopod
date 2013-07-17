package geopod.gui.panels.datadisplay;

import geopod.gui.GridEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * The GridCellManager is used by the {@link SensorDisplayPanel} to handle all
 * mappings between parameter names and {@link GridEntry}
 * 
 * @author Geopod Team
 * 
 */
class GridCellManager
		implements Serializable
{
	private static final long serialVersionUID = 6774588094446812101L;
	private BiMap<GridEntry, String> m_cellParamMap;
	private Set<GridEntry> m_possibleEntries;
	private SensorDisplayPanel m_displayPanel;

	/**
	 * Creates a new GridCellManager to manage a specified
	 * {@link SensorDisplayPanel}
	 * 
	 * @param sensorDisplay
	 *            the sensorDisplay to be managed by this manager
	 */
	public GridCellManager (SensorDisplayPanel sensorDisplay)
	{
		m_displayPanel = sensorDisplay;
		m_cellParamMap = HashBiMap.create ();
		m_possibleEntries = initializeEntrySet ();
	}

	/**
	 * Maps entry -> parameterName. If parameterName is null, entry is removed
	 * from this manager, and an empty tooltip is set.
	 * 
	 * @param entry
	 *            the GridEntry
	 * @param parameterName
	 *            the parameter name to
	 */
	public void addParameterMapping (GridEntry entry, String parameterName)
	{
		if (parameterName == null)
		{
			m_cellParamMap.remove (entry);
			m_displayPanel.setCellToolTip (entry, null);
		}
		else
		{
			m_cellParamMap.forcePut (entry, parameterName);
			m_displayPanel.setCellToolTip (entry, parameterName);
		}
	}

	/**
	 * Adds the specified Map to this manager
	 * 
	 * @param parameterMap
	 *            - the map to add
	 */
	public void addParameterMapping (Map<GridEntry, String> parameterMap)
	{
		for (Entry<GridEntry, String> entry : parameterMap.entrySet ())
		{
			m_cellParamMap.put (entry.getKey (), entry.getValue ());
			m_displayPanel.setCellToolTip (entry.getKey (), entry.getValue ());
		}
	}

	/**
	 * Clears all mappings associated with this manager
	 */
	public void clearMappings ()
	{
		m_cellParamMap.clear ();
	}

	/**
	 * Determines the next empty cell (if available). Cells are sorted by column
	 * then row such that in a 3 x 3 grid, if both cell 3,0 and 0,1 are both
	 * available, cell 3,0 is returned.
	 * 
	 * @return the {@link GridEntry} associated with the next available cell
	 */
	public GridEntry findNextAvailableCell ()
	{
		List<GridEntry> entryList = new ArrayList<GridEntry> (m_possibleEntries);
		Collections.sort (entryList, GridEntry.getComparator ());

		for (GridEntry e : entryList)
		{
			if (!m_cellParamMap.containsKey (e))
			{
				return (e);
			}
		}

		// no available slots
		return (null);
	}

	/**
	 * Returns all the parameters that are currently managed
	 * 
	 * @return the set of all mapped parameters
	 */
	public Set<String> getAllMappedParameters ()
	{
		return (m_cellParamMap.inverse ().keySet ());
	}

	/**
	 * Returns all the grid entries currently mapped to parameter names.
	 * 
	 * @return the set of all currently mapped grid entries.
	 */
	public Set<GridEntry> getGridEntries ()
	{
		return (m_cellParamMap.keySet ());
	}

	/**
	 * Returns the parameter name that is mapped by entry.
	 * 
	 * @param entry
	 *            the specified {@link GridEntry}
	 * @return the parameter name mapped by the specified GridEntry, null
	 *         otherwise
	 */
	public String getParameterName (GridEntry entry)
	{
		return (m_cellParamMap.get (entry));
	}

	/**
	 * Returns the set of all possible {@link GridEntry} that are managed
	 * 
	 * @return the set of GridEntry
	 */
	public Set<GridEntry> getPossibleGridEntries ()
	{
		return (m_possibleEntries);
	}

	/**
	 * Initializes the set of all possible GridEntry based on the current size
	 * of the sensor display panel.
	 * 
	 * @return the set of all possible GridEntry
	 */
	private Set<GridEntry> initializeEntrySet ()
	{
		Set<GridEntry> entries = new HashSet<GridEntry> ();

		for (int r = 0; r < m_displayPanel.getRows (); ++r)
		{
			for (int c = 0; c < m_displayPanel.getColumns (); ++c)
			{
				entries.add (new GridEntry (r, c));
			}
		}
		return (entries);
	}

	/**
	 * Determines whether or not any parameters are mapped by GridEntry
	 * 
	 * @return true if there are no mapped parameter names, false otherwise
	 */
	public boolean isEmpty ()
	{
		return (m_cellParamMap.isEmpty ());
	}

	/**
	 * Determines whether or not a given cell is empty
	 * 
	 * @param entry
	 *            the {@link GridEntry} of the cell
	 * @return true if empty, otherwise false
	 */
	public boolean isEmptyCell (GridEntry entry)
	{
		// A Cell is Empty iff the entry is not contained in the map
		return (!m_cellParamMap.containsKey (entry));
	}

	/**
	 * isMapped returns true if and only if there exists a {@link GridEntry} e
	 * such that e -> parameterName
	 * 
	 * @param parameterName
	 * @return true if and only if there exists a {@link GridEntry} e such that
	 *         e -> parameterName.
	 */
	public boolean isMapped (String parameterName)
	{
		return (m_cellParamMap.containsValue (parameterName));
	}

	/**
	 * Reinitializes the set of all possible grid entries. Should be called
	 * after {@link SensorDisplayPanel} resizes.
	 */
	public void reinitializeEntrySet ()
	{
		m_possibleEntries = this.initializeEntrySet ();
	}

	/**
	 * Removes the specified parameter from this manager
	 * 
	 * @param paramName
	 *            - the parameter name to remove
	 */
	public void removeParameter (String paramName)
	{
		GridEntry entry = m_cellParamMap.inverse ().get (paramName);
		m_cellParamMap.remove (entry);
		if (entry != null)
		{
			m_displayPanel.setCellToolTip (entry, null);
		}
	}

	/**
	 * Maps the parameter name associated with entryA to entryB. Likewise, the
	 * parameter name associated with entryB is mapped to entryA.
	 * 
	 * @param entryA
	 *            the first grid entry
	 * @param entryB
	 *            the second grid entry
	 */
	public void swapParameterNames (GridEntry entryA, GridEntry entryB)
	{
		String parameterA = m_cellParamMap.get (entryA);
		String parameterB = m_cellParamMap.get (entryB);

		this.addParameterMapping (entryA, parameterB);
		this.addParameterMapping (entryB, parameterA);
	}
}
