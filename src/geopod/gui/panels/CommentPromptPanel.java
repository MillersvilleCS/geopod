package geopod.gui.panels;

import geopod.Geopod;
import geopod.constants.UIConstants;
import geopod.devices.FlightDataRecorder;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.components.BorderFactory;
import geopod.gui.components.ButtonFactory;
import geopod.gui.components.PainterFactory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;

import visad.Real;
import visad.georef.EarthLocation;

public class CommentPromptPanel
		extends JXPanel
		implements ActionListener, ISubject
{
	private static final long serialVersionUID = -7573801766135022171L;
	private static final String NOTE_LOCATION_HEADER;
	private static final String ENTER_BUTTON_TEXT;
	private static final String DEFAULT_COMMENT;
	static
	{
		NOTE_LOCATION_HEADER = "Please enter a comment for this location.";
		ENTER_BUTTON_TEXT = "SAVE PARAMETERS";
		DEFAULT_COMMENT = "<no comment>";
	}

	private Geopod m_geopod;
	private SubjectImpl m_subjectImpl;
	private NotedLocationsPanel m_notesPanel;
	private JTextField m_commentField;

	public CommentPromptPanel (Geopod geopod, NotedLocationsPanel notedLocationsPanel)
	{
		m_subjectImpl = new SubjectImpl ();

		// Flight Recorder listens for location noted events and, if recording enabled, records them
		FlightDataRecorder dataRecorder = geopod.getFlightRecorder ();
		this.addObserver (dataRecorder, GeopodEventId.LOCATION_NOTED);

		// store the Geopod so can get note location data
		m_geopod = geopod;
		// store the noted locations panel so can send it the data
		m_notesPanel = notedLocationsPanel;

		setUpPanel ();
	}

	private void setUpPanel ()
	{
		setupPanelBackground ();

		addHeaderLabel ();

		addCommentField ();

		addButtons ();
	}

	private void setupPanelBackground ()
	{
		this.setBorder (BorderFactory.createStandardBorder ());
		this.setLayout (new MigLayout ("align center", "", "[] 20 [] 20 []"));

		Color[] colors = { Color.DARK_GRAY, Color.LIGHT_GRAY, Color.LIGHT_GRAY, Color.DARK_GRAY };
		float[] fractions = { 0.0f, 0.2f, 0.8f, 1.0f };
		MattePainter painter = PainterFactory.createMattePainter (0, 0, 420, 160, fractions, colors);
		super.setBackgroundPainter (painter);
	}

	private void addHeaderLabel ()
	{
		JLabel headerLabel = new JLabel (NOTE_LOCATION_HEADER, JLabel.CENTER);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (Font.BOLD, 12.0f);
		headerLabel.setFont (font);
		this.add (headerLabel, "wrap, grow, center");
	}

	private void addCommentField ()
	{
		m_commentField = new JTextField ();
		m_commentField.setMinimumSize (new Dimension (1, 30));
		m_commentField.setPreferredSize (new Dimension (300, 30));
		m_commentField.addActionListener (this);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (12.0f);
		m_commentField.setFont (font);
		m_commentField.setActionCommand (ENTER_BUTTON_TEXT);

		this.setDefaultCommentText ();

		this.add (m_commentField, "wrap, center");
	}

	private void addButtons ()
	{
		String constraints = "alignx left, split 2, gapleft push, gapright 20";
		addButton (ENTER_BUTTON_TEXT, constraints);

		constraints = "alignx right, split 2, gapright push";
		addButton ("CANCEL", constraints);
	}

	private void addButton (String buttonName, String constraints)
	{
		JButton newButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE, UIConstants.GEOPOD_GREEN,
				false);
		newButton.setText (buttonName);
		newButton.addActionListener (this);
		newButton.setActionCommand (buttonName);
		this.add (newButton, constraints);
	}

	public void requestFocusInCommentField ()
	{
		m_commentField.requestFocusInWindow ();
	}

	private void setDefaultCommentText ()
	{
		m_commentField.setText (DEFAULT_COMMENT);
		// is recommended that use these two calls rather than the "select" method for text fields
		m_commentField.setCaretPosition (0);
		m_commentField.moveCaretPosition (DEFAULT_COMMENT.length ());
	}

	@Override
	public void actionPerformed (ActionEvent e)
	{
		if (e.getActionCommand ().equals (ENTER_BUTTON_TEXT))
		{
			// get the data to send to notes panel
			String comment = m_commentField.getText ();
			EarthLocation currentLocation = m_geopod.getEarthLocation ();
			Map<String, Real> parameters = m_geopod.getCurrentSensorValues ();

			m_notesPanel.appendLatestNote (comment, currentLocation, parameters);

			// notify the flight log that the location was noted
			this.notifyObservers (GeopodEventId.LOCATION_NOTED);
		}
		setDefaultCommentText ();
		this.setVisible (false);
		this.notifyObservers (GeopodEventId.ADDNOTE_BUTTON_STATE_CHANGED);
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
