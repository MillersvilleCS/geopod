package geopod.gui.components;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Screen3D;

public class OffScreenCanvas3D
		extends Canvas3D
{
	private static final long serialVersionUID = -5850049957890111127L;

	private Deque<BufferedImage> m_unsavedImages;

	private int m_screenCaptureWidth;
	private int m_screenCaptureHeight;
	private double m_onScreenCanvasWidth;
	private double m_onScreenCanvasHeight;

	private BufferedImage m_bImage;
	private ImageComponent2D m_imageBuffer;

	/**
	 * Raised by calling {@link OnScreenCanvas3D#getScreenshot()}
	 */
	private boolean m_isTakingScreenshot = false;

	public OffScreenCanvas3D (GraphicsConfiguration gconfig, boolean offscreenflag)
	{
		super (gconfig, offscreenflag);
	}

	/**
	 * This method is only meant to be called by the OnScreenCanvas3D that "has"
	 * it. This method syncs the virtual (.getSize()) and physical
	 * (.getScreen3D()) sizes of the on and off screen canvases and instantiates
	 * the Deque of unsaved images, the BufferedImage m_bImage, and the
	 * ImageComponent2D m_buffer.
	 * 
	 * @see OnScreenCanvas3D#syncSizesWithOffScreenCanvas()
	 */
	protected void postInit (Dimension d, Screen3D sOn)
	{
		m_unsavedImages = new ArrayDeque<BufferedImage> ();

		m_onScreenCanvasWidth = d.getWidth ();
		m_onScreenCanvasHeight = d.getHeight ();

		Screen3D sOff = this.getScreen3D ();
		sOff.setSize (sOn.getSize ());
		sOff.setPhysicalScreenWidth (sOn.getPhysicalScreenWidth ());
		sOff.setPhysicalScreenHeight (sOn.getPhysicalScreenHeight ());

		if (m_bImage == null)
		{
			this.setScreenCaptureSize ((int) m_onScreenCanvasWidth);
			m_bImage = new BufferedImage (m_screenCaptureWidth, m_screenCaptureHeight, BufferedImage.TYPE_INT_ARGB);
			m_imageBuffer = new ImageComponent2D (ImageComponent2D.FORMAT_RGBA, m_bImage);
		}
	}

	/**
	 * Retrieves an image from the head of the deque. Meant to be used in a loop
	 * after stopping movie capture.
	 * 
	 * @return the least recently rendered frame.
	 * @see #getLastUnsavedImage()
	 */
	public BufferedImage getNextUnsavedImage ()
	{
		if (m_unsavedImages.isEmpty ())
		{
			return null;
		}

		BufferedImage bImage = m_unsavedImages.pollFirst ();
		return bImage;
	}

	/**
	 * Retrieves an image from the tail of the deque. Meant to be used for
	 * single image captures.
	 * 
	 * @return the most recently rendered frame.
	 * @see #getNextUnsavedImage()
	 */
	public BufferedImage getLastUnsavedImage ()
	{
		if (m_unsavedImages.isEmpty ())
		{
			return null;
		}

		BufferedImage bImage = m_unsavedImages.pollLast ();
		return bImage;
	}

	//TODO: write descriptors

	public Dimension getScreenCaptureSize ()
	{
		return (new Dimension (m_screenCaptureWidth, m_screenCaptureHeight));
	}

	/**
	 * Update the render size of the screen capture. The height will be
	 * automatically determined by using the OnScreenCanvas' aspect ratio.
	 * 
	 * @see #setScreenCaptureSize(int, int)
	 * @see #setScreenCaptureSize(int, int, boolean)
	 */
	public void setScreenCaptureSize (int width)
	{
		this.setScreenCaptureSize (width, m_screenCaptureHeight, true);
	}

	/**
	 * Update the render size of the screen capture. Assumes you don't want to
	 * keep the aspect ratio since you are specifying two dimensions.
	 * 
	 * @see #setScreenCaptureSize(int)
	 * @see #setScreenCaptureSize(int, int, boolean)
	 */
	public void setScreenCaptureSize (int width, int height)
	{
		this.setScreenCaptureSize (width, height, false);
	}

	/**
	 * Update the render size of the screen capture.
	 * 
	 * @param keepAspectRatio
	 *            If true, will only use width to figure out the height based on
	 *            the onScreenBuffer's size.
	 */
	public void setScreenCaptureSize (int width, int height, boolean keepAspectRatio)
	{
		assert (m_onScreenCanvasWidth > 0);
		assert (m_onScreenCanvasHeight > 0);

		m_screenCaptureWidth = width;

		if (keepAspectRatio)
		{
			double aspectRatio = m_onScreenCanvasWidth / m_onScreenCanvasHeight;
			m_screenCaptureHeight = (int) ((double) m_screenCaptureWidth / aspectRatio);
		}
		else
		{
			m_screenCaptureHeight = height;
		}

		//TODO: if resized during run-time, do I need to change height/width of m_bImage and m_imageBuffer? 
	}

	/**
	 * Schedules a the rendering of one frame into this OffScreenCanvas3D's off
	 * screen buffer. Raises a flag to tell {@link #postSwap()} to copy the
	 * rendered frame into deque of unsaved images. Only meant to be called by
	 * {@link OnScreenCanvas3D#getScreenshot()}.
	 * 
	 * @see #getLastUnsavedImage()
	 * @see #getNextUnsavedImage()
	 */
	public void getScreenshot ()
	{
		m_isTakingScreenshot = true;
		this.renderOffScreenBuffer ();
	}

	/**
	 * If a frame has been rendered by {@link #getScreenshot()}, then we add
	 * that frame to the tail end of the deque of unsaved images.
	 */
	@Override
	public void postSwap ()
	{
		if (m_isTakingScreenshot)
		{
			m_isTakingScreenshot = false;
			super.postSwap ();
			m_unsavedImages.add (m_imageBuffer.getImage ());
		}
	}
}
