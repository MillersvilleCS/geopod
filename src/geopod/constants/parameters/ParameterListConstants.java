package geopod.constants.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Deprecated
public final class ParameterListConstants
{
	@Deprecated
	public static final int ISOBARIC_INDEX = 0;
	@Deprecated
	public static final int PRESSURE_INDEX = 1;
	@Deprecated
	public static final int FROM_T_AND_RH_INDEX = 0;
	@Deprecated
	public static final int FROM_U_AND_V_COMP_INDEX = 1;
	@Deprecated
	public static final int FROM_U_AND_V_WIND_INDEX = 0;
	@Deprecated
	public static final int MIXINGRATIO_INDEX = 0;
	@Deprecated
	public static final int HEIGHT_ABOVE_GROUND_INDEX = 0;
	// INTRINSIC PARAMETERS
	//public static final List<String> ABSOLUTE_VORTICITY;
	//public static final List<String> ALBEDO;
	//public static final List<String> BASEFLOW_GROUNDWATER;
	//public static final List<String> BEST_LIFTED_INDEX;
	//public static final List<String> BLACKADARS_MIXING_LENGTH_SCALE;
	//public static final List<String> CANOPY_CONDUCTANCE;
	//public static final List<String> CATEGORICAL_FREEZING_RAIN;
	//public static final List<String> CATEGORICAL_ICE_PELLETS;
	//public static final List<String> CATEGORICAL_RAIN;
	//public static final List<String> CATEGORICAL_SNOW;
	//public static final List<String> CLOUD_ICE;
	//public static final List<String> CLOUD_MIXING_RATIO;
	//public static final List<String> CONVECTIVE_AVAILABLE_POTENTIAL_ENERGY;
	//public static final List<String> CONVECTIVE_CLOUD_COVER;
	//public static final List<String> CONVECTIVE_CLOUD_EFFICIENCY;
	//public static final List<String> CONVECTIVE_INHIBITION;
	//public static final List<String> CONVECTIVE_PRECIPITATION_0_HOURS;
	//public static final List<String> CONVECTIVE_PRECIPITATION_RATE;
	@Deprecated
	public static final List<String> DEWPOINT_TEMPERATURE;
	//public static final List<String> DIRECT_EVAPORATION_CEASE_SOIL_MOISTURE;
	//public static final List<String> DOWNWARD_LONG_WAVE_RAD_FLUX;
	//public static final List<String> DOWNWARD_LONG_WAVE_RAD_FLUX_0_HOURS;
	//public static final List<String> DOWNWARD_SHORT_WAVE_RAD_FLUX;
	//public static final List<String> DOWNWARD_SHORT_WAVE_RAD_FLUX_0_HOURS;
	//public static final List<String> DRAG_COEFFICIENT;
	//public static final List<String> EVAPORATION_0_HOURS;
	//public static final List<String> EXCHANGE_COEFFICIENT;
	//public static final List<String> FRICTIONAL_VELOCITY;
	@Deprecated
	public static final List<String> GEOPOTENTIAL_HEIGHT;
	//public static final List<String> GROUND_HEAT_FLUX_0_HOURS;
	//public static final List<String> HIGH_CLOUD_COVER;
	//public static final List<String> HORIZONTAL_MOISTURE_DIVERGENCE;
	//public static final List<String> HUMIDITY_PARAMETER_IN_CANOPY_CONDUCTANCE;
	//public static final List<String> ICE_COVER_PROPORTION;
	//public static final List<String> LAND_COVER_1_LAND_2_SEA;
	//public static final List<String> LARGE_SCALE_PRECIPITATION_NONCONDUCTIVE_0_HOURS;
	//public static final List<String> LATENT_HEAT_FLUX;
	//public static final List<String> LATENT_HEAT_FLUX_0_HOURS;
	//public static final List<String> LIQUID_VOLUMETRIC_SOIL_MOISTURE_NON_FROZEN;
	//public static final List<String> LOW_CLOUD_COVER;
	//public static final List<String> MAXIMUM_SNOW_ALBEDO;
	//public static final List<String> MEDIUM_CLOUD_COVER;
	//public static final List<String> MINIMAL_STOMATAL_RESISTANCE;
	//public static final List<String> MOISTURE_AVAILABILITY;
	//public static final List<String> MOMENTUM_FLUX_U_COMPONENT;
	//public static final List<String> MOMENTUM_FLUX_V_COMPONENT;
	//public static final List<String> MSLP_ETA_REDUCTION;
	//public static final List<String> NUMBER_OF_SOIL_LAYERS_IN_ROOT_ZONE;
	//public static final List<String> PARCEL_LIFTED_INDEX_TO_500_HPA;
	//public static final List<String> PLANETARY_BOUNDARY_LAYER_HEIGHT;
	//public static final List<String> PLANT_CANOPY_SURFACE_WATER;
	//public static final List<String> POTENTIAL_EVAPORATION_0_HOURS;
	@Deprecated
	public static final List<String> POTENTIAL_TEMPERATURE;
	//public static final List<String> PRECIPITABLE_WATER;
	//public static final List<String> PRECIPITATION_RATE;
	//public static final List<String> PRESSURE_OF_LEVEL_FROM_WHICH_PARCEL_WAS_LIFTED;
	//public static final List<String> PRESSURE_REDUCED_TO_MSL;
	@Deprecated
	public static final List<String> PRESSURE;
	//public static final List<String> PROBABILITY_OF_FROZEN_PRECIPITATION;
	//public static final List<String> RAIN_MIXING_RATIO;
	@Deprecated
	public static final List<String> RELATIVE_HUMIDITY;
	//public static final List<String> RIME_FACTOR;
	//public static final List<String> SENSIBLE_HEAT_NET_FLUX;
	//public static final List<String> SNOW_FREE_ALBEDO;
	//public static final List<String> SNOW_COVER;
	//public static final List<String> SNOW_DEPTH;
	//public static final List<String> SNOW_MELT_0_HOURS;
	//public static final List<String> SNOW_MIXING_RATIO;
	//public static final List<String> SNOW_PHASE_CHANGE_HEAT_FLUX_0_HOURS;
	//public static final List<String> SOIL_MOISTURE_CONTENT;
	//public static final List<String> SOIL_MOISTURE_PARAMETER_IN_CANOPY_CONDUCTANCE;
	//public static final List<String> SOIL_POROSITY;
	//public static final List<String> SOIL_TEMPERATURE;
	//public static final List<String> SOIL_TYPE;
	//public static final List<String> SOLAR_PARAMETER_IN_CANOPY_CONDUCTANCE;
	//public static final List<String> SPECIFIC_HUMIDITY;
	//public static final List<String> STORM_RELATIVE_HELICITY;
	//public static final List<String> STORM_SURFACE_RUNOFF_0_HOURS;
	//public static final List<String> STREAM_FUNCTION;
	//public static final List<String> SURFACE_LIFTED_INDEX;
	//public static final List<String> SURFACE_ROUGHNESS;
	//public static final List<String> SURFACE_SLOPE_TYPE;
	@Deprecated
	public static final List<String> TEMPERATURE;
	//public static final List<String> TEMPERATURE_PARAMETER_IN_CANOPY_CONDUCTANCE;
	//public static final List<String> TOTAL_CLOUD_COVER;
	//public static final List<String> TOTAL_CLOUD_COVER_0_HOURS;
	//public static final List<String> TOTAL_COLUMN_INTEGRATED_CLOUD_ICE;
	//public static final List<String> TOTAL_COLUMN_INTEGRATED_CLOUD_WATER;
	//public static final List<String> TOTAL_COLUMN_INTEGRATED_CONDENSATE;
	//public static final List<String> TOTAL_COLUMN_INTEGRATED_RAIN;
	//public static final List<String> TOTAL_COLUMN_INTEGRATED_SNOW;
	//public static final List<String> TOTAL_CONDENSATE;
	//public static final List<String> TOTAL_PRECIPITATION_0_HOURS;
	//public static final List<String> TRANSPIRATION_STRESS_ONSET_SOIL_MOISTURE;
	//public static final List<String> TURBULENT_KINETIC_ENERGY;
	@Deprecated
	public static final List<String> U_WIND;
	//public static final List<String> U_STORM_MOTION;
	//public static final List<String> UPWARD_LONG_WAVE_RAD_FLUX;
	//public static final List<String> UPWARD_LONG_WAVE_RAD_FLUX_0_HOURS;
	//public static final List<String> UPWARD_SHORT_WAVE_RAD_FLUX;
	//public static final List<String> UPWARD_SHORT_WAVE_RAD_FLUX_0_HOURS;
	@Deprecated
	public static final List<String> V_WIND;
	//public static final List<String> V_STORM_MOTION;
	//public static final List<String> VEGETATION;
	//public static final List<String> VEGETATION_TYPE;
	//public static final List<String> VERTICAL_SPEED_SHEER;
	//public static final List<String> VERTICAL_VELOCITY_PRESSURE;
	//public static final List<String> VISIBILITY;
	//public static final List<String> VOLUMETRIC_SOIL_MOISTURE_CONTENT;
	//public static final List<String> WATER_EQUIVALENT_OF_ACCUMULATED_SNOW_DEPTH;
	//public static final List<String> WATER_EQUIVALENT_OF_ACCUMULATED_SNOW_DEPTH_0_HOURS;
	//public static final List<String> WATER_TEMPERATURE;
	//public static final List<String> WILTING_POINT;
	//public static final List<String> WIND_SPEED_GUST;
	// DERIVED PARAMETERS
	@Deprecated
	public static final List<String> SPEED_D;
	//public static final List<String> FLOW_VECTORS_D;
	//public static final List<String> TRUE_WIND_VECTORS_D;
	@Deprecated
	public static final List<String> DEWPOINT_D;
	@Deprecated
	public static final List<String> MIXING_RATIO_D;
	@Deprecated
	public static final List<String> DEWPOINT_DEPRESSION_D;
	@Deprecated
	public static final List<String> EQUIVALENT_POTENTIAL_TEMPERATURE_D;
	//public static final List<String> SOUNDING_DATA_D;
	@Deprecated
	public static final List<String> POTENTIAL_TEMPERATURE_D;
	//public static final List<String> HORIZONTAL_DIVERGENCE_D;
	//public static final List<String> RELATIVE_VORTICITY_D;
	//public static final List<String> ABSOLUTE_VORTICITY_D;
	//public static final List<String> HORIZONTAL_ADVECTION_D;
	//public static final List<String> HORIZONTAL_FLUX_DIVERGENCE_D;
	//public static final List<String> GEOSTROPHIC_WIND_VECTORS_D;
	@Deprecated
	public static final List<String> RELATIVE_HUMIDITY_D;
	//public static final List<String> ISENTROPIC_POTENTIAL_VORTICITY_D;
	//public static final List<String> POTENTIAL_VORTICITY;

	// Format follows pattern:
	// { <GRIB 1>, <GRIB 2> }

	static
	{
		//ABSOLUTE_VORTICITY = Arrays.asList ("Absolute_vorticity @ pressure");
		//ALBEDO = Arrays.asList ("Albedo @ surface");
		//BASEFLOW_GROUNDWATER = Arrays.asList ("Baseflow-Groundwater_Runoff_0hours @ surface");
		//BEST_LIFTED_INDEX = Arrays.asList ("Best_Lifted_Index @ pressure_difference_layer");
		//BLACKADARS_MIXING_LENGTH_SCALE = Arrays.asList ("Blackadars_Mixing_Length_Scale @ hybrid");
		//CANOPY_CONDUCTANCE = Arrays.asList ("Canopy_Conductance @ surface");
		//CATEGORICAL_FREEZING_RAIN = Arrays.asList ("Categorical_Freezing_Rain @ surface");
		//CATEGORICAL_ICE_PELLETS = Arrays.asList ("Categorical_Ice_Pellets @ surface");
		//CATEGORICAL_RAIN = Arrays.asList ("Categorical_Rain @ surface");
		//CATEGORICAL_SNOW = Arrays.asList ("Categorical_Snow @ surface");
		//CLOUD_ICE = Arrays.asList ("Cloud_Ice @ pressure", "Cloud_Ice @ hybrid");
		//CLOUD_MIXING_RATIO = Arrays.asList ("Cloud_mixing_ratio @ pressure", "Cloud_mixing_ratio @ hybrid");
		//CONVECTIVE_AVAILABLE_POTENTIAL_ENERGY = Arrays.asList ("Convective_available_potential_energy @ pressure_difference_layer", "Convective_available_potential_energy @ surface");
		//CONVECTIVE_CLOUD_COVER = Arrays.asList ("Convective_available_potential_energy @ surface", "Convective_cloud_cover @ entire_atmosphere");
		//CONVECTIVE_CLOUD_EFFICIENCY = Arrays.asList ("Convective_Cloud_Efficiency @ entire_atmosphere");
		//CONVECTIVE_INHIBITION = Arrays.asList ("Convective_inhibition @ pressure_difference_layer", "Convective_inhibition @ surface");
		//CONVECTIVE_PRECIPITATION_0_HOURS = Arrays.asList ("Convective_precipitation_0hours @ surface");
		//CONVECTIVE_PRECIPITATION_RATE = Arrays.asList ("Convective_Precipitation_Rate @ surface");
		DEWPOINT_TEMPERATURE = Arrays.asList ("Dew_point_temperature @ pressure_difference_layer",
				"Dew_point_temperature @ pressure");
		//DIRECT_EVAPORATION_CEASE_SOIL_MOISTURE = Arrays.asList ("Direct_Evaporation_Cease_soil_moisture @ surface");
		//DOWNWARD_LONG_WAVE_RAD_FLUX = Arrays.asList ("Downward_Long-Wave_Rad_Flux @ surface");
		//DOWNWARD_LONG_WAVE_RAD_FLUX_0_HOURS = Arrays.asList ("Downward_Long-Wave_Rad_Flux_0hours @ surface");
		//DOWNWARD_SHORT_WAVE_RAD_FLUX = Arrays.asList ("Downward_Short-Wave_Rad_Flux @ surface");
		//DOWNWARD_SHORT_WAVE_RAD_FLUX_0_HOURS = Arrays.asList ("Downward_Short-Wave_Rad_Flux_0hours @ surface");
		//DRAG_COEFFICIENT = Arrays.asList ("Drag_Coefficient @ surface");
		//EVAPORATION_0_HOURS = Arrays.asList ("Evaporation_0hours @ surface");
		//EXCHANGE_COEFFICIENT = Arrays.asList ("Exchange_Coefficient @ surface");
		//FRICTIONAL_VELOCITY = Arrays.asList ("Frictional_Velocity @ surface");
		GEOPOTENTIAL_HEIGHT = Arrays.asList ("Geopotential_height @ isobaric", "Geopotential_height @ pressure",
				"Geopotential_height @ adiabatic_condensation_lifted",
				"Geopotential_height @ highest_tropospheric_freezing", "Geopotential_height @ hybrid",
				"Geopotential_height @ lowest_level_of_the_wet_bulb_zero", "Geopotential_height @ maximum_wind",
				"Geopotential_height @ surface", "Geopotential_height @ zeroDegC_isotherm");
		//GROUND_HEAT_FLUX_0_HOURS = Arrays.asList ("Ground_Heat_Flux_0hours @ surface");
		//HIGH_CLOUD_COVER = Arrays.asList ("High_cloud_cover @ high_cloud");
		//HORIZONTAL_MOISTURE_DIVERGENCE = Arrays.asList ("Horizontal_Moisture_Divergence @ pressure", "Horizontal_Moisture_Divergence @ pressure_difference_layer");
		//HUMIDITY_PARAMETER_IN_CANOPY_CONDUCTANCE = Arrays.asList ("Humidity_parameter_in_canopy_conductance @ surface");
		//ICE_COVER_PROPORTION = Arrays.asList ("Ice_cover_Proportion @ surface");
		//LAND_COVER_1_LAND_2_SEA = Arrays.asList ("Land_cover_1land_2sea @ surface");
		//LARGE_SCALE_PRECIPITATION_NONCONDUCTIVE_0_HOURS = Arrays.asList ("Large_scale_precipitation_non-convective_0hours @ surface");
		//LATENT_HEAT_FLUX = Arrays.asList ("Latent_heat_net_flux @ surface");
		//LATENT_HEAT_FLUX_0_HOURS = Arrays.asList ("Latent_heat_net_flux_0hours @ surface");
		//LIQUID_VOLUMETRIC_SOIL_MOISTURE_NON_FROZEN = Arrays.asList ("Liquid_Volumetric_Soil_Moisture_non_frozen @ depth_below_surface_layer");
		//LOW_CLOUD_COVER = Arrays.asList ("Low_cloud_cover @ low_cloud");
		//MAXIMUM_SNOW_ALBEDO = Arrays.asList ("Maximum_Snow_Albedo @ surface");
		//MEDIUM_CLOUD_COVER = Arrays.asList ("Medium_cloud_cover @ middle_cloud");
		//MINIMAL_STOMATAL_RESISTANCE = Arrays.asList ("Minimal_Stomatal_Resistance @ surface");
		//MOISTURE_AVAILABILITY = Arrays.asList ("Moisture_Availability @ depth_below_surface_layer");
		//MOMENTUM_FLUX_U_COMPONENT = Arrays.asList ("Momentum_flux_u_component @ surface");
		//MOMENTUM_FLUX_V_COMPONENT = Arrays.asList ("Momentum_flux_v_component @ surface");
		//MSLP_ETA_REDUCTION = Arrays.asList ("MSLP_Eta_Reduction @ msl");
		//NUMBER_OF_SOIL_LAYERS_IN_ROOT_ZONE = Arrays.asList ("Number_of_Soil_Layers_in_Root_Zone @ surface");
		//PARCEL_LIFTED_INDEX_TO_500_HPA = Arrays.asList ("Parcel_lifted_index_to_500_hPa @ pressure_difference_layer");
		//PLANETARY_BOUNDARY_LAYER_HEIGHT = Arrays.asList ("Planetary_Boundary_Layer_Height @ surface");
		//PLANT_CANOPY_SURFACE_WATER = Arrays.asList ("Plant_Canopy_Surface_Water @ surface");
		//POTENTIAL_EVAPORATION_0_HOURS = Arrays.asList ("Potential_Evaporation_0hours @ surface");
		POTENTIAL_TEMPERATURE = Arrays.asList ("Potential_temperature @ height_above_ground",
				"Potential_temperature @ pressure_difference_layer");
		//PRECIPITABLE_WATER = Arrays.asList ("Precipitable_water @ entire_atmosphere", "Precipitable_water @ pressure_difference_layer");
		//PRECIPITATION_RATE = Arrays.asList ("Precipitation_rate @ surface");
		//PRESSURE_OF_LEVEL_FROM_WHICH_PARCEL_WAS_LIFTED = Arrays.asList ("Pressure_of_level_from_which_parcel_was_lifted @ pressure_difference_layer");
		//PRESSURE_REDUCED_TO_MSL = Arrays.asList ("Pressure_reduced_to_MSL @ msl");
		PRESSURE = Arrays.asList ("Pressure @ pressure_difference_layer", "Pressure @ adiabatic_condensation_lifted",
				"Pressure @ cloud_base", "Pressure @ cloud_tops", "Pressure @ convective_cloud_bottom",
				"Pressure @ convective_cloud_top", "Pressure @ deep_convective_cloud_bottom",
				"Pressure @ deep_convective_cloud_top", "Pressure @ grid_scale_cloud_bottom",
				"Pressure @ grid_scale_cloud_top", "Pressure @ hybrid", "Pressure @ maximum_wind",
				"Pressure @ shallow_convective_cloud_bottom", "Pressure @ shallow_convective_cloud_top",
				"Pressure @ surface", "Pressure @ tropopause");
		//PROBABILITY_OF_FROZEN_PRECIPITATION = Arrays.asList ("Probability_of_frozen_Precipitation @ surface");
		//RAIN_MIXING_RATIO = Arrays.asList ("Rain_mixing_ratio @ hybrid");
		RELATIVE_HUMIDITY = Arrays.asList ("Relative_humidity @ isobaric", "Relative_humidity @ pressure",
				"Rain_mixing_ratio @ hybrid", "Relative_humidity @ pressure_difference_layer",
				"Relative_humidity @ height_above_ground", "Relative_humidity @ hybrid",
				"Relative_humidity @ sigma_layer", "Relative_humidity @ zeroDegC_isotherm");
		//RIME_FACTOR = Arrays.asList ("Rime_Factor @ hybrid");
		//SENSIBLE_HEAT_NET_FLUX = Arrays.asList ("Sensible_heat_net_flux_0hours @ surface", "Sensible_heat_net_flux @ surface");
		//SNOW_FREE_ALBEDO = Arrays.asList ("Snow-Free_Albedo @ surface");
		//SNOW_COVER = Arrays.asList ("Snow_Cover @ surface");
		//SNOW_DEPTH = Arrays.asList ("Snow_depth @ surface");
		//SNOW_MELT_0_HOURS = Arrays.asList ("Snow_melt_0hours @ surface");
		//SNOW_MIXING_RATIO = Arrays.asList ("Snow_mixing_ratio @ pressure", "Snow_mixing_ratio @ hybrid");
		//SNOW_PHASE_CHANGE_HEAT_FLUX_0_HOURS = Arrays.asList ("Snow_Phase_Change_Heat_Flux_0hours @ surface");
		//SOIL_MOISTURE_CONTENT = Arrays.asList ("Soil_moisture_content @ depth_below_surface_layer");
		//SOIL_MOISTURE_PARAMETER_IN_CANOPY_CONDUCTANCE = Arrays.asList ("Soil_moisture_parameter_in_canopy_conductance @ surface");
		//SOIL_POROSITY = Arrays.asList ("Soil_Porosity @ surface");
		//SOIL_TEMPERATURE = Arrays.asList ("Soil_temperature @ depth_below_surface_layer", "Soil_temperature @ depth_below_surface");
		//SOIL_TYPE = Arrays.asList ("Soil_type_as_in_Zobler @ surface");
		//SOLAR_PARAMETER_IN_CANOPY_CONDUCTANCE = Arrays.asList ("Solar_parameter_in_canopy_conductance @ surface");
		//SPECIFIC_HUMIDITY = Arrays.asList ("Specific_humidity @ pressure", "Specific_humidity @ pressure_difference_layer", "Specific_humidity @ height_above_ground", "Specific_humidity @ hybrid", "Specific_humidity @ surface");
		//STORM_RELATIVE_HELICITY = Arrays.asList ("Storm_relative_helicity @ height_above_ground_layer");
		//STORM_SURFACE_RUNOFF_0_HOURS = Arrays.asList ("Storm_Surface_Runoff_0hours @ surface");
		//STREAM_FUNCTION = Arrays.asList ("Stream_function @ pressure");
		//SURFACE_LIFTED_INDEX = Arrays.asList ("Surface_Lifted_Index @ pressure_layer");
		//SURFACE_ROUGHNESS = Arrays.asList ("Surface_roughness @ surface");
		//SURFACE_SLOPE_TYPE = Arrays.asList ("Surface_Slope_Type @ surface");
		TEMPERATURE = Arrays.asList ("Temperature @ isobaric", "Temperature @ pressure",
				"Temperature @ pressure_difference_layer", "Temperature @ cloud_tops",
				"Temperature @ height_above_ground", "Temperature @ hybrid", "Temperature @ surface",
				"Temperature @ tropopause");
		//TEMPERATURE_PARAMETER_IN_CANOPY_CONDUCTANCE = Arrays.asList ("Temperature_parameter_in_canopy_conductance @ surface");
		//TOTAL_CLOUD_COVER = Arrays.asList ("Total_cloud_cover @ entire_atmosphere");
		//TOTAL_CLOUD_COVER_0_HOURS = Arrays.asList ("Total_cloud_cover_0hours @ entire_atmosphere");
		//TOTAL_COLUMN_INTEGRATED_CLOUD_ICE = Arrays.asList ("Total_Column-Integrated_Cloud_Ice @ entire_atmosphere");
		//TOTAL_COLUMN_INTEGRATED_CLOUD_WATER = Arrays.asList ("Total_Column-Integrated_Cloud_Water @ entire_atmosphere");
		//TOTAL_COLUMN_INTEGRATED_CONDENSATE = Arrays.asList ("Total_Column-Integrated_Condensate @ entire_atmosphere");
		//TOTAL_COLUMN_INTEGRATED_RAIN = Arrays.asList ("Total_Column_Integrated_Rain @ entire_atmosphere");
		//TOTAL_COLUMN_INTEGRATED_SNOW = Arrays.asList ("Total_Column_Integrated_Snow @ entire_atmosphere");
		//TOTAL_CONDENSATE = Arrays.asList ("Total_Condensate @ hybrid");
		//TOTAL_PRECIPITATION_0_HOURS = Arrays.asList ("Total_precipitation_0hours @ surface");
		//TRANSPIRATION_STRESS_ONSET_SOIL_MOISTURE = Arrays.asList ("Transpiration_Stress-onset_soil_moisture @ surface");
		//TURBULENT_KINETIC_ENERGY = Arrays.asList ("Turbulent_kinetic_energy @ pressure", "Turbulent_kinetic_energy @ hybrid");
		U_WIND = Arrays.asList ("u_wind @ isobaric", "U-component_of_wind @ pressure",
				"U-component_of_wind @ pressure_difference_layer", "U-component_of_wind @ hybrid",
				"U-component_of_wind @ height_above_ground", "U-component_of_wind @ maximum_wind",
				"U-component_of_wind @ tropopause");
		//U_STORM_MOTION = Arrays.asList ("U-Component_Storm_Motion @ height_above_ground_layer");
		//UPWARD_LONG_WAVE_RAD_FLUX = Arrays.asList ("Upward_Long-Wave_Rad_Flux @ surface");
		//UPWARD_LONG_WAVE_RAD_FLUX_0_HOURS = Arrays.asList ("Upward_Long-Wave_Rad_Flux_0hours @ surface", "Upward_Long-Wave_Rad_Flux_0hours @ atmosphere_top");
		//UPWARD_SHORT_WAVE_RAD_FLUX = Arrays.asList ("Upward_Short-Wave_Rad_Flux @ surface");
		//UPWARD_SHORT_WAVE_RAD_FLUX_0_HOURS = Arrays.asList ("Upward_Short-Wave_Rad_Flux_0hours @ atmosphere_top", "Upward_Short-Wave_Rad_Flux_0hours @ surface");
		V_WIND = Arrays.asList ("v_wind @ isobaric", "V-component_of_wind @ pressure",
				"V-component_of_wind @ pressure_difference_layer", "V-component_of_wind @ hybrid",
				"V-component_of_wind @ height_above_ground", "V-component_of_wind @ maximum_wind",
				"V-component_of_wind @ tropopause");
		//V_STORM_MOTION = Arrays.asList ("V-Component_Storm_Motion @ height_above_ground_layer");
		//VEGETATION = Arrays.asList ("Vegetation @ surface");
		//VEGETATION_TYPE = Arrays.asList ("Vegetation_Type @ surface");
		//VERTICAL_SPEED_SHEER = Arrays.asList ("Vertical_speed_sheer @ tropopause");
		//VERTICAL_VELOCITY_PRESSURE = Arrays.asList ("Vertical_velocity_pressure @ pressure", "Vertical_velocity_pressure @ pressure_difference_layer", "Vertical_velocity_pressure @ hybrid");
		//VISIBILITY = Arrays.asList ("Visibility @ surface");
		//VOLUMETRIC_SOIL_MOISTURE_CONTENT = Arrays.asList ("Volumetric_Soil_Moisture_Content @ depth_below_surface_layer");
		//WATER_EQUIVALENT_OF_ACCUMULATED_SNOW_DEPTH = Arrays.asList ("Water_equivalent_of_accumulated_snow_depth @ surface");
		//WATER_EQUIVALENT_OF_ACCUMULATED_SNOW_DEPTH_0_HOURS = Arrays.asList ("Water_equivalent_of_accumulated_snow_depth_0hours @ surface");
		//WATER_TEMPERATURE = Arrays.asList ("Water_temperature @ surface");
		//WILTING_POINT = Arrays.asList ("Wilting_Point @ surface");
		//WIND_SPEED_GUST = Arrays.asList ("Wind_speed_gust @ surface");
		// DERIVED PARAMETERS
		SPEED_D = Arrays.asList ("Speed (from u_wind & v_wind)",
				"Speed (from U-component_of_wind & V-component_of_wind)",
				"Speed (from U-component_of_wind_height_above_ground & V-component_of_wind_height_above_ground)");
		//FLOW_VECTORS_D = Arrays.asList ("Flow Vectors (from U-component_of_wind_height_above_ground & V-component_of_wind_height_above_ground)", "Flow Vectors (from U-component_of_wind & V-component_of_wind)");
		//TRUE_WIND_VECTORS_D = Arrays.asList ("True Wind vectors (from U-component_of_wind_height_above_ground & V-component_of_wind_height_above_ground)", "True Wind vectors (from U-component_of_wind & V-component_of_wind)");
		DEWPOINT_D = Arrays.asList ("Dewpoint (from Temperature & Relative_humidity)");
		MIXING_RATIO_D = Arrays.asList ("mixingratio", "Mixing ratio (from Temperature & Relative_humidity)");
		DEWPOINT_DEPRESSION_D = Arrays.asList ("Dewpoint Depression (from Temperature & dewpoint)");
		EQUIVALENT_POTENTIAL_TEMPERATURE_D = Arrays
				.asList ("Equiv. Potential Temperature (from Temperature & Relative_humidity)");
		//SOUNDING_DATA_D = Arrays.asList ("Sounding Data (Temperature & dewpoint only)");
		POTENTIAL_TEMPERATURE_D = Arrays.asList ("Potential Temperature (from Temperature)",
				"Potential Temperature (from Temperature & Pressure)");
		//HORIZONTAL_DIVERGENCE_D = Arrays.asList ("Horizontal Divergence (from U-component_of_wind_height_above_ground & V-component_of_wind_height_above_ground)", "Horizontal Divergence (from U-component_of_wind & V-component_of_wind)");
		//RELATIVE_VORTICITY_D = Arrays.asList ("Relative Vorticity (from U-component_of_wind_height_above_ground & V-component_of_wind_height_above_ground)", "Relative Vorticity (from U-component_of_wind & V-component_of_wind)");
		//ABSOLUTE_VORTICITY_D = Arrays.asList ("Absolute Vorticity (from U-component_of_wind_height_above_ground & V-component_of_wind_height_above_ground)", "Absolute Vorticity (from U-component_of_wind & V-component_of_wind)");
		//HORIZONTAL_ADVECTION_D = Arrays.asList ("Horizontal Advection (from U-component_of_wind_height_above_ground & V-component_of_wind_height_above_ground)", "Horizontal Advection (from U-component_of_wind & V-component_of_wind)");
		//HORIZONTAL_FLUX_DIVERGENCE_D = Arrays.asList ("Horizontal Flux Divergence (from U-component_of_wind_height_above_ground & V-component_of_wind_height_above_ground)", "Horizontal Flux Divergence (from U-component_of_wind & V-component_of_wind)");
		//GEOSTROPHIC_WIND_VECTORS_D = Arrays.asList ("Geostrophic Wind Vectors (from Geopotential_height)");
		RELATIVE_HUMIDITY_D = Arrays.asList ("Relative Humidity (from Temperature & mixingratio)");
		//ISENTROPIC_POTENTIAL_VORTICITY_D = Arrays.asList ("Isentropic Potential Vorticity (from Temperature & absvort)");
		//POTENTIAL_VORTICITY = Arrays.asList ("Potential Vorticity (from theta & flowvectors)");
	}

	@Deprecated
	private ParameterListConstants ()
	{
		// Static final class. No instantiation.
	}

	@Deprecated
	public static List<String> getDefaultGeopodParameters ()
	{
		List<String> paramList = new ArrayList<String> ();

		paramList.add (TEMPERATURE.get (ISOBARIC_INDEX)); // @ isobaric
		paramList.add (TEMPERATURE.get (PRESSURE_INDEX)); // @ pressure

		paramList.add (SPEED_D.get (FROM_U_AND_V_WIND_INDEX)); // from u_wind and v_wind
		paramList.add (SPEED_D.get (FROM_U_AND_V_COMP_INDEX)); // from U-comp & V-comp

		paramList.add (GEOPOTENTIAL_HEIGHT.get (ISOBARIC_INDEX)); // @ isobaric
		paramList.add (GEOPOTENTIAL_HEIGHT.get (PRESSURE_INDEX)); // @ pressure

		paramList.add (RELATIVE_HUMIDITY.get (ISOBARIC_INDEX)); // @ isobaric
		paramList.add (RELATIVE_HUMIDITY.get (PRESSURE_INDEX)); // @ pressure

		paramList.add (DEWPOINT_D.get (FROM_T_AND_RH_INDEX)); // from T & RH
		paramList.add (DEWPOINT_TEMPERATURE.get (PRESSURE_INDEX)); // @ pressure

		paramList.add (MIXING_RATIO_D.get (MIXINGRATIO_INDEX)); // mixingratio

		return Collections.unmodifiableList (paramList);
	}

	@Deprecated
	public static List<String> getNonRemoveableParameters ()
	{
		List<String> paramList = new ArrayList<String> ();

		paramList.add (TEMPERATURE.get (ISOBARIC_INDEX)); // @ isobaric
		paramList.add (TEMPERATURE.get (PRESSURE_INDEX)); // @ pressure

		paramList.add (RELATIVE_HUMIDITY.get (ISOBARIC_INDEX)); // @ isobaric
		paramList.add (RELATIVE_HUMIDITY.get (PRESSURE_INDEX)); // @ pressure

		return Collections.unmodifiableList (paramList);
	}

	@Deprecated
	public static Set<String> getDefaultDropsondeParameters ()
	{
		Set<String> paramList = new HashSet<String> ();

		paramList.add (TEMPERATURE.get (ISOBARIC_INDEX)); // @ isobaric
		paramList.add (TEMPERATURE.get (PRESSURE_INDEX)); // @ pressure

		paramList.add (DEWPOINT_D.get (FROM_T_AND_RH_INDEX)); // from T & RH
		paramList.add (DEWPOINT_TEMPERATURE.get (PRESSURE_INDEX)); // @ pressure

		paramList.add (U_WIND.get (ISOBARIC_INDEX)); // @ isobaric
		paramList.add (U_WIND.get (PRESSURE_INDEX)); // @ pressure

		paramList.add (V_WIND.get (ISOBARIC_INDEX)); // @ isobaric
		paramList.add (V_WIND.get (PRESSURE_INDEX)); // @ pressure

		paramList.add (RELATIVE_HUMIDITY.get (ISOBARIC_INDEX)); // @ isobaric
		paramList.add (RELATIVE_HUMIDITY.get (PRESSURE_INDEX)); // @ pressure

		paramList.add (EQUIVALENT_POTENTIAL_TEMPERATURE_D.get (FROM_T_AND_RH_INDEX)); // from T & RH
		paramList.add (POTENTIAL_TEMPERATURE.get (HEIGHT_ABOVE_GROUND_INDEX)); // @ height above ground

		return Collections.unmodifiableSet (paramList);
	}

}
