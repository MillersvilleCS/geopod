package geopod.constants.parameters.enums;

/**
 * Represents a component parameter used in deriving other parameters.
 * 
 * @author Geopod Team
 * 
 */
public enum ComponentParameter
{
	U_WIND("u-component_of_wind"), V_WIND("v-component_of_wind"),
	//GEOPOTENTIAL_HEIGHT("Geopotential_height"),
	//VERTICAL_VELOCITY_PRESSURE("Vertical_velocity_pressure"),
	TEMPERATURE("Temperature"),
	RELATIVE_HUMIDITY("Relative_humidity"),
	//MIXING_RATIO("mixingratio"),
	DEWPOINT("dewpoint"),
	//ABSOLUTE_VORTICITY("absvort"),
	//DPDK("dpdk"), // I have no idea what this is, but it was present.
	//THETA_D("theta"),
	//FLOW_VECTORS("flowvectors"),
	;

	private String name;

	private ComponentParameter (String name)
	{
		this.name = name;
	}

	@Override
	public String toString ()
	{
		return name;
	}
}