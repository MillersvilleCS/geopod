package geopod.devices;

import geopod.GeopodPlugin;
import geopod.utils.FileLoadingUtility;
import geopod.utils.ThreadUtility;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

import ucar.unidata.util.LogUtil;

/**
 * The Compass displays a current heading value from 0 to 360 degrees.
 * 
 * @author Geopod Team
 * 
 */
public class Compass
		extends JComponent
{
	private static final long serialVersionUID = 7110524086174189973L;

	private int m_heading;
	private BufferedImage m_compassStrip;
	private BufferedImage m_marker;
	private BufferedImage m_glass;

	/**
	 * Constructs a Compass with a default heading of 0 degrees (North)
	 */
	public Compass ()
	{
		m_heading = 0;
		loadImages ();
		this.setPreferredSize (new Dimension (100, 50));
		this.setMaximumSize (new Dimension (100, 50));
		this.setBorder (BorderFactory.createBevelBorder (BevelBorder.RAISED));
	}

	/**
	 * Loads and sets the images associated with the Compass
	 */
	private void loadImages ()
	{
		String path = "//Resources/Images/User Interface/";

		m_compassStrip = null;
		m_marker = null;
		m_glass = null;

		try
		{
			m_compassStrip = FileLoadingUtility.loadBufferedImage (path + "CompassStrip.png");
			m_marker = FileLoadingUtility.loadBufferedImage (path + "CompassMarker.png");
			m_glass = FileLoadingUtility.loadBufferedImage (path + "CompassGlass.png");
		}
		catch (IOException e)
		{
			LogUtil.printException (GeopodPlugin.LOG_CATEGORY, "Data not loaded yet", e);
		}
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected void paintComponent (Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;

		// The image is designed such that we can "index" into the image using our current heading value
		int startX = m_heading;

		g2d.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint (RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		// We only draw an 180 pixel wide slice of the compass strip, starting at startX.
		g2d.drawImage (m_compassStrip, 0, 0, super.getWidth (), super.getHeight (), startX, 0, startX + 180, 100, this);

		drawMarker (g2d);

		g2d.drawImage (m_glass, 0, 0, super.getWidth (), super.getHeight (), 0, 0, m_glass.getWidth (),
				m_glass.getHeight (), null);
	}

	/**
	 * Draws a marker centered on this component which indicates the current
	 * heading value
	 * 
	 * @param g2d
	 *            the Graphics2D object to draw with
	 */
	private void drawMarker (Graphics2D g2d)
	{
		int markerWidth = m_marker.getWidth ();
		int markerHeight = m_marker.getHeight ();

		int centerX = super.getWidth () / 2;
		centerX = centerX - (markerWidth / 2);

		g2d.drawImage (m_marker, centerX, 0, centerX + markerWidth, super.getHeight (), 0, 0, markerWidth,
				markerHeight, null);
	}

	/**
	 * Sets the heading to the specified value. If value is not contained in
	 * [0,360], the Compass will fail. This method is thread safe.
	 * 
	 * @param value
	 *            - the heading to be set
	 */
	public void setHeading (int value)
	{
		m_heading = value;

		ThreadUtility.invokeOnEdt (new Runnable ()
		{
			@Override
			public void run ()
			{
				repaint ();
			}
		});
	}

	/**
	 * Returns the current heading of this Compass
	 * 
	 * @return the heading value
	 */
	public int getHeading ()
	{
		return (m_heading);
	}
}
