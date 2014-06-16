package geopod.constants.parameters;

public final class SimpleParameterNameConstants
{
	public static final String TEMPERATURE;
	public static final String GEOPOTENTIAL_HEIGHT;
	public static final String SPEED;
	public static final String RELATIVE_HUMIDITY;
	public static final String DEWPOINT;

	static
	{
		TEMPERATURE = "T";
		GEOPOTENTIAL_HEIGHT = "Z";
		SPEED = "WS";
		RELATIVE_HUMIDITY = "RH";
		DEWPOINT = "Td";
	}

	private SimpleParameterNameConstants ()
	{
		// Static final class. No instantiation.
	}
}
