package geopod.constants;

import static geopod.constants.parameters.IDV4ParameterConstants.*;
import static geopod.constants.parameters.SimpleParameterNameConstants.*;
import geopod.utils.FileLoadingUtility;

import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class containing constants to be used by the UI. Contains default colors,
 * fonts, and standard parameter abbreviations.
 */
public class UIConstants
{
	// User interface constants
	public static final Color GEOPOD_GREEN;
	public static final Color GEOPOD_DARK_GREEN;
	public static final Color GEOPOD_RED;
	public static final Color GEOPOD_NOTE_COLOR;

	// Fonts
	public static final Font GEOPOD_BANDY;
	public static final Font GEOPOD_VERDANA;

	// Font Sizes
	public static final float TITLE_SIZE;
	public static final float SUBTITLE_SIZE;
	public static final float BUTTON_FONT_SIZE;
	public static final float CONTENT_FONT_SIZE;

	/**
	 * A mapping from <tt>parameterName -> parameterAbbreviation</tt>.
	 */
	public static final Map<String, String> SIMPLE_NAME_MAP;

	static
	{
		GEOPOD_GREEN = new Color (0xe4ffc6);
		GEOPOD_DARK_GREEN = new Color (0x83d824);
		GEOPOD_RED = new Color (0xff4e4e);
		GEOPOD_NOTE_COLOR = new Color (0xfff2c5);
		GEOPOD_BANDY = FileLoadingUtility.loadFont (Font.TRUETYPE_FONT, "//Resources/Fonts/CW_BANDY.TTF");
		GEOPOD_VERDANA = FileLoadingUtility.loadFont (Font.TRUETYPE_FONT, "//Resources/Fonts/VERDANA.TTF");

		TITLE_SIZE = 22.0f;
		SUBTITLE_SIZE = 18.0f;
		BUTTON_FONT_SIZE = 16.0f;
		CONTENT_FONT_SIZE = 14.0f;

		HashMap<String, String> simpleNameMap = new HashMap<String, String> ();

		//simpleNameMap.put ("Temperature @ isobaric", "T");
		//simpleNameMap.put ("Temperature @ pressure", "T");
		/*
		for (String parameter : ParameterListConstants.TEMPERATURE) {
			simpleNameMap.put (parameter, SimpleParameterNameConstants.TEMPERATURE);
		}
		*/
		simpleNameMap.put (intrinsicParameter (PARAM_TEMPERATURE, AT_ISOBARIC), TEMPERATURE);
		simpleNameMap.put (intrinsicParameter (PARAM_TEMPERATURE, AT_PRESSURE_LAYER), TEMPERATURE);

		//simpleNameMap.put ("Geopotential_height @ isobaric", "Z");
		//simpleNameMap.put ("Geopotential_height @ pressure", "Z");
		/*
		for (String parameter : ParameterListConstants.GEOPOTENTIAL_HEIGHT) {
			simpleNameMap.put (parameter, GEOPOTENTIAL_HEIGHT);
		}
		*/
		simpleNameMap.put (intrinsicParameter (PARAM_GEOPOTENTIAL_HEIGHT, AT_ISOBARIC), GEOPOTENTIAL_HEIGHT);
		simpleNameMap.put (intrinsicParameter (PARAM_GEOPOTENTIAL_HEIGHT, AT_PRESSURE_LAYER), GEOPOTENTIAL_HEIGHT);

		//simpleName_Map.put ("Speed (from u_wind & v_wind)", "WS");
		//simpleNameMap.put ("Speed (from U-component_of_wind & V-component_of_wind)", "WS");
		/*
		for (String parameter : ParameterListConstants.SPEED_D) {
			simpleNameMap.put (parameter, SPEED);
		}
		*/
		simpleNameMap.put (derivedParameter (PARAM_SPEED, (COMP_U_WIND_D + ISOBARIC_D), (COMP_V_WIND_D + ISOBARIC_D)),
				SPEED);
		simpleNameMap.put (
				derivedParameter (PARAM_SPEED, (COMP_U_WIND_D + HEIGHT_ABOVE_GROUND_D),
						(COMP_V_WIND_D + HEIGHT_ABOVE_GROUND_D)), SPEED);
		//simpleNameMap.put ("Relative_humidity @ isobaric", "RH");
		//simpleNameMap.put ("Relative_humidity @ pressure", "RH");
		/*
		for (String parameter : ParameterListConstants.RELATIVE_HUMIDITY) {
			simpleNameMap.put (parameter, RELATIVE_HUMIDITY);
		}
		*/
		simpleNameMap.put (intrinsicParameter (PARAM_RELATIVE_HUMIDITY_L, AT_ISOBARIC), RELATIVE_HUMIDITY);
		simpleNameMap.put (intrinsicParameter (PARAM_RELATIVE_HUMIDITY_L, AT_PRESSURE_LAYER), RELATIVE_HUMIDITY);

		/*
		for (String parameter : ParameterListConstants.RELATIVE_HUMIDITY_D) {
			simpleNameMap.put (parameter, RELATIVE_HUMIDITY);
		}
		*/

		//simpleNameMap.put ("Dewpoint (from Temperature & Relative_humidity)", "Td");
		//simpleNameMap.put ("Dewpoint Depression (from Temperature & dewpoint)", "Td");
		/*
		for (String parameter : ParameterListConstants.DEWPOINT_D) {
			simpleNameMap.put (parameter, DEWPOINT);
		}
		for (String parameter : ParameterListConstants.DEWPOINT_DEPRESSION_D) {
			simpleNameMap.put (parameter, DEWPOINT);
		}
		for (String parameter : ParameterListConstants.DEWPOINT_TEMPERATURE) {
			simpleNameMap.put (parameter, DEWPOINT);
		}
		*/

		simpleNameMap.put (
				derivedParameter (PARAM_DEWPOINT, (COMP_TEMPERATURE_D + ISOBARIC_D),
						(COMP_RELATIVE_HUMIDITY_D + ISOBARIC_D)), DEWPOINT);
		simpleNameMap.put (
				derivedParameter (PARAM_DEWPOINT_DEPRESSION, (COMP_TEMPERATURE_D + ISOBARIC_D), COMP_DEWPOINT_D),
				DEWPOINT);

		//simpleNameMap.put ("mixingratio", "r");

		SIMPLE_NAME_MAP = Collections.unmodifiableMap (simpleNameMap);
	}

	private UIConstants ()
	{
		// Static class, no public constructor.
	}

}
