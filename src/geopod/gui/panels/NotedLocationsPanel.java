package geopod.gui.panels;

import geopod.constants.UIConstants;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.components.BorderFactory;
import geopod.gui.components.ButtonFactory;
import geopod.gui.components.PainterFactory;
import geopod.utils.debug.Debug;
import geopod.utils.math.LatLonAltValidator;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;

import visad.CommonUnit;
import visad.Real;
import visad.georef.EarthLocation;

/**
 * This class creates the notepad interface and functionality.
 * 
 * @author Geopod Team
 * 
 */
public class NotedLocationsPanel
		extends JXPanel
		implements ActionListener, ISubject
{
	private static final long serialVersionUID = -7631665305184581398L;
	private static final String DECIMAL_FORMAT;
	private static final String NOTED_LOCATIONS_HEADER;
	private static final String INTRODUCTORY_LINE;
	private static final String FILE_LINE_HEADER;
	private static final String NOT_SAVED;
	private static final String NEW_LINE;
	private static final String NO_LOCATIONS_NOTED;
	private static final String LOAD_NEW_FILE_WARNING;
	private static final String LOAD_FILE_QUESTION;
	private static final String SAVE_FILE_QUESTION;
	private static final String NEW_NOTES_QUESTION;
	private static final String FILE_LOADED;
	private static final String FILE_NOT_LOADED;
	private static final String FILE_SAVED;
	private static final String FILE_NOT_SAVED;
	private static final String ACCEPTED_EXTENTION;
	static
	{
		DECIMAL_FORMAT = "0.##";

		NOTED_LOCATIONS_HEADER = "NOTED LOCATIONS";
		INTRODUCTORY_LINE = "You have noted the following locations:";

		FILE_LINE_HEADER = "File: ";
		NOT_SAVED = "not saved";

		NEW_LINE = "\n";

		NO_LOCATIONS_NOTED = "No noted locations to display.";
		LOAD_NEW_FILE_WARNING = "Warning: Unless the current document has been saved, loading a new file will delete it.";
		LOAD_FILE_QUESTION = "Loading a new file will overwrite your current content. \nDo you want to load this file anyway?";
		SAVE_FILE_QUESTION = "Saving your current document to this file will overwrite the file's contents. \nDo you want to save to this file anyway?";
		NEW_NOTES_QUESTION = "Creating a new blank notes page will overwrite your current content. \nDo you want to save your current notes before proceeding?";
		FILE_LOADED = "File has been loaded.";
		FILE_NOT_LOADED = "Nothing loaded";
		FILE_SAVED = "File has been saved";
		FILE_NOT_SAVED = "Nothing saved";

		ACCEPTED_EXTENTION = ".txt";
	}
	private String m_currentFileName;
	private String m_lastSavedContents;
	private int m_numNotedLocations;

	private JLabel m_fileLine;
	private JTextArea m_textArea;
	private JTextField m_statusLine;

	private DecimalFormat m_formatter;
	private JFileChooser m_fileChooser;

	private SubjectImpl m_subjectImpl;

	public NotedLocationsPanel ()
	{
		m_subjectImpl = new SubjectImpl ();

		m_formatter = new DecimalFormat (DECIMAL_FORMAT);

		setupPanelBackground ();

		setUpFileChooser ();

		createAndAddComponents ();

		m_currentFileName = "";
		m_lastSavedContents = "";

		m_numNotedLocations = 0;
	}

	private void setupPanelBackground ()
	{
		this.setLayout (new MigLayout ("wrap 1", "", "[][]0[]0[]0[]"));

		this.setBorder (BorderFactory.createStandardBorder ());

		MattePainter mattePainter = PainterFactory.createStandardGradientBackground ();

		super.setBackgroundPainter (mattePainter);
	}

	private void setUpFileChooser ()
	{
		m_fileChooser = new JFileChooser ();
		m_fileChooser.setFileFilter (new TextFileFilter ());
		m_fileChooser.setAcceptAllFileFilterUsed (false);
	}

	private void createAndAddComponents ()
	{
		JLabel headerLabel = createHeader ();
		this.add (headerLabel, "gapbottom 8");

		m_fileLine = createFileLine ();
		this.add (m_fileLine, "gapright 25, gapleft 26");

		JScrollPane scrollPane = createScrollPane ();
		this.add (scrollPane, "growpriox 200, growprioy 200, gapright 25, gapleft 25");

		m_statusLine = createStatusLine ();
		this.add (m_statusLine, "gapright 25, gapleft 26");

		addButtons ();
	}

	private JLabel createHeader ()
	{
		JLabel headerLabel = new JLabel (NOTED_LOCATIONS_HEADER, JLabel.CENTER);
		Font font = UIConstants.GEOPOD_BANDY.deriveFont (Font.BOLD, UIConstants.TITLE_SIZE);
		headerLabel.setFont (font);
		headerLabel.setPreferredSize (new Dimension (2000, 50));

		return (headerLabel);
	}

	private JLabel createFileLine ()
	{
		JLabel fileLine = new JLabel (FILE_LINE_HEADER + NOT_SAVED);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (Font.BOLD, 14.0f);
		fileLine.setFont (font);
		// this makes it append long file names to the width of the note page area
		fileLine.setMinimumSize (new Dimension (1, 22));
		// this makes it grow horizontally (expand length of name shown) when resized
		fileLine.setPreferredSize (new Dimension (2000, 22));
		fileLine.setForeground (UIConstants.GEOPOD_GREEN);

		return (fileLine);
	}

	private JScrollPane createScrollPane ()
	{
		JScrollPane scrollPane = new JScrollPane ();
		scrollPane.setPreferredSize (new Dimension (2000, 2000));

		setUpTextArea ();

		scrollPane.setViewportView (m_textArea);

		return (scrollPane);

	}

	private void setUpTextArea ()
	{
		m_textArea = new JTextArea ();
		m_textArea.setEditable (true);
		m_textArea.setText (INTRODUCTORY_LINE);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (12.0f);
		m_textArea.setFont (font);
		m_textArea.setBackground (UIConstants.GEOPOD_GREEN);
	}

	private JTextField createStatusLine ()
	{
		JTextField statusLine = new JTextField (NO_LOCATIONS_NOTED);
		statusLine.setEditable (false);
		statusLine.setPreferredSize (new Dimension (2000, 20));
		statusLine.setBackground (UIConstants.GEOPOD_GREEN);
		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (11.0f);
		statusLine.setFont (font);

		return (statusLine);
	}

	private void addButtons ()
	{
		String buttonConstraints = "split 4, align center, gaptop 10, gapbottom 3, gapright push";

		JButton loadButton = createButton ("LOAD");
		this.add (loadButton, buttonConstraints + ", gapleft push");

		JButton newButton = createButton ("N E W");
		this.add (newButton, buttonConstraints);

		JButton saveButton = createButton ("SAVE");
		this.add (saveButton, buttonConstraints);

		JButton closeButton = createButton ("CLOSE");
		this.add (closeButton, buttonConstraints);
	}

	private JButton createButton (String buttonName)
	{
		JButton newButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE, UIConstants.GEOPOD_GREEN,
				false);
		newButton.setText (buttonName);
		newButton.addActionListener (this);
		newButton.setActionCommand (buttonName);

		return (newButton);
	}

	/**
	 * Adds the information about a noted location to the notepad.
	 * 
	 * @param comment
	 *            - string user entered to associate with the noted location
	 * @param lat
	 *            - {@link Real} representing latitude where noted
	 * @param lon
	 *            - {@link Real} representing longitude where noted
	 * @param alt
	 *            - {@link Real} representing altitude where noted
	 * @param parametersMap
	 *            - the sensor values displayed when the location was noted
	 */
	public void appendLatestNote (String comment, EarthLocation currentLocation, Map<String, Real> parametersMap)
	{
		// Use a StringBuilder to avoid repeated allocation of new String's
		StringBuilder stringRepresentation = new StringBuilder (NEW_LINE + NEW_LINE);

		String commentLine = "Comment: " + comment + NEW_LINE;
		stringRepresentation.append (commentLine);

		String location = extractLatLonAlt (currentLocation);
		stringRepresentation.append (location);

		String parameters = extractStringParameters (parametersMap);
		stringRepresentation.append (parameters);

		m_textArea.append (stringRepresentation.toString ());

		m_numNotedLocations++;
		updateStatusLine ();
	}

	private String extractLatLonAlt (EarthLocation currentLocation)
	{
		StringBuilder location = new StringBuilder ();

		double latitude = LatLonAltValidator.getLatitudeValue (currentLocation);
		String latitudeTwoDecimalPlaces = m_formatter.format (latitude);
		String latitudeUnits = CommonUnit.degree.getIdentifier ();
		String lat = "Lat: " + latitudeTwoDecimalPlaces + " " + latitudeUnits + ", ";
		location.append (lat);

		double longitude = LatLonAltValidator.getLongitudeValue (currentLocation);
		String longitudeTwoDecimalPlaces = m_formatter.format (longitude);
		String longitudeUnits = CommonUnit.degree.getIdentifier ();
		String lon = "Lon: " + longitudeTwoDecimalPlaces + " " + longitudeUnits + ", ";
		location.append (lon);

		double altitudeInMeters = LatLonAltValidator.getAltitudeValue (currentLocation);
		String altitudeTwoDecimalPlaces = m_formatter.format (altitudeInMeters);
		String altitudeUnits = CommonUnit.meter.getIdentifier ();
		String alt = "Alt: " + altitudeTwoDecimalPlaces + " " + altitudeUnits + NEW_LINE;
		location.append (alt);

		return (location.toString ());
	}

	private String extractStringParameters (Map<String, Real> parameters)
	{
		StringBuilder parameterList = new StringBuilder ("");

		Set<String> parameterNames = parameters.keySet ();
		List<String> parameterNamesList = new ArrayList<String> (parameterNames);
		for (int i = 0; i < parameterNamesList.size (); ++i)
		{
			String parameterName = parameterNamesList.get (i);
			Real parameterReal = parameters.get (parameterName);
			String parameterText = "no value available";
			if (!parameterReal.isMissing ())
			{
				double parameterDoubleValue = parameterReal.getValue ();
				String parameterValue = m_formatter.format (parameterDoubleValue);
				String parameterUnit = parameterReal.getUnit ().toString ();
				parameterText = parameterValue + " " + parameterUnit;
			}

			String parameter = parameterName + ": " + parameterText + NEW_LINE;
			parameterList.append (parameter);
		}

		return (parameterList.toString ());
	}

	private void updateStatusLine ()
	{
		String notification = "You have noted " + m_numNotedLocations + " location(s).";
		updateStatusLine (notification);
	}

	private void updateStatusLine (String lineToWrite)
	{
		m_statusLine.setText (lineToWrite);
	}

	private void updateFileLine (String lineToWrite)
	{
		m_fileLine.setText (FILE_LINE_HEADER + lineToWrite);
	}

	@Override
	public void actionPerformed (ActionEvent e)
	{
		if (e.getActionCommand ().equals ("SAVE"))
		{
			processSaveCommand ();
		}
		else if (e.getActionCommand ().equals ("LOAD"))
		{
			processLoadCommand ();
		}
		else if (e.getActionCommand ().equals ("N E W"))
		{
			processNewCommand ();
		}
		else if (e.getActionCommand ().equals ("CLOSE"))
		{
			this.setVisible (false);
			this.notifyObservers (GeopodEventId.NOTEPAD_BUTTON_STATE_CHANGED);
		}
	}

	private void processSaveCommand ()
	{
		// these strings will be written to the file and status lines, unless 
		// they are changed later, when the file is saved.
		String statusLineText = FILE_NOT_SAVED;
		String fileLineText = NOT_SAVED;

		if (m_currentFileName.isEmpty ())
		{
			// file has not been saved yet, so have to prompt user for place to save
			int responseToFileChooser = m_fileChooser.showSaveDialog (this);
			if (responseToFileChooser == JFileChooser.APPROVE_OPTION)
			{
				File fileToSaveTo = m_fileChooser.getSelectedFile ();

				// add appropriate extension, if there the path does not have one already
				String absoluteFilePath = fileToSaveTo.getAbsolutePath ();
				if (!endsWithExtension (fileToSaveTo, ACCEPTED_EXTENTION))
				{
					absoluteFilePath += ACCEPTED_EXTENTION;
					fileToSaveTo = new File (absoluteFilePath);
				}

				// if user chooses to save to an existing file, warn that content will be overwritten
				int responseToWarning = JOptionPane.YES_OPTION;
				if (fileToSaveTo.exists ())
				{
					responseToWarning = JOptionPane.showOptionDialog (this, SAVE_FILE_QUESTION, "Warning",
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				}

				if (responseToWarning == JOptionPane.YES_OPTION)
				{
					// write to file and update appropriate values
					writeContentsToFile (fileToSaveTo);
					m_currentFileName = absoluteFilePath;
					statusLineText = FILE_SAVED;
					fileLineText = fileToSaveTo.getName ();
				}
			}
		}
		else
		{
			// user has already saved to a file, will just update that file
			File file = new File (m_currentFileName);
			writeContentsToFile (file);
			// update appropriate values
			statusLineText = FILE_SAVED;
			fileLineText = file.getName ();
		}

		// update file and status lines
		updateFileLine (fileLineText);
		updateStatusLine (statusLineText);
	}

	private void writeContentsToFile (File fileToWriteTo)
	{
		String contents = m_textArea.getText ();
		try
		{
			PrintWriter outputStream = new PrintWriter (new BufferedWriter (new FileWriter (fileToWriteTo)));
			outputStream.print (contents);
			outputStream.flush ();
			outputStream.close ();
		}
		catch (Exception e1)
		{
			if (Debug.isDebuggingOn ())
			{
				e1.printStackTrace ();
			}
			updateStatusLine (FILE_NOT_SAVED);
		}

		// every time a file is written, take a 'snapshot' of content at that time, 
		// so can determine later if there are new changes that have not been saved
		m_lastSavedContents = contents;
	}

	private void processLoadCommand ()
	{
		updateStatusLine (LOAD_NEW_FILE_WARNING);
		String statusLineText = FILE_NOT_LOADED;

		int responseToFileChooser = m_fileChooser.showOpenDialog (this);
		if (responseToFileChooser == JFileChooser.APPROVE_OPTION)
		{
			int responseToWarning = JOptionPane.YES_OPTION;
			if (!currentContentSaved ())
			{
				responseToWarning = JOptionPane.showOptionDialog (this, LOAD_FILE_QUESTION, "Warning",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
			}
			if (responseToWarning == JOptionPane.YES_OPTION)
			{
				File fileToOpen = m_fileChooser.getSelectedFile ();
				if (fileToOpen.exists ())
				{
					readFromFile (fileToOpen);
					m_currentFileName = fileToOpen.getAbsolutePath ();
					updateFileLine (fileToOpen.getName ());
					m_numNotedLocations = 0;
					statusLineText = FILE_LOADED;
				}
			}
		}
		updateStatusLine (statusLineText);
	}

	private void readFromFile (File fileToOpen)
	{
		StringBuilder fileContents = new StringBuilder ();
		try
		{
			Scanner fileReader = new Scanner (fileToOpen);
			while (fileReader.hasNext ())
			{
				String nextLine = fileReader.nextLine () + NEW_LINE;
				fileContents.append (nextLine);
			}
		}
		catch (FileNotFoundException e1)
		{
			if (Debug.isDebuggingOn ())
			{
				e1.printStackTrace ();
			}
			updateStatusLine (FILE_NOT_LOADED);
		}

		String newContents = fileContents.toString ();
		m_textArea.setText (newContents);
		m_textArea.setCaretPosition (0);
		m_lastSavedContents = newContents;
	}

	private void processNewCommand ()
	{
		int responseToQuestion = JOptionPane.NO_OPTION;
		if (!currentContentSaved ())
		{
			responseToQuestion = JOptionPane.showOptionDialog (this, NEW_NOTES_QUESTION, "Warning",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
		}

		if (responseToQuestion == JOptionPane.YES_OPTION)
		{
			// user responded yes, need to save old notes
			processSaveCommand ();
		}

		if (responseToQuestion != JOptionPane.CANCEL_OPTION && responseToQuestion != JOptionPane.CLOSED_OPTION)
		{
			// user responded either yes or no, either way, want to load new notes page 
			m_currentFileName = "";
			m_lastSavedContents = "";
			m_textArea.setText (INTRODUCTORY_LINE);
			m_numNotedLocations = 0;
			updateFileLine (NOT_SAVED);
			updateStatusLine ();
		}
	}

	public boolean currentContentSaved ()
	{
		String contents = m_textArea.getText ();
		return (contents.equals (m_lastSavedContents) || contents.equals (INTRODUCTORY_LINE));
	}

	private class TextFileFilter
			extends FileFilter
	{

		@Override
		public boolean accept (File f)
		{
			if (f.isDirectory ())
			{
				return (true);
			}
			boolean endsWithCorrectExtension = endsWithExtension (f, ACCEPTED_EXTENTION);
			return (endsWithCorrectExtension);
		}

		@Override
		public String getDescription ()
		{
			return (ACCEPTED_EXTENTION + " files only");
		}

	}

	private boolean endsWithExtension (File file, String extension)
	{
		String fullFileName = file.getName ();
		fullFileName = fullFileName.toLowerCase ();
		boolean endsWithExtension = fullFileName.endsWith (extension);
		return (endsWithExtension);
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
