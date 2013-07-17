package geopod.constants.parameters.enums;

/**
 * Represents a normal, or intrinsic parameter.
 * 
 * @author Geopod Team
 * 
 */
public enum IntrinsicParameter
{
	//THOUSAND_FIVE_HUNDRED_HPA_THICKNESS("1000-500 hPa Thickness"),
	//AGEOSTROPHIC_WIND("AGeostrophic Wind"),
	//AGEOSTROPHIC_WIND_VECTORS("AGeostrophic Wind Vectors"),
	//ABSOLUTE_VORTICITY("Absolute Vorticity"),
	//BEST_4_LAYER_LIFTED_INDEX("Best 4 layer lifted index"),
	//CONVECTIVE_AVAILABLE_POTENTIAL_ENERGY("Convective Available Potential Energy"),
	//CONVECTIVE_INHIBITION("Convective inhibition"),
	//CONVECTIVE_PRECIPITATION("Convective precipitation (Mixed_intervals Accumulation)"),
	DEWPOINT("Dewpoint"),
	DEWPOINT_DEPRESSION("Dewpoint Depression"),
	EQUIVALENT_POTENTIAL_TEMPERATURE("Equiv. Potential Temperature"),
	//FLOW_VECTORS("Flow Vectors"),
	GEOPOTENTIAL_HEIGHT("Geopotential height"),
	//GEOSTOPHIC_WIND("Geostrophic Wind"),
	//GEOSTOPHIC_WIND_VECTORS("Geostrophic Wind Vectors"),
	//GRID_2D_TRAJECTORY("Grid 2D Trajectory"),
	//GRID_3D_TRAJECTORY("Grid 3D Trajectory"),
	//HORIZONTAL_ADVECTION("Horizontal Advection"),
	//HORIZONTAL_DIVERGENCE("Horizontal Divergence"),
	//HORIZONTAL_FLUX_DIVERGENCE("Horizontal Flux Divergence"),
	//ISENTROPIC_POTENTIAL_VORTICITY("Isentropic Potential Vorticity"),
	//MEAN_SEA_LEVEL_PRESSURE_NAM_MODEL_REDUCTION("Mean Sea Level Pressure NAM Model Reduction"),
	MIXING_RATIO("Mixing ratio"),
	//PARCEL_LIFTED_INDEX_TO_500_HPA("Parcel lifted index to 500 hPa"),
	POTENTIAL_TEMPERATURE("Potential Temperature"),
	//POTENTIAL_VORTICITY("Potential Vorticity"),
	//PRECIPITABLE_WATER("Precipitable water"),
	PRESSURE("Pressure"),
	//PRESSURE_REDUCED_TO_MSL("Pressure reduced to MSL"),
	RELATIVE_HUMIDITY_U("Relative Humidity"),
	RELATIVE_HUMIDITY_L("Relative humidity"),
	//RELATIVE_VORTICITY = "Relative Vorticity";
	SPEED("Speed"),
	//STORM_RELATIVE_HELICITY = "Storm relative helicity";
	TEMPERATURE("Temperature"),
	//THICKNESS = "Thickness";
	//TOTAL_PRECIPITATION = "Total precipitation (Mixed_intervals Accumulation)";
	//TRUE_WIND_VECTORS = "True Wind vectors";
	//VERTICAL_VELOCITY_PRESSURE = "Vertical velocity pressure";
	U_WIND("u-component of wind"),
	V_WIND("v-component of wind"), ;

	private String name;

	private IntrinsicParameter (final String name)
	{
		this.name = name;
	}

	@Override
	public String toString ()
	{
		return name;
	}
}