package geopod.gui.panels;

import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.Hud;
import geopod.gui.components.GeopodButton;
import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;

import javax.swing.JPanel;

import ucar.visad.display.AnimationWidget;

/** Controls which time frame (or animation) is being shown in the scene by calling AnimationWidget functions.
 * 
 * During my Windows 7 testing, a few seconds after Geopod finished loading all of its data choices, the IDV window
 * captioned "Dashboard" would move to front, grab focus, wait for about one second, then move to back and relinquish focus
 * to the Geopod window. Afterwards, whenever the animation was changed, the entire program would hang for about one second.
 * This peculiarity happens every run, even on the previous commit that does not include these controls, and it happens 
 * reliably (every time) on IDV versions 4.0u1, 4.1, and 4.1u1. Reason unknown.
 * 
 * To be added is a button with a cog icon to bring up the "options" menu.
 * 
 * @author Samuel Wiley
 *
 */
public class AnimationControlPanel extends JPanel implements ISubject {
	
	/**************************** Ideas for later ************************/

	private static final long serialVersionUID = -5949653667709670679L;
	
	private GeopodButton m_btnPrevious;
	private GeopodButton m_btnPlay;
	private GeopodButton m_btnNext;
	
	private Hud m_hud;
	private SubjectImpl m_subjectImpl;
	
	private AnimationWidget m_animationWidget;

	public AnimationControlPanel (Hud hud) {
		m_hud = hud;
		m_subjectImpl = new SubjectImpl ();
		
		m_animationWidget = m_hud.getAnimationWidget();
		
		initButtons ();
		this.setOpaque (false);
	}
	
	private void initButtons() {
		String iconPath = "//Resources/Images/User Interface/Buttons/";
		m_btnPrevious = new GeopodButton (iconPath + "zPrevious.png", iconPath + "zPrevious.png", iconPath + "zPreviousPressed.png", iconPath + "zPreviousPressed.png", iconPath + "zPreviousHover.png", iconPath + "zPreviousHover.png");
		m_btnPlay = new GeopodButton (iconPath + "zPlay.png", iconPath + "zPause.png", iconPath + "zPlayPressed.png", iconPath + "zPausePressed.png", iconPath + "zPlayHover.png", iconPath + "zPauseHover.png");
		m_btnNext = new GeopodButton (iconPath + "zNext.png", iconPath + "zNext.png", iconPath + "zNextPressed.png", iconPath + "zNextPressed.png", iconPath + "zNextHover.png", iconPath + "zNextHover.png");
		
		m_btnPrevious.setToolTipText ("Previous Time Frame");
		m_btnPlay.setToolTipTexts ("Play Animation", "Pause Animation");
		m_btnNext.setToolTipText ("Next Time Frame");
		
		m_btnPrevious.setActionCommand ("animationPrevious");
		m_btnPlay.setActionCommand ("animationPlay");
		m_btnNext.setActionCommand ("animationNext");
		
		m_btnPrevious.addActionListener (m_hud);
		m_btnPlay.addActionListener (m_hud);
		m_btnNext.addActionListener (m_hud);
		
		addObserver (m_btnPrevious, GeopodEventId.ANIMATION_CONTROL_PREVIOUS_BUTTON_STATE_CHANGED);
		addObserver (m_btnPlay, GeopodEventId.ANIMATION_CONTROL_PLAY_BUTTON_STATE_CHANGED);
		addObserver (m_btnNext, GeopodEventId.ANIMATION_CONTROL_NEXT_BUTTON_STATE_CHANGED);
		
		this.add (m_btnPrevious, "gapleft 10, gapright 4");
		this.add (m_btnPlay, "gapleft 4, gapright 4");
		this.add (m_btnNext, "gapleft 4, gapright 10");
	}

	public void playPause() {
		Debug.println(DebugLevel.MEDIUM, "AnimationControlPanel.playPause()");
		notifyObservers (GeopodEventId.ANIMATION_CONTROL_PLAY_BUTTON_STATE_CHANGED);

		m_animationWidget.actionPerformed(AnimationWidget.CMD_STARTSTOP);
	}
	
	public void previous() {
		Debug.println(DebugLevel.MEDIUM, "AnimationControlPanel.previous()");
		m_animationWidget.actionPerformed(AnimationWidget.CMD_BACKWARD);
	}
	
	public void next() {
		Debug.println(DebugLevel.MEDIUM, "AnimationControlPanel.next()");
		m_animationWidget.actionPerformed(AnimationWidget.CMD_FORWARD);
	}
	
	/** No button yet implements this function. */
	public void showOptions() {
		Debug.println(DebugLevel.MEDIUM, "AnimationControlPanel.showOptions()");
		m_animationWidget.actionPerformed(AnimationWidget.CMD_PROPS);
	}
	
	@Override
	public void addObserver (IObserver observer, GeopodEventId eventId) {
		m_subjectImpl.addObserver (observer, eventId);
	}

	@Override
	public void removeObserver (IObserver observer, GeopodEventId eventId) {
		m_subjectImpl.removeObserver (observer, eventId);
	}

	@Override
	public void removeObservers () {
		m_subjectImpl.removeObservers ();
	}

	@Override
	public void notifyObservers (GeopodEventId eventId) {
		m_subjectImpl.notifyObservers (eventId);
	}
	
}
