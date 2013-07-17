package geopod.constants.parameters.enums;

/**
 * Represents a modifier attached to a component parameter to distinguish it
 * from other component parameters.
 * 
 * @author Geopod Team
 * 
 */
public enum ComponentAtLevelModifier
{
	_ISOBARIC("_isobaric"),
	_HEIGHT_ABOVE_GROUND("_height_above_ground"),
	;

	private String name;

	private ComponentAtLevelModifier (String name)
	{
		this.name = name;
	}

	@Override
	public String toString ()
	{
		return name;
	}

}
