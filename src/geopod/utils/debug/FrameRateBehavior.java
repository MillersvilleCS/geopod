package geopod.utils.debug;

import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupOnElapsedFrames;

class FrameRateBehavior
		extends Behavior
{
	private WakeupCondition m_wakeup;
	private long m_startTime;

	public FrameRateBehavior ()
	{
		m_wakeup = new WakeupOnElapsedFrames (0);
	}

	@Override
	public void initialize ()
	{
		m_startTime = System.nanoTime ();
		super.wakeupOn (m_wakeup);
	}

	@Override
	public void processStimulus (@SuppressWarnings("rawtypes") Enumeration criteria)
	{
		long elapsedTime = System.nanoTime () - m_startTime;
		System.out.printf ("FPS = %f\n", (1.0f / elapsedTime));
		super.wakeupOn (m_wakeup);
		m_startTime = System.nanoTime ();
	}

}
