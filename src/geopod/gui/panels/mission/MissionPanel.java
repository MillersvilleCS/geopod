package geopod.gui.panels.mission;

import geopod.constants.UIConstants;
import geopod.devices.FlightDataRecorder;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.components.BorderFactory;
import geopod.gui.components.ButtonFactory;
import geopod.gui.components.GeopodLabel;
import geopod.gui.components.PainterFactory;
import geopod.gui.styles.GeopodTabbedPaneUI;
import geopod.mission.ConditionsNotMetPanel;
import geopod.mission.Mission;
import geopod.mission.MissionHyperlinkListener;
import geopod.mission.ObjectivesPane;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXPanel;

/**
 * A Mission Panel creates and displays the mission interface, including tabs
 * with background information and objectives, and a screen which allows the
 * user to search for and load missions.
 * 
 * @author geopod
 * 
 */
public class MissionPanel
		extends JXPanel
		implements ISubject
{
	private static final long serialVersionUID = 5325290875623772920L;

	/********************************************************/
	/* Button Listeners */
	/********************************************************/

	/**
	 * Listens for events from the Find Mission button, displays a FileChooser
	 * when clicked, and displays the mission location and Load Mission button
	 * if a file is selected.
	 * 
	 */
	public class LoadMissionButtonListener
			implements ActionListener
	{
		@Override
		public void actionPerformed (ActionEvent event)
		{

			int returnVal = m_fileChooser.showOpenDialog (MissionPanel.this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				String missionLocation = m_fileChooser.getSelectedFile ().toString ();
				loadMission (missionLocation);
			}
		}
	}

	private static class GEOFileFilter
			extends FileFilter
	{

		@Override
		public boolean accept (File f)
		{
			if (f.isDirectory ())
			{
				return (true);
			}

			String fullFileName = f.getName ();
			fullFileName = fullFileName.toLowerCase ();
			boolean isAcceptedExtention = fullFileName.endsWith (".geo");

			return (isAcceptedExtention);
		}

		@Override
		public String getDescription ()
		{
			return ("GEO files only");
		}

	}

	private static final String INITIAL_MISSION_HEADER;
	private static final String ERROR_MESSAGE_TOP;
	private static final String ERROR_MESSAGE_BOTTOM;
	// Tab names
	private static final String LOAD_SAVE;
	private static final String OBJECTIVES;
	private static final String BACKGROUND;
	static
	{
		INITIAL_MISSION_HEADER = "MISSION PANEL";
		ERROR_MESSAGE_TOP = "Error: incorrect format for mission loaded from";
		ERROR_MESSAGE_BOTTOM = "Please try a different mission.";

		LOAD_SAVE = "Load New Mission";
		OBJECTIVES = "Objectives";
		BACKGROUND = "Mission";
	}

	private GeopodLabel m_missionNameLabel;

	private JPanel m_loadSavePanel;

	private JLabel m_missionErrorTop;
	private JLabel m_missionLocation;
	private JLabel m_missionErrorBottom;

	private JFileChooser m_fileChooser;

	private JTabbedPane m_tabbedPane;

	private ObjectivesPane m_objectivesPane;

	private Mission m_mission;

	private FlightDataRecorder m_flightRecorder;

	private SubjectImpl m_subjectImpl;

	/**
	 * Constructs a new Mission Panel whose mission's objectives use the given
	 * FlightDataRecorder to test if assessment conditions are met.
	 * 
	 * @param flightRecorder
	 */
	public MissionPanel (FlightDataRecorder flightRecorder)
	{
		m_subjectImpl = new SubjectImpl ();

		m_flightRecorder = flightRecorder;

		setupFileChooser ();

		setupPanelBackground ();

		addHeader ();

		addTabbedPane ();

		addCloseButton ();
	}

	private void setupFileChooser ()
	{
		m_fileChooser = new JFileChooser ();
		m_fileChooser.setCurrentDirectory (new File (""));
		m_fileChooser.setFileFilter (new GEOFileFilter ());
	}

	private void setupPanelBackground ()
	{
		this.setLayout (new MigLayout ("wrap 1, fill", "[align center]", ""));
		this.setBorder (BorderFactory.createStandardBorder ());

		super.setBackgroundPainter (PainterFactory.createStandardMattePainter (1045, 670));
	}

	private void addHeader ()
	{
		m_missionNameLabel = new GeopodLabel (INITIAL_MISSION_HEADER, JLabel.CENTER);
		Font font = UIConstants.GEOPOD_BANDY.deriveFont (Font.BOLD, UIConstants.TITLE_SIZE);
		m_missionNameLabel.setFont (font);
		// trick to make title have '...' when doesn't fit, but 
		// expand to show as much of title as possible if resize
		//m_missionNameLabel.setMinimumSize (new Dimension (1, 30));
		//m_missionNameLabel.setPreferredSize (new Dimension (2000, 30));
		this.add (m_missionNameLabel, "gaptop 8, gapbottom 5, gapleft 25, gapright 25, width 1:n:2000");
	}

	private void addTabbedPane ()
	{
		setUpTabbedPane ();

		super.add (m_tabbedPane, "gapright 15, gapleft 15, growpriox 200, growprioy 200");

		setUpLoadSavePanel ();

		addLoadSaveTab ();
	}

	private void setUpTabbedPane ()
	{
		m_tabbedPane = new JTabbedPane (SwingConstants.TOP);
		m_tabbedPane.setPreferredSize (new Dimension (2000, 2000));
		m_tabbedPane.setUI (new GeopodTabbedPaneUI ());
	}

	private void setUpLoadSavePanel ()
	{
		Font errorLabelFont = UIConstants.GEOPOD_VERDANA.deriveFont (UIConstants.BUTTON_FONT_SIZE);

		m_missionErrorTop = new JLabel (ERROR_MESSAGE_TOP, JLabel.CENTER);
		m_missionErrorTop.setFont (errorLabelFont);

		m_missionLocation = new JLabel ("", JLabel.CENTER);
		m_missionLocation.setFont (errorLabelFont);

		m_missionErrorBottom = new JLabel (ERROR_MESSAGE_BOTTOM, JLabel.CENTER);
		m_missionErrorBottom.setFont (errorLabelFont);

		JButton loadMissionButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
				UIConstants.GEOPOD_GREEN, false);
		loadMissionButton.setText ("LOAD MISSION");
		loadMissionButton.addActionListener (new LoadMissionButtonListener ());

		m_loadSavePanel = new JPanel (new MigLayout ("wrap 1, fillx, hidemode 3", "[align center]", ""));
		m_loadSavePanel.setBackground (UIConstants.GEOPOD_GREEN);
		m_loadSavePanel.add (m_missionErrorTop);
		m_loadSavePanel.add (m_missionLocation, "width 1:n:2000");
		m_loadSavePanel.add (m_missionErrorBottom);
		m_loadSavePanel.add (loadMissionButton);
	}

	private void setMissionTitle ()
	{
		String missionTitle = m_mission.getMissionTitle ();
		String missionTitleAllCaps = missionTitle.toUpperCase ();
		m_missionNameLabel.setText (missionTitleAllCaps);
		m_missionNameLabel.setToolTipText (missionTitle);
	}

	private void addBackgroundTab ()
	{
		String backgroundHtmlText = m_mission.getBackgroundText ();
		JEditorPane textPane = new JEditorPane ();

		textPane.setContentType ("text/html");
		textPane.setText (backgroundHtmlText);

		textPane.setEditable (false);
		textPane.addHyperlinkListener (new MissionHyperlinkListener ());

		JScrollPane scrollPane = new JScrollPane (textPane);

		m_tabbedPane.addTab (BACKGROUND, scrollPane);
	}

	private void addObjectivesTab ()
	{
		m_objectivesPane = new ObjectivesPane (m_mission, m_flightRecorder);

		m_tabbedPane.addTab (OBJECTIVES, m_objectivesPane);
	}

	private void addLoadSaveTab ()
	{
		m_missionErrorTop.setVisible (false);
		m_missionLocation.setVisible (false);
		m_missionErrorBottom.setVisible (false);
		m_tabbedPane.addTab (LOAD_SAVE, m_loadSavePanel);
	}

	private void loadMission (String missionLocation)
	{
		m_mission = new Mission (missionLocation);

		if (m_mission.initializationFailed ())
		{
			m_missionErrorTop.setVisible (true);
			m_missionLocation.setText (missionLocation);
			m_missionLocation.setVisible (true);
			m_missionErrorBottom.setVisible (true);
		}
		else
		{
			m_tabbedPane.removeAll ();

			setMissionTitle ();

			addBackgroundTab ();

			addObjectivesTab ();

			addLoadSaveTab ();

			this.updateUI ();
		}
	}

	private void addCloseButton ()
	{
		JButton closeButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
				UIConstants.GEOPOD_GREEN, false);
		closeButton.setText ("CLOSE");
		closeButton.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				setVisible (false);
				notifyObservers (GeopodEventId.MISSION_BUTTON_STATE_CHANGED);
			}
		});
		this.add (closeButton, "gaptop 5");
	}

	private boolean missionOpen ()
	{
		return (m_mission != null && !m_mission.initializationFailed ());
	}

	/**
	 * Toggles visibility, and if 1) Mission Panel will be set visible, 2) a
	 * mission is loaded and 3) a conditionsNotMetPanel will be displayed,
	 * rechecks the assessment to make sure the conditions not met panel is up
	 * to date, or, if conditions are met, the appropriate assessment is
	 * displayed instead.
	 */
	public void toggleVisibility ()
	{
		if (!this.isVisible () && missionOpen () && m_objectivesPane.conditionsNotMetPanelDisplayed ())
		{
			ConditionsNotMetPanel conditionsNotMetPanel = (ConditionsNotMetPanel) m_objectivesPane.getCurrentContent ();
			conditionsNotMetPanel.reCheckAssessment ();
		}
		this.setVisible (!this.isVisible ());
		notifyObservers (GeopodEventId.MISSION_BUTTON_STATE_CHANGED);
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
