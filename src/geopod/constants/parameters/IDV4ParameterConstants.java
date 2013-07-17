package geopod.constants.parameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Deprecated
public class IDV4ParameterConstants
{
	private static final String INTRINSIC_SEPERATOR = " @ ";
	private static final String DERIVED_SEPERATOR_BEGIN = " (from ";
	private static final String DERIVED_SEPERATOR_END = ")";
	private static final String AND_SEPERATOR = " & ";
	private static final String COMMA_SEPERATOR = ", ";
	
	// @ LEVELS
	//public static final String AT_0DEG_ISOTHERM = "Level of 0deg C isotherm";
	//public static final String AT_GROUND_OR_WATER_SURFACE = "Ground or water surface";
	public static final String AT_ISOBARIC = "Isobaric surface";
	public static final String AT_PRESSURE_LAYER = "Layer between 2 level at pressure difference from ground to level layer";
	//public static final String AT_MEAN_SEA_LEVEL = "Mean sea level";
	//public static final String AT_ENTIRE_ATMOSPHERE = "Entire atmosphere";
	//public static final String AT_CLOUD_BASE_LEVEL = "Cloud base level";
	//public static final String AT_LEVEL_OF_CLOUD_TOPS = "Level of cloud tops";
	//public static final String AT_MAXIMUM_WIND_LEVEL = "Maximum wind level";
	//public static final String AT_TROPOPAUSE = "Tropopause";
	public static final String AT_HEIGHT_ABOVE_GROUND = "Specified height level above ground";
	
	// PARAMETER NAMES
	//public static final String PARAM_THOUSAND_FIVE_HUNDRED_HPA_THICKNESS = "1000-500 hPa Thickness";
	//public static final String PARAM_AGEOSTROPHIC_WIND = "AGeostrophic Wind";
	//public static final String PARAM_AGEOSTROPHIC_WIND_VECTORS = "AGeostrophic Wind Vectors";
	//public static final String PARAM_ABSOLUTE_VORTICITY = "Absolute Vorticity";
	//public static final String PARAM_BEST_4_LAYER_LIFTED_INDEX = "Best 4 layer lifted index";
	//public static final String PARAM_CONVECTIVE_AVAILABLE_POTENTIAL_ENERGY = "Convective Available Potential Energy";
	//public static final String PARAM_CONVECTIVE_INHIBITION = "Convective inhibition";
	//public static final String PARAM_CONVECTIVE_PRECIPITATION = "Convective precipitation (Mixed_intervals Accumulation)";
	public static final String PARAM_DEWPOINT = "Dewpoint";
	public static final String PARAM_DEWPOINT_DEPRESSION = "Dewpoint Depression";
	public static final String PARAM_EQUIVALENT_POTENTIAL_TEMPERATURE = "Equiv. Potential Temperature";
	//public static final String PARAM_FLOW_VECTORS = "Flow Vectors";
	public static final String PARAM_GEOPOTENTIAL_HEIGHT = "Geopotential height";
	//public static final String PARAM_GEOSTOPHIC_WIND = "Geostrophic Wind";
	//public static final String PARAM_GEOSTOPHIC_WIND_VECTORS = "Geostrophic Wind Vectors";
	//public static final String PARAM_GRID_2D_TRAJECTORY = "Grid 2D Trajectory";
	//public static final String PARAM_GRID_3D_TRAJECTORY = "Grid 3D Trajectory";
	//public static final String PARAM_HORIZONTAL_ADVECTION = "Horizontal Advection";
	//public static final String PARAM_HORIZONTAL_DIVERGENCE = "Horizontal Divergence";
	//public static final String PARAM_HORIZONTAL_FLUX_DIVERGENCE = "Horizontal Flux Divergence";
	//public static final String PARAM_ISENTROPIC_POTENTIAL_VORTICITY = "Isentropic Potential Vorticity";
	//public static final String PARAM_MEAN_SEA_LEVEL_PRESSURE_NAM_MODEL_REDUCTION = "Mean Sea Level Pressure NAM Model Reduction";
	public static final String PARAM_MIXING_RATIO = "Mixing ratio";
	//public static final String PARAM_PARCEL_LIFTED_INDEX_TO_500_HPA = "Parcel lifted index to 500 hPa";
	public static final String PARAM_POTENTIAL_TEMPERATURE = "Potential Temperature";
	//public static final String PARAM_POTENTIAL_VORTICITY = "Potential Vorticity";
	//public static final String PARAM_PRECIPITABLE_WATER = "Precipitable water";
	public static final String PARAM_PRESSURE = "Pressure";
	//public static final String PARAM_PRESSURE_REDUCED_TO_MSL = "Pressure reduced to MSL";
	public static final String PARAM_RELATIVE_HUMIDITY_U = "Relative Humidity";
	public static final String PARAM_RELATIVE_HUMIDITY_L = "Relative humidity";
	//public static final String PARAM_RELATIVE_VORTICITY = "Relative Vorticity";
	public static final String PARAM_SPEED = "Speed";
	//public static final String PARAM_STORM_RELATIVE_HELICITY = "Storm relative helicity";
	public static final String PARAM_TEMPERATURE = "Temperature";
	//public static final String PARAM_THICKNESS = "Thickness";
	//public static final String PARAM_TOTAL_PRECIPITATION = "Total precipitation (Mixed_intervals Accumulation)";
	//public static final String PARAM_TRUE_WIND_VECTORS = "True Wind vectors";
	//public static final String PARAM_VERTICAL_VELOCITY_PRESSURE = "Vertical velocity pressure";
	public static final String PARAM_U_WIND = "u-component of wind";
	public static final String PARAM_V_WIND = "v-component of wind";
	
	// COMPONENT PARAMETERS (FOR DERIVATION)
	public static final String COMP_U_WIND_D = "u-component_of_wind";
	public static final String COMP_V_WIND_D = "v-component_of_wind";
	//public static final String COMP_GEOPOTENTIAL_HEIGHT_D = "Geopotential_height";
	//public static final String COMP_VERTICAL_VELOCITY_PRESSURE_D = "Vertical_velocity_pressure";
	public static final String COMP_TEMPERATURE_D = PARAM_TEMPERATURE;
	public static final String COMP_RELATIVE_HUMIDITY_D = "Relative_humidity";
	//public static final String COMP_MIXING_RATIO_D = "mixingratio";
	public static final String COMP_DEWPOINT_D = "dewpoint";
	//public static final String COMP_ABSOLUTE_VORTICITY_D = "absvort";
	//public static final String COMP_DPDK = "dpdk"; // I have no idea what this is, but it was present.
	//public static final String COMP_THETA_D = "theta";
	//public static final String COMP_FLOW_VECTORS_D = "flowvectors";
	
	// COMPONENT PARAMETER TYPES
	public static final String ISOBARIC_D = "_isobaric";
	public static final String HEIGHT_ABOVE_GROUND_D = "_height_above_ground";
	
	/* DERIVED LEVELS
	//public static final String FROM_U_WIND_ISOBARIC_V_WIND_ISOBARIC_AND_GEOPOTENTIAL_HEIGHT_ISOBARIC = " (from u-component_of_wind_isobaric, v-component_of_wind_isobaric, and Geopotential_height_isobaric)";
	//public static final String FROM_U_WIND_ISOBARIC_AND_V_WIND_ISOBARIC = " (from u-component_of_wind_isobaric & v-component_of_wind_isobaric)";
	//public static final String FROM_U_WIND_HEIGHT_ABOVE_GROUND_AND_V_WIND_HEIGHT_ABOVE_GROUND = "(from u-component_of_wind_height_above_ground & v-component_of_wind_height_above_ground)";
	//public static final String FROM_U_WIND_ISOBARIC_V_WIND_ISOBARIC_AND_VERTICAL_VELOCITY_PRESSURE_ISOBARIC = "(from u-component_of_wind_isobaric & v-component_of_wind_isobaric  & Vertical_velocity_pressure_isobaric)";
	//public static final String FROM_TEMPERATURE_ISOBARIC = "(from Temperature_isobaric)";
	//public static final String FROM_TEMPERATURE_ISOBARIC_AND_RELATIVE_HUMIDITY_ISOBARIC = " (from Temperature_isobaric & Relative_humidity_isobaric)";
	//public static final String FROM_TEMPERATURE_ISOBARIC_AND_RELATIVE_HUMIDITY_HEIGHT_ABOVE_GROUND = " (from Temperature_tropopause & Relative_humidity_height_above_ground)";
	//public static final String FROM_TEMPERATURE_ISOBARIC_AND_MIXINGRATIO = "(from Temperature_isobaric & mixingratio)";
	//public static final String FROM_TEMPERATURE_ISOBARIC_AND_DEWPOINT = " (from Temperature_isobaric & dewpoint)";
	//public static final String FROM_TEMPERATURE_ISOBARIC_AND_ABSOLUTE_VORTICITY = "(from Temperature_isobaric & absvort)";
	//public static final String FROM_TEMPERATURE_HEIGHT_ABOVE_GROUND_AND_DPDK = " (from Temperature_height_above_ground & dpdk)";
	//public static final String FROM_TEMPERATURE_TROPOPAUSE_AND_DEWPOINT = "(from Temperature_tropopause & dewpoint)";
	//public static final String FROM_TEMPERATURE_TROPOPAUSE_AND_RELATIVE_HUMIDITY_HEIGHT_ABOVE_GROUND = "(from Temperature_tropopause & Relative_humidity_height_above_ground)";
	//public static final String FROM_TEMPERATURE_TROPOPAUSE_AND_MIXINGRATIO = "(from Temperature_tropopause & mixingratio)";
	//public static final String FROM_GEOPOTENTIAL_HEIGHT_ISOBARIC = "(from Geopotential_height_isobaric)";
	//public static final String FROM_THETA_AND_FLOWVECTORS = "(from theta & flowvectors)";
	 */
	
	public static final String intrinsicParameter(String param, String component) {
		return param + INTRINSIC_SEPERATOR + component;
	}
	
	public static final String derivedParameter(String derivative, List<String> components) {
		return derivedParameter(derivative, (String[]) components.toArray ());
	}
	
	public static final String derivedParameter (String derivative, String... components) {
		String derivedParameter = null;
		if (components.length > 1) {
			derivedParameter = derivative + DERIVED_SEPERATOR_BEGIN;
			if (components.length > 2) {
				for (int i = 0; i < components.length - 1; i++) {
					derivedParameter += components[i] + COMMA_SEPERATOR;
				}
				derivedParameter += "and " + components[components.length - 1];
			} else {
				derivedParameter += components[0];
				if (components.length == 2) {
					derivedParameter += AND_SEPERATOR + components[1];
				}
			}
			derivedParameter += DERIVED_SEPERATOR_END;
		}
		return derivedParameter;
	}
	
	public static List<String> getDefaultGeopodParameters() {
		List<String> params = new ArrayList<String>();
		
		params.add (intrinsicParameter (PARAM_TEMPERATURE, AT_ISOBARIC)); // @ Isobaric surface
		params.add (intrinsicParameter (PARAM_TEMPERATURE, AT_PRESSURE_LAYER)); // @ Layer between...
		
		params.add (derivedParameter (PARAM_SPEED, (COMP_U_WIND_D + ISOBARIC_D), (COMP_V_WIND_D + ISOBARIC_D))); // from u and v wind
		params.add (derivedParameter (PARAM_SPEED, (COMP_U_WIND_D + HEIGHT_ABOVE_GROUND_D), (COMP_V_WIND_D + HEIGHT_ABOVE_GROUND_D))); // height above ground
		
		params.add (intrinsicParameter (PARAM_GEOPOTENTIAL_HEIGHT, AT_ISOBARIC));
		params.add (intrinsicParameter (PARAM_GEOPOTENTIAL_HEIGHT, AT_PRESSURE_LAYER));
		
		params.add (intrinsicParameter (PARAM_RELATIVE_HUMIDITY_L, AT_ISOBARIC));
		params.add (intrinsicParameter (PARAM_RELATIVE_HUMIDITY_L, AT_PRESSURE_LAYER));
		
		params.add (derivedParameter (PARAM_DEWPOINT, (COMP_TEMPERATURE_D + ISOBARIC_D), (COMP_RELATIVE_HUMIDITY_D + ISOBARIC_D)));
		params.add (derivedParameter (PARAM_DEWPOINT_DEPRESSION, (COMP_TEMPERATURE_D + ISOBARIC_D), COMP_DEWPOINT_D));
		
		params.add (derivedParameter (PARAM_MIXING_RATIO, (PARAM_TEMPERATURE + ISOBARIC_D), (COMP_RELATIVE_HUMIDITY_D + ISOBARIC_D)));
		
		return Collections.unmodifiableList (params);
	}
	
	public static List<String> getPermanentGeopodParameters() {
		List<String> params = new ArrayList<String>();
		
		params.add (intrinsicParameter (PARAM_TEMPERATURE, AT_ISOBARIC));
		params.add (intrinsicParameter (PARAM_TEMPERATURE, AT_PRESSURE_LAYER));
		
		params.add (intrinsicParameter (PARAM_RELATIVE_HUMIDITY_L, AT_ISOBARIC));
		params.add (intrinsicParameter (PARAM_RELATIVE_HUMIDITY_L, AT_PRESSURE_LAYER));
		
		return Collections.unmodifiableList (params);
	}
	
	public static Set<String> getDefaultDropsondeParameters() {
		Set<String> params = new HashSet<String>();
		
		params.add (intrinsicParameter (PARAM_TEMPERATURE, AT_ISOBARIC));
		params.add (intrinsicParameter (PARAM_TEMPERATURE, AT_PRESSURE_LAYER));
		
		params.add (derivedParameter (PARAM_DEWPOINT, (COMP_TEMPERATURE_D + ISOBARIC_D), (COMP_RELATIVE_HUMIDITY_D + ISOBARIC_D)));
		params.add (derivedParameter (PARAM_DEWPOINT_DEPRESSION, (COMP_TEMPERATURE_D + ISOBARIC_D), COMP_DEWPOINT_D));
		
		params.add (intrinsicParameter (PARAM_U_WIND, AT_ISOBARIC));
		params.add (intrinsicParameter (PARAM_U_WIND, AT_PRESSURE_LAYER));
		
		params.add (intrinsicParameter (PARAM_V_WIND, AT_ISOBARIC));
		params.add (intrinsicParameter (PARAM_V_WIND, AT_PRESSURE_LAYER));
		
		params.add (intrinsicParameter (PARAM_RELATIVE_HUMIDITY_L, AT_ISOBARIC));
		params.add (intrinsicParameter (PARAM_RELATIVE_HUMIDITY_L, AT_PRESSURE_LAYER));
		
		params.add (derivedParameter (PARAM_EQUIVALENT_POTENTIAL_TEMPERATURE, (COMP_TEMPERATURE_D + ISOBARIC_D), (COMP_RELATIVE_HUMIDITY_D + ISOBARIC_D)));
		params.add (intrinsicParameter (PARAM_POTENTIAL_TEMPERATURE, AT_HEIGHT_ABOVE_GROUND));
		
		return Collections.unmodifiableSet (params);
	}
	
	public static List<String> getDropsondeTDpParameters() {
		List<String> params = new ArrayList<String>();
		
		params.add (intrinsicParameter (PARAM_TEMPERATURE, AT_ISOBARIC));
		params.add (intrinsicParameter (PARAM_TEMPERATURE, AT_PRESSURE_LAYER));
		params.add (derivedParameter (PARAM_DEWPOINT, (COMP_TEMPERATURE_D + ISOBARIC_D), (COMP_RELATIVE_HUMIDITY_D + ISOBARIC_D)));
		params.add (derivedParameter (PARAM_DEWPOINT_DEPRESSION, (COMP_TEMPERATURE_D + ISOBARIC_D), COMP_DEWPOINT_D));
		
		return Collections.unmodifiableList (params);
	}
	
	public static String[] getDropsondeUToVWindParameters() {
		return new String[] {intrinsicParameter (PARAM_U_WIND, AT_ISOBARIC),
				intrinsicParameter (PARAM_U_WIND, AT_PRESSURE_LAYER),
				intrinsicParameter (PARAM_V_WIND, AT_ISOBARIC),
				intrinsicParameter (PARAM_V_WIND, AT_PRESSURE_LAYER)};
	}
	
	public static List<String> getDropsondeUVWindParameters() {
		List<String> params = new ArrayList<String>();
		
		params.add (intrinsicParameter (PARAM_U_WIND, AT_ISOBARIC));
		params.add (intrinsicParameter (PARAM_U_WIND, AT_PRESSURE_LAYER));
		params.add (intrinsicParameter (PARAM_V_WIND, AT_ISOBARIC));
		params.add (intrinsicParameter (PARAM_V_WIND, AT_PRESSURE_LAYER));
		
		return Collections.unmodifiableList (params);
	}
	
	public static List<String> getDropsondeRHParameters() {
		List<String> params = new ArrayList<String>();
		
		params.add (intrinsicParameter (PARAM_RELATIVE_HUMIDITY_L, AT_ISOBARIC));
		params.add (intrinsicParameter (PARAM_RELATIVE_HUMIDITY_L, AT_PRESSURE_LAYER));
		
		return Collections.unmodifiableList (params);
	}
	
	public static List<String> getDropsondeThetaEParameters() {
		List<String> params = new ArrayList<String>();
		
		params.add (derivedParameter (PARAM_EQUIVALENT_POTENTIAL_TEMPERATURE, (COMP_TEMPERATURE_D + ISOBARIC_D), (COMP_RELATIVE_HUMIDITY_D + ISOBARIC_D)));
		params.add (intrinsicParameter (PARAM_POTENTIAL_TEMPERATURE, AT_HEIGHT_ABOVE_GROUND));
		
		return Collections.unmodifiableList (params);
	}

}
