package geopod.constants.parameters;

import geopod.constants.parameters.enums.AtLevelModifier;
import geopod.constants.parameters.enums.ComponentAtLevelModifier;
import geopod.constants.parameters.enums.ComponentParameter;
import geopod.constants.parameters.enums.IntrinsicParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contains utility methods for dealing with Parameters and Parameter handling.
 * 
 * @author Geopod Team
 * 
 */
public class ParameterUtil
{

	private static final String INTRINSIC_SEPERATOR = " @ ";
	private static final String DERIVED_SEPERATOR_BEGIN = " (from ";
	private static final String DERIVED_SEPERATOR_END = ")";
	private static final String AND_SEPERATOR = " & ";
	private static final String COMMA_SEPERATOR = ", ";

	/**
	 * Return a String representation of a normal, or intrinsic, parameter.
	 * 
	 * @param param
	 * @param atLevel
	 * @return
	 */
	public static String intrinsicParameter (final IntrinsicParameter param, final AtLevelModifier atLevel)
	{
		return param.toString () + INTRINSIC_SEPERATOR + atLevel.toString ();
	}

	/**
	 * Return a String representation of a component parameter.
	 * 
	 * @param param
	 * @param atLevel
	 * @return
	 */
	public static String componentParameter (final ComponentParameter param, final ComponentAtLevelModifier atLevel)
	{
		return param.toString () + atLevel.toString ();
	}

	/**
	 * Return a String representation of a derived parameter.
	 * 
	 * @param param
	 * @param components
	 * @return
	 */
	public static String derivedParameter (final IntrinsicParameter param, String... components)
	{
		String derivedParameter = null;
		if (components.length > 1)
		{
			derivedParameter = param.toString () + DERIVED_SEPERATOR_BEGIN;
			if (components.length > 2)
			{
				for (int i = 0; i < components.length - 1; i++)
				{
					derivedParameter += components[i].toString () + COMMA_SEPERATOR;
				}
				derivedParameter += "and " + components[components.length - 1].toString ();
			}
			else
			{
				derivedParameter += components[0].toString ();
				if (components.length == 2)
				{
					derivedParameter += AND_SEPERATOR + components[1].toString ();
				}
			}
			derivedParameter += DERIVED_SEPERATOR_END;
		}
		return derivedParameter;
	}

	/**
	 * Return a List of default parameters for Geopod to load.
	 * 
	 * @return
	 */
	public static Collection<String> getDefaultGeopodParameters ()
	{
		Set<String> params = new HashSet<String> ();

		// Speed
		params.add (derivedParameter (IntrinsicParameter.SPEED,
				componentParameter (ComponentParameter.U_WIND, ComponentAtLevelModifier._ISOBARIC),
				componentParameter (ComponentParameter.V_WIND, ComponentAtLevelModifier._ISOBARIC)));
		params.add (derivedParameter (IntrinsicParameter.SPEED,
				componentParameter (ComponentParameter.U_WIND, ComponentAtLevelModifier._HEIGHT_ABOVE_GROUND),
				componentParameter (ComponentParameter.V_WIND, ComponentAtLevelModifier._HEIGHT_ABOVE_GROUND)));

		// Geopotential Height
		params.add (intrinsicParameter (IntrinsicParameter.GEOPOTENTIAL_HEIGHT, AtLevelModifier.AT_ISOBARIC));
		params.add (intrinsicParameter (IntrinsicParameter.GEOPOTENTIAL_HEIGHT, AtLevelModifier.AT_PRESSURE));

		// Relative Humidity
		params.addAll (getDropsondeRHParameters ());

		// Mixing Ratio
		params.add (derivedParameter (IntrinsicParameter.MIXING_RATIO,
				componentParameter (ComponentParameter.TEMPERATURE, ComponentAtLevelModifier._ISOBARIC),
				componentParameter (ComponentParameter.RELATIVE_HUMIDITY, ComponentAtLevelModifier._ISOBARIC)));

		// Temperature
		params.add (intrinsicParameter (IntrinsicParameter.TEMPERATURE, AtLevelModifier.AT_ISOBARIC));
		params.add (intrinsicParameter (IntrinsicParameter.TEMPERATURE, AtLevelModifier.AT_PRESSURE));

		// Dewpoint
		/*
		 * NOTE: The second parameter added here, Dewpoint Depression requires
		 * the unqualified name of the Dewpoint parameter as one of its components.
		 * This is yet another inconsistency in the new IDV4.0u parameter scheme.
		 * Should this nomenclature ever change, the methods provided above should
		 * more than suffice for future alteration.
		 */
		params.add (derivedParameter (IntrinsicParameter.DEWPOINT,
				componentParameter (ComponentParameter.TEMPERATURE, ComponentAtLevelModifier._ISOBARIC),
				componentParameter (ComponentParameter.RELATIVE_HUMIDITY, ComponentAtLevelModifier._ISOBARIC)));
		params.add (derivedParameter (IntrinsicParameter.DEWPOINT_DEPRESSION,
				componentParameter (ComponentParameter.TEMPERATURE, ComponentAtLevelModifier._ISOBARIC),
				ComponentParameter.DEWPOINT.toString ()));

		// Add all the parameters for the dropsonde
		params.addAll (getDefaultDropsondeParameters ());

		// To please the WRF gods
		params.addAll (WRFParameterUtil.getDefaultGeopodParameters ());

		System.err.println (params.toString ());

		return Collections.unmodifiableSet (params);
	}

	/**
	 * Return a List of parameters which Geopod should ALWAYS load.
	 * 
	 * @return
	 */
	public static Collection<String> getPermanentGeopodParameters ()
	{
		List<String> params = new ArrayList<String> ();

		// Temperature
		params.add (intrinsicParameter (IntrinsicParameter.TEMPERATURE, AtLevelModifier.AT_ISOBARIC));
		params.add (intrinsicParameter (IntrinsicParameter.TEMPERATURE, AtLevelModifier.AT_PRESSURE));

		// Relative Humidity
		params.addAll (getDropsondeRHParameters ());

		return Collections.unmodifiableList (params);
	}

	/**
	 * Return a Set of parameters for the dropsonde to load by default.
	 * 
	 * @return
	 */
	public static Set<String> getDefaultDropsondeParameters ()
	{
		Set<String> params = new HashSet<String> ();

		// Temperature + Dewpoint
		params.addAll (getDropsondeTDpParameters ());

		// Relative Humidity
		params.addAll (getDropsondeRHParameters ());

		// U-Wind + V-Wind
		params.addAll (getDropsondeUVWindParameters ());

		// Equivalent Potential Temperature
		params.addAll (getDropsondeThetaEParameters ());

		return Collections.unmodifiableSet (params);
	}

	/**
	 * Return a List of parameters used for the Temperature/Dewpoint Dropsonde
	 * display
	 * 
	 * @return
	 */
	public static List<String> getDropsondeTDpParameters ()
	{
		List<String> params = new ArrayList<String> ();

		// Temperature
		params.add (intrinsicParameter (IntrinsicParameter.TEMPERATURE, AtLevelModifier.AT_ISOBARIC));
		params.add (intrinsicParameter (IntrinsicParameter.TEMPERATURE, AtLevelModifier.AT_PRESSURE));

		// Dewpoint
		/*
		 * NOTE: The second parameter added here, Dewpoint Depression requires
		 * the unqualified name of the Dewpoint parameter as one of its components.
		 * This is yet another inconsistency in the new IDV4.0u parameter scheme.
		 * Should this nomenclature ever change, the methods provided above should
		 * more than suffice for future alteration.
		 */
		params.add (derivedParameter (IntrinsicParameter.DEWPOINT,
				componentParameter (ComponentParameter.TEMPERATURE, ComponentAtLevelModifier._ISOBARIC),
				componentParameter (ComponentParameter.RELATIVE_HUMIDITY, ComponentAtLevelModifier._ISOBARIC)));
		params.add (derivedParameter (IntrinsicParameter.DEWPOINT_DEPRESSION,
				componentParameter (ComponentParameter.TEMPERATURE, ComponentAtLevelModifier._ISOBARIC),
				ComponentParameter.DEWPOINT.toString ()));

		// To please the WRF gods
		params.addAll (WRFParameterUtil.getDropsondeTDewParameters ());

		return Collections.unmodifiableList (params);
	}

	/**
	 * Return a List of parameters used for the U/V-Wind Dropsonde display
	 * 
	 * @return
	 */
	public static List<String> getDropsondeUVWindParameters ()
	{
		List<String> params = new ArrayList<String> ();

		// U-Wind
		params.add (intrinsicParameter (IntrinsicParameter.U_WIND, AtLevelModifier.AT_ISOBARIC));
		params.add (intrinsicParameter (IntrinsicParameter.U_WIND, AtLevelModifier.AT_PRESSURE));

		// V-Wind
		params.add (intrinsicParameter (IntrinsicParameter.V_WIND, AtLevelModifier.AT_ISOBARIC));
		params.add (intrinsicParameter (IntrinsicParameter.V_WIND, AtLevelModifier.AT_PRESSURE));

		// To please the WRF gods
		params.addAll (WRFParameterUtil.getDropsondeWindParameters ());

		return Collections.unmodifiableList (params);
	}

	/**
	 * Return a List of parameters used for the Relative Humidity Dropsonde
	 * display
	 * 
	 * @return
	 */
	public static List<String> getDropsondeRHParameters ()
	{
		List<String> params = new ArrayList<String> ();

		// Relative Humidity
		/*
		 * NOTE: The _L is to indicate the "lower-case" parameter name.
		 * This is one of the many inconsistencies with the new IDV4.0u parameters.
		 * Should the lower-case version fall out of use, the upper-case is
		 * available by appending _U.
		 */
		params.add (intrinsicParameter (IntrinsicParameter.RELATIVE_HUMIDITY_L, AtLevelModifier.AT_ISOBARIC));
		params.add (intrinsicParameter (IntrinsicParameter.RELATIVE_HUMIDITY_L, AtLevelModifier.AT_PRESSURE));

		// To please the WRF gods
		params.addAll (WRFParameterUtil.getDropsondeRHParameters ());

		return Collections.unmodifiableList (params);

	}

	/**
	 * Return a List of parameters used for the Equivalent Potential Temperature
	 * Dropsonde display
	 * 
	 * @return
	 */
	public static List<String> getDropsondeThetaEParameters ()
	{
		List<String> params = new ArrayList<String> ();

		// Equivalent Potential Temperature
		/*
		 * NOTE: The second parameter needed is Potential Temperature.
		 * This is yet another of the many inconsistencies with IDV 4.0u parameters.
		 * Remove if not required.
		 */
		params.add (intrinsicParameter (IntrinsicParameter.EQUIVALENT_POTENTIAL_TEMPERATURE,
				AtLevelModifier.AT_HEIGHT_ABOVE_GROUND));
		params.add (derivedParameter (IntrinsicParameter.POTENTIAL_TEMPERATURE,
				componentParameter (ComponentParameter.TEMPERATURE, ComponentAtLevelModifier._ISOBARIC),
				componentParameter (ComponentParameter.RELATIVE_HUMIDITY, ComponentAtLevelModifier._ISOBARIC)));

		params.addAll (WRFParameterUtil.getDropsondeThetaEParameters ());

		return Collections.unmodifiableList (params);
	}
}
