package geopod.constants;

import java.awt.Dimension;
import java.awt.Rectangle;

public final class FrameConstants
{
	public static final Dimension CANVAS_SIZE;
	public static final Dimension FRAME_SIZE_IN_IMAGE;
	public static final Dimension FRAME_OPTIMUM_SIZE;
	public static final Dimension FRAME_MINIMUM_SIZE;

	public static final Rectangle INNER_VIEWING_AREA_BOUNDS;

	// Buttons
	public static final Rectangle CONFIG_BUTTON_BOUNDS;
	public static final Rectangle HELP_BUTTON_BOUNDS;
	public static final Rectangle IDV_BUTTON_BOUNDS;
	public static final Rectangle MU_BUTTON_BOUNDS;

	// Logos
	public static final Rectangle NFS_LOGO_BOUNDS;

	public static final Rectangle PRIMARY_BUTTON_PANEL_BOUNDS;
	public static final Rectangle TOOL_PANEL_BOUNDS;
	public static final Rectangle PRIMARY_DISPLAY_PANEL_BOUNDS;
	public static final Rectangle OVERFLOW_PANEL_BOUNDS;
	public static final Rectangle LOOK_UP_PANEL_BOUNDS;
	public static final Rectangle STATUS_PANEL_BOUNDS;
	public static final Rectangle NAVIGATION_PANEL_BOUNDS;
	public static final Rectangle NAV_EXT_PANEL_BOUNDS;
	public static final Rectangle PARTICLE_PANEL_BOUNDS;
	public static final Rectangle DROPSONDE_PANEL_BOUNDS;

	public static final Rectangle TOP_VIEW_CANVAS_BOUNDS;

	// Panels centered relative to the Inner Viewing Area
	public static final Rectangle PARAMETER_CHOOSER_PANEL_BOUNDS;
	public static final Rectangle HELP_PANEL_BOUNDS;
	public static final Rectangle LOADING_PANEL_BOUNDS;
	public static final Rectangle EVENT_NOTIFICATION_PANEL_BOUNDS;
	public static final Rectangle COMMENT_PROMPT_PANEL_BOUNDS;
	public static final Rectangle NOTED_LOCATIONS_PANEL_BOUNDS;
	public static final Rectangle ISOSURFACE_VIEW_PANEL_BOUNDS;
	public static final Rectangle DISTANCE_PANEL_BOUNDS;
	public static final Rectangle MOVIECAPTURE_PANEL_BOUNDS;
	public static final Rectangle CONFIG_PANEL_BOUNDS;

	public static final String APPLICATION_ICON_PATH;
	public static final String NSF_LOGO_PATH;

	static
	{
		CANVAS_SIZE = new Dimension (1024, 640);
		FRAME_SIZE_IN_IMAGE = new Dimension (1680, 1050);
		FRAME_OPTIMUM_SIZE = new Dimension (1440, 900);
		FRAME_MINIMUM_SIZE = new Dimension (1296, 768);

		INNER_VIEWING_AREA_BOUNDS = new Rectangle (75, 75, 1532, 850);

		CONFIG_BUTTON_BOUNDS = new Rectangle (1608, 75, 75, 80);
		HELP_BUTTON_BOUNDS = new Rectangle (1608, 0, 75, 80);
		IDV_BUTTON_BOUNDS = new Rectangle (935, 35, 105, 40);
		MU_BUTTON_BOUNDS = new Rectangle (625, 35, 105, 40);

		NFS_LOGO_BOUNDS = new Rectangle (0, 0, 75, 80);

		PRIMARY_BUTTON_PANEL_BOUNDS = new Rectangle (0, 930, 480, 120);
		TOOL_PANEL_BOUNDS = new Rectangle (10, 600, 60, 300);
		PRIMARY_DISPLAY_PANEL_BOUNDS = new Rectangle (490, 948, 703, 95);
		OVERFLOW_PANEL_BOUNDS = new Rectangle (75, 325, 200, 600);
		LOOK_UP_PANEL_BOUNDS = new Rectangle (505, 570, 655, 355);
		STATUS_PANEL_BOUNDS = new Rectangle (78, 78, 200, 50);
		NAVIGATION_PANEL_BOUNDS = new Rectangle (1194, 930, 485, 120);
		NAV_EXT_PANEL_BOUNDS = new Rectangle (1615, 600, 60, 300);
		PARTICLE_PANEL_BOUNDS = new Rectangle (1200, 78, 406, 337);
		DROPSONDE_PANEL_BOUNDS = new Rectangle (1200, 420, 406, 505);
		// The bounding rectangle height and the panel height should be the same
		DISTANCE_PANEL_BOUNDS = centerRectangle (new Rectangle (78, 78, INNER_VIEWING_AREA_BOUNDS.width, 190), new Dimension (412, 190));
		MOVIECAPTURE_PANEL_BOUNDS = centerRectangle (INNER_VIEWING_AREA_BOUNDS, new Dimension (1140, 820));
		
		TOP_VIEW_CANVAS_BOUNDS = new Rectangle (78, 78, 225, 225);

		PARAMETER_CHOOSER_PANEL_BOUNDS = centerRectangle (INNER_VIEWING_AREA_BOUNDS, new Dimension (1500, 820));
		HELP_PANEL_BOUNDS = centerRectangle (INNER_VIEWING_AREA_BOUNDS, new Dimension (360, 480));
		LOADING_PANEL_BOUNDS = centerRectangle (INNER_VIEWING_AREA_BOUNDS, new Dimension (500, 200));
		EVENT_NOTIFICATION_PANEL_BOUNDS = centerRectangle (INNER_VIEWING_AREA_BOUNDS, new Dimension (500, 200));
		COMMENT_PROMPT_PANEL_BOUNDS = centerRectangle (INNER_VIEWING_AREA_BOUNDS, new Dimension (500, 200));
		NOTED_LOCATIONS_PANEL_BOUNDS = centerRectangle (INNER_VIEWING_AREA_BOUNDS, new Dimension (730, 820));
		ISOSURFACE_VIEW_PANEL_BOUNDS = centerRectangle (INNER_VIEWING_AREA_BOUNDS, new Dimension (720, 820));

		CONFIG_PANEL_BOUNDS = centerRectangle (INNER_VIEWING_AREA_BOUNDS, new Dimension (570, 820));

		APPLICATION_ICON_PATH = "//Resources/Images/User Interface/GeopodLogo.png";
		NSF_LOGO_PATH = "//Resources/Images/User Interface/NSF_Logo.png";
	}
	
	private FrameConstants() {
		// Uninstantiable class. Private constructor.
	}

	private static final Rectangle centerRectangle (final Rectangle outerRectangle, final Dimension innerDimension)
	{
		int widthOuter = outerRectangle.width;
		int heightOuter = outerRectangle.height;

		int widthInner = innerDimension.width;
		int heightInner = innerDimension.height;

		int deltaX = (widthOuter - widthInner) / 2;
		int deltaY = (heightOuter - heightInner) / 2;

		int xOffset = outerRectangle.x + deltaX;
		int yOffset = outerRectangle.y + deltaY;

		Rectangle centeredRect = new Rectangle (xOffset, yOffset, widthInner, heightInner);

		return (centeredRect);
	}

}
