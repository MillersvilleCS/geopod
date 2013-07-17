package geopod;

import geopod.constants.DirectionConstants;
import geopod.devices.AttitudeIndicator;
import geopod.devices.Compass;
import geopod.devices.Dropsonde;
import geopod.devices.FlightDataRecorder;
import geopod.devices.ParticleImager;
import geopod.devices.Sensor;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.DataLoadedEvent;
import geopod.eventsystem.events.DropsondeEvent;
import geopod.eventsystem.events.FlightEvent;
import geopod.eventsystem.events.GeopodEventId;
import geopod.eventsystem.events.LocationNotedEvent;
import geopod.eventsystem.events.MovementEvent;
import geopod.eventsystem.events.MovementEventEncoder;
import geopod.eventsystem.events.ParticleImageChangedEvent;
import geopod.eventsystem.events.ParticleImagerDisplayEvent;
import geopod.gui.panels.NotificationPanel;
import geopod.gui.panels.dropsonde.DropsondeMarker;
import geopod.input.KeyBehavior;
import geopod.input.MouseBehavior;
import geopod.utils.Pose;
import geopod.utils.ThreadUtility;
import geopod.utils.TransformGroupControl;
import geopod.utils.TransformGroupControl.RotationDirection;
import geopod.utils.coordinate.IdvCoordinateUtility;
import geopod.utils.debug.Debug;
import geopod.utils.geometry.IsosurfaceLock;
import geopod.utils.idv.SceneGraphControl;
import geopod.utils.idv.ViewBranch;
import geopod.utils.math.VisadUtility;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.MutableComboBoxModel;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import ucar.unidata.data.DataSource;
import visad.Real;
import visad.georef.EarthLocation;
import visad.georef.LatLonPoint;

import com.thoughtworks.xstream.XStream;

public class Geopod
		implements ISubject
{
	// Constants

	/**
	 * Rotation speed in degrees per update
	 */
	public static final double DEFAULT_ROTATION_SPEED;

	/**
	 * Minimum speed of the Geopod.
	 */
	public static final double MINIMUM_SPEED;

	/**
	 * Maximum speed of the Geopod.
	 */
	public static final double MAXIMUM_SPEED;

	/**
	 * Minimum angular speed of the Geopod.
	 */
	public static final double MINIMUM_ANGULAR_SPEED;

	/**
	 * Maximum angular speed of the Geopod.
	 */
	public static final double MAXIMUM_ANGULAR_SPEED;

	/**
	 * Amount by which speed is increased or decreased
	 */
	public static final double ACCELERATION;
	/**
	 * Amount by which angular speed is increased or decreased
	 */
	public static final double ANGULAR_ACCELERATION;

	/**
	 * 
	 */
	public static int PLAYBACK_FRAME_SPEED_IN_MS;

	/**
	 * 
	 */
	public static final int TIME_TO_DISPLAY_EVENT_NOTIFICATION_IN_MS;

	/**
	 * 
	 */
	public static final int PLAYBACK_FRAME_SPEED_INCREMENT_IN_MS;

	/**
	 * 
	 */
	public static final int MIN_PLAYBACK_FRAME_SPEED_IN_MS;

	/**
	 * 
	 */
	private static final Vector3d INITIAL_WORLD_POSITION;

	/**
	 * 
	 */
	private static final AxisAngle4d INITIAL_ROTATION;

	/**
	 * 
	 */
	private static final Pose INITIAL_POSE;

	/**
	 * 
	 */
	private static String FLIGHT_LOG_LOCATION;

	/**
	 * Time spent in the Geopod realignment animaton (in milliseconds).
	 */
	private static final long REALIGNMENT_TIME_MS;

	/**************************************************************************/

	// Initialize static fields
	static
	{
		MINIMUM_SPEED = 1.00e-5;
		MAXIMUM_SPEED = 2.00e-2;
		ACCELERATION = 1.00e-4;

		DEFAULT_ROTATION_SPEED = 1.0;
		MINIMUM_ANGULAR_SPEED = 0.125;
		MAXIMUM_ANGULAR_SPEED = 5.0;
		ANGULAR_ACCELERATION = 0.125;

		PLAYBACK_FRAME_SPEED_IN_MS = 10;
		PLAYBACK_FRAME_SPEED_INCREMENT_IN_MS = 1;
		MIN_PLAYBACK_FRAME_SPEED_IN_MS = 1;
		TIME_TO_DISPLAY_EVENT_NOTIFICATION_IN_MS = 500;

		INITIAL_WORLD_POSITION = new Vector3d (-0.05, -1.25, 1.0);
		INITIAL_ROTATION = new AxisAngle4d (1, 0, 0, Math.PI / 4);
		INITIAL_POSE = new Pose (INITIAL_ROTATION, INITIAL_WORLD_POSITION);

		REALIGNMENT_TIME_MS = 250;
	}

	/**************************************************************************/

	private ViewBranch m_geopodViewBranch;

	/**
	 * The autopilot system.
	 */
	private Autopilot m_autopilot;

	/**
	 * Object allowing the Geopod to follow an isosurface.
	 */
	private IsosurfaceLock m_isosurfaceLock;

	/**
	 * Device to record the Geopod's flight path and other events.
	 */
	private FlightDataRecorder m_blackbox;

	/**
	 * Helps manipulate the position of the Geopod
	 */
	private TransformGroupControl m_transformHelper;

	/**
	 * Current location of the Geopod
	 */
	private EarthLocation m_currentLocation;

	/**
	 * The currently set rotational speed of the Geopod;
	 */
	private double m_rotationalSpeed;

	/**
	 * All the sensors the Geopod currently has active. In the form SensorName
	 * -> Sensor
	 */
	private Map<String, Sensor> m_sensorMap;

	/**
	 * A list of the dropsondes that have been launched
	 */
	private MutableComboBoxModel<Dropsonde> m_dropsondeHistory;

	/**
	 * Sensor values from the last update. In the form SensorName -> RealValue
	 */
	private Map<String, Real> m_currentSensorValues;

	/**
	 * Implements the subject interface for the Geopod.
	 */
	private SubjectImpl m_subjectImpl;

	/**
	 * The particle imager used by the Geopod.
	 */
	private ParticleImager m_particleImager;

	/**
	 * Reference to the GeopodPlugin.
	 */
	private GeopodPlugin m_plugin;

	/**
	 * Is the Geopod able to move or not.
	 */
	private boolean m_isMovementEnabled;

	/**
	 * Stores the current time as reported by IDV.
	 */
	private Real m_time;

	/**
	 * Thread for running a flight playback
	 */
	private Thread m_flightPlaybackThread;

	/**
	 * Mechanism for creating, pausing, and stopping flight playback
	 */
	private FlightPlayback m_flightPlayback;

	/**
	 * Whether or not a flight playback is currently in progress
	 */
	private boolean m_flightPlaybackInProgress;

	/**
	 * 
	 */
	private NotificationPanel m_notificationPanel;

	/**
	 * The behavior that handles mouse input for Geopod movement.
	 */
	private Behavior m_mouseBehavior;

	/**
	 * Maps the Dropsones to the dropsondeMarkers
	 */
	private Map<Dropsonde, DropsondeMarker> dropsondeMap;
	
	private Dropsonde m_selectedSonde;
	/**
	 * The behavior that handles key input for Geopod movement.
	 */
	private KeyBehavior m_keyBehavior;

	private Compass m_compass;

	private BoundedRangeModel m_speedometerModel;

	private AttitudeIndicator m_attitudeIndicator;

	private DeviceUpdater m_deviceUpdater;

	/**
	 * Construct a new Geopod
	 * 
	 * @param plugin
	 *            - the plugin constructing the geopod.
	 */
	public Geopod (GeopodPlugin plugin)
	{
		m_plugin = plugin;
		m_subjectImpl = new SubjectImpl ();
		dropsondeMap = new HashMap<Dropsonde, DropsondeMarker> ();

		m_currentSensorValues = new HashMap<String, Real> ();
		m_dropsondeHistory = new DefaultComboBoxModel<Dropsonde> ();

		//m_currentSpeed = Geopod.DEFAULT_TRANSLATION_SPEED;
		m_rotationalSpeed = Geopod.DEFAULT_ROTATION_SPEED;
		m_flightPlaybackInProgress = false;

		// Create the Geopod's view into the 3D scene
		m_geopodViewBranch = new ViewBranch ();

		// Create the TransformGroupControl responsible for movement
		TransformGroup viewTransform = m_geopodViewBranch.getViewTransformGroup ();
		m_transformHelper = new TransformGroupControl (viewTransform, m_geopodViewBranch.getTopViewTransformGroup ());

		createMovementBehaviors ();

		createDevices ();

		// Create navigation aids
		m_autopilot = createAutopilot ();
		m_isosurfaceLock = new IsosurfaceLock (this.getViewTransformControl ());

		// Move the Geopod to its starting position
		setPose (INITIAL_POSE);
	}

	private void createDevices ()
	{
		m_particleImager = new ParticleImager (this);
		m_blackbox = createFlightRecorder ();
		m_compass = new Compass ();
		m_speedometerModel = new DefaultBoundedRangeModel (500, 1, 0, 1000);

		m_deviceUpdater = new DeviceUpdater ();

		m_attitudeIndicator = new AttitudeIndicator ();
	}

	/**
	 * Create behaviors to control Geopod movement.
	 * 
	 * @param geopod
	 *            - The Geopod to be controlled
	 */
	private void createMovementBehaviors ()
	{
		Point3d center = new Point3d ();
		double radius = 1.0;
		BoundingSphere bounds = new BoundingSphere (center, radius);

		KeyBehavior keyBehavior = new KeyBehavior (this);
		keyBehavior.setSchedulingBounds (bounds);
		m_geopodViewBranch.addNodeToMovementGroup (keyBehavior);
		m_keyBehavior = keyBehavior;

		Behavior mouseBehavior = new MouseBehavior (this);
		mouseBehavior.setSchedulingBounds (bounds);
		m_geopodViewBranch.addNodeToMovementGroup (mouseBehavior);
		m_mouseBehavior = mouseBehavior;
	}

	private FlightDataRecorder createFlightRecorder ()
	{
		FlightDataRecorder blackbox = null;

		// calculate flight log location (in plugins directory of user's IDV folder)
		String idvUserDirectory = m_plugin.getStore ().getUserDirectory ().toString ();
		FLIGHT_LOG_LOCATION = idvUserDirectory + File.separator + "plugins" + File.separator + "geopod.flightlog";

		File testFile = new File (FLIGHT_LOG_LOCATION);
		if (testFile.exists ())
		{
			try
			{
				XStream xstream = new XStream ();
				ClassLoader classLoader = FlightDataRecorder.class.getClassLoader ();
				xstream.setClassLoader (classLoader);
				Class<?>[] classesWithAnnotations = { FlightDataRecorder.class, FlightEvent.class, MovementEvent.class,
						DropsondeEvent.class, ParticleImageChangedEvent.class, ParticleImagerDisplayEvent.class,
						DataLoadedEvent.class, LocationNotedEvent.class };
				xstream.processAnnotations (classesWithAnnotations);
				xstream.registerConverter (new MovementEventEncoder ());
				FileInputStream fileInputStream = new FileInputStream (FLIGHT_LOG_LOCATION);
				blackbox = (FlightDataRecorder) xstream.fromXML (fileInputStream);
				blackbox.initilizeFlightRecorder (this);
			}
			catch (Exception e)
			{
				Debug.println ("Could not read flight log. Data missing or corrupted. New log created.");
				Debug.println (e.getMessage ());
				blackbox = new FlightDataRecorder (this);
			}
		}
		else
		{
			Debug.println (testFile.getAbsolutePath () + " does not exist");
			blackbox = new FlightDataRecorder (this);
		}
		this.addObserver (blackbox, GeopodEventId.GEOPOD_TRANSLATED);
		this.addObserver (blackbox, GeopodEventId.DROPSONDE_LAUNCHED);
		m_particleImager.addObserver (blackbox, GeopodEventId.PARTICLE_IMAGED);
		m_particleImager.addObserver (blackbox, GeopodEventId.PARTICLE_IMAGER_OPENED);
		m_particleImager.addObserver (blackbox, GeopodEventId.PARTICLE_IMAGER_CLOSED);
		m_plugin.addObserver (blackbox, GeopodEventId.ALL_CHOICES_LOADING_FINISHED);

		return (blackbox);
	}

	// KW/GMZ: Begin area of concern. These methods should be less accessible.
	public void addNodeToMovementGroup (Node node)
	{
		m_geopodViewBranch.addNodeToMovementGroup (node);
	}

	public void setCanvasOfViewBranch (Canvas3D canvas)
	{
		m_geopodViewBranch.addCanvas (canvas);
	}

	public void setCanvasOfTopViewBranch (Canvas3D canvas)
	{
		m_geopodViewBranch.addTopCanvas (canvas);
	}

	public void attachViewBranch ()
	{
		m_geopodViewBranch.attachToSceneGraph ();

	}

	public void detachViewBranch ()
	{
		m_geopodViewBranch.detachFromSceneGraph ();
	}

	/**
	 * @return the {@link TransformGroupControl} that handles the Geopod's
	 *         movement.
	 */
	public TransformGroupControl getViewTransformControl ()
	{
		// TODO: determine if this is ever null. If not, remove this check.
		if (m_transformHelper == null)
		{
			TransformGroup viewTransform = m_geopodViewBranch.getViewTransformGroup ();
			m_transformHelper = new TransformGroupControl (viewTransform,
					m_geopodViewBranch.getTopViewTransformGroup ());
		}

		return (m_transformHelper);
	}

	// End area of concern.

	private Autopilot createAutopilot ()
	{
		Autopilot autopilot = new Autopilot (this);
		BoundingSphere bounds = new BoundingSphere (new Point3d (), 1);
		autopilot.setSchedulingBounds (bounds);
		autopilot.setEnable (false);
		m_geopodViewBranch.addNodeToMovementGroup (autopilot);

		return (autopilot);
	}

	/**
	 * Turn the Geopod's movement on or off.
	 * 
	 * @param movementEnabled
	 */
	public void setMovementEnabled (boolean movementEnabled)
	{
		m_isMovementEnabled = movementEnabled && !m_autopilot.getEnable ();
		this.resetKeyBuffer ();

		m_keyBehavior.setIgnoreKeys (!m_isMovementEnabled);
	}

	private void resetKeyBuffer ()
	{
		if (m_keyBehavior != null)
		{
			m_keyBehavior.resetKeyBuffer ();
		}
	}

	/**
	 * Is the Geopod accepting movement commands or not.
	 * 
	 * @return true if movement is enabled.
	 */
	public boolean isMovementEnabled ()
	{
		return (m_isMovementEnabled);
	}

	public boolean isAutoPilotEnabled ()
	{
		return (m_autopilot.getEnable ());
	}

	/**
	 * @return the {@link ParticleImager} used by the Geopod.
	 */
	public ParticleImager getParticleImager ()
	{
		return (m_particleImager);
	}

	/**
	 * @return the sensor for the parameter chosen at startup.
	 */
	public Sensor getStartingSensor ()
	{
		return (m_plugin.getStartingSensor ());
	}

	/**
	 * @return the Geopod's {@link Compass}.
	 */
	public Compass getCompass ()
	{
		return (m_compass);
	}

	/**
	 * @return the {@link BoundedRangeModel} representing the speedometer range.
	 */
	public BoundedRangeModel getSpeedometerModel ()
	{
		return m_speedometerModel;
	}

	/**
	 * @return the Geopod's {@link AttitudeIndicator}.
	 */
	public AttitudeIndicator getAttitudeIndicator ()
	{
		return (m_attitudeIndicator);
	}

	/**
	 * 
	 * @return the current lat/lon/alt of the geopod
	 */
	public EarthLocation getEarthLocation ()
	{
		return (m_currentLocation);
	}

	/**
	 * @return the pose (world position and rotation) of the geopod.
	 */
	public Pose getWorldPose ()
	{
		Quat4d rotation = m_transformHelper.getRotation ();
		Point3d position = IdvCoordinateUtility.convertEarthToWorld (m_currentLocation);
		Pose pose = new Pose (rotation, position);

		return (pose);
	}

	/**
	 * Get a vector containing the yaw, pitch, and roll components in radians.
	 * 
	 * @return a vector3d consisting of yaw (x component), pitch (y component),
	 *         roll (z component);
	 */
	public Vector3d getYawPitchRoll ()
	{
		return m_transformHelper.getYawPitchRoll ();
	}

	/**
	 * Fly the Geopod to the specified earth location using the autopilot.
	 * 
	 * @param el
	 */
	public void flyToLocationUsingAutopilot (EarthLocation el)
	{
		Transform3D currentPose = new Transform3D ();
		m_transformHelper.getTransform (currentPose);
		// Get destination position in world coordinates
		Point3d targetPosition = IdvCoordinateUtility.convertEarthToWorld (el);

		if (m_autopilot.calculateFlightPath (currentPose, targetPosition))
		{
			Debug.println ("Flying to " + el);
			m_autopilot.setEnable (true);
			notifyObservers (GeopodEventId.GO_BUTTON_STATE_CHANGED);
		}
		else
		{
			System.out.println ("Destination is too close");
			displayTimedNotificationPanel ("Destination is too close", 900);
		}
	}

	/**
	 * Locks the Geopod's vertical position to the first available isosurface.
	 * 
	 * @param lockEnabled
	 *            - true turns the lock on, false turns it off.
	 */
	public void lockToSurface (boolean lockEnabled)
	{
		if (m_autopilot.getEnable ())
		{
			displayTimedNotificationPanel ("You can't lock to the surface while using the autopilot", 1300);
		}
		else if (m_flightPlaybackInProgress)
		{
			m_flightPlayback.pause ();
			ThreadUtility.execute (new Runnable ()
			{

				@Override
				public void run ()
				{
					m_notificationPanel.setNotificationText ("You can't lock to the surface during flight playback");
					m_notificationPanel.setVisible (true);
					long time = System.currentTimeMillis ();
					long timeNow = System.currentTimeMillis ();
					while (timeNow < time + 3000)
					{
						timeNow = System.currentTimeMillis ();
						m_notificationPanel.setVisible (true);
					}
					m_notificationPanel.setVisible (false);
					m_flightPlayback.resume ();
				}
			});
		}
		else
		{
			m_isosurfaceLock.setEnabled (lockEnabled);
			if (lockEnabled)
			{
				this.notifyObservers (GeopodEventId.ISOSURFACE_LOCKED);
			}
			else
			{
				this.notifyObservers (GeopodEventId.ISOSURFACE_UNLOCKED);
			}
			this.notifyObservers (GeopodEventId.LOCK_BUTTON_STATE_CHANGED);
		}
	}

	/**
	 * Toggle the state of the isosurface lock.
	 */
	public void toggleSurfaceLock ()
	{
		boolean isLocked = m_isosurfaceLock.getEnabled ();
		this.lockToSurface (!isLocked);
	}

	/**
	 * @return true if the isosurface lock is enabled.
	 */
	public boolean getIsosurfaceLockEnabled ()
	{
		return (m_isosurfaceLock.getEnabled ());
	}

	/**
	 * Recalculate the Geopod's position in Earth coordinates based on the
	 * current transform.
	 */
	private void computeEarthLocationFromWorldPosition ()
	{
		Point3d worldPosition = m_transformHelper.getPosition ();
		m_currentLocation = IdvCoordinateUtility.convertWorldToEarth (worldPosition);
	}

	/**
	 * Setup the Geopod sensor system.
	 * 
	 * @param sensors
	 *            - the sensors the Geopod should use
	 */
	public void initAfterDataLoaded (Map<String, Sensor> sensors)
	{
		m_sensorMap = sensors;
		this.updateSensorValues ();
	}

	/**
	 * Causes the geopod to launch a dropsonde to create a vertical profile of
	 * the atmosphere at the current location.
	 * 
	 * @return - the dropsonde that was just launched.
	 */
	public Dropsonde launchStandardDropsonde ()
	{
		Set<String> dropsondeParameters = Dropsonde.getDefaultParameterNames ();

		Map<String, Sensor> dropsondeSensors = new HashMap<String, Sensor> ();
		for (String paramName : dropsondeParameters)
		{
			Sensor s = m_sensorMap.get (paramName);
			if (s != null)
			{
				dropsondeSensors.put (paramName, s);
			}
		}

		Dropsonde sonde = null;

		try
		{
			// Setup the dropsonde
			sonde = new Dropsonde (dropsondeSensors);
			// Collect data
			sonde.launch (getEarthLocation (), m_time);

			// Only show the dropsonde if it contains valid data
			if (sonde.hasData () && ! historyContains (sonde))
			{
				DropsondeMarker marker = new DropsondeMarker (getWorldPose ().getPosition ());
				// Maps the marker to the sonde so it can be deleted when the sonde is destroyed
				dropsondeMap.put (sonde, marker);
				SceneGraphControl.spliceIntoIdvContentBranch (marker);
				// Add the dropsonde to the history
				m_dropsondeHistory.addElement (sonde);
				
				Debug.println (sonde + " added. (" + m_dropsondeHistory.getSize () + " dropsondes total)");

				// Notify observers that the dropsonde was launched successfully
				notifyObservers (GeopodEventId.DROPSONDE_LAUNCHED);
			}
			else
			{
				Debug.println ("No dropsonde data to collect.");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace ();
		}

		// return the dropsonde that was just launched
		return (sonde);
	}
	/**
	 * Checks whether a Dropsonde with duplicate data already exists.
	 * @param sonde
	 */
	private boolean historyContains (Dropsonde sonde)
	{
		for (int i = 0; i < m_dropsondeHistory.getSize (); ++i)
		{
			Dropsonde pastSonde = ((Dropsonde)m_dropsondeHistory.getElementAt (i));
			LatLonPoint currPoint = pastSonde.getLauchLocation ();
			if (currPoint.equals (sonde.getLauchLocation ()))
			{
				return true;
			}
		}
		return false;
	}
	/**
	 * 
	 * @param hash
	 */
	public void removeDropsonde (Dropsonde sonde)
	{
		Node marker = SceneGraphControl.findNodeWithHash (dropsondeMap.get (sonde).hashCode ());
		if (marker != null && marker instanceof DropsondeMarker)
		{
			((DropsondeMarker) marker).detach ();
		}
		dropsondeMap.remove (sonde);
	}
	public void updateSelectedSonde(Dropsonde sonde)
	{
		if (m_selectedSonde != null && dropsondeMap.containsKey (m_selectedSonde))
		{
			dropsondeMap.get (m_selectedSonde).unFocus ();
		}
		if (sonde != null && dropsondeMap.containsKey (sonde))
		{
			dropsondeMap.get (sonde).focus ();
		}
		m_selectedSonde = sonde;
	}
	/**
	 * @return a list of all the dropsondes on record.
	 */
	public MutableComboBoxModel<Dropsonde> getDropsondeHistory ()
	{
		return (m_dropsondeHistory);
	}

	/**
	 * Called when the time changes
	 * 
	 * @param time
	 *            - the new time.
	 */
	public void timeChanged (Real time)
	{
		m_time = time;
		ThreadUtility.execute (m_deviceUpdater);
		this.notifyObservers (GeopodEventId.TIME_CHANGED);
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
	 * Perform actions that must be done after each move command, such as
	 * updating the earth location and the sensor values. It will also adjust
	 * the new position to follow an isosurface if tracking is enabled.
	 * 
	 * After it has finished, this method notifies observers of the Geopod that
	 * a movement event has occurred.
	 * 
	 * <p>
	 * NOTE: This method should only be called by the Geopod or Autopilot
	 * classes!
	 * </p>
	 */
	public void updateAfterMove ()
	{
		// Lock to the surface if we're following one
		m_isosurfaceLock.lockOnSurface ();
		if (m_isosurfaceLock.inOnSurface ())
		{
			this.alignWithEarthUp (false);
		}

		m_transformHelper.updateTopDownView ();

		computeEarthLocationFromWorldPosition ();

		notifyObservers (GeopodEventId.GEOPOD_TRANSLATED);

		// Run the device updating on a separate thread to avoid slowing down
		// the Java3d behavior thread.
		ThreadUtility.execute (m_deviceUpdater);
	}

	/**
	 * Perform actions that must be done after each rotation command, such as
	 * updating compass.
	 */
	public void updateAfterRotate ()
	{
		updateHeading ();
		updateAttitudeIndicator ();
	}

	private void updateAttitudeIndicator ()
	{
		Vector3d yawPitchRoll = this.getYawPitchRoll ();
		m_attitudeIndicator.setYawPitchRoll (yawPitchRoll);
	}

	/**
	 * Increases the Geopod's forward speed.
	 */
	public void accelerate ()
	{
		int current = m_speedometerModel.getValue ();
		m_speedometerModel.setValue (current + m_speedometerModel.getExtent () * 10);
	}

	/**
	 * Decreases the Geopod's forward speed.
	 */
	public void decelerate ()
	{
		int current = m_speedometerModel.getValue ();
		m_speedometerModel.setValue (current - m_speedometerModel.getExtent () * 10);
	}

	/**
	 * @return the speed of the Geopod in world units.
	 */
	public double getSpeed ()
	{
		float value = m_speedometerModel.getValue ();
		float low1 = m_speedometerModel.getMinimum ();
		float high1 = m_speedometerModel.getMaximum ();
		float low2 = (float) MINIMUM_SPEED;
		float high2 = (float) MAXIMUM_SPEED;

		return MINIMUM_SPEED + ((value - low1) * (high2 - low2)) / (high1 - low1);
	}

	/**
	 * Increases the Geopod's angular speed.
	 */
	public void increaseAngularSpeed ()
	{
		m_rotationalSpeed = Math.min (m_rotationalSpeed + ANGULAR_ACCELERATION, MAXIMUM_ANGULAR_SPEED);
	}

	/**
	 * Decreases the Geopod's angular speed.
	 */
	public void decreaseAngularSpeed ()
	{
		m_rotationalSpeed = Math.max (m_rotationalSpeed - ANGULAR_ACCELERATION, MINIMUM_ANGULAR_SPEED);
	}

	/**
	 * Update the cache of current sensor values.
	 */
	protected void updateSensorValues ()
	{
		if (m_sensorMap == null)
		{
			return;
		}
		synchronized (m_sensorMap)
		{
			String name = null;
			Sensor sensor = null;

			for (Map.Entry<String, Sensor> entry : m_sensorMap.entrySet ())
			{
				name = entry.getKey ();
				sensor = entry.getValue ();

				try
				{
					Real realValue = sensor.obtainSampleAsReal (m_currentLocation);
					if (realValue != null && !realValue.isMissing ())
					{
						realValue = VisadUtility.convertParameterValue (name, realValue);
					}
					m_currentSensorValues.put (name, realValue);
				}
				catch (Exception e)
				{
					e.printStackTrace ();
				}
			}
		}
	}

	/**
	 * @return a map containing the current cached sensor values
	 */
	public Map<String, Real> getCurrentSensorValues ()
	{
		return (m_currentSensorValues);
	}

	/**
	 * Reset the sensor cache.
	 */
	public void clearSensorCache ()
	{
		m_currentSensorValues.clear ();
	}

	/**
	 * Get sensor data at the current time and location
	 * 
	 * @param sensorName
	 *            - the sensor name to get data from
	 * @return - the current Real value
	 */
	public Real getSensorValue (String sensorName)
	{
		return (m_currentSensorValues.get (sensorName));
	}

	/**
	 * Set the pose of the Geopod using position and rotation.
	 * 
	 * @param rotation
	 * @param position
	 */
	public void setPose (Quat4d rotation, Point3d position)
	{
		m_transformHelper.setRotation (rotation);
		m_transformHelper.setPosition (position);

		this.updateAfterMove ();
		this.updateAfterRotate ();
	}

	/**
	 * Set the pose of the Geopod.
	 * 
	 * @param p
	 *            - the new pose
	 */
	public void setPose (Pose p)
	{
		m_transformHelper.setPose (p);

		this.updateAfterMove ();
		this.updateAfterRotate ();
	}

	/**
	 * Reset the geopod to it's starting position;
	 */
	public void resetPose ()
	{
		this.setPose (INITIAL_POSE);
	}

	/**
	 * 
	 * @param localDirection
	 *            - the direction to move in local coordinates
	 */
	public void moveLocal (Vector3d localDirection)
	{
		this.moveLocal (localDirection, getSpeed ());
	}

	/**
	 * 
	 * @param localDirection
	 *            - the direction to move in local coordinates
	 * @param distance
	 *            - the distance to move in world units
	 */
	public void moveLocal (Vector3d localDirection, double distance)
	{
		m_transformHelper.moveLocal (localDirection, distance);
		updateAfterMove ();
	}

	/**
	 * Yaw about the local y-axis in the indicated direction with the default
	 * speed.
	 * 
	 * @param direction
	 *            - clockwise or counterclockwise
	 */
	public void yaw (RotationDirection direction)
	{
		if (direction == RotationDirection.CLOCKWISE)
		{
			this.yaw (m_rotationalSpeed);
		}
		else
		{
			this.yaw (-m_rotationalSpeed);
		}
	}

	/**
	 * Yaw about the world or local y-axis with the default speed. Which axis
	 * depends on the LIMIT_ROLL setting in the {@link ConfigurationManager}.
	 * 
	 * @param angleDegrees
	 */
	public void yaw (double angleDegrees)
	{
		boolean limitRoll = ConfigurationManager.isEnabled (ConfigurationManager.DisableRoll);

		if (limitRoll)
		{
			// Rotate about the world and discard the roll component.
			m_transformHelper.rotateWorldAxisAboutPoint (DirectionConstants.EARTH_UP, m_transformHelper.getPosition (),
					angleDegrees);
		}
		else
		{
			m_transformHelper.yaw (angleDegrees);
		}
	}

	/**
	 * Pitch about the local x-axis.
	 * 
	 * @param angleDegrees
	 */
	public void pitch (double angleDegrees)
	{
		m_transformHelper.pitch (angleDegrees);
	}

	/**
	 * Roll about the local z-axis in the indicated direction with the default
	 * speed.
	 * 
	 * @param direction
	 *            - clockwise or counterclockwise
	 */
	public void roll (RotationDirection direction)
	{
		if (direction == RotationDirection.CLOCKWISE)
		{
			this.roll (-m_rotationalSpeed);
		}
		else
		{
			this.roll (m_rotationalSpeed);
		}
	}

	/**
	 * Roll about the local z-axis.
	 * 
	 * @param angleDegrees
	 *            - the number of degrees to rotate by.
	 */
	public void roll (double angleDegrees)
	{
		m_transformHelper.roll (angleDegrees);
	}

	/**
	 * Set the Geopod's rotation.
	 * 
	 * @param rotation
	 *            - the rotation to use as a {@link Quat4d}.
	 */
	public void setRotaton (Quat4d rotation)
	{
		m_transformHelper.setRotation (rotation);
	}

	/**
	 * Align the Geopod with the EARTH_UP vector.
	 * 
	 * @param animate
	 *            - set to true to use an animation.
	 */
	public void alignWithEarthUp (boolean animate)
	{
		if (animate)
		{
			Transform3D startTransform = new Transform3D ();
			m_transformHelper.getTransform (startTransform);
			Transform3D endTransform = m_transformHelper.createAlignedTransform (DirectionConstants.EARTH_UP);

			m_autopilot.calculateFlightPath (startTransform, endTransform, REALIGNMENT_TIME_MS);
			m_autopilot.setEnable (true);

			notifyObservers (GeopodEventId.GO_BUTTON_STATE_CHANGED);
		}
		else
		{
			m_transformHelper.alignLocalYWithWorldVector (DirectionConstants.EARTH_UP);
			updateAfterRotate ();
		}
	}

	/**
	 * @return the Geopod's {@link FlightDataRecorder}.
	 */
	public FlightDataRecorder getFlightRecorder ()
	{
		return (m_blackbox);
	}

	/**
	 * Playback the Geopod's flight path from the start of the session.
	 */
	public void recreateFlightPath ()
	{
		if (!m_flightPlaybackInProgress)
		{
			m_flightPlayback = new FlightPlayback (m_blackbox, this, m_notificationPanel);
			m_flightPlaybackThread = new Thread (m_flightPlayback);
			m_flightPlaybackThread.start ();
		}
	}

	/**
	 * Pause or resume flight playback.
	 */
	public void toggleFlightPlayback ()
	{
		if (m_flightPlaybackInProgress)
		{
			if (m_flightPlayback.isPaused ())
			{
				m_flightPlayback.resume ();
			}
			else
			{
				m_flightPlayback.pause ();
			}
		}
	}

	/**
	 * Stop a flight playback in progress.
	 */
	public void stopFlightPlayback ()
	{
		if (m_flightPlaybackInProgress)
		{
			m_flightPlayback.stopPlayback ();
			m_flightPlaybackThread = null;
		}
	}

	/**
	 * This method is used by the FlightPlayback class to set the geopod's
	 * flight playback status. Geopod needs to know the flight playback status
	 * in order to appropriately handle calls to recreate or stop flight
	 * playback.
	 * 
	 * @param inProgress
	 *            - true if flight path recreation is in progress, false if not
	 */
	public void setFlightPlaybackStatus (boolean inProgress)
	{
		m_flightPlaybackInProgress = inProgress;
	}

	/**
	 * Reverse the direction of the flight playback.
	 */
	public void reverseFlightPlaybackDirection ()
	{
		if (m_flightPlayback != null && m_flightPlaybackInProgress)
		{
			m_flightPlayback.reverseDirection ();
		}
	}

	/**
	 * Reduce the speed of the flight playback.
	 */
	public void decrementFlightPlaybackSpeed ()
	{
		PLAYBACK_FRAME_SPEED_IN_MS += PLAYBACK_FRAME_SPEED_INCREMENT_IN_MS;
	}

	/**
	 * Increase the speed of the flight playback.
	 */
	public void incrementFlightPlaybackSpeed ()
	{
		PLAYBACK_FRAME_SPEED_IN_MS = Math.max (PLAYBACK_FRAME_SPEED_IN_MS - PLAYBACK_FRAME_SPEED_INCREMENT_IN_MS,
				MIN_PLAYBACK_FRAME_SPEED_IN_MS);
	}

	/**
	 * 
	 * @param eventNotificationPanel
	 */
	public void setEventNotificationPanel (NotificationPanel eventNotificationPanel)
	{
		m_notificationPanel = eventNotificationPanel;
	}

	/**
	 * 
	 * @return the name of the {@link DataSource} used to startup the plugin.
	 */
	public String extractCurrentDataSourceName ()
	{
		DataSource currentDataSource = m_plugin.getDataSource ();
		String dataSourceName = currentDataSource.getName ();
		// Test if DataSource was loaded from file on computer
		File testFile = new File (dataSourceName);
		if (testFile.exists ())
		{
			// Data source was loaded from computer, so need to 
			//remove local file address portion from name
			dataSourceName = testFile.getName ();
		}
		return (dataSourceName);
	}

	/**
	 * @return the particle category at the current location.
	 */
	public String getCurrentCategoryFromParticleImager ()
	{
		String category = m_particleImager.getCurrentCategory ();
		return (category);
	}

	/**
	 * Turn the particle imager on or off.
	 */
	public void toggleParticleImager ()
	{
		m_particleImager.updateParticleImage ();
		m_particleImager.toggleParticleImager ();
	}

	/**
	 * 
	 * @return <tt>true</tt> if the particel imager is active.
	 */
	public boolean particleImagerActive ()
	{
		boolean isDeployed = m_particleImager.isParticleImagerActive ();
		return (isDeployed);
	}

	/**
	 * @return The behavior that handles mouse input for Geopod movement.
	 */
	public Behavior getMouseBehavior ()
	{
		return (m_mouseBehavior);
	}

	/**
	 * @return The behavior that handles keyboard input for Geopod movement.
	 */
	public KeyBehavior getKeyBehavior ()
	{
		return (m_keyBehavior);
	}

	private void updateHeading ()
	{
		// Obtain normalized forward vector from geopod orientation
		Vector3d forward = m_transformHelper.getForward ();

		// Project onto the XY plane
		forward.z = 0;

		// Normalize to obtain a direction vector
		forward.normalize ();

		// Get angle between forward and north vectors.
		// x and z components are 0 in the north vector, so the dot product 
		// is just the y component of forward.
		double northDotForward = forward.y;
		double angleRad = Math.acos (northDotForward);

		// convert to degrees
		double angleDeg = Math.toDegrees (angleRad);

		// Convert from range [0, 180] to range [180, 360]
		if (forward.x < 0)
		{
			angleDeg = 360 - angleDeg;
		}

		m_compass.setHeading ((int) angleDeg);
	}

	/**
	 * Class to update slow devices on a thread other the the Java3D behavior
	 * scheduler.
	 * 
	 * @author geopod
	 * 
	 */
	private final class DeviceUpdater
			implements Runnable
	{
		@Override
		public void run ()
		{
			Geopod.this.updateSensorValues ();
			m_particleImager.updateParticleImage ();
		}
	}

	/**
	 * Converts contents of FlightDataRecorder into xml file so flight log is
	 * preserved for multiple sessions of geopod use.
	 */
	public void encodeFlightLog ()
	{

		try
		{
			FileOutputStream fileOutputStream;
			fileOutputStream = new FileOutputStream (FLIGHT_LOG_LOCATION);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream (fileOutputStream);
			XStream xstream = new XStream ();
			Class<?>[] classesWithAnnotations = { FlightDataRecorder.class, FlightEvent.class, MovementEvent.class,
					DropsondeEvent.class, ParticleImageChangedEvent.class, ParticleImagerDisplayEvent.class,
					DataLoadedEvent.class, LocationNotedEvent.class };
			xstream.processAnnotations (classesWithAnnotations);
			xstream.registerConverter (new MovementEventEncoder ());
			xstream.toXML (m_blackbox, bufferedOutputStream);
			bufferedOutputStream.close ();
		}
		catch (Exception e)
		{
			e.printStackTrace ();
		}
		finally
		{
		}

	}

	public void displayTimedNotificationPanel (String notificationText, int millisecsToDisplay)
	{
		ThreadUtility.execute (new NotificationTimedDisplayer (notificationText, millisecsToDisplay));
	}

	public Thread displayNotificationPanel (String notificationText)
	{
		//ThreadUtility.execute (new NotificationShower (notificationText));
		return (new Thread (new NotificationShower (notificationText)));
	}

	private class NotificationTimedDisplayer
			implements Runnable
	{
		private String m_notification;
		private int m_millisecToDisplay;

		public NotificationTimedDisplayer (String notification, int millisecToDisplay)
		{
			m_notification = notification;
			m_millisecToDisplay = millisecToDisplay;
		}

		@Override
		public void run ()
		{
			m_notificationPanel.setNotificationText (m_notification);
			m_notificationPanel.setVisible (true);
			//long time = System.currentTimeMillis ();
			//long timeNow = System.currentTimeMillis ();
			//while (timeNow < time + m_millisecToDisplay)
			//{
			//	timeNow = System.currentTimeMillis ();
			//}
			synchronized (this)
			{
				try
				{
					wait (m_millisecToDisplay);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace ();
				}
			}
			m_notificationPanel.setVisible (false);
		}
	}

	private class NotificationShower
			implements Runnable
	{
		private String m_notification;

		public NotificationShower (String notification)
		{
			m_notification = notification;
		}

		@Override
		public void run ()
		{
			try
			{
				m_notificationPanel.setNotificationText (m_notification);
				m_notificationPanel.setVisible (true);
			}
			catch (Exception e)
			{
				e.printStackTrace ();
			}
		}
	}

	@Override
	public void removeObservers ()
	{
		m_subjectImpl.removeObservers ();
	}
}
