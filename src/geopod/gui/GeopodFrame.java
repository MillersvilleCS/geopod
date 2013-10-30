package geopod.gui;

import static geopod.constants.FrameConstants.*;
import geopod.Geopod;
import geopod.constants.UIConstants;
import geopod.devices.ParticleImager;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.components.GeopodButton;
import geopod.gui.components.OffScreenCanvas3D;
import geopod.gui.components.OnScreenCanvas3D;
import geopod.gui.components.SpeedControlSlider;
import geopod.gui.panels.CommentPromptPanel;
import geopod.gui.panels.ConfigurationPanel;
import geopod.gui.panels.HelpPanel;
import geopod.gui.panels.ImagePanel;
import geopod.gui.panels.LoadingPanel;
import geopod.gui.panels.MovieCapturePanel;
import geopod.gui.panels.NotedLocationsPanel;
import geopod.gui.panels.NotificationPanel;
import geopod.gui.panels.ParameterChooserPanel;
import geopod.gui.panels.PrimaryButtonPanel;
import geopod.gui.panels.StatusPanel;
import geopod.gui.panels.ToolPanel;
import geopod.gui.panels.datadisplay.OverflowPanel;
import geopod.gui.panels.datadisplay.SensorDisplayPanel;
import geopod.gui.panels.distance.DistancePanel;
import geopod.gui.panels.dropsonde.MultipleDropsondeChartPanel;
import geopod.gui.panels.isosurface.IsosurfaceViewPanel;
import geopod.gui.panels.mission.MissionPanel;
import geopod.gui.panels.navigation.*;
import geopod.utils.FileLoadingUtility;
import geopod.utils.web.WebUtility;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.j3d.Canvas3D;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.BevelBorder;

import org.jdesktop.swingx.JXCollapsiblePane.Direction;

import ucar.unidata.data.DataChoice;
import visad.georef.EarthLocation;

import com.sun.j3d.utils.universe.SimpleUniverse;

/**
 * GeopodFrame extends JFrame to allow better control of components added to the
 * frame.
 * 
 * @author Geopod Team
 * 
 */
public class GeopodFrame extends JFrame {
	private static final long serialVersionUID = -5465725759592211496L;

	private OnScreenCanvas3D m_canvas;
	private OnScreenCanvas3D m_topCanvas;

	// LayeredPanes
	private JLayeredPane m_rootLayeredPane;
	private JLayeredPane m_managedLayeredPane;

	// Panels
	private ImagePanel m_dashboardPanel;
	private JPanel m_topViewPanel;
	private PrimaryButtonPanel m_buttonPanel;
	private OverflowPanel m_overFlowPanel;
	private SensorDisplayPanel m_primaryDisplayPanel;
	private SensorDisplayPanel m_secondaryDisplayPanel;
	private NavigationPanel m_navigationPanel;
	private MissionPanel m_missionPanel;
	private IsosurfaceViewPanel m_isosurfaceViewPanel;
	private DistancePanel m_distancePanel;
	private MovieCapturePanel m_movieCapturePanel;
	private MultipleDropsondeChartPanel m_multipleChartPanel;
	private HelpPanel m_helpPanel;
	private ParameterChooserPanel m_parameterChooserPanel;
	private StatusPanel m_statusPanel;

	private LoadingPanel m_loadingPanel;
	private LookUpPanel m_lookUpPanel;
	private NotificationPanel m_eventNotificationPanel;
	private CommentPromptPanel m_commentPromptPanel;
	private NotedLocationsPanel m_notedLocationsPanel;
	private ConfigurationPanel m_configurationPanel;

	private Geopod m_geopod;
	private Hud m_hud;
	private DisplayPanelManager m_displayManager;
	private Component m_glassPane;

	/**
	 * Constructor.
	 * 
	 * @param hud
	 *            - the {@link Hud} this frame is associated with.
	 * @param geopod
	 *            - the {@link Geopod} this frame is associated with.
	 */
	public GeopodFrame(Hud hud, Geopod geopod) {
		super("Geopod" + " - " + hud.getDataSourceName());

		m_hud = hud;
		m_geopod = geopod;

		setupApplicationIcon();
		setupFrameSizes();
		setupLayering();
		setupGlassPane();

		buildDashboard();
		buildInterfaceComponents();

		this.setFocusTraversalPolicy(new CanvasOnlyPolicy(m_canvas));
	}

	/**
	 * Performs initialization that must be done after the data has loaded.
	 */
	public void initAfterDataLoad() {
		// add some listeners
		if (m_canvas != null && m_canvas.getFocusListeners().length == 0) {
			m_canvas.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					// Tell the Geopod to start moving again
					m_hud.setMovementEnabled(true);
				}

				@Override
				public void focusLost(FocusEvent e) {
					m_hud.setMovementEnabled(false);
				}
			});
		}
	}

	/**
	 * Enables user interaction once data has finished loading
	 */
	public void enableUserInteraction() {
		// data has finished loading - hide the panel
		m_loadingPanel.setVisible(false);

		// set everything up properly
		blockMouseEvents(false);
		updateDisplay();
		requestFocusOnCanvas();
	}

	/**
	 * Build a glass pane that intercepts mouse events and ignores them.
	 */
	private void setupGlassPane() {
		m_glassPane = this.getGlassPane();

		m_glassPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// This is only here to block mouse events from reaching other
				// swing components.
				// Alternatively, we could also forward mouse events to select
				// components if necessary.
			}
		});
	}

	/**
	 * Set the application icon that goes in the title bar.
	 */
	private void setupApplicationIcon() {
		BufferedImage applicationIcon = null;

		try {
			applicationIcon = FileLoadingUtility
					.loadBufferedImage(APPLICATION_ICON_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}

		super.setIconImage(applicationIcon);
	}

	private void setupFrameSizes() {
		super.setPreferredSize(FRAME_OPTIMUM_SIZE);
		super.setSize(FRAME_OPTIMUM_SIZE);
		super.setMinimumSize(FRAME_MINIMUM_SIZE);
	}

	private void setupLayering() {
		m_rootLayeredPane = this.getLayeredPane();

		m_managedLayeredPane = new JLayeredPane();
		m_managedLayeredPane.setLayout(new ScaleLayout(FRAME_SIZE_IN_IMAGE));

		m_rootLayeredPane.add(m_managedLayeredPane);
		m_rootLayeredPane.addComponentListener(new ResizeListener());
	}

	private void addLayeredComponent(Component c, Integer layer,
			Rectangle constraints) {
		m_managedLayeredPane.setLayer(c, layer);
		m_managedLayeredPane.add(c, constraints);
	}

	private void buildDashboard() {
		m_dashboardPanel = new ImagePanel(
				"//Resources/Images/User Interface/GeopodDashboardNoDisplay.png");
		m_dashboardPanel.setOpaque(false);

		super.setContentPane(m_dashboardPanel);
	}

	private void buildInterfaceComponents() {
		// Create and add Canvas3D
		m_canvas = createOnScreenCanvas(CANVAS_SIZE);
		OffScreenCanvas3D m_OffScreenCanvas = createOffScreenCanvas(CANVAS_SIZE);
		m_canvas.setOffScreenCanvas(m_OffScreenCanvas);

		addLayeredComponent(m_canvas, JLayeredPane.DEFAULT_LAYER,
				INNER_VIEWING_AREA_BOUNDS);
		addLayeredComponent(m_OffScreenCanvas, JLayeredPane.DEFAULT_LAYER,
				INNER_VIEWING_AREA_BOUNDS);

		// Mini map
		m_topViewPanel = new JPanel();
		m_topViewPanel.setLayout(new BorderLayout());
		m_topViewPanel.setBorder(BorderFactory.createBevelBorder(
				BevelBorder.RAISED, Color.WHITE, Color.WHITE, Color.GRAY,
				Color.GRAY));
		m_topViewPanel.setOpaque(false);

		m_topCanvas = createOnScreenCanvas(new Dimension(225, 225));
		OffScreenCanvas3D m_OffScreenTopCanvas = createOffScreenCanvas(new Dimension(
				255, 255));
		m_topCanvas.setOffScreenCanvas(m_OffScreenTopCanvas);
		m_topCanvas.setFocusable(false);

		m_topViewPanel.add(m_topCanvas, BorderLayout.CENTER);
		addLayeredComponent(m_topViewPanel, JLayeredPane.PALETTE_LAYER,
				TOP_VIEW_CANVAS_BOUNDS);

		m_buttonPanel = new PrimaryButtonPanel(m_hud);
		addLayeredComponent(m_buttonPanel, JLayeredPane.PALETTE_LAYER,
				PRIMARY_BUTTON_PANEL_BOUNDS);

		JPanel toolPanel = new ToolPanel(m_hud);
		addLayeredComponent(toolPanel, JLayeredPane.PALETTE_LAYER,
				TOOL_PANEL_BOUNDS);

		NavigationPanelExt navPanelExt = new NavigationPanelExt(m_hud, m_geopod);
		addLayeredComponent(navPanelExt, JLayeredPane.PALETTE_LAYER,
				NAV_EXT_PANEL_BOUNDS);

		m_navigationPanel = new NavigationPanel(m_hud, m_geopod, m_canvas);
		addLayeredComponent(m_navigationPanel, JLayeredPane.PALETTE_LAYER,
				NAVIGATION_PANEL_BOUNDS);

		m_lookUpPanel = new LookUpPanel(m_navigationPanel);
		m_geopod.addObserver(m_lookUpPanel, GeopodEventId.GEOPOD_TRANSLATED);
		m_lookUpPanel.addObserver(m_hud,
				GeopodEventId.LOOKUP_BUTTON_STATE_CHANGED);
		addLayeredComponent(m_lookUpPanel, JLayeredPane.PALETTE_LAYER,
				LOOK_UP_PANEL_BOUNDS);

		GeopodButton helpButton = createHelpButton();
		addLayeredComponent(helpButton, JLayeredPane.PALETTE_LAYER,
				HELP_BUTTON_BOUNDS);

		GeopodButton configButton = createConfigButton();
		addLayeredComponent(configButton, JLayeredPane.PALETTE_LAYER,
				CONFIG_BUTTON_BOUNDS);

		GeopodButton muButton = createMuButton();
		addLayeredComponent(muButton, JLayeredPane.PALETTE_LAYER,
				MU_BUTTON_BOUNDS);

		GeopodButton idvButton = createIdvButton();
		addLayeredComponent(idvButton, JLayeredPane.PALETTE_LAYER,
				IDV_BUTTON_BOUNDS);

		ImagePanel nsfLogo = new ImagePanel(NSF_LOGO_PATH);
		nsfLogo.setOpaque(false);
		addLayeredComponent(nsfLogo, JLayeredPane.PALETTE_LAYER,
				NFS_LOGO_BOUNDS);

		m_displayManager = new DisplayPanelManager();

		m_primaryDisplayPanel = new SensorDisplayPanel(m_geopod);
		m_primaryDisplayPanel.setGridSize(3, 3);

		Map<GridEntry, String> defaultParameterMap = createDefaultParameterMap();
		m_primaryDisplayPanel.setDefaultParameterMappings(defaultParameterMap);

		String displayAreaImagePath = "//Resources/Images/User Interface/Slices/bottomDisplay.png";
		BufferedImage displayImage = null;

		try {
			displayImage = FileLoadingUtility
					.loadBufferedImage(displayAreaImagePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		m_primaryDisplayPanel.setImage(displayImage);
		addLayeredComponent(m_primaryDisplayPanel, JLayeredPane.PALETTE_LAYER,
				PRIMARY_DISPLAY_PANEL_BOUNDS);

		m_secondaryDisplayPanel = new SensorDisplayPanel(m_geopod);
		m_secondaryDisplayPanel.setOpaque(true);
		m_secondaryDisplayPanel.setBackground(UIConstants.GEOPOD_GREEN);

		m_secondaryDisplayPanel.setGridSize(10, 1);
		m_secondaryDisplayPanel.setVisible(true);
		m_secondaryDisplayPanel.setPreferredSize(new Dimension(200, 600));

		m_displayManager.addDisplayPanel(m_primaryDisplayPanel);
		m_displayManager.addDisplayPanel(m_secondaryDisplayPanel);

		m_overFlowPanel = new OverflowPanel();
		m_overFlowPanel.setDirection(Direction.LEFT);
		m_overFlowPanel.setCollapsed(true);
		m_secondaryDisplayPanel.addObserver(m_overFlowPanel,
				GeopodEventId.DISPLAY_PANEL_ACTIVE);
		m_secondaryDisplayPanel.addObserver(m_overFlowPanel,
				GeopodEventId.DISPLAY_PANEL_EMPTY);
		m_overFlowPanel.add(m_secondaryDisplayPanel, BorderLayout.CENTER);
		addLayeredComponent(m_overFlowPanel, JLayeredPane.PALETTE_LAYER,
				OVERFLOW_PANEL_BOUNDS);

		m_parameterChooserPanel = m_hud.createParameterChooserPanel();
		m_parameterChooserPanel.addObserver(m_hud,
				GeopodEventId.PARAMETER_BUTTON_STATE_CHANGED);
		m_parameterChooserPanel.setBorder(geopod.gui.components.BorderFactory
				.createStandardBorder());
		m_parameterChooserPanel.setVisible(false);
		addLayeredComponent(m_parameterChooserPanel, JLayeredPane.MODAL_LAYER,
				PARAMETER_CHOOSER_PANEL_BOUNDS);

		m_missionPanel = new MissionPanel(m_geopod.getFlightRecorder());
		m_missionPanel.setVisible(false);
		m_missionPanel.addObserver(m_hud,
				GeopodEventId.MISSION_BUTTON_STATE_CHANGED);
		addLayeredComponent(m_missionPanel, JLayeredPane.MODAL_LAYER,
				MISSION_PANEL_BOUNDS);

		m_isosurfaceViewPanel = new IsosurfaceViewPanel(m_hud);
		m_isosurfaceViewPanel.setVisible(false);
		this.addWindowFocusListener(new WindowAdapter() {
			public void windowGainedFocus(WindowEvent e) {
				m_isosurfaceViewPanel.refreshComponents();
			}
		});
		m_isosurfaceViewPanel.addObserver(m_hud,
				GeopodEventId.ISOSURFACE_BUTTON_STATE_CHANGED);
		addLayeredComponent(m_isosurfaceViewPanel, JLayeredPane.MODAL_LAYER,
				ISOSURFACE_VIEW_PANEL_BOUNDS);

		m_distancePanel = new DistancePanel(m_geopod, m_hud);
		m_distancePanel.setVisible(false);
		m_distancePanel.addObserver(m_hud,
				GeopodEventId.DISTANCE_BUTTON_STATE_CHANGED);
		addLayeredComponent(m_distancePanel, JLayeredPane.MODAL_LAYER,
				DISTANCE_PANEL_BOUNDS);

		m_movieCapturePanel = new MovieCapturePanel(m_hud);
		m_movieCapturePanel.setVisible(false);
		m_movieCapturePanel.addObserver(m_hud,
				GeopodEventId.MOVIECAPTURE_BUTTON_STATE_CHANGED);
		addLayeredComponent(m_movieCapturePanel, JLayeredPane.MODAL_LAYER,
				MOVIECAPTURE_PANEL_BOUNDS);

		m_configurationPanel = new ConfigurationPanel();
		m_configurationPanel.addObserver(m_hud,
				GeopodEventId.REQUEST_FLIGHT_LOG_RESET);
		m_configurationPanel.setVisible(false);
		m_configurationPanel.addObserver(m_hud,
				GeopodEventId.CONFIG_BUTTON_STATE_CHANGED);
		addLayeredComponent(m_configurationPanel, JLayeredPane.POPUP_LAYER,
				CONFIG_PANEL_BOUNDS);

		m_helpPanel = new HelpPanel();
		m_helpPanel.setVisible(false);
		m_helpPanel.addObserver(m_hud, GeopodEventId.HELP_BUTTON_STATE_CHANGED);
		addLayeredComponent(m_helpPanel, JLayeredPane.POPUP_LAYER,
				HELP_PANEL_BOUNDS);

		m_multipleChartPanel = new MultipleDropsondeChartPanel(
				m_geopod.getDropsondeHistory(), m_hud);
		m_multipleChartPanel.addObserver(m_hud,
				GeopodEventId.DROPSONDE_BUTTON_STATE_CHANGED);
		addLayeredComponent(m_multipleChartPanel, JLayeredPane.PALETTE_LAYER,
				DROPSONDE_PANEL_BOUNDS);
		m_multipleChartPanel.setVisible(false);

		ParticleImager particleImager = m_geopod.getParticleImager();
		JPanel particlePanel = particleImager.getParticlePanel();
		particlePanel.setBorder(BorderFactory.createBevelBorder(
				BevelBorder.RAISED, Color.WHITE, Color.WHITE, Color.GRAY,
				Color.GRAY));
		particlePanel.setVisible(false);
		particlePanel.setBackground(Color.BLACK);

		addLayeredComponent(particlePanel, JLayeredPane.PALETTE_LAYER,
				PARTICLE_PANEL_BOUNDS);

		m_loadingPanel = new LoadingPanel();
		m_loadingPanel.setVisible(false);
		addLayeredComponent(m_loadingPanel, JLayeredPane.POPUP_LAYER,
				LOADING_PANEL_BOUNDS);

		m_eventNotificationPanel = new NotificationPanel();
		m_eventNotificationPanel.setVisible(false);
		addLayeredComponent(m_eventNotificationPanel, JLayeredPane.POPUP_LAYER,
				EVENT_NOTIFICATION_PANEL_BOUNDS);
		m_geopod.setEventNotificationPanel(m_eventNotificationPanel);

		m_notedLocationsPanel = new NotedLocationsPanel();
		m_notedLocationsPanel.setVisible(false);
		m_notedLocationsPanel.addObserver(m_hud,
				GeopodEventId.NOTEPAD_BUTTON_STATE_CHANGED);
		addLayeredComponent(m_notedLocationsPanel, JLayeredPane.MODAL_LAYER,
				NOTED_LOCATIONS_PANEL_BOUNDS);

		m_commentPromptPanel = new CommentPromptPanel(m_geopod,
				m_notedLocationsPanel);
		m_commentPromptPanel.setVisible(false);
		m_commentPromptPanel.addObserver(m_hud,
				GeopodEventId.ADDNOTE_BUTTON_STATE_CHANGED);
		addLayeredComponent(m_commentPromptPanel, JLayeredPane.POPUP_LAYER,
				COMMENT_PROMPT_PANEL_BOUNDS);

		m_statusPanel = new StatusPanel("test status message");
		m_statusPanel.setCollapsed(true);
		addLayeredComponent(m_statusPanel, JLayeredPane.PALETTE_LAYER,
				STATUS_PANEL_BOUNDS);
		m_geopod.addObserver(m_statusPanel, GeopodEventId.ISOSURFACE_LOCKED);
		m_geopod.addObserver(m_statusPanel, GeopodEventId.ISOSURFACE_UNLOCKED);

		JSlider speedometer = new SpeedControlSlider(
				m_geopod.getSpeedometerModel());
		speedometer.setFocusable(false);
		speedometer.setOrientation(JSlider.VERTICAL);
		speedometer.setPaintLabels(false);
		speedometer.setOpaque(false);
		speedometer.setForeground(UIConstants.GEOPOD_GREEN);

		addLayeredComponent(speedometer, JLayeredPane.PALETTE_LAYER,
				new Rectangle(1628, 450, 30, 250));
	}

	private OnScreenCanvas3D createOnScreenCanvas(Dimension size) {
		GraphicsConfiguration config = SimpleUniverse
				.getPreferredConfiguration();
		OnScreenCanvas3D canvas = new OnScreenCanvas3D(config, false);
		canvas.setSize(size);

		return (canvas);
	}

	private OffScreenCanvas3D createOffScreenCanvas(Dimension size) {
		GraphicsConfiguration config = SimpleUniverse
				.getPreferredConfiguration();
		OffScreenCanvas3D canvas = new OffScreenCanvas3D(config, true);
		canvas.setSize(size);
		return (canvas);
	}

	private Map<GridEntry, String> createDefaultParameterMap() {
		Map<GridEntry, String> map = new HashMap<GridEntry, String>();

		map.put(new GridEntry(0, 0), "Temperature @ isobaric");
		map.put(new GridEntry(1, 0), "Geopotential_height @ isobaric");
		map.put(new GridEntry(2, 0), "Speed (from u_wind & v_wind)");
		map.put(new GridEntry(0, 1), "Relative_humidity @ isobaric");

		return (map);
	}

	private GeopodButton createIdvButton() {
		String filePathUp = "//Resources/Images/User Interface/Buttons/IdvButton.png";
		String filePathDown = "//Resources/Images/User Interface/Buttons/IdvButtonDown.png";

		GeopodButton idvButton = new GeopodButton(filePathUp, filePathDown);

		idvButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				m_hud.toggleIdvDisplay();
			}
		});

		return (idvButton);
	}

	private GeopodButton createMuButton() {
		String filePathUp = "//Resources/Images/User Interface/Buttons/MuButton.png";
		String filePathDown = "//Resources/Images/User Interface/Buttons/MuButtonDown.png";

		GeopodButton muButton = new GeopodButton(filePathUp, filePathDown);
		muButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WebUtility.browse("http://www.millersville.edu");
			}
		});

		return (muButton);
	}

	private GeopodButton createConfigButton() {
		String imageUp = "//Resources/Images/User Interface/Buttons/ConfigurationButton.png";
		String imageDown = "//Resources/Images/User Interface/Buttons/ConfigurationButtonDown.png";
		String imageHover = "//Resources/Images/User Interface/Buttons/ConfigurationButtonHover.png";

		GeopodButton configButton = new GeopodButton(imageUp, imageDown,
				imageHover);
		configButton.setToolTipText(" Settings ");
		configButton.setActionCommand("settings");
		configButton.addActionListener(m_hud);
		m_hud.addObserver(configButton,
				GeopodEventId.CONFIG_BUTTON_STATE_CHANGED);

		return (configButton);
	}

	private GeopodButton createHelpButton() {
		String imageUp = "//Resources/Images/User Interface/Buttons/HelpButton.png";
		String imageDown = "//Resources/Images/User Interface/Buttons/HelpButtonDown.png";
		String imageHover = "//Resources/Images/User Interface/Buttons/HelpButtonHover.png";
		GeopodButton helpButton = new GeopodButton(imageUp, imageDown,
				imageHover);
		helpButton.setToolTipTexts(" Help ", " Close Help ");
		helpButton.setActionCommand("help");
		helpButton.addActionListener(m_hud);
		m_hud.addObserver(helpButton, GeopodEventId.HELP_BUTTON_STATE_CHANGED);

		return (helpButton);
	}

	/**
	 * Add a mouse listener to the canvas.
	 * 
	 * @param listener
	 *            - the {@link MouseListener} to add.
	 */
	public void addCanvasMouseListener(MouseListener listener) {
		m_canvas.addMouseListener(listener);
	}

	/**
	 * @return - the frame's {@link Canvas3D canvas}.
	 */
	public Canvas3D getCanvas() {
		return (m_canvas);
	}

	/**
	 * 
	 * @return - the {@link EarthLocation} that the user has entered in the
	 *         lat/lon/alt fields.
	 */
	public EarthLocation getInputtedEarthLocation() {
		return (m_navigationPanel.parseAndValidateInputtedEarthLocation());
	}

	/**
	 * Set the {@link EarthLocation} to display to the user.
	 * 
	 * @param el
	 *            - the earth location to display
	 */
	public void setInputEarthLocation(EarthLocation el) {
		m_navigationPanel.setInputEarthLocation(el);
	}

	/**
	 * Notify the frame that the earth location has changed.
	 */
	public void indicateEarthLocationChange() {
		m_navigationPanel.indicateValueChange();
	}

	/**
	 * Enables a <tt>glassPane</tt> over the entire frame to block mouse events
	 * from reaching other swing components.
	 * 
	 * @param blockEvents
	 *            - true to block mouse events, false to allow them through.
	 */
	public void blockMouseEvents(boolean blockEvents) {
		m_glassPane.setVisible(blockEvents);
	}

	/**
	 * Request focus on the canvas. The canvas must have focus to receive input
	 * and allow the movement behaviors to work.
	 */
	public void requestFocusOnCanvas() {
		m_canvas.requestFocus();
	}

	/**
	 * Update all displays.
	 */
	public void updateDisplay() {
		EarthLocation geopodLocation = m_geopod.getEarthLocation();
		m_navigationPanel.setInputEarthLocation(geopodLocation);
		m_distancePanel.setEndPosition(geopodLocation);
		m_displayManager.updateDisplayPanels();
	}

	/**
	 * Notify the frame that the parameter list has changed.
	 * 
	 * @param newChoices
	 */
	public void updateDisplayMappings(List<DataChoice> newChoices) {
		m_displayManager.updateDisplayMappings(newChoices);
	}

	/**
	 * Show the comment prompt panel after the user requests to note a location.
	 */
	public void noteLocation() {
		m_commentPromptPanel.setVisible(!m_commentPromptPanel.isVisible());
		if (m_commentPromptPanel.isVisible()) {
			m_commentPromptPanel.requestFocusInCommentField();
		} else {
			this.requestFocusOnCanvas();
		}
		m_commentPromptPanel
				.notifyObservers(GeopodEventId.ADDNOTE_BUTTON_STATE_CHANGED);
	}

	public boolean areNotedLocationsSaved() {
		return (m_notedLocationsPanel.currentContentSaved());
	}

	/**
	 * Display a panel showing the data being loaded.
	 * 
	 * @param dataChoiceName
	 *            - the {@link DataChoice data choice} being loaded.
	 * @param dataSourceName
	 *            - the {@link ucar.unidata.data.DataSource data source} being
	 *            loaded.
	 */
	public void showDataLoading(String dataChoiceName, String dataSourceName) {
		m_loadingPanel.showDataLoading(dataChoiceName, dataSourceName);
	}

	/**
	 * Toggle the visibility of a {@link Component}.
	 * 
	 * @param component
	 *            - the component to toggle
	 */
	private void toggleVisibility(Component component) {
		boolean visible = component.isVisible();
		component.setVisible(!visible);
	}

	/**
	 * Toggle the visibility of the {@link ParameterChooserPanel parameter
	 * chooser panel}.
	 */
	public void toggleParameterChooserPanel() {
		m_parameterChooserPanel.updateDataChoices();
		toggleVisibility(m_parameterChooserPanel);
		m_parameterChooserPanel
				.notifyObservers(GeopodEventId.PARAMETER_BUTTON_STATE_CHANGED);
	}

	/**
	 * Toggle the visibility of the {@link MissionPanel mission panel}.
	 */
	public void toggleMissionPanel() {
		m_missionPanel.toggleVisibility();
	}

	/**
	 * Toggle the visibility of the isosurface view panel
	 */
	public void toggleIsosurfaceViewPanel() {
		m_isosurfaceViewPanel.toggleVisibility();
	}

	/**
	 * Toggle the visibility of the distance panel
	 */

	public void toggleDistancePanel() {
		m_distancePanel.toggleVisibility();
	}

	/**
	 * Toggle the visibility of the movie capture panel
	 */

	public void toggleMovieCapturePanel() {
		m_movieCapturePanel.click();
	}

	/**
	 * Toggle the visibility of the {@link HelpPanel help panel}.
	 */
	public void toggleHelpPanel() {
		toggleVisibility(m_helpPanel);
	}

	/**
	 * Toggle the visibility of the {@link LookUpPanel address lookup panel}.
	 */
	public void toggleLookUpPanel() {
		m_lookUpPanel.toggleCollapsedState();
	}

	/**
	 * Show a status message to the user.
	 * 
	 * @param message
	 *            - the massage to show.
	 */
	public void showStatusMessage(String message) {
		// TODO: find out if this is still being used...
		m_statusPanel.showStatusMessage(message);
	}

	/**
	 * Hide the status message panel.
	 */
	public void hideStatus() {
		m_statusPanel.hideStatus();
	}

	/**
	 * Toggle the visibility of the {@link OverflowPanel overflow panel}.
	 */
	public void toggleOverflowPanel() {
		m_overFlowPanel.setCollapsed(!m_overFlowPanel.isCollapsed());
	}

	/**
	 * Toggle the visibility of the {@link MultipleDropsondeChartPanel dropsonde
	 * chart panel}.
	 */
	public void toggleDropsondeChartPanel() {
		toggleVisibility(m_multipleChartPanel);
	}

	/**
	 * Toggle the glass pane. When on, it disallows the rest of the interface
	 * from receiving mouse or key events.
	 */
	public void toggleGlassPane() {
		toggleVisibility(m_glassPane);
	}

	/**
	 * Toggle the visibility of the {@link NotedLocationsPanel}.
	 */
	public void toggleNotedLocationsPanel() {
		toggleVisibility(m_notedLocationsPanel);
	}

	/**
	 * Show or hide the configuration panel.
	 */
	public void toggleConfigurationPanel() {
		toggleVisibility(m_configurationPanel);
	}

	/**
	 * @return the {@link Canvas3D canvas} used for the top down view.
	 */
	public Canvas3D getTopViewCanvas() {
		return (m_topCanvas);
	}

	/**
	 * @return the {@link JPanel panel} that contains the mini-map.
	 */
	public JPanel getTopViewPanel() {
		return m_topViewPanel;
	}

	// **** Begin inner classes ****

	/**
	 * This ComponentAdapter is added as a componentListener to
	 * m_rootLayeredPane and ensures m_managedLayeredPane maintains the same
	 * bounds as the root layered pane.
	 */
	private class ResizeListener extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			m_managedLayeredPane.setBounds(m_rootLayeredPane.getBounds());
		}
	}

	private class CanvasOnlyPolicy extends FocusTraversalPolicy {
		Canvas3D m_canvas;

		public CanvasOnlyPolicy(Canvas3D canvas) {
			m_canvas = canvas;
		}

		@Override
		public Component getComponentAfter(Container aContainer,
				Component aComponent) {
			return m_canvas;
		}

		@Override
		public Component getComponentBefore(Container aContainer,
				Component aComponent) {
			return m_canvas;
		}

		@Override
		public Component getFirstComponent(Container aContainer) {
			return m_canvas;
		}

		@Override
		public Component getLastComponent(Container aContainer) {
			return m_canvas;
		}

		@Override
		public Component getDefaultComponent(Container aContainer) {
			return m_canvas;
		}

	}

}
