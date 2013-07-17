package geopod;

import geopod.utils.FileLoadingUtility;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class handles loading configuration options from disk and providing that
 * information to other classes.
 * 
 */
public class ConfigurationManager
{
	private static String m_defaultPropertiesPath = "//Resources/Config/geopod.default.properties";
	private static String m_userPropertiesPath = "";

	private static Properties m_properties;
	private static final List<String> m_expectedProperties;
	private static Map<String, Boolean> m_enabled;
	private static Map<String, String[]> m_availableValues;

	// String versions of properties
	public static final String DisableRoll;
	public static final String ChartDomainUnit;
	public static final String Debug;
	public static final String UserName;
	public static final String RecordFlightPath;

	/**
	 * The property support field provides fully implemented property change
	 * methods. Properties and their values are interpreted as
	 * <code>Strings</code>. This field allows us to implement only the
	 * necessary parts of a property changed user interface.
	 */
	private static PropertyChangeSupport m_propertySupport;

	static
	{
		// The properties we rely on. The properties file
		// must contain these keys.
		DisableRoll = "Disable Roll";
		ChartDomainUnit = "Chart Domain Unit";
		Debug = "Debug";
		UserName = "User Name";
		RecordFlightPath = "Record Flight Path";
		m_expectedProperties = Arrays.asList (DisableRoll, ChartDomainUnit, Debug, UserName, RecordFlightPath);

		m_availableValues = new TreeMap<String, String[]> ();
		m_availableValues.put (ConfigurationManager.DisableRoll, new String[] { "On", "Off" });
		m_availableValues.put (ConfigurationManager.ChartDomainUnit, new String[] { "Altitude", "Pressure" });
		m_availableValues.put (ConfigurationManager.Debug, new String[] { "On", "Off" });
		m_availableValues.put (ConfigurationManager.UserName, new String[] { "" });
		m_availableValues.put (ConfigurationManager.RecordFlightPath, new String[] { "Yes", "No" });

		m_properties = new Properties ();
		m_propertySupport = new PropertyChangeSupport (ConfigurationManager.class);

		m_enabled = new TreeMap<String, Boolean> ();
		m_enabled.put ("True", true);
		m_enabled.put ("On", true);
		m_enabled.put ("Yes", true);
		m_enabled.put ("Ok", true);
	}

	private ConfigurationManager ()
	{
		// Static class
	}

	/**
	 * Associates a property with the given listener.
	 * 
	 * @param property
	 *            - the String representation of the property to add.
	 * @param listener
	 *            - the {@link PropertyChangeListener} listener
	 */
	public static void addPropertyChangeListener (String property, PropertyChangeListener listener)
	{
		m_propertySupport.addPropertyChangeListener (property, listener);
	}

	/**
	 * Stop listening to a property and remove its listener
	 * 
	 * @param property
	 *            - the <code>String</code> representation of the property to
	 *            remove
	 * @param listener
	 *            - the {@link PropertyChangeListener} listener to remove
	 */
	public static void removePropertyChangeListener (String property, PropertyChangeListener listener)
	{
		m_propertySupport.removePropertyChangeListener (property, listener);
	}

	private static void firePropertyChange (String property, String newValue)
	{
		m_propertySupport.firePropertyChange (property, null, newValue);
	}

	/**
	 * Load both default and custom preferences.
	 * 
	 * @param userHomePath
	 *            - path to the users IDV home path.
	 */
	public static void loadPreferences (String userHomePath)
	{
		// Load user properties
		m_userPropertiesPath = userHomePath + File.separator + "plugins" + File.separator + "geopod.properties";
		loadUserProperties ();
		validateLoadedProperties ();
	}

	/**
	 * Save user preferences to the static user properties path that is set when
	 * the properties are loaded.user
	 */
	public static void savePreferences ()
	{
		// Save to user properties file
		try
		{
			OutputStream os = new FileOutputStream (m_userPropertiesPath);
			m_properties.store (os, "Custom user properties for Geopod plugin.");
			os.close ();
			System.out.println ("Saved user preferences.");
		}
		catch (Exception e)
		{
			// Unable to load properties file.
			System.err.println ("Unable to save user properties to: " + m_userPropertiesPath);
			e.printStackTrace ();
		}
	}

	/**
	 * Reset properties to defaults.
	 */
	public static void resetPreferences ()
	{
		m_properties.clear ();
		loadDefaultProperties ();
	}

	/**
	 * Set a property with a value
	 * 
	 * @param property
	 *            - the <code>String</code> representation of the property whose
	 *            value we want to set
	 * @param value
	 *            - the value the property should have
	 */
	public static void setProperty (String property, String value)
	{
		m_properties.setProperty (property, value);
		firePropertyChange (property, value);
	}

	/**
	 * Get the specified property's value
	 * 
	 * @param property
	 *            - the <code>String</code> representation of the propery whose
	 *            value we want to get
	 * @return the property's value as a String
	 */
	public static String getProperty (String property)
	{
		String value = m_properties.getProperty (property);
		return (value);
	}

	/**
	 * Load default properties from the static default properties path.
	 */
	private static void loadDefaultProperties ()
	{
		try
		{
			InputStream defaultProperties = FileLoadingUtility.loadFileStream (m_defaultPropertiesPath);
			m_properties.load (defaultProperties);
			defaultProperties.close ();
		}
		catch (IOException e)
		{
			// Unable to load properties file.
			System.err.println ("Default properties file not found at: " + m_defaultPropertiesPath);
			e.printStackTrace ();
		}
	}

	/**
	 * Load custom properties from the static user properties path.
	 */
	private static void loadUserProperties ()
	{
		try
		{
			FileInputStream userProperties = new FileInputStream (m_userPropertiesPath);
			m_properties.load (userProperties);
			userProperties.close ();
		}
		catch (IOException e)
		{
			// Unable to load properties file.
			System.out.println ("User properties file not found at: " + m_userPropertiesPath);
			System.out.println ("Using defaults.");
			loadDefaultProperties ();
			if (!(e instanceof FileNotFoundException))
			{
				e.printStackTrace ();
			}
		}
	}

	/**
	 * Check if a boolean setting is enabled or not.
	 * 
	 * @param name
	 *            - the {@link SettingName setting} to check.
	 * @return - true if the setting is enabled.
	 */
	public static boolean isEnabled (String property)
	{
		String value = m_properties.getProperty (property);
		boolean isEnabled = (value != null) ? m_enabled.containsKey (value) : false;

		return (isEnabled);
	}

	public static String[] getExpectablePropertyValues (String property)
	{
		return (m_availableValues.get (property));
	}

	public static boolean isPropertyValueExceptable (String property, String value)
	{
		String[] options = getExpectablePropertyValues (property);
		boolean optionFound = false;

		for (String opt : options)
		{
			if (opt.equals (value))
			{
				optionFound = true;
				break;
			}
		}

		return (optionFound);
	}

	// TODO: temperary hack, please remove once we implement the
	// Option interface -- NTO

	public static Set<Entry<String, String[]>> getExpectableValueEntrySet ()
	{
		return (m_availableValues.entrySet ());
	}

	/**
	 * Set our properties from the map of properties and their values.
	 * 
	 * @param m_changedProperties
	 *            - map of <code>Strings</code> representing properties and
	 *            their values
	 */
	public static void setProperties (Map<String, String> m_changedProperties)
	{
		for (Map.Entry<String, String> entry : m_changedProperties.entrySet ())
		{
			setProperty (entry.getKey (), entry.getValue ());
		}
	}

	/**
	 * Iterate through the list of expected properties and make sure all of them
	 * are loaded.
	 */
	private static void validateLoadedProperties ()
	{
		for (String property : m_expectedProperties)
		{
			// For each expected property, attempt get its value from the 
			// loaded properties
			String value = getProperty (property);
			// Make sure the expected property has a non-null value
			// and the property is exceptable
			if (value == null || !isPropertyValueExceptable (property, value))
			{
				System.err.println ("User properties file is corrupted. ");
				System.err.println ("Loading default properties");

				File propertiesFile = new File (m_userPropertiesPath);
				// The current user properties file is worthless, delete it
				// to avoid future issues.
				propertiesFile.delete ();
				resetPreferences ();
				break;
			}
		}
	}
}
