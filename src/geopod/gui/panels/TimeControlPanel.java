package geopod.gui.panels;

import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.Hud;
import geopod.gui.components.GeopodButton;

import javax.swing.JPanel;

public class TimeControlPanel extends JPanel implements ISubject {
	
	/**************************** Ideas for later ************************/
	/*
	 * Loop button to loop time animation during play
	 * 
	 */
	
	
	private static final long serialVersionUID = -5949653667709670679L;
	
	private GeopodButton m_btnPrevious;
	private GeopodButton m_btnPlay;
	private GeopodButton m_btnNext;
	
	private Hud m_hud;
	private SubjectImpl m_subjectImpl;

	public TimeControlPanel (Hud hud) {
		m_hud = hud;
		m_subjectImpl = new SubjectImpl ();
		
		initButtons ();
		this.setOpaque (false);
	}
	
	private void initButtons() {
		String iconPath = "//Resources/Images/User Interface/Buttons/";
		m_btnPrevious = new GeopodButton (iconPath + "zPrevious.png", iconPath + "zPrevious.png", iconPath + "zPreviousPressed.png", iconPath + "zPreviousPressed.png", iconPath + "zPreviousHover.png", iconPath + "zPreviousHover.png");
		m_btnPlay = new GeopodButton (iconPath + "zPlay.png", iconPath + "zPause.png", iconPath + "zPlayPressed.png", iconPath + "zPausePressed.png", iconPath + "zPlayHover.png", iconPath + "zPauseHover.png");
		m_btnNext = new GeopodButton (iconPath + "zNext.png", iconPath + "zNext.png", iconPath + "zNextPressed.png", iconPath + "zNextPressed.png", iconPath + "zNextHover.png", iconPath + "zNextHover.png");
		
		m_btnPrevious.setToolTipText ("Previous Time Frame");
		m_btnPlay.setToolTipTexts ("Play Time Animation", "Pause Time Animation");
		m_btnNext.setToolTipText ("Next Time Frame");
		
		m_btnPrevious.setActionCommand ("timePrevious");
		m_btnPlay.setActionCommand ("timePlay");
		m_btnNext.setActionCommand ("timeNext");
		
		m_btnPrevious.addActionListener (m_hud);
		m_btnPlay.addActionListener (m_hud);
		m_btnNext.addActionListener (m_hud);
		
		addObserver (m_btnPrevious, GeopodEventId.TIMECONTROLPREVIOUS_BUTTON_STATE_CHANGED);
		addObserver (m_btnPlay, GeopodEventId.TIMECONTROLPLAY_BUTTON_STATE_CHANGED);
		addObserver (m_btnNext, GeopodEventId.TIMECONTROLNEXT_BUTTON_STATE_CHANGED);
		
		this.add (m_btnPrevious, "gapleft 10, gapright 4");
		this.add (m_btnPlay, "gapleft 4, gapright 4");
		this.add (m_btnNext, "gapleft 4, gapright 10");
	}

	public void play() {
		notifyObservers (GeopodEventId.TIMECONTROLPLAY_BUTTON_STATE_CHANGED);
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
