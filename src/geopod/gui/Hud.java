package geopod.gui;

import geopod.ConfigurationManager;
import geopod.Geopod;
import geopod.GeopodPlugin;
import geopod.GeopodWindowListener;
import geopod.GridPointDisplayer;
import geopod.GridPointSelector;
import geopod.devices.Dropsonde;
import geopod.devices.FlightDataRecorder;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.components.OffScreenCanvas3D;
import geopod.gui.components.OnScreenCanvas3D;
import geopod.gui.panels.ParameterChooserPanel;
import geopod.input.Keys;
import geopod.utils.ThreadUtility;
import geopod.utils.coordinate.IdvCoordinateUtility;
import geopod.utils.coordinate.Java3dCoordinateUtility;
import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;
import geopod.utils.debug.JythonConsole;
import geopod.utils.geometry.PickUtility;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.Future;

import javax.swing.WindowConstants;
import javax.vecmath.Point3d;

import ucar.unidata.data.DataChoice;
import ucar.unidata.data.DataInstance;
import ucar.unidata.data.DataSource;
import ucar.unidata.idv.ViewManager;
import ucar.unidata.idv.control.ThreeDSurfaceControl;
import ucar.unidata.util.LogUtil;
import visad.georef.EarthLocation;

/**
 * Acts as controller between Geopod (model) and GeopodFrame (view)
 * 
 * @author Geopod Team
 * 
 */
public class Hud
		implements ActionListener, IObserver, ISubject
{
	private GeopodPlugin m_plugin;
	private Geopod m_geopod;
	private GeopodFrame m_flightFrame;

	private SubjectImpl m_subjectImpl;

	private GridPointDisplayer m_gridPointDisplayer;
	private GridPointSelector m_pointSelector;
	private int m_pointsToSkip;

	private String m_calculatorCommand;
	private Future<?> m_calculatorStatus;

	private Runnable m_displayUpdater;

	/**
	 * Construct a default {@link Hud}, storing references to the
	 * {@link GeopodPlugin} and {@link Geopod}.
	 * 
	 * @param plugin
	 * @param geopod
	 */
	public Hud (GeopodPlugin plugin, Geopod geopod)
	{
		m_plugin = plugin;
		m_geopod = geopod;

		m_subjectImpl = new SubjectImpl ();

		m_calculatorStatus = null;

		// Set OS commands for launching a calculator.
		this.setOsCommands ();

		m_pointsToSkip = GridPointDisplayer.DEFAULT_GRID_POINT_STRIDE;

		m_displayUpdater = new DisplayUpdater ();

		ThreadUtility.invokeOnEdtAndWait (new Runnable ()
		{
			@Override
			public void run ()
			{
				m_flightFrame = createFlightWindow ();
			}
		});

		initializeCanvas ();
		setupGeopodWindowListener ();

		ConfigurationManager.addPropertyChangeListener (ConfigurationManager.DisableRoll, new PropertyChangeListener ()
		{

			@Override
			public void propertyChange (PropertyChangeEvent evt)
			{
				if (ConfigurationManager.isEnabled (ConfigurationManager.DisableRoll))
				{
					Hud.this.m_geopod.alignWithEarthUp (true);
				}
			}

		});
	}

	public ViewManager getIdvViewManager ()
	{
		return m_plugin.getViewManager ();
	}

	/**
	 * Perform initialization that needs to be done after the data sets have
	 * finished loading.
	 */
	public void initAfterDataLoaded ()
	{
		m_gridPointDisplayer = new GridPointDisplayer ();

		// Request focus on canvas after initialization
		// m_flightFrame.requestFocusOnCanvas ();
	}

	/**
	 * Tell the hud to dispose of any other classes that need to be removed.
	 */
	public void dispose ()
	{
		m_gridPointDisplayer.detachFromSceneGraph ();
		m_flightFrame.dispose ();
	}

	/**
	 * Make the IDV windows visible or not.
	 * 
	 * <p>
	 * NOTE: Setting the IDV windows to not visible may cause a slow down due to
	 * unknown bugs in Java3D.
	 * </p>
	 */
	public void toggleIdvDisplay ()
	{
		boolean isIconified = m_plugin.isIdvCoreIconified ();
		m_plugin.setIdvWindowsIconified (!isIconified);
	}

	public void iconifyIdvDisplay ()
	{
		m_plugin.iconifyIdvWindows ();
	}

	// Build the main flight interface
	private GeopodFrame createFlightWindow ()
	{
		GeopodFrame frame = new GeopodFrame (this, m_geopod);
		// Prevent window from closing
		frame.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);

		frame.pack ();
		frame.setVisible (true);

		return (frame);
	}

	public GeopodFrame getFlightFrame ()
	{
		return m_flightFrame;
	}

	/**
	 * Now that canvas is constructed, perform further initialization and ensure
	 * dependent objects have access.
	 */
	private void initializeCanvas ()
	{
		OnScreenCanvas3D canvas = (OnScreenCanvas3D) m_flightFrame.getCanvas ();
		m_geopod.setCanvasOfViewBranch (canvas);
		OffScreenCanvas3D offScreenCanvas = (OffScreenCanvas3D) canvas.getOffScreenCanvas ();
		m_geopod.setCanvasOfViewBranch (offScreenCanvas);

		OnScreenCanvas3D topCanvas = (OnScreenCanvas3D) m_flightFrame.getTopViewCanvas ();
		m_geopod.setCanvasOfTopViewBranch (topCanvas);
		OffScreenCanvas3D offScreenTopCanvas = (OffScreenCanvas3D) topCanvas.getOffScreenCanvas ();
		m_geopod.setCanvasOfTopViewBranch (offScreenTopCanvas);

		canvas.requestFocusInWindow ();
		canvas.addKeyListener (new KeyListener ());

		// Initialize utility classes
		Java3dCoordinateUtility.setCanvas (canvas);
		PickUtility.setCanvas (canvas);
	}

	/**
	 * Sets up the Geopod window listener to block IDV mouse behavior
	 */
	private void setupGeopodWindowListener ()
	{
		// Monitor window focus
		GeopodWindowListener windowListener = new GeopodWindowListener (this);
		m_flightFrame.addWindowFocusListener (windowListener);
		m_flightFrame.addWindowListener (windowListener);
	}

	/**
	 * Get Geopod movement enabled or diabled.
	 */
	public boolean isMovementEnabled ()
	{
		return m_geopod.isMovementEnabled ();
	}

	/**
	 * Set Geopod movement enabled or disabled.
	 * 
	 * @param enableMovement
	 *            - set to true to enable geopod movement.
	 */
	public void setMovementEnabled (boolean enableMovement)
	{
		m_geopod.setMovementEnabled (enableMovement);
	}

	public void resetGridPoints ()
	{
		if (m_gridPointDisplayer != null)
		{
			boolean wasVisible = m_gridPointDisplayer.isVisible ();

			m_gridPointDisplayer.resetGridPoints ();

			boolean validationSuccess = validateGridpoints ();
			if (wasVisible)
			{
				m_gridPointDisplayer.setVisible (validationSuccess);
				if (!validationSuccess)
				{
					notifyObservers (GeopodEventId.GRIDPOINTS_BUTTON_STATE_CHANGED);
				}
			}
		}
	}

	// don't call if m_gridPointDisplayer is null
	private boolean validateGridpoints ()
	{
		boolean validationSuccessful = true;
		if (!m_gridPointDisplayer.hasPoints ())
		{
			// build the grid points
			List<ThreeDSurfaceControl> surfaceControls = GeopodPlugin.findIsosurfaceDisplayControls (m_plugin
					.getViewManager ());
			if (surfaceControls.size () >= 1)
			{
				DataInstance firstSurfaceDataInstance = surfaceControls.get (0).getDataInstance ();
				m_gridPointDisplayer.buildGridPoints (firstSurfaceDataInstance, m_pointsToSkip);

				// TODO: this code was meant to happen just once, but after m_gridPointDisplayer 
				// was initialized for the first time. Now no place for code to happen just once
				// could it go in initAfterDataLoaded or would that be too soon for some things?
				//// Only build the grid points and create point selectors one time
				m_pointSelector = new GridPointSelector (m_gridPointDisplayer);
				m_pointSelector.addObserver (this, GeopodEventId.GRID_POINT_SELECTED);
				m_flightFrame.getCanvas ().addMouseListener (m_pointSelector);
			}
			else
			{
				validationSuccessful = false;
			}
		}
		return (validationSuccessful);
	}

	/**
	 * Toggle the grid points on and off.
	 */
	public void toggleGridPointVisibility ()
	{
		if (m_gridPointDisplayer != null)
		{
			boolean pointsSuccessfullyValidated = validateGridpoints ();
			boolean visible = m_gridPointDisplayer.isVisible ();
			if (pointsSuccessfullyValidated || visible)
			{
				// if points were successfully displayed, toggle & change button 
				// state as usual (regardless of whether points were visible or not) 
				// if not successfully displayed, but were visible, remove
				m_gridPointDisplayer.setVisible (!visible);
				notifyObservers (GeopodEventId.GRIDPOINTS_BUTTON_STATE_CHANGED);
			}
		}
	}

	/**
	 * @return a string representing the data source
	 */
	public String getDataSourceName ()
	{
		DataSource source = m_plugin.getDataSource ();

		return (source.getName ());
	}

	/**
	 * Change how dense the grid point display is.
	 * 
	 * @param pointsToSkip
	 *            - how many points to skip between each displayed point.
	 */
	public void adjustGridPointDensity (int pointsToSkip)
	{
		m_pointsToSkip = pointsToSkip;

		if (pointsToSkip != 0 && m_gridPointDisplayer != null && m_gridPointDisplayer.hasPoints ())
		{

			Runnable task = new Runnable ()
			{
				@Override
				public void run ()
				{
					GridPointDisplayer pointDisplayer = m_gridPointDisplayer;
					pointDisplayer.adjustGridPointDensity (m_pointsToSkip);
				}
			};

			ThreadUtility.execute (task);
		}
	}

	/**
	 * Bring up a shutdown dialog.
	 */
	public void displayShutdownDialogue ()
	{
		m_plugin.displayShutDownDialogue ();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed (ActionEvent event)
	{
		String actionCommand = event.getActionCommand ();

		try
		{
			if (actionCommand.equals ("displayPoints"))
			{
				handleGridPointButtonEvent ();
			}
			else if (actionCommand.equals ("distance"))
			{
				handleDistanceButtonEvent ();
			}
			else if (actionCommand.equals ("movieCapture"))
			{
				handleMovieCaptureButtonEvent ();
			}
			else if (actionCommand.equals ("isosurface"))
			{
				handleIsosurfaceViewButtonEvent ();
			}
			else if (actionCommand.equals ("calculator"))
			{
				handleCalculatorButtonEvent ();
			}
			else if (actionCommand.equals ("notepad"))
			{
				handleNotepadButtonEvent ();
			}
			else if (actionCommand.equals ("help"))
			{
				handleHelpButtonEvent ();
			}
			else if (actionCommand.equals ("settings"))
			{
				handleConfigurationButtonEvent ();
			}
			else if (actionCommand.equals ("particle"))
			{
				handleParticleButtonEvent ();
			}
			else if (actionCommand.equals ("dropsonde"))
			{
				handleDropsondeButtonEvent ();
			}
			else if (actionCommand.equals ("flyTo"))
			{
				EarthLocation el = m_flightFrame.getInputtedEarthLocation ();

				if (el != null)
				{
					if (m_geopod.getIsosurfaceLockEnabled ())
					{
						m_geopod.displayTimedNotificationPanel (
								"You can't use the autopilot when locked onto a surface", 1500);
					}
					else if (!m_geopod.isAutoPilotEnabled ()) // temporary fix
					{
						m_geopod.flyToLocationUsingAutopilot (el);
					}
				}
				m_flightFrame.requestFocusOnCanvas ();
			}
			else if (actionCommand.equals ("lookUp"))
			{
				m_flightFrame.toggleLookUpPanel ();
			}
			else if (actionCommand.equals ("parameterChooser"))
			{
				m_flightFrame.toggleParameterChooserPanel ();
			}
			else if (actionCommand.equals ("note_location"))
			{
				m_flightFrame.noteLocation ();
			}
			else if (actionCommand.equals ("lock"))
			{
				m_geopod.toggleSurfaceLock ();
			}
		}
		catch (NullPointerException e)
		{
			LogUtil.printException (GeopodPlugin.LOG_CATEGORY, "Data not loaded yet", e);
		}
	}

	/**
	 * Set the OS-specific commands to launch a calculator or basic text editor.
	 */
	private void setOsCommands ()
	{
		String osName = System.getProperty ("os.name");
		if (osName.startsWith ("Windows"))
		{
			m_calculatorCommand = "calc.exe";
		}
		else if (osName.startsWith ("Linux"))
		{
			m_calculatorCommand = "gnome-calculator";
		}
		else if (osName.startsWith ("Mac OS X"))
		{
			// GMZ: Geopod doesn't run on Mac b/c of heavy/light mixing
			m_calculatorCommand = "open -a Calculator";
		}
	}

	private void handleNotepadButtonEvent ()
	{
		m_flightFrame.toggleNotedLocationsPanel ();
		notifyObservers (GeopodEventId.NOTEPAD_BUTTON_STATE_CHANGED);
	}

	private void handleCalculatorButtonEvent ()
	{
		if (m_calculatorStatus == null || m_calculatorStatus.isDone ())
		{
			m_calculatorStatus = ThreadUtility.submit (new ProcessLauncher (m_calculatorCommand));
			notifyObservers (GeopodEventId.CALC_BUTTON_STATE_CHANGED);
		}
	}

	/**
	 * Launch a new dropsonde at the current location.
	 */
	public void launchDropsonde ()
	{
		m_geopod.launchStandardDropsonde ();

		// Transfer the focus back to the canvas so we can still get key events.
		m_flightFrame.requestFocusOnCanvas ();
	}

	/**
	 * removes the dropsonde with the corresponding hash code.
	 * 
	 * @param hash
	 */
	public void removeDropsonde (Dropsonde ds)
	{
		m_geopod.removeDropsonde (ds);
	}

	private void handleDropsondeButtonEvent ()
	{
		m_flightFrame.toggleDropsondeChartPanel ();
		notifyObservers (GeopodEventId.DROPSONDE_BUTTON_STATE_CHANGED);
	}

	private void handleParticleButtonEvent ()
	{
		m_geopod.toggleParticleImager ();
		notifyObservers (GeopodEventId.PARTICLE_BUTTON_STATE_CHANGED);
	}

	private void handleGridPointButtonEvent ()
	{
		toggleGridPointVisibility ();
	}

	private void handleHelpButtonEvent ()
	{
		m_flightFrame.toggleHelpPanel ();
		notifyObservers (GeopodEventId.HELP_BUTTON_STATE_CHANGED);
	}

	private void handleIsosurfaceViewButtonEvent ()
	{
		m_flightFrame.toggleIsosurfaceViewPanel ();
	}

	private void handleDistanceButtonEvent ()
	{
		m_flightFrame.toggleDistancePanel ();

	}

	private void handleMovieCaptureButtonEvent ()
	{
		m_flightFrame.toggleMovieCapturePanel ();

	}

	private void handleConfigurationButtonEvent ()
	{
		m_flightFrame.toggleConfigurationPanel ();
		notifyObservers (GeopodEventId.CONFIG_BUTTON_STATE_CHANGED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleNotification (GeopodEventId eventId)
	{
		if (eventId.equals (GeopodEventId.GEOPOD_TRANSLATED) || eventId.equals (GeopodEventId.TIME_CHANGED))
		{
			ThreadUtility.invokeOnEdt (m_displayUpdater);

			if (m_gridPointDisplayer != null && !m_geopod.isAutoPilotEnabled ())
			{
				m_gridPointDisplayer.deselectCurrentlySelectedPoint ();
			}
		}
		else if (eventId.equals (GeopodEventId.GRID_POINT_SELECTED))
		{
			Point3d selectedPoint = m_pointSelector.getIntersectedPoint ();
			EarthLocation el = IdvCoordinateUtility.convertBoxToEarth (selectedPoint);
			m_flightFrame.setInputEarthLocation (el);
			m_flightFrame.indicateEarthLocationChange ();
		}
		else if (eventId.equals (GeopodEventId.DATA_CHOICE_LOADING_STARTED))
		{
			String currentlyLoading = m_plugin.getDataChoiceNameBeingLoaded ();
			String dataSourceName = m_geopod.extractCurrentDataSourceName (); // this.getDataSourceName
			// ();
			m_flightFrame.showDataLoading (currentlyLoading, dataSourceName);

			// Block mouse events using the glass pane so the user cannot
			// activate virtual devices
			m_flightFrame.blockMouseEvents (true);
		}
		else if (eventId.equals (GeopodEventId.AUTO_PILOT_FINISHED))
		{
			m_gridPointDisplayer.deselectCurrentlySelectedPoint ();
			notifyObservers (GeopodEventId.GO_BUTTON_STATE_CHANGED);
		}
		else if (eventId.equals (GeopodEventId.ALL_CHOICES_LOADING_FINISHED))
		{
			this.updateParameterDisplayMapping (m_plugin.getDataChoices ());
			m_flightFrame.initAfterDataLoad ();
			m_flightFrame.enableUserInteraction ();
			m_plugin.iconifyIdvWindows ();
		}
		else if (eventId.toString ().endsWith ("_BUTTON_STATE_CHANGED"))
		{
			this.notifyObservers (eventId);
		}
		else if (eventId.equals (GeopodEventId.REQUEST_FLIGHT_LOG_RESET))
		{
			FlightDataRecorder flightRecorder = m_geopod.getFlightRecorder ();
			flightRecorder.resetLog ();
		}
	}

	public boolean areNotedLocationsSaved ()
	{
		return (m_flightFrame.areNotedLocationsSaved ());
	}

	/**
	 * Get a {@link ParameterChooserPanel parameter chooser panel}.
	 * 
	 * @return the newly constructed parameter chooser panel.
	 */
	public ParameterChooserPanel createParameterChooserPanel ()
	{
		// Pass request to the plugin, which has the parameter lists.
		return (m_plugin.createParameterChooserPanel ());
	}

	/**
	 * Notify the hud that the parameter list has changed.
	 * 
	 * @param newChoices
	 */
	public void updateParameterDisplayMapping (List<DataChoice> newChoices)
	{
		m_flightFrame.updateDisplayMappings (newChoices);
	}

	private final class DisplayUpdater
			implements Runnable
	{
		@Override
		public void run ()
		{
			m_flightFrame.updateDisplay ();
		}
	}

	/**
	 * Reset the isosurface to it's original colors.
	 */
	public void resetIsosurfacesToDefaultColors ()
	{
		m_plugin.resetIsosurfacesToDefaultColors ();
	}

	private class ProcessLauncher
			implements Runnable
	{
		private String m_command;

		public ProcessLauncher (String command)
		{
			setCommand (command);
		}

		public void setCommand (String command)
		{
			m_command = command;
		}

		@Override
		public void run ()
		{
			try
			{
				Process process = Runtime.getRuntime ().exec (m_command);
				process.waitFor ();
				notifyObservers (GeopodEventId.CALC_BUTTON_STATE_CHANGED);
			}
			catch (Exception e)
			{
				e.printStackTrace ();
			}
		}
	}

	/**
	 * Pass a message to the {@link GeopodPlugin} to reset the data volume to a
	 * top view.
	 */
	public void setTopView ()
	{
		m_plugin.setTopView ();
	}

	/**
	 * Request focus on the canvas. The canvas must have focus to receive input
	 * and allow the movement behaviors to work.
	 */
	public void requestFocusOnCanvas ()
	{
		m_flightFrame.requestFocusOnCanvas ();
	}

	private class KeyListener
			extends KeyAdapter
	{

		@Override
		public void keyReleased (KeyEvent event)
		{
			int keyCode = event.getKeyCode ();

			if (keyCode == Keys.Bindings.get (Keys.GeopodKeys.ParameterDisplay).getKeyCode ())
			{
				if (event.isControlDown ())
				{
					// Ctrl + P -- toggle parameter chooser
					m_flightFrame.toggleParameterChooserPanel ();
				}
				else
				{
					// P -- toggle particle imager
					handleParticleButtonEvent ();
				}
			}
			else if (keyCode == Keys.Bindings.get (Keys.GeopodKeys.OverflowDisplay).getKeyCode ())
			{
				m_flightFrame.toggleOverflowPanel ();
			}
			else if (keyCode == Keys.Bindings.get (Keys.GeopodKeys.NoteLocation).getKeyCode ())
			{
				if (event.isControlDown ())
				{
					// Ctrl + N -- display note panel
					handleNotepadButtonEvent ();
				}
				else
				{
					// N -- note location
					m_flightFrame.noteLocation ();
				}
			}
			else if (keyCode == Keys.Bindings.get (Keys.GeopodKeys.RecreateFlightPath).getKeyCode ()
					&& event.isControlDown ())
			{
				m_geopod.recreateFlightPath ();
			}
			else if (keyCode == Keys.Bindings.get (Keys.GeopodKeys.StopFlightPlayback).getKeyCode ())
			{
				m_geopod.stopFlightPlayback ();
			}
			else if (keyCode == Keys.Bindings.get (Keys.GeopodKeys.PauseFlightPlayback).getKeyCode ())
			{
				m_geopod.toggleFlightPlayback ();
			}
			else if (keyCode == Keys.Bindings.get (Keys.GeopodKeys.ResetIsosurfaceColors).getKeyCode ())
			{
				if (event.isControlDown ())
				{
					// Ctrl + I -- toggle isosurface panel
					handleIsosurfaceViewButtonEvent ();
				}
				else
				{
					// I -- repaint surface
					resetIsosurfacesToDefaultColors ();
				}
			}
			else if (keyCode == Keys.Bindings.get (Keys.GeopodKeys.ToggleGridpoints).getKeyCode ())
			{
				toggleGridPointVisibility ();
			}
			else if (keyCode == Keys.Bindings.get (Keys.GeopodKeys.ToggleCalculator).getKeyCode ()
					&& event.isControlDown ())
			{
				handleCalculatorButtonEvent ();
			}
			else if (keyCode == Keys.Bindings.get (Keys.GeopodKeys.ToggleSettings).getKeyCode ()
					&& event.isControlDown ())
			{
				handleConfigurationButtonEvent ();
			}
			else if (keyCode == Keys.Bindings.get (Keys.GeopodKeys.ToggleHelp).getKeyCode ())
			{
				if (event.isControlDown ())
				{
					// Ctrl + ? (or /) -- look up location
					m_flightFrame.toggleLookUpPanel ();
				}
				else
				{
					// ? (or /) -- help
					handleHelpButtonEvent ();
				}
			}
			else if (keyCode == KeyEvent.VK_D && event.isControlDown ())
			{
				if (event.isShiftDown ())
				{
					// Ctrl + Shift + D -- toggle dropsonde panel
					handleDropsondeButtonEvent ();
				}
				else
				{
					// Ctrl + D -- toggle distance measurer
					handleDistanceButtonEvent ();
				}
			}
			else if (keyCode == Keys.Bindings.get (Keys.GeopodKeys.Menu).getKeyCode ())
			{
				displayShutdownDialogue ();
			}
			// temporary
			else if (keyCode == KeyEvent.VK_SEMICOLON && event.isControlDown () && event.isShiftDown ())
			{
				m_geopod.reverseFlightPlaybackDirection ();
			}
			// temporary
			else if (keyCode == KeyEvent.VK_U && event.isControlDown () && event.isShiftDown ())
			{
				m_geopod.incrementFlightPlaybackSpeed ();
			}
			// temporary
			else if (keyCode == KeyEvent.VK_Y && event.isControlDown () && event.isShiftDown ())
			{
				m_geopod.decrementFlightPlaybackSpeed ();
			}
			else if (isControlShiftBackQuote (event))
			{
				if (Debug.consoleEnabled ())
				{
					JythonConsole.toggleVisibility ();
				}
			}
			else if (keyCode == KeyEvent.VK_7 && Debug.isDebuggingOn ())
			{
				m_geopod.getFlightRecorder ().printLog ();
			}
			else if (keyCode == KeyEvent.VK_PAUSE && Debug.levelAtLeast (DebugLevel.HIGH))
			{
				m_flightFrame.toggleGlassPane ();
			}
		}

		private boolean isControlShiftBackQuote (KeyEvent event)
		{
			boolean isBackQuote = event.getKeyCode () == KeyEvent.VK_BACK_QUOTE;
			boolean areModifiersDown = event.isControlDown () && event.isShiftDown ();

			return (isBackQuote && areModifiersDown);
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

	public void updateSelectedSonde (Dropsonde sonde)
	{
		m_geopod.updateSelectedSonde (sonde);
	}
}
