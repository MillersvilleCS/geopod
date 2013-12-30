/*****************************************************************************/
package geopod;

/*****************************************************************************/

import geopod.constants.parameters.ParameterUtil;
import geopod.devices.Sensor;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.GeopodFrame;
import geopod.gui.Hud;
import geopod.gui.panels.ParameterChooserPanel;
import geopod.utils.ThreadUtility;
import geopod.utils.coordinate.IdvCoordinateUtility;
import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;
import geopod.utils.debug.JythonConsole;
import geopod.utils.idv.SceneGraphControl;

import java.awt.Frame;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.media.j3d.Appearance;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import ucar.unidata.data.DataCancelException;
import ucar.unidata.data.DataCategory;
import ucar.unidata.data.DataChoice;
import ucar.unidata.data.DataInstance;
import ucar.unidata.data.DataSelection;
import ucar.unidata.data.DataSource;
import ucar.unidata.data.grid.GridDataInstance;
import ucar.unidata.idv.DisplayControl;
import ucar.unidata.idv.ViewManager;
import ucar.unidata.idv.control.ColorTableWidget;
import ucar.unidata.idv.control.DisplayControlImpl;
import ucar.unidata.idv.control.ThreeDSurfaceControl;
import ucar.unidata.idv.ui.IdvWindow;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.LogUtil.LogCategory;
import ucar.unidata.view.geoloc.NavigatedDisplay;
import visad.DisplayImpl;
import visad.Real;
import visad.VisADException;
import visad.java3d.DefaultDisplayRendererJ3D;

/*****************************************************************************/
/**
 * GeopodPlugin
 * 
 * @author Millersville Geopod Team
 */

public class GeopodPlugin
		extends DisplayControlImpl
		implements ISubject
{
	/**************************************************************************/

	/**
	 * This allows the plugin to check if a instance of Geopod is already
	 * running.
	 */
	private static boolean ms_isGeopodInstanceCreated = false;

	/**
	 * The data sets to use when initially creating a Geopod. Not used if
	 * restoring from a bundle.
	 */
	public static final Collection<String> m_defaultDataChoices;

	/**
	 * Skip DataChoice's in this category as we only want 3d volumes.
	 */
	private static final DataCategory TWO_DIMENSIONAL_CATEGORY;

	/**
	 * The IDV {@link LogCategory} to use.
	 */
	public static final LogCategory LOG_CATEGORY;

	/**
	 * List of sensors we definitely do not want to remove
	 */
	private static final Collection<String> m_nonRemovableSensorNames;

	/**************************************************************************/

	// Initialize static fields
	static
	{
		// Turn debugging on here if needed
		// configure log category
		LOG_CATEGORY = LogUtil.getLogInstance (GeopodPlugin.class.getName ());

		//m_defaultDataChoices = new ArrayList<>();
		//m_defaultDataChoices.addAll (ParameterListConstants.TEMPERATURE);
		//m_defaultDataChoices.addAll (ParameterListConstants.SPEED_D);
		//m_defaultDataChoices.addAll (ParameterListConstants.GEOPOTENTIAL_HEIGHT);
		//m_defaultDataChoices.addAll (ParameterListConstants.RELATIVE_HUMIDITY);
		//m_defaultDataChoices.addAll (ParameterListConstants.DEWPOINT_D);

		//m_defaultDataChoices = ParameterListConstants.getDefaultGeopodParameters ();
		//m_defaultDataChoices = IDV4ParameterConstants.getDefaultGeopodParameters ();
		m_defaultDataChoices = ParameterUtil.getDefaultGeopodParameters ();
		/*
		// Skip mixingratio for testing. It takes a long time to load
		if (!Debug.isDebuggingOn ())
		{
			m_defaultDataChoices = Arrays.asList ("Temperature @ isobaric", "Speed (from u_wind & v_wind)",
					"Geopotential_height @ isobaric", "Relative_humidity @ isobaric", "Temperature @ pressure",
					"Speed (from U-component_of_wind & V-component_of_wind)", "Geopotential_height @ pressure",
					"Relative_humidity @ pressure", "Dewpoint (from Temperature & Relative_humidity)",
					"Dew_point_temperature @ pressure");
			
			//m_defaultDataChoices.addAll (ParameterListConstants.MIXING_RATIO_D);
		}
		
		else
		{
			/*m_defaultDataChoices = Arrays.asList ("Temperature @ isobaric", "Speed (from u_wind & v_wind)",
					"Geopotential_height @ isobaric", "mixingratio", "Relative_humidity @ isobaric",
					"Temperature @ pressure", "Speed (from U-component_of_wind & V-component_of_wind)",
					"Geopotential_height @ pressure", "Relative_humidity @ pressure",
					"Dewpoint (from Temperature & Relative_humidity)", "Dew_point_temperature @ pressure");

			
		}
		
		*/
		// We skip DataChoice's in this category, as we don't need to handle 2D
		// data
		TWO_DIMENSIONAL_CATEGORY = new DataCategory (DataCategory.CATEGORY_2D, false);
		/*
				m_nonRemovableSensorNames = new ArrayList<String> (Arrays.asList ("Temperature @ isobaric",
						"Relative_humidity @ isobaric", "Temperature @ pressure", "Relative_humidity @ pressure"));
		*/
		//m_nonRemovableSensorNames = ParameterListConstants.getNonRemoveableParameters ();
		//m_nonRemovableSensorNames = IDV4ParameterConstants.getPermanentGeopodParameters ();
		m_nonRemovableSensorNames = ParameterUtil.getPermanentGeopodParameters ();
	}

	/**************************************************************************/

	private Geopod m_geopod;
	private Hud m_hud;

	/**
	 * A map containing the currently loaded sensors. In the form
	 * <tt>sensorName -> Sensor</tt>.
	 */
	private Map<String, Sensor> m_sensorMap;

	/**
	 * Implementation to handle observer notification.
	 */
	private SubjectImpl m_subjectImpl;

	/**
	 * The name of the data choice currently being loaded. Used for loading
	 * screens.
	 */
	private String m_dataChoiceNameBeingLoaded;

	/**
	 * Cached reference to the data choice the Geopod was created with. This
	 * field is persisted by the XmlEncoder.
	 */
	private DataChoice m_startingDataChoice;

	/**
	 * The sensor the Geopod was initially created with. Built using
	 * m_startingDataChoice for persistence.
	 */
	private Sensor m_startingSensor;

	/**
	 * Generic constructor. Most initialization code is in init().
	 */
	public GeopodPlugin ()
	{
		m_sensorMap = new HashMap<String, Sensor> ();
		m_subjectImpl = new SubjectImpl ();
	}

	/**
	 * Initialization method called by IDV.
	 * 
	 * @param choices
	 *            - The data choices to construct the plugin with.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean init (@SuppressWarnings("rawtypes") List choices)
	{
		// Terminate the plugin if another copy is already running.
		if (ms_isGeopodInstanceCreated)
		{
			// Throwing a DataCancelException allows for graceful termination.
			Debug.println ("Instance of Geopod already exists. Not creating new one.");
			throw new DataCancelException ();
		}
		else
		{
			ms_isGeopodInstanceCreated = true;
		}

		// Establish links with IDV scene graph
		NavigatedDisplay navigatedDisplay = super.getNavigatedDisplay ();
		DisplayImpl displayImpl = (DisplayImpl) navigatedDisplay.getDisplay ();
		DefaultDisplayRendererJ3D displayRenderer = (DefaultDisplayRendererJ3D) displayImpl.getDisplayRenderer ();

		// Disable display of text in 3D scene
		ViewManager viewManager = getViewManager ();
		viewManager.setShowDisplayList (false);

		// Initialize the scene graph control
		SceneGraphControl.setDisplayRenderer (displayRenderer);
		// Initialize coordinate system converter utility class
		IdvCoordinateUtility.setGeopodPlugin (this);
		// For executing concurrent tasks
		ThreadUtility.startupExecutors ();

		// Load user and default configuration files.
		String userPath = this.getIdv ().getResourceManager ().getUserPath ();
		ConfigurationManager.loadPreferences (userPath);
		// Need to set debug level here because the Debug class is loaded before
		// preferences are loaded
		if (ConfigurationManager.isEnabled (ConfigurationManager.Debug))
		{
			Debug.setDebugLevel (DebugLevel.MEDIUM);
		}

		// Build the Geopod
		m_geopod = new Geopod (this);
		m_geopod.setMovementEnabled (false);

		// Build the Hud
		m_hud = new Hud (this, m_geopod);

		this.addObserver (m_hud, GeopodEventId.DATA_CHOICE_LOADING_STARTED);
		this.addObserver (m_hud, GeopodEventId.ALL_CHOICES_LOADING_FINISHED);

		m_geopod.addObserver (m_hud, GeopodEventId.GEOPOD_TRANSLATED);
		m_geopod.addObserver (m_hud, GeopodEventId.TIME_CHANGED);
		m_geopod.addObserver (m_hud, GeopodEventId.AUTO_PILOT_FINISHED);
		m_geopod.addObserver (m_hud, GeopodEventId.LOCK_BUTTON_STATE_CHANGED);
		m_geopod.addObserver (m_hud, GeopodEventId.GO_BUTTON_STATE_CHANGED);

		// Splice Geopod view branch into the scene graph
		m_geopod.attachViewBranch ();

		this.processInitialDataChoices (choices);

		// Pass the sensors to the Geopod
		m_geopod.initAfterDataLoaded (m_sensorMap);
		m_hud.initAfterDataLoaded ();

		setMapGeometryInvisible ();
		// It would be nice to be able to minimize windows
		// using an event, but no such luck
		iconifyIdvWindowsAfterDelay (2000);

		if (Debug.consoleEnabled ())
		{
			this.createJythonConsole ();
		}

		// Dump the scene graph to stderr
		//SceneGraphDumper.dump (SceneGraphControl.getIdvContentBranch (), System.err);

		return (true);
	}

	// Setting the map to invisible ensures it's drawn in the minimap
	//   Only invisible objects are drawn in the minimap
	private void setMapGeometryInvisible ()
	{
		List<Shape3D> shapes = SceneGraphControl.findNodesOfType (Shape3D.class);

		// Huge assumption that first element is the map line geometry
		Shape3D s = shapes.get (0);
		RenderingAttributes attribs = s.getAppearance ().getRenderingAttributes ();

		Appearance app = s.getAppearance ();
		boolean writable = app.getCapability (Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
		if (attribs != null && writable)
		{
			attribs.setVisible (false);

			Debug.println (DebugLevel.HIGH, s.getGeometry (0).toString ());
		}
	}

	private void createJythonConsole ()
	{
		JythonConsole.create ();
		JythonConsole.addNameBinding ("plugin", this);
		JythonConsole.addNameBinding ("geopod", m_geopod);
		JythonConsole.setVisible (false);
	}

	/**
	 * 
	 * @param choices
	 *            - The list of choices that were selected from the during
	 *            Geopod creation
	 */
	@SuppressWarnings("unchecked")
	private void processInitialDataChoices (List<DataChoice> choices)
	{
		// Create list of choices to load
		List<DataChoice> selectedDataChoices = new ArrayList<DataChoice> ();

		boolean wasUnpersisted = this.getWasUnPersisted ();
		// Select default data choices
		if (wasUnpersisted)
		{
			Debug.println ("Loading bundle file. Ignoring default data choices.");
		}
		else
		{
			Debug.println ("No bundle selected. Adding default data choices.");

			// Iterate over data source and add all data choices
			// that are in the default data choices list.
			for (String name : m_defaultDataChoices)
			{
				boolean found = false;
				DataSource dataSource = this.getDataSource ();
				List<DataChoice> dataChoices = dataSource.getDataChoices ();

				for (DataChoice dataChoice : dataChoices)
				{
					if (name.equals (dataChoice.getDescription ()))
					{
						found = true;
						selectedDataChoices.add (dataChoice);
						System.err.println ("\tFound " + name + ". Loading...");
						break;
					}
				}

				if (!found)
				{
					Debug.println ("\t" + name + " not available in the selected data sources");
				}
			}
			DataChoice startingChoice = (DataChoice) super.getInitDataChoices ().get (0);
			this.setStartingDataChoice (startingChoice);

			Debug.printf ("Set initial data choice to %s.\n", startingChoice);
		}

		// Add user specified data choices to list
		selectedDataChoices.addAll (choices);
		Debug.printf ("Starting data choice is %s.\n", getStartingDataChoice ());

		// Load all selected choices
		try
		{
			this.addNewData (selectedDataChoices);
		}
		catch (Exception e1)
		{
			e1.printStackTrace ();
		}

	}

	/**
	 * Override base class method which is called when the user has selected new
	 * data choices. This method actually sets new data, instead of appending
	 * it.
	 * 
	 * @param newChoices
	 *            new list of choices
	 * 
	 * @throws RemoteException
	 *             Java RMI error
	 * @throws VisADException
	 *             VisAD Error
	 */
	@Override
	protected void addNewData (@SuppressWarnings("rawtypes") List newChoices)
			throws VisADException, RemoteException
	{
		// Convert to a set and back to remove any duplicates from the new
		// choices list
		@SuppressWarnings("unchecked")
		Set<DataChoice> newChoicesSet = new HashSet<DataChoice> (newChoices);
		List<DataChoice> newChoicesNoDups = new ArrayList<DataChoice> (newChoicesSet);

		// Get any currently loaded data choices
		List<DataChoice> loadedChoices = this.getDataChoices ();

		// Form a list of choices that need to be removed from the sensor map
		List<DataChoice> choicesToRemove = new ArrayList<DataChoice> (loadedChoices);
		choicesToRemove.removeAll (newChoicesNoDups);

		// Form a list of choices that need to be added to the sensor map
		List<DataChoice> choicesToAdd = new ArrayList<DataChoice> (newChoicesNoDups);
		List<DataChoice> choicesAlreadyLoaded = new ArrayList<DataChoice> ();

		for (DataChoice dataChoice : choicesToAdd)
		{
			if (m_sensorMap.containsKey (dataChoice.getDescription ()))
			{
				choicesAlreadyLoaded.add (dataChoice);
			}
		}
		choicesToAdd.removeAll (choicesAlreadyLoaded);

		// Remove old sensors
		if (choicesToRemove.size () > 0)
		{
			for (DataChoice dataChoice : choicesToRemove)
			{

				String parameterName = dataChoice.getDescription ();
				if (!m_nonRemovableSensorNames.contains (parameterName))
				{
					// Try to remove sensor
					if (m_sensorMap.remove (dataChoice.getDescription ()) != null)
					{
						Debug.printf ("Removed sensor %s.\n", dataChoice.getDescription ());
					}
					else
					{
						Debug.printf ("Unable to remove sensor %s.\n", dataChoice.getDescription ());
					}
				}
			}

			m_geopod.clearSensorCache ();
		}

		// Add new sensors
		for (DataChoice dataChoice : choicesToAdd)
		{
			processNewData (dataChoice);
		}

		// Record which dataChoices are now being used
		super.setDataChoices (newChoicesNoDups);

		// Update sensor values
		m_geopod.updateSensorValues ();

		// Tell observers that the data has finished loading.
		notifyObservers (GeopodEventId.ALL_CHOICES_LOADING_FINISHED);
	}

	/**
	 * Called when the user has selected new data choices
	 * 
	 * @param newChoices
	 *            new list of choices
	 * 
	 * @throws RemoteException
	 *             Java RMI error
	 * @throws VisADException
	 *             VisAD Error
	 */
	protected void processNewData (DataChoice choice)
			throws VisADException, RemoteException
	{
		// We use the description as an identifier, as the name is not unique
		String choiceIdentifier = choice.getDescription ();

		Debug.print ("Loading \"" + choiceIdentifier + "\"...");

		m_dataChoiceNameBeingLoaded = choiceIdentifier;
		this.notifyObservers (GeopodEventId.DATA_CHOICE_LOADING_STARTED);

		synchronized (m_sensorMap)
		{
			if (m_sensorMap.containsKey (choiceIdentifier))
			{
				// This should not happen anymore now that we are removing
				// duplicates from the data choices
				Debug.println (" data choice already loaded.");
			}
			else
			{
				DataSelection selection = super.getDataSelection ();

				try
				{
					DataInstance newDataInstance = new GridDataInstance (choice, selection,
							super.getRequestProperties ());

					Sensor newSensor = new Sensor (this, newDataInstance);

					m_sensorMap.put (choiceIdentifier, newSensor);

					// Cache the sensor the Geopod was started with. This is
					// used for the grid point displayer.
					if (choice.toString ().equals (m_startingDataChoice.toString ()))
					{
						m_startingSensor = newSensor;
					}

					this.notifyObservers (GeopodEventId.DATA_CHOICE_LOADING_FINISHED);
				}
				catch (DataCancelException dce)
				{
					// Data selection was canceled. Do not add it to the sensor
					// map.
				}
			}
		}
	}

	/**
	 * Wrapper for the protected method addNewData(). Used to allow other
	 * classes to change which dataChoices are loaded.
	 * 
	 * @param newChoices
	 */
	public void resetDataChoices (List<DataChoice> newChoices)
	{
		try
		{
			this.addNewData (newChoices);
		}
		catch (RemoteException e)
		{
			e.printStackTrace ();
		}
		catch (VisADException e)
		{
			e.printStackTrace ();
		}
	}

	@Override
	public void dataChanged ()
	{
		super.dataChanged ();
	}

	/**
	 * Create a panel to allow the selected data choices to be changed.
	 * 
	 * @return - a parameter chooser panel
	 */
	public ParameterChooserPanel createParameterChooserPanel ()
	{
		ParameterChooserPanel parameterPanel = new ParameterChooserPanel (this, this.getPossibleDataChoices (),
				this.getDataChoices ());
		return (parameterPanel);
	}

	/**
	 * Handle the removal of this GeopodPlugin from IDV. Catches and ignores all
	 * exceptions thrown by IDV in the process.
	 */
	@Override
	public void doRemove ()
	{
		ThreadUtility.shutdownExecutors ();
		if (Debug.consoleEnabled ())
		{
			JythonConsole.shutdown ();
		}

		m_hud.dispose ();
		m_geopod.detachViewBranch ();

		m_hud = null;
		m_geopod = null;

		try
		{
			super.doRemove ();
		}
		catch (Exception e)
		{
			e.printStackTrace ();
		}

		// Allow another Geopod instance to be created.
		ms_isGeopodInstanceCreated = false;
	}

	/**
	 * Display a dialog asking if the user wants to close Geopod or all of IDV.
	 */
	public void displayShutDownDialogue ()
	{
		final String EXIT_GEOPOD = "Exit Geopod";
		final String EXIT_IDV = "Exit IDV";
		final String CANCEL = "Cancel";
		Object[] options = { EXIT_GEOPOD, EXIT_IDV, CANCEL };
		JOptionPane closePane = new JOptionPane ("Are you sure you want to exit?", JOptionPane.QUESTION_MESSAGE,
				JOptionPane.DEFAULT_OPTION, null, options, EXIT_IDV);
		GeopodFrame flightFrame = m_hud.getFlightFrame ();
		JDialog closeDialog = closePane.createDialog (flightFrame, "Confirm Exit");

		closeDialog.setLocationRelativeTo (flightFrame);
		closeDialog.setVisible (true);
		// 'choice' will be "null" if closePane is null
		String choice = String.valueOf (closePane.getValue ());

		// Don't attempt to exit if we only hit cancel or closed the dialog
		if (!(choice.equals (CANCEL) || choice.equals ("null")))
		{
			ConfigurationManager.savePreferences ();
			int continueChoice = JOptionPane.YES_OPTION;
			if (!m_hud.areNotedLocationsSaved ())
			{
				JOptionPane continuePane = new JOptionPane ("You have unsaved changes to your notepad. Exit anyway?",
						JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION, null, null, JOptionPane.NO_OPTION);
				JDialog continueDialog = continuePane.createDialog (flightFrame, "Unsaved Changes");

				continueDialog.setLocationRelativeTo (flightFrame);
				continueDialog.setVisible (true);
				String continueDialogResult = String.valueOf (continuePane.getValue ());

				if (continueDialogResult.equals ("null"))
				{
					continueChoice = JOptionPane.NO_OPTION;
				}
				else
				{
					continueChoice = Integer.valueOf (continueDialogResult);
				}
			}
			if (continueChoice == JOptionPane.YES_OPTION)
			{
				if (choice.equals (EXIT_GEOPOD))
				{
					ThreadUtility.execute (new Runnable ()
					{
						@Override
						public void run ()
						{
							GeopodPlugin.this.shutdownGeopod ();
						}
					});
				}
				else if (choice.equals (EXIT_IDV))
				{
					ThreadUtility.execute (new Runnable ()
					{
						@Override
						public void run ()
						{
							GeopodPlugin.this.shutdownIdv ();
						}
					});
				}
			}
		}
	}

	/**
	 * Shutdown the plugin and all of IDV.
	 */
	public void shutdownIdv ()
	{
		this.shutdownGeopod ();
		super.getIdv ().quit ();
	}

	/**
	 * Tell the Geopod to save its flight log and then close the plugin.
	 */
	public void shutdownGeopod ()
	{
		// Display a message saying the flight log is being saved.
		Thread displayNotificationThread = m_geopod.displayNotificationPanel ("Shuting down\n(please wait)");
		ThreadUtility.invokeOnEdt (displayNotificationThread);

		Thread flightLogEncoderThread = new Thread (new Runnable ()
		{
			@Override
			public void run ()
			{
				m_geopod.encodeFlightLog ();
			}
		});

		final Future<?> encodingTask = ThreadUtility.submit (flightLogEncoderThread);

		try
		{
			// Block until log encoding is finished.
			encodingTask.get ();
		}
		catch (Exception e)
		{
			Debug.println ("Error saving flight log.");
			e.printStackTrace ();
		}

		// Tell IDV to remove the plugin.
		doRemove ();
	}

	/**
	 * @return a list of the available data choices.
	 */
	@SuppressWarnings("unchecked")
	public List<DataChoice> getPossibleDataChoices ()
	{
		// Need to use a set; getDataSources returns duplicates
		Set<DataChoice> choicesNoDupsSet = new HashSet<DataChoice> ();

		DataSource dataSource = this.getDataSource ();
		List<DataChoice> dataChoices = dataSource.getDataChoices ();
		for (DataChoice dataChoice : dataChoices)
		{
			List<DataCategory> categories = dataChoice.getCategories ();
			// Skip soundings and 2D data choices
			if (!(DataCategory.GRID_3D_SOUNDING_CATEGORY.applicableTo (categories) || TWO_DIMENSIONAL_CATEGORY
					.applicableTo (categories)))
			{
				choicesNoDupsSet.add (dataChoice);
			}
		}
		List<DataChoice> choicesNoDups = new ArrayList<DataChoice> (choicesNoDupsSet);

		return (choicesNoDups);
	}

	/**
	 * Get the {@link DataChoice} that the plugin was created with in the IDV
	 * chooser.
	 * 
	 * The startingDataChoice getter and setter are necessary for persistence.
	 * 
	 * @return the DataChoice that the plugin was created with.
	 */
	public DataChoice getStartingDataChoice ()
	{
		return (m_startingDataChoice);
	}

	/**
	 * Set the data choice that the plugin was created with.
	 * 
	 * The startingDataChoice getter and setter are necessary for persistence.
	 * 
	 * @param choice
	 */
	public void setStartingDataChoice (DataChoice choice)
	{
		m_startingDataChoice = choice;
	}

	/**
	 * Allows loading screens to display the data choice name currently being
	 * loaded.
	 * 
	 * @return the data choice currently being loaded
	 */
	public String getDataChoiceNameBeingLoaded ()
	{
		return (m_dataChoiceNameBeingLoaded);
	}

	/**
	 * Respond to a timeChange event from an animation of a sequence of times
	 * 
	 * @param time
	 *            - new time
	 */
	@Override
	protected void timeChanged (Real time)
	{
		m_geopod.timeChanged (time);

		for (Entry<String, Sensor> sen : m_sensorMap.entrySet ())
		{
			try
			{
				DataInstance oldInstance = sen.getValue ().getDataInstance ();
				DataInstance newDataInstance = new GridDataInstance (oldInstance.getDataChoice (),
						oldInstance.getDataSelection (), super.getRequestProperties ());
				sen.getValue ().setDataInstance (newDataInstance);
			}
			catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace ();
			}
			catch (VisADException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace ();
			}
		}
		super.timeChanged (time);

	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void addObserver (IObserver observer, GeopodEventId eventId)
	{
		m_subjectImpl.addObserver (observer, eventId);
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void removeObserver (IObserver observer, GeopodEventId eventId)
	{
		m_subjectImpl.removeObserver (observer, eventId);
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void notifyObservers (GeopodEventId eventId)
	{
		m_subjectImpl.notifyObservers (eventId);
	}

	/**
	 * Minimize the IDV windows.
	 * 
	 * @param millisecondDelay
	 *            - The delay before the windows are minimized.
	 */
	public void iconifyIdvWindowsAfterDelay (long millisecondDelay)
	{
		Runnable task = new Runnable ()
		{
			@Override
			public void run ()
			{
				iconifyIdvWindows ();
			}
		};
		ThreadUtility.execute (task, millisecondDelay, TimeUnit.MILLISECONDS);
	}

	/**
	 * Minimize the IDV Windows and create debugging console if debugging is
	 * enabled.
	 * 
	 * @param millisecondDelay
	 *            - the delay before the windows are minimized.
	 */
	public void minimizeIdvWindows (long milisecondDelay)
	{
		this.iconifyIdvWindowsAfterDelay (2000);
		if (Debug.consoleEnabled ())
		{
			this.createJythonConsole ();
		}
	}

	/**
	 * Show or hide the IDV windows.
	 * 
	 * @param show
	 *            - true to set the IDV windows to visible, false otherwise.
	 */
	@SuppressWarnings("unchecked")
	public void setIdvWindowsVisible (boolean show)
	{
		List<IdvWindow> idvWindows = IdvWindow.getWindows ();
		for (IdvWindow window : idvWindows)
		{
			window.setVisible (show);
		}
	}

	/**
	 * @return <tt>true</tt> if the IDV main window is visible.
	 */
	@SuppressWarnings("unchecked")
	public boolean isIdvCoreVisible ()
	{
		boolean allWindowsNotIconified = false;
		List<IdvWindow> idvWindows = IdvWindow.getWindows ();
		Debug.println (idvWindows.toString ());
		for (IdvWindow window : idvWindows)
		{
			if (!((window.getState () & Frame.ICONIFIED) == Frame.ICONIFIED))
			{
				allWindowsNotIconified = true;
				break;
			}
		}
		return (allWindowsNotIconified);
	}

	/**
	 * @return <tt>true</tt> if the IDV active window is iconified.
	 */
	@SuppressWarnings("unchecked")
	public boolean isIdvCoreIconified ()
	{
		boolean oneWindowIsIconified = false;
		List<IdvWindow> idvWindows = IdvWindow.getWindows ();
		for (IdvWindow window : idvWindows)
		{
			if ((window.getState () & Frame.ICONIFIED) == Frame.ICONIFIED)
			{
				oneWindowIsIconified = true;
				break;
			}
		}
		return (oneWindowIsIconified);
	}

	/**
	 * Minimize the IDV windows.
	 */
	@SuppressWarnings("unchecked")
	public void iconifyIdvWindows ()
	{
		List<IdvWindow> idvWindows = IdvWindow.getWindows ();
		for (IdvWindow window : idvWindows)
		{
			window.setState (Frame.ICONIFIED);
		}
	}

	/**
	 * Restores IDV Windows
	 */
	@SuppressWarnings("unchecked")
	public void restoreIdvWindows ()
	{
		List<IdvWindow> idvWindows = IdvWindow.getWindows ();
		for (IdvWindow window : idvWindows)
		{
			window.setState (Frame.NORMAL);
		}
	}

	/**
	 * If </tt>iconify</tt> is true, minimize IDV windows. Else, restore them.
	 * 
	 * @param iconify
	 */
	public void setIdvWindowsIconified (boolean iconify)
	{
		if (iconify)
		{
			iconifyIdvWindows ();
		}
		else
		{
			restoreIdvWindows ();
		}
	}

	/**
	 * @return the sensor for the parameter chosen at startup
	 */
	public Sensor getStartingSensor ()
	{
		return (m_startingSensor);
	}

	/**
	 * Save the current IDV state as the default bundle.
	 */
	public void saveAsDefaultBundle ()
	{
		getIdv ().doSaveAsDefault ();
	}

	/**
	 * Remove the current default IDV bundle.
	 */
	public void removeDefaultBundle ()
	{
		getIdv ().doClearDefaults ();
	}

	/**
	 * Get the data sources with duplicates removed.
	 * 
	 * @return a list of data sources.
	 */
	public List<DataSource> getDataSourcesWithoutDuplicates ()
	{
		@SuppressWarnings("unchecked")
		List<DataSource> dataSourcesWithDuplicates = super.getDataSources ();

		Set<DataSource> dataSourcesHashSet = new HashSet<DataSource> (dataSourcesWithDuplicates);

		List<DataSource> dataSourcesWithoutDuplicates = new ArrayList<DataSource> (dataSourcesHashSet);
		return (dataSourcesWithoutDuplicates);
	}

	/**
	 * @return the data source used by the plugin.
	 */
	public DataSource getDataSource ()
	{
		List<DataSource> dataSourcesNoDuplicates = this.getDataSourcesWithoutDuplicates ();
		//System.err.println(dataSourcesNoDuplicates);
		if (dataSourcesNoDuplicates.size () != 1)
		{
			Debug.println ("Error: more than one data source");
			//throw new IllegalStateException("More than one data source!");
		}
		return (dataSourcesNoDuplicates.get (0));
	}

	/**
	 * Override DisplayControlImpl version to add type information.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<DataChoice> getDataChoices ()
	{
		List<DataChoice> dataChoices = super.getDataChoices ();
		return (dataChoices);
	}

	/**
	 * Reset all isosurfaces to their default colors.
	 */
	public void resetIsosurfacesToDefaultColors ()
	{
		ViewManager viewManager = super.getViewManager ();
		List<ThreeDSurfaceControl> isosurfaceControls = GeopodPlugin.findIsosurfaceDisplayControls (viewManager);
		for (ThreeDSurfaceControl control : isosurfaceControls)
		{
			ColorTableWidget ctw = null;
			try
			{
				ctw = control.getColorTableWidget (null);
				ctw.doUseDefault ();
			}
			catch (Exception e)
			{
				e.printStackTrace ();
			}
		}
	}

	/**
	 * Pass a message to the {@link NavigatedDisplay} to set the data volume to
	 * a top view.
	 */
	public void setTopView ()
	{
		try
		{
			this.getNavigatedDisplay ().resetScaleTranslate ();
		}
		catch (RemoteException e)
		{
			e.printStackTrace ();
		}
		catch (VisADException e)
		{
			e.printStackTrace ();
		}
	}

	public static List<ThreeDSurfaceControl> findIsosurfaceDisplayControls (ViewManager viewManager)
	{
		@SuppressWarnings("unchecked")
		List<DisplayControl> controls = viewManager.getControls ();
		List<ThreeDSurfaceControl> surfaceControls = new ArrayList<ThreeDSurfaceControl> ();
		for (DisplayControl control : controls)
		{
			if (control instanceof ThreeDSurfaceControl)
			{
				surfaceControls.add ((ThreeDSurfaceControl) control);
			}
		}

		return (surfaceControls);
	}

	@Override
	public void removeObservers ()
	{
		m_subjectImpl.removeObservers ();
	}
}
