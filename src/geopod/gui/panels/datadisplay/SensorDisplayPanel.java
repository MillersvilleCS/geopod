package geopod.gui.panels.datadisplay;

import geopod.Geopod;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.DisplayPanelManager;
import geopod.gui.GridEntry;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import visad.Real;

/**
 * A specialized {@link GeopodGridCellPanel} that formats and displays
 * {@link geopod.devices.Sensor Sensor} values in a grid. This class utilizes a
 * {@link GridCellManager} to facilitate in mappings between sensors and cells.
 * 
 * @author Geopod Team
 * 
 */
public class SensorDisplayPanel
		extends GeopodGridCellPanel
		implements ISubject

{
	private static final long serialVersionUID = 8152010143101949635L;
	private static final String FORMAT_PATTERN;
	private static final String DEFAULT_INSETS;

	static
	{
		FORMAT_PATTERN = "0.#";
		DEFAULT_INSETS = "   ";
	}

	private transient Geopod m_geopod;
	private DecimalFormat m_formatter;
	private GridCellManager m_cellManager;
	private SubjectImpl m_subject;
	private String m_insets;
	private DisplayPanelManager m_displayManager;

	/**
	 * Default SensorDisplayPanel constructor. This constructor initializes a
	 * formatter and a set of default mappings.
	 * 
	 * @param geopod
	 *            the geopod to get sensors from.
	 */
	public SensorDisplayPanel (Geopod geopod)
	{
		m_geopod = geopod;
		m_formatter = new DecimalFormat (FORMAT_PATTERN);
		m_cellManager = new GridCellManager (this);
		m_subject = new SubjectImpl ();
		m_insets = DEFAULT_INSETS;
		initializeDefaultMappings ();
	}

	/**
	 * Clears and reinitializes all possible grid entries.
	 */
	private void initializeDefaultMappings ()
	{
		m_cellManager.clearMappings ();
		m_cellManager.reinitializeEntrySet ();
	}

	@Override
	public void setGridSize (int rows, int cols)
	{
		super.setGridSize (rows, cols);
		initializeDefaultMappings ();
	}

	/**
	 * Associates the SensorDisplayPanel with a {@link DisplayPanelManager}.
	 * 
	 * @param manager
	 *            the DisplayPanelManager to register with the display
	 */
	public void setDisplayManager (DisplayPanelManager manager)
	{
		m_displayManager = manager;
	}

	/**
	 * Sets the initial mapping of {@link GridEntry} to fully qualified
	 * parameter names
	 * 
	 * @param parameterMap
	 *            the map of GridEntry -> ParameterName
	 */
	public void setDefaultParameterMappings (Map<GridEntry, String> parameterMap)
	{
		m_cellManager.addParameterMapping (parameterMap);
	}

	/**
	 * Sets a mapping from a {@link GridEntry} to a fully qualified parameter
	 * name
	 * 
	 * @param entry
	 *            the {@link GridEntry}
	 * @param parameterName
	 *            the fully qualified parameter name
	 */
	public void setParameterMapping (GridEntry entry, String parameterName)
	{
		m_cellManager.addParameterMapping (entry, parameterName);

		if (this.isEmpty ())
		{
			this.notifyObservers (GeopodEventId.DISPLAY_PANEL_EMPTY);
		}
	}

	/**
	 * Returns the {@link DisplayPanelManager} that is associated with this
	 * display
	 * 
	 * @return the DisplayPanelManager
	 */
	public DisplayPanelManager getDisplayManager ()
	{
		return (m_displayManager);
	}

	/**
	 * Returns the formal parameter name associated with a {@link GridEntry}
	 * 
	 * @param entry
	 *            the grid entry
	 * @return the formal parameter name
	 */
	public String getMappedParameterName (GridEntry entry)
	{
		return (m_cellManager.getParameterName (entry));
	}

	/**
	 * Returns a set of all mapped parameter names
	 * 
	 * @return a set of all mapped parameter names
	 */
	public Set<String> getAllMappedParameterNames ()
	{
		return (m_cellManager.getAllMappedParameters ());
	}

	/**
	 * Removes all parameter mappings to all parameters contained in the set If
	 * the sensor panel is empty, it will notify any observers
	 * 
	 * @param parameterNames
	 *            the parameters to remove
	 */
	public void removeParameters (Set<String> parameterNames)
	{
		for (String s : parameterNames)
		{
			m_cellManager.removeParameter (s);
		}

		if (this.isEmpty ())
		{
			this.notifyObservers (GeopodEventId.DISPLAY_PANEL_EMPTY);
		}
	}

	/**
	 * Determines whether or not this DisplayPanel is empty
	 * 
	 * @return true if it is empty, false otherwise
	 */
	public boolean isEmpty ()
	{
		return (m_cellManager.isEmpty ());
	}

	/**
	 * Attempts to add all the parameters in the specified set of parameter
	 * names If this Display is empty before parameters are added, observers are
	 * notified that the display panel is active.
	 * 
	 * @param parameterNames
	 *            the set of all parameters to add
	 * @return a set of parameters that could not be mapped (overflow)
	 */
	public Set<String> addParameters (Set<String> parameterNames)
	{
		if (this.isEmpty ())
		{
			this.notifyObservers (GeopodEventId.DISPLAY_PANEL_ACTIVE);
		}

		Set<String> overFlow = new HashSet<String> ();

		for (String parameter : parameterNames)
		{
			GridEntry openCell = m_cellManager.findNextAvailableCell ();

			if (openCell != null)
			{
				m_cellManager.addParameterMapping (openCell, parameter);
			}
			else
			{
				overFlow.add (parameter);
			}
		}

		return (overFlow);
	}

	/**
	 * Sets the insets to a string a specified length
	 * 
	 * @param numSpaces
	 *            the number of spaces to use to build a string
	 */
	public void setInsets (int numSpaces)
	{
		char[] spaces = new char[numSpaces];
		Arrays.fill (spaces, ' ');
		m_insets = new String (spaces);
	}

	/**
	 * Updates this DisplayPanel to reflect the values of all currently loaded
	 * sensors in {@link geopod}
	 */
	public void updateDisplay ()
	{
		Set<GridEntry> gridEntries = m_cellManager.getPossibleGridEntries ();

		for (GridEntry cellLocation : gridEntries)
		{
			// Write "" to empty cells
			if (m_cellManager.isEmptyCell (cellLocation))
			{
				super.setGridCellText (cellLocation.getRow (), cellLocation.getCol (), "");
			}
			else
			{
				String formalParameterName = m_cellManager.getParameterName (cellLocation);
				String simpleName = ParameterAbbreviator.getAbbreviation (formalParameterName);

				Real sample = m_geopod.getSensorValue (formalParameterName);
				String formattedSampleData = formatSampleString (sample, simpleName);
				super.setGridCellText (cellLocation.getRow (), cellLocation.getCol (), m_insets + formattedSampleData);
			}
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
	public void swapGridCellContents (GridEntry entryA, GridEntry entryB)
	{
		m_cellManager.swapParameterNames (entryA, entryB);
	}

	/**
	 * Formats a {@link Real} sample value and label into a string
	 * 
	 * @param sample
	 *            the sample to be formatted
	 * @param parameterLabel
	 *            the label to be affixed to the sample
	 * @return a formatted string containing a label and a sample value
	 */
	private String formatSampleString (Real sample, String parameterLabel)
	{
		String formattedString;
		String missingText = "--";

		if (sample == null || sample.isMissing ())
		{
			formattedString = parameterLabel + "  " + missingText;

			return (formattedString);
		}

		double value = 0;
		String unit = "";
		
		value = sample.getValue ();
		unit = sample.getUnit ().toString ();

		StringBuilder textBuilder = new StringBuilder ();
		textBuilder.append (parameterLabel);
		textBuilder.append ("  ");
		textBuilder.append (m_formatter.format (value));
		textBuilder.append (" ");
		textBuilder.append (unit);

		formattedString = textBuilder.toString ();

		return (formattedString);
	}

	@Override
	public void addObserver (IObserver observer, GeopodEventId eventId)
	{
		m_subject.addObserver (observer, eventId);
	}

	@Override
	public void removeObserver (IObserver observer, GeopodEventId eventId)
	{
		m_subject.removeObserver (observer, eventId);
	}

	@Override
	public void notifyObservers (GeopodEventId eventId)
	{
		m_subject.notifyObservers (eventId);
	}

	@Override
	public void removeObservers ()
	{
		m_subject.removeObservers ();
	}
}
