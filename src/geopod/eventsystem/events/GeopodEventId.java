package geopod.eventsystem.events;

/**
 * An enumeration of possible events used by the {@link geopod.eventsystem event
 * system}.
 * 
 * @author Geopod Team
 * 
 */
public enum GeopodEventId
{
	PARTICLE_IMAGED,
	PARTICLE_IMAGER_OPENED,
	PARTICLE_IMAGER_CLOSED,
	GEOPOD_TRANSLATED,
	DROPSONDE_LAUNCHED,
	GRID_POINT_SELECTED,
	TIME_CHANGED,
	// For each choice
	DATA_CHOICE_LOADING_STARTED,
	DATA_CHOICE_LOADING_FINISHED,
	// When all choices have finished loading
	ALL_CHOICES_LOADING_FINISHED,
	DISPLAY_PANEL_EMPTY,
	DISPLAY_PANEL_ACTIVE,
	LOCATION_NOTED,
	ISOSURFACE_LOCKED,
	ISOSURFACE_UNLOCKED,
	// TODO: Remove when we get notification from IDV about ThreeDSurfaceControl events, 
	// this is for TreeDSurfaceControl, IsovalueTextField
	ISOSURFACE_LEVEL_CHANGED,
	ISOSURFACE_BUTTON_STATE_CHANGED,
	KEY_PRESSED,
	NOTEPAD_BUTTON_STATE_CHANGED,
	CALC_BUTTON_STATE_CHANGED,
	ADDNOTE_BUTTON_STATE_CHANGED,
	DROPSONDE_BUTTON_STATE_CHANGED,
	PARAMETER_BUTTON_STATE_CHANGED,
	GO_BUTTON_STATE_CHANGED,
	LOOKUP_BUTTON_STATE_CHANGED,
	LOCK_BUTTON_STATE_CHANGED,
	HELP_BUTTON_STATE_CHANGED,
	CONFIG_BUTTON_STATE_CHANGED,
	PARTICLE_BUTTON_STATE_CHANGED,
	GRIDPOINTS_BUTTON_STATE_CHANGED,
	// for distance traveled button
	DISTANCE_BUTTON_STATE_CHANGED, 
	MOVIECAPTURE_BUTTON_STATE_CHANGED,
	AUTO_PILOT_FINISHED,
	REQUEST_FLIGHT_LOG_RESET, 
}