package geopod.gui.components;

import java.awt.GraphicsConfiguration;

import javax.media.j3d.Canvas3D;

public class OnScreenCanvas3D
		extends Canvas3D
{
	private static final long serialVersionUID = -285918237121096542L;

	private OffScreenCanvas3D m_offScreenCanvas;

	private boolean m_isSendingFrameRenderRequest = false;

	public OnScreenCanvas3D (GraphicsConfiguration gconfig, boolean offscreenflag)
	{
		super (gconfig, offscreenflag);
	}

	public OffScreenCanvas3D getOffScreenCanvas ()
	{
		return m_offScreenCanvas;
	}

	/**
	 * This method MUST be called before attempting to
	 * {@linkplain #syncSizesWithOffScreenCanvas() take screenshots}.
	 */
	public void setOffScreenCanvas (OffScreenCanvas3D c)
	{
		m_offScreenCanvas = c;
		this.syncSizesWithOffScreenCanvas ();
	}

	/**
	 * This method should only be called by
	 * {@link #setOffScreenCanvas(OffScreenCanvas3D)}.
	 */
	public void syncSizesWithOffScreenCanvas ()
	{
		m_offScreenCanvas.postInit (this.getSize (), this.getScreen3D ());
	}

	/**
	 * Schedules the rendering of one frame by this instance's
	 * {@link OffScreenCanvas3D}. Before calling this method,
	 * {@link #syncSizesWithOffScreenCanvas()} MUST be called.
	 */
	public void getScreenshot ()
	{
		m_isSendingFrameRenderRequest = true;
	}

	@Override
	public void postSwap ()
	{
		if (m_isSendingFrameRenderRequest)
		{
			m_isSendingFrameRenderRequest = false;
			m_offScreenCanvas.getScreenshot ();
		}
	}
}
