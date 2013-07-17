package geopod.gui.panels.navigation;

import geopod.Geopod;
import geopod.constants.UIConstants;
import geopod.devices.AttitudeIndicator;
import geopod.devices.Compass;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.Hud;
import geopod.gui.components.GeopodButton;
import geopod.gui.components.GeopodPulseTextField;
import geopod.utils.component.LatLonAltTextFieldManager;
import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;
import geopod.utils.math.VisadUtility;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Canvas3D;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.Animator.EndBehavior;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

import visad.georef.EarthLocation;
import visad.georef.LatLonPoint;

/**
 * The NavigationPanel is a specialized {@link JPanel} that features views
 * associated with the Geopod's navigation system. These views include a
 * {@link Compass}, a lat/lon/alt display widget, and buttons. The lat/lon/alt
 * display widget is responsible for the proper formatting and display of the
 * Geopod's current {@link EarthLocation}
 * 
 * @author Geopod Team
 */
public class NavigationPanel
		extends JPanel

{
	private static final long serialVersionUID = -8744120545646289919L;

	private static final int NUM_PULSES;
	private static final int ANIMATION_DURATION;

	static
	{
		NUM_PULSES = 6;
		ANIMATION_DURATION = 500;
	}

	private GeopodPulseTextField m_latitudeInput;
	private GeopodPulseTextField m_longitudeInput;
	private GeopodPulseTextField m_altitudeInput;

	private Hud m_hud;

	private Geopod m_geopod;

	private List<Animator> m_animators;

	private static LatLonAltTextFieldManager m_latLonAltManager;

	/**
	 * Constructs a transparent NavigationPanel
	 * 
	 * @param hud
	 *            the {@link Hud} to serve as a listener
	 * @param geopod
	 *            the {@link Geopod}
	 */
	public NavigationPanel (Hud hud, Geopod geopod, Canvas3D canvas)
	{
		m_hud = hud;
		m_geopod = geopod;

		setupPanelProperties ();
		setupPanelLayout ();

		AttitudeIndicator attitudeIndicator = m_geopod.getAttitudeIndicator ();
		// Changed gapright from 15 to 5 for extra space
		super.add (attitudeIndicator, "gapleft 10, spany 3, gapright 5, split 2");

		Compass compass = m_geopod.getCompass ();
		super.add (compass, "gapleft 5, gapright 5");

		buildLatLonDisplay ();
		buildNavigationButtons ();

		FocusTraversalPolicy traversalPolicy = new LatLonAltTraversalPolicy (canvas);
		this.setFocusCycleRoot (true);
		this.setFocusTraversalPolicy (traversalPolicy);
	}

	/**
	 * Makes this NavigationPanel transparent
	 */
	private void setupPanelProperties ()
	{
		super.setOpaque (false);
		if (Debug.levelAtLeast (DebugLevel.HIGH))
		{
			super.setBorder (BorderFactory.createLineBorder (Color.green));
		}
	}

	/**
	 * Create, setup, and add a {@link LayoutManager}
	 */
	private void setupPanelLayout ()
	{
		super.setLayout (new MigLayout ("ins 0, aligny center"));
	}

	/**
	 * Create and add navigation buttons
	 */
	private void buildNavigationButtons ()
	{
		String imageUp = "//Resources/Images/User Interface/Buttons/NavigateButton.png";
		String imageDown = "//Resources/Images/User Interface/Buttons/NavigateButtonDown.png";
		String imageHover = "//Resources/Images/User Interface/Buttons/NavigateButtonHover.png";

		GeopodButton flyToButton = new GeopodButton (imageUp, imageDown, imageHover);
		flyToButton.addActionListener (m_hud);
		flyToButton.setActionCommand ("flyTo");
		flyToButton.setToolTipText (" Fly ");
		m_hud.addObserver (flyToButton, GeopodEventId.GO_BUTTON_STATE_CHANGED);

		super.add (flyToButton, "aligny center, gaptop 0, spany 3, cell 3 0");
	}

	/**
	 * Create and add a lat lon display widget
	 */
	private void buildLatLonDisplay ()
	{
		// Use this to make lat, lon, alt tooltips with proper units
		EarthLocation el = m_geopod.getEarthLocation ();
		String latitudeTooltip = "Latitude (" + VisadUtility.getLongitudeUnitIdentifier (el) + ") ";
		String longitudeTooltip = " Longitude (" + VisadUtility.getLongitudeUnitIdentifier (el) + ") ";
		String altitudeTooltip = " Altitude (" + VisadUtility.getAltitudeUnitIdentifier (el) + ") ";

		Font labelFont = UIConstants.GEOPOD_BANDY;
		labelFont = labelFont.deriveFont (16.0f);

		ChangeListener changeListener = new ChangeListener ();

		final int textFieldColumns = 7;
		m_latitudeInput = new GeopodPulseTextField (textFieldColumns);
		m_latitudeInput.setToolTipText (latitudeTooltip);
		m_latitudeInput.addKeyListener (changeListener);

		PropertySetter setter = new PropertySetter (m_latitudeInput, "backgroundAlpha", 0.0f, 1.0f);

		m_animators = new ArrayList<Animator> ();
		Animator latitudeAnimator = new Animator (ANIMATION_DURATION, NUM_PULSES, Animator.RepeatBehavior.REVERSE,
				setter);
		latitudeAnimator.setEndBehavior (EndBehavior.RESET);

		JLabel latLabel = new JLabel ("LAT");
		latLabel.setFont (labelFont);
		latLabel.setForeground (UIConstants.GEOPOD_GREEN);

		m_longitudeInput = new GeopodPulseTextField (textFieldColumns);
		m_longitudeInput.setToolTipText (longitudeTooltip);
		m_longitudeInput.addKeyListener (changeListener);

		//PulsatingBorder longitudeBorder = new PulsatingBorder (m_longitudeInput);
		//m_longitudeInput.setBorder (new CompoundBorder(m_longitudeInput, longitudeBorder));
		PropertySetter longitudePropertySetter = new PropertySetter (m_longitudeInput, "backgroundAlpha", 0.0f, 1.0f);
		Animator longitudeAnimator = new Animator (ANIMATION_DURATION, NUM_PULSES, Animator.RepeatBehavior.REVERSE,
				longitudePropertySetter);
		longitudeAnimator.setEndBehavior (EndBehavior.RESET);

		JLabel lonLabel = new JLabel ("LON");
		lonLabel.setFont (labelFont);
		lonLabel.setForeground (UIConstants.GEOPOD_GREEN);

		m_altitudeInput = new GeopodPulseTextField (textFieldColumns);
		m_altitudeInput.setToolTipText (altitudeTooltip);
		m_altitudeInput.addKeyListener (changeListener);

		//PulsatingBorder altitudeBorder = new PulsatingBorder (m_altitudeInput);
		//m_altitudeInput.setBorder (new CompoundBorder(m_altitudeInput.getBorder (), altitudeBorder));
		PropertySetter altitudePropertySetter = new PropertySetter (m_altitudeInput, "backgroundAlpha", 0.0f, 1.0f);
		Animator altitudeAnimator = new Animator (ANIMATION_DURATION, NUM_PULSES, Animator.RepeatBehavior.REVERSE,
				altitudePropertySetter);
		altitudeAnimator.setEndBehavior (EndBehavior.RESET);

		m_animators.add (latitudeAnimator);
		m_animators.add (altitudeAnimator);
		m_animators.add (longitudeAnimator);

		JLabel altLabel = new JLabel ("ALT");
		altLabel.setFont (labelFont);
		altLabel.setForeground (UIConstants.GEOPOD_GREEN);

		m_latLonAltManager = new LatLonAltTextFieldManager (m_latitudeInput, m_longitudeInput, m_altitudeInput);
		super.add (latLabel, "split 1");
		super.add (m_latitudeInput, "wrap");
		super.add (lonLabel, "split 1");
		super.add (m_longitudeInput, "wrap");
		super.add (altLabel, "split 1");
		super.add (m_altitudeInput, "wrap");
	}

	/**
	 * Sets the latitude, longitude, and altitude fields to the values specified
	 * by a given {@link EarthLocation}
	 * 
	 * @param el
	 *            the specified EarthLocation
	 */
	public void setInputEarthLocation (EarthLocation el)
	{
		m_latLonAltManager.setInputEarthLocation (el);
	}

	/**
	 * Sets the latitude, longitude, and altitude fields to the values specified
	 * individually
	 * 
	 * @param latitude
	 *            the specified latitude
	 * @param longitude
	 *            the specified longitude
	 * @param altitude
	 *            the specified altitude
	 */
	public void setInputEarthLocation (double latitude, double longitude, double altitude)
	{
		m_latLonAltManager.setInputEarthLocation (latitude, longitude, altitude);
	}

	/**
	 * Sets only the Latitude and Longitude of the lat/lon/alt display to the
	 * specified {@link LatLonPoint}
	 * 
	 * @param llp
	 *            the specified Latitude and Longitude
	 */
	public void setInputLatLon (LatLonPoint llp)
	{
		// need geopod's earth location to give altitude a reasonable value if needed
		m_latLonAltManager.setInputLatLon (llp, m_geopod.getEarthLocation ());
	}

	/**
	 * Returns an {@link EarthLocation} that represents the current values in
	 * the lat/lon/alt display.
	 * 
	 * @return the currently displayed EarthLocation, null if the current values
	 *         cannot be parsed.
	 */
	public EarthLocation parseInputtedEarthLocation ()
	{
		return (m_latLonAltManager.parseInputtedEarthLocation ());
	}

	/**
	 * Returns an {@link EarthLocation} that represents the current values in
	 * the lat/lon/alt display.
	 * 
	 * @return the currently displayed EarthLocation, null if the current values
	 *         cannot be parsed.
	 */
	public EarthLocation parseAndValidateInputtedEarthLocation ()
	{
		return (m_latLonAltManager.parseAndValidateInputtedEarthLocation ());
	}

	/**
	 * Provides a contextual clue that the lat/lon/alt display has changed
	 */
	public void indicateValueChange ()
	{
		//ThreadUtility.execute (m_blinkAnimator);

		for (Animator a : m_animators)
		{
			if (!a.isRunning ())
			{
				a.start ();
			}
		}
		// this.setInputAreaBackgroundColor (Color.blue);
	}

	/**
	 * Resets the background colors of the lat/lon/alt display
	 */
	public void resetColors ()
	{
		m_latLonAltManager.resetColors ();
	}

	private class ChangeListener
			extends KeyAdapter
	{
		@Override
		public void keyReleased (KeyEvent e)
		{
			if (e.getKeyCode () == KeyEvent.VK_ENTER)
			{
				ActionEvent flyToEvent = new ActionEvent (m_altitudeInput, ActionEvent.ACTION_PERFORMED, "flyTo");
				m_hud.actionPerformed (flyToEvent);
			}
			else
			{
				m_latLonAltManager.validateLatLonAlt ();
			}
		}
	}

	private class LatLonAltTraversalPolicy
			extends FocusTraversalPolicy
	{
		private Canvas3D m_canvas;

		public LatLonAltTraversalPolicy (Canvas3D canvas)
		{
			m_canvas = canvas;
		}

		@Override
		public Component getComponentAfter (Container arg0, Component arg1)
		{
			if (arg1.equals (m_latitudeInput))
			{
				return (m_longitudeInput);
			}
			else if (arg1.equals (m_longitudeInput))
			{
				return (m_altitudeInput);
			}
			else
			{
				return (m_canvas);
			}
		}

		@Override
		public Component getComponentBefore (Container arg0, Component arg1)
		{
			if (arg1.equals (m_longitudeInput))
			{
				return (m_latitudeInput);
			}
			else if (arg1.equals (m_altitudeInput))
			{
				return (m_longitudeInput);
			}
			else
			{
				return (m_canvas);
			}
		}

		@Override
		public Component getDefaultComponent (Container arg0)
		{
			return (m_canvas);
		}

		@Override
		public Component getFirstComponent (Container arg0)
		{
			return (m_canvas);
		}

		@Override
		public Component getLastComponent (Container arg0)
		{
			return (m_canvas);
		}

	}
}
