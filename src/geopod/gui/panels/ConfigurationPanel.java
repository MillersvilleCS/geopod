package geopod.gui.panels;

import geopod.ConfigurationManager;
import geopod.constants.UIConstants;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.components.BorderFactory;
import geopod.gui.components.ButtonFactory;
import geopod.gui.components.GeopodButton;
import geopod.gui.components.PainterFactory;
import geopod.gui.styles.GeopodTabbedPaneUI;
import geopod.utils.debug.Debug;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXPanel;

/**
 * A panel to display configuration options to the user.
 * 
 */
public class ConfigurationPanel
		extends JXPanel
		implements ISubject
{
	private static final long serialVersionUID = 8250484409002431993L;
	private JTabbedPane m_tabbedPane;
	private JPanel m_controlPanel;
	private SubjectImpl m_subjectImpl;
	private Map<String, String> m_changedProperties;
	private Map<String, Object> m_widgetMap;

	/**
	 * Construct a default configuration panel.
	 */
	public ConfigurationPanel ()
	{

		m_changedProperties = new TreeMap<String, String> ();
		m_widgetMap = new TreeMap<String, Object> ();
		m_subjectImpl = new SubjectImpl ();
		setupPanelBackground ();
		addHeader ();
		addTabbedPane ();
		addCloseButtons ();
	}

	/**
	 * Set up with panel with a layout and a border
	 */
	private void setupPanelBackground ()
	{
		this.setLayout (new MigLayout ("wrap 3", "[align center]", ""));
		this.setBorder (BorderFactory.createStandardBorder ());

		super.setBackgroundPainter (PainterFactory.createStandardMattePainter (1045, 670));
	}

	/**
	 * Sets up a {@link TabbedPane}, adds it to this panel, sets up the controls
	 * and adds the control group to the panel's tab.
	 */
	private void addTabbedPane ()
	{
		setUpTabbedPane ();

		super.add (m_tabbedPane, "span, gapright 50, gapleft 50, growpriox 200, growprioy 200");

		setupControlPanel ();

		addControlTab ();
	}

	/**
	 * Sets the orientation, preferred size, and the UI of the tabbed pane.
	 */
	private void setUpTabbedPane ()
	{
		m_tabbedPane = new JTabbedPane (SwingConstants.TOP);
		m_tabbedPane.setPreferredSize (new Dimension (2000, 2000));
		m_tabbedPane.setUI (new GeopodTabbedPaneUI ());
	}

	/**
	 * Sets up the control panel with a layout, background, and adds the
	 * interface for each control
	 */
	private void setupControlPanel ()
	{
		m_controlPanel = new JPanel (new MigLayout ("wrap 2, fillx", "[align center]", ""));
		m_controlPanel.setBackground (UIConstants.GEOPOD_GREEN);

		addLimitRollControls ();
		addChartDomainControls ();
		addDebugControls ();
		addUserNameControls ();
		addFlightLogControls ();
		addClearFlightLogButton ();
	}

	/**
	 * A helper method used to make an option box from a {@link JComboBox} that
	 * corresponds to a particular {@link SettingName} setting.
	 * 
	 * @param key
	 *            - the setting that corresponding to this option box.
	 * @param labels
	 *            - the list of labels for the options
	 * @return - the option box
	 */
	private JComboBox makeOptionBox (String key, String[] labels)
	{
		JComboBox optionBox = new JComboBox (labels);
		optionBox.setLightWeightPopupEnabled (true);
		optionBox.setSelectedItem (ConfigurationManager.getProperty (key));
		final String settingKey = key;
		optionBox.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				String selection = (String) ((JComboBox) e.getSource ()).getSelectedItem ();
				m_changedProperties.put (settingKey, selection);
			}
		});
		m_widgetMap.put (settingKey, optionBox);
		return (optionBox);
	}

	private JTextField makeUserNameTextField (String key, String[] userNames)
	{
		JTextField userNameTextField = new JTextField (userNames[0]);
		final String settingKey = key;
		userNameTextField.addKeyListener (new KeyAdapter ()
		{
			@Override
			public void keyReleased (KeyEvent e)
			{
				String newUserName = (String) ((JTextField) e.getSource ()).getText ();
				m_changedProperties.put (settingKey, newUserName);
			}
		});
		m_widgetMap.put (settingKey, userNameTextField);
		return (userNameTextField);
	}

	/**
	 * Adds the option UI corresponding to the Limit Roll While Flying setting.
	 */
	private void addLimitRollControls ()
	{
		String limitRollKey = ConfigurationManager.DisableRoll;
		m_controlPanel.add (new JLabel (limitRollKey));
		JPanel optionPanel = makeOptions (limitRollKey, ConfigurationManager.getExpectablePropertyValues (limitRollKey));
		m_controlPanel.add (optionPanel);
	}

	/**
	 * Adds the option UI corresponding to the user name setting.
	 */
	private void addUserNameControls ()
	{
		String userNameKey = ConfigurationManager.UserName;
		m_controlPanel.add (new JLabel (userNameKey));
		JTextField userNameField = makeUserNameTextField (userNameKey, ConfigurationManager.getExpectablePropertyValues (userNameKey));
		m_controlPanel.add (userNameField, "width 120");
	}

	/**
	 * Adds the option UI corresponding to the chart domain setting.
	 */
	private void addChartDomainControls ()
	{
		String chartDomainKey = ConfigurationManager.ChartDomainUnit;
		m_controlPanel.add (new JLabel (chartDomainKey));
		JComboBox optionBox = makeOptionBox (chartDomainKey, ConfigurationManager.getExpectablePropertyValues (chartDomainKey));
		m_controlPanel.add (optionBox);
	}

	/**
	 * Adds the option UI corresponding to the debug setting.
	 */
	private void addDebugControls ()
	{
		String debugKey = ConfigurationManager.Debug;
		m_controlPanel.add (new JLabel (debugKey));
		m_controlPanel.add (makeOptions (debugKey, ConfigurationManager.getExpectablePropertyValues (debugKey)));
	}

	/**
	 * Adds the option UI that determines if the users want to save the flight
	 * log on program exit.
	 */
	private void addFlightLogControls ()
	{
		String flightLogKey = ConfigurationManager.RecordFlightPath;
		m_controlPanel.add (new JLabel (flightLogKey));
		m_controlPanel.add (makeOptions (flightLogKey, ConfigurationManager.getExpectablePropertyValues (flightLogKey)));
	}

	/**
	 * Adds the option UI to allow users to clear the contents of the flight
	 * log.
	 */
	private void addClearFlightLogButton ()
	{
		GeopodButton clearButton = ButtonFactory.createGradientButton (UIConstants.CONTENT_FONT_SIZE,
				UIConstants.GEOPOD_GREEN, false);
		clearButton.setText ("CLEAR");
		clearButton.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				// Resetting the flight log is the same as clearing it
				notifyObservers (GeopodEventId.REQUEST_FLIGHT_LOG_RESET);
			}
		});
		m_controlPanel.add (new JLabel ("Clear Flight Log"));
		m_controlPanel.add (clearButton);
	}

	/**
	 * A general helper method that takes a {@link SettingName}
	 * <code>setting</code> and an array of {@link String} labels that are the
	 * user options corresponding to <code>setting</code>. It returns the panel
	 * that the options are attached to.
	 * 
	 * @param key
	 *            - the setting
	 * @param labels
	 *            - the array of labels
	 * @return - the panel the option UI is attached to.
	 */
	private JPanel makeOptions (String key, String[] labels)
	{
		JPanel optionPanel = makeOptionPanel ();
		ArrayList<JRadioButton> optionList = makeOptionButtons (labels);
		addActionListenerToOptions (optionList, key);
		setSelectedOption (optionList, key);
		m_widgetMap.put (key, optionList);
		addOptionsToOptionPanel (optionPanel, optionList);

		return (optionPanel);
	}

	/**
	 * Set the UI for a panel that will hold the options corresponding to a
	 * setting.
	 * 
	 * @return - the panel
	 */
	private JPanel makeOptionPanel ()
	{
		JPanel menu = new JPanel ();
		menu.setBackground (UIConstants.GEOPOD_GREEN);
		return (menu);
	}

	/**
	 * Makes the buttons that allow users to select options.
	 * 
	 * @param labels
	 *            - the array of labels used to set the text of the option
	 *            buttons.
	 * @return - an array list of buttons.
	 */
	private ArrayList<JRadioButton> makeOptionButtons (String[] labels)
	{
		ButtonGroup group = new ButtonGroup ();

		ArrayList<JRadioButton> radioButtonList = new ArrayList<JRadioButton> ();
		for (String label : labels)
		{
			JRadioButton button = new JRadioButton (label);
			button.setBackground (UIConstants.GEOPOD_GREEN);
			radioButtonList.add (button);
			group.add (button);
		}
		return (radioButtonList);
	}

	/**
	 * Determine which option should be selected by examining the
	 * {@link SettingName} key and an {@link ArrayList} of option buttons.
	 * 
	 * @param buttonList
	 *            - list of option buttons
	 * @param key
	 *            - the setting corresponding to the buttons
	 */
	private void setSelectedOption (ArrayList<JRadioButton> buttonList, String key)
	{
		for (JRadioButton b : buttonList)
		{
			String buttonLabel = b.getText ();
			if (buttonLabel.equals (ConfigurationManager.getProperty (key)))
			{
				Debug.printf ("Config: %s is selected.\n", key);
				b.setSelected (true);
				return;
			}
		}
	}

	/**
	 * Adds a default listener to a list of options. This causes a property
	 * changed event to fire when an option is selected. This causes the
	 * corresponding setting value to change.
	 * 
	 * @param buttonList
	 *            - the list of buttons
	 * @param key
	 *            - the setting corresponding to the buttons
	 */
	private void addActionListenerToOptions (ArrayList<JRadioButton> buttonList, String key)
	{
		final String settingKey = key;
		for (JRadioButton b : buttonList)
		{
			b.addActionListener (new ActionListener ()
			{
				@Override
				public void actionPerformed (ActionEvent e)
				{
					String selection = (String) ((JRadioButton) e.getSource ()).getText ();
					m_changedProperties.put (settingKey, selection);
				}
			});

		}
	}

	/**
	 * Adds the list of options to the option panel.
	 * 
	 * @param optionPanel
	 * @param list
	 */
	private void addOptionsToOptionPanel (JPanel optionPanel, ArrayList<JRadioButton> list)
	{
		for (JRadioButton b : list)
		{
			optionPanel.add (b);
		}
	}

	private void addControlTab ()
	{
		m_tabbedPane.addTab ("Controls", m_controlPanel);
	}

	private void addCloseButtons ()
	{
		JPanel buttonPanel = new JPanel ();
		buttonPanel.setOpaque (false);

		JButton saveButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
				UIConstants.GEOPOD_GREEN, false);
		saveButton.setText ("SAVE");
		saveButton.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				ConfigurationManager.setProperties (m_changedProperties);
				setVisible (false);
				notifyObservers (GeopodEventId.CONFIG_BUTTON_STATE_CHANGED);
			}
		});
		buttonPanel.add (saveButton);

		JButton closeButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
				UIConstants.GEOPOD_GREEN, false);
		closeButton.setText ("CANCEL");
		closeButton.addActionListener (new ActionListener ()

		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				setVisible (false);
				notifyObservers (GeopodEventId.CONFIG_BUTTON_STATE_CHANGED);
			}
		});
		buttonPanel.add (closeButton);

		this.add (buttonPanel, "span");
	}

	private void addHeader ()
	{
		JLabel headerLabel = new JLabel ("SETTINGS", JLabel.CENTER);
		Font font = UIConstants.GEOPOD_BANDY.deriveFont (Font.BOLD, UIConstants.TITLE_SIZE);
		headerLabel.setFont (font);
		this.add (headerLabel, "span");
	}

	@Override
	public void setVisible (boolean isVisible)
	{
		super.setVisible (isVisible);
		for (Map.Entry<String, String[]> entry : ConfigurationManager.getExpectableValueEntrySet ())
		{
			String property = entry.getKey ();
			Object option = m_widgetMap.get (property);
			if (option instanceof JComboBox)
			{
				JComboBox optionBox = (JComboBox) option;
				optionBox.setSelectedItem (ConfigurationManager.getProperty (property));
			}
			else if (option instanceof JTextField)
			{
				JTextField userNameField = (JTextField) option;
				userNameField.setText (ConfigurationManager.getProperty (property));
			}
			else
			{
				// If we are not getting a comboBox widget, we are getting a list of radio buttons
				// If we need to make drastic changes, consider using an interface and 
				//   writing specialized classes. The complexity is not needed, now.

				ArrayList<JRadioButton> buttonList = (ArrayList<JRadioButton>) option;
				setSelectedOption (buttonList, property);
			}

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
}
