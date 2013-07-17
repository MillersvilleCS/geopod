package geopod.gui.panels;

import geopod.utils.FileLoadingUtility;
import geopod.utils.ThreadUtility;
import geopod.utils.debug.Debug;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JPanel;

/**
 * ImagePanel is a JPanel that can optionally paint an image as a background.
 * 
 * @author Geopod Team
 */
public class ImagePanel
		extends JPanel
{
	private static final long serialVersionUID = 4887853876432920657L;
	private BufferedImage m_image;
	private Runnable m_paintRunnable;

	/**
	 * Constructs an ImagePanel with the image specified by imagePath
	 * 
	 * @param imagePath
	 *            the location of the image
	 */
	public ImagePanel (String imagePath)
	{
		m_image = loadImage (imagePath);
		m_paintRunnable = new PaintRunnable ();
	}

	/**
	 * Constructs an ImagePanel with no background image
	 */
	public ImagePanel ()
	{
		m_image = null;
		m_paintRunnable = new PaintRunnable ();
	}

	/**
	 * Attempts to load and return the image specified by imagePath
	 * 
	 * @param imagePath
	 *            the path of the image to be loaded
	 * @return the loaded image
	 */
	private BufferedImage loadImage (String imagePath)
	{
		BufferedImage image = null;
		try
		{
			image = FileLoadingUtility.loadBufferedImage (imagePath);
		}
		catch (IOException e)
		{
			Debug.println ("Couldn't load image");
		}
		return (image);
	}

	/**
	 * Sets the background image of this ImagePanel to the specified
	 * {@link BufferedImage}. This method will trigger a repaint of this
	 * component; however, this method is thread safe.
	 * 
	 * @param image
	 *            the image to be set for this ImagePanel
	 */
	public void setImage (BufferedImage image)
	{
		m_image = image;

		ThreadUtility.invokeOnEdt (new Runnable ()
		{
			@Override
			public void run ()
			{
				m_paintRunnable.run ();
			}
		});
	}

	public BufferedImage getImage ()
	{
		return (m_image);
	}

	protected void drawBufferedImage (Graphics g)
	{
		if (m_image != null)
		{
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage (m_image, 0, 0, super.getWidth (), super.getHeight (), 0, 0, m_image.getWidth (),
					m_image.getHeight (), this);
		}
	}
	
	@Override
	protected void paintComponent (Graphics g)
	{
		super.paintComponent (g);
		drawBufferedImage (g);
	}

	private class PaintRunnable
			implements Runnable
	{
		@Override
		public void run ()
		{
			ImagePanel.this.repaint ();
		}
	}
}
