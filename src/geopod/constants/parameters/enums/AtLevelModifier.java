package geopod.constants.parameters.enums;

/**
 * Represents a modifier attached to a normal parameter name. For example:
 * Geopotential Height @ Isobaric
 *                     ==========
 * AT_ISOBARIC is used to distinguish the IntrinsicParameter GEOPOTENTIAL_HEIGHT
 * from other similar readings.
 * 
 * @author Geopod Team
 * 
 */
public enum AtLevelModifier
{
	//AT_0DEG_ISOTHERM("Level of 0deg C isotherm"),
	//AT_CLOUD_BASE_LEVEL("Cloud base level"),
	//AT_ENTIRE_ATMOSPHERE("Entire atmosphere"),
	//AT_GROUND_OR_WATER_SURFACE("Ground or water surface"),
	AT_HEIGHT_ABOVE_GROUND("Specified height level above ground"),
	AT_ISOBARIC("Isobaric surface"),
	
	AT_ISOBARIC_WRF("isobaric"),
	AT_SURFACE_WRF("surface"),
	AT_PRESSURE_WRF("pressure"),
	AT_HEIGHT_ABOVE_GROUND_WRF("height_above_ground"),
	
	//AT_LEVEL_OF_CLOUD_TOPS("Level of cloud tops"),
	//AT_MAXIMUM_WIND_LEVEL("Maximum wind level"),
	//AT_MEAN_SEA_LEVEL("Mean sea level"),
	AT_PRESSURE("Layer between 2 level at pressure difference from ground to level layer"),
	//AT_TROPOPAUSE("Tropopause"),
	;

	private String name;

	private AtLevelModifier (final String name)
	{
		this.name = name;
	}

	@Override
	public String toString ()
	{
		return name;
	}
}
