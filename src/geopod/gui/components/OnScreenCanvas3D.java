package geopod.gui.components;

import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingDeque;

import javax.media.j3d.Canvas3D;

public class OnScreenCanvas3D 
		extends Canvas3D {
	private static final long serialVersionUID = -285918237121096542L;
	
	private OffScreenCanvas3D m_offScreenCanvas;
	private boolean m_isSchedulingScreenshot;
	
	public OnScreenCanvas3D (GraphicsConfiguration gconfig, boolean offscreenflag) {
		super (gconfig, offscreenflag);
	}
	
	public OffScreenCanvas3D getOffScreenCanvas () {
		return m_offScreenCanvas;
	}
	
	public void setOffScreenCanvas (OffScreenCanvas3D c) {
		m_offScreenCanvas = c;
		m_offScreenCanvas.postInit (this);
	}
	
	/**
	 * Must be called before {@link #getScreenshot() taking screenshots}.
	 */
	public void setContainer (BlockingDeque<BufferedImage> images) {
		m_offScreenCanvas.setContainer (images);
	}
	
	public int getScreenCaptureWidth () {
		return m_offScreenCanvas.getScreenCaptureWidth ();
	}
	
	public int getScreenCaptureHeight () {
		return m_offScreenCanvas.getScreenCaptureHeight ();
	}
	
	public void setScreenCaptureSize (int width, int height) {
		m_offScreenCanvas.setScreenCaptureSize (width, height);
	}
	
	/**
	 * Requests the rendering of one frame from this {@code OnScreenCanvas3D}'s
	 * {@link OffScreenCanvas3D}. The {@code OffScreenCanvas3D} will then add
	 * the frame to its image container.
	 */
	public void getScreenshot () {
		this.m_isSchedulingScreenshot = true;
	}
	
	public void postSwap () {
		if (m_isSchedulingScreenshot) {
			m_isSchedulingScreenshot = false;
			m_offScreenCanvas.renderOffScreenBuffer ();
		}
		
	}
}
