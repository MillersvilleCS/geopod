package geopod.devices;

import geopod.GeopodPlugin;
import geopod.utils.FileLoadingUtility;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.vecmath.Vector3d;

import ucar.unidata.util.LogUtil;

/**
 * An attitude indicator that displays pitch and roll in a graphical format.
 */
public class AttitudeIndicator
		extends JComponent
{
	private static final long serialVersionUID = -6705744571396431413L;

	private double m_pitch;
	private double m_roll;
	private BufferedImage m_background;
	private BufferedImage m_targetMarker;

	/**
	 * Default constructor.
	 */
	public AttitudeIndicator ()
	{
		setPitch (0);
		setRoll (0);

		loadImages ();

		this.setPreferredSize (new Dimension (80, 80));
		this.setMaximumSize (new Dimension (80, 80));
		//super.setBackground (Color.black);
		super.setBorder (BorderFactory.createBevelBorder (BevelBorder.RAISED));
	}

	private void loadImages ()
	{
		String path = "//Resources/Images/User Interface/";

		m_background = null;
		m_targetMarker = null;

		try
		{
			m_background = FileLoadingUtility.loadBufferedImage (path + "AttitudeBackground.png");
			m_targetMarker = FileLoadingUtility.loadBufferedImage (path + "AttitudeReticle.png");
		}
		catch (IOException e)
		{
			LogUtil.printException (GeopodPlugin.LOG_CATEGORY, "Data not loaded yet", e);
		}
	}

	/**
	 * Set the yaw, pitch, and roll.
	 * 
	 * @param pitchYawRoll
	 *            - A {@link Vector3d} with X=pitch, and Y=roll (in radians).
	 */
	public void setYawPitchRoll (Vector3d pitchYawRoll)
	{
		this.setPitch (pitchYawRoll.x * 180 / Math.PI);
		this.setRoll (pitchYawRoll.y * 180 / Math.PI);
		this.repaint ();
	}

	/**
	 * @param pitch
	 *            - the pitch in radians.
	 */
	public void setPitch (double pitch)
	{
		this.m_pitch = pitch;
	}

	/**
	 * @return the pitch in radians.
	 */
	public double getPitch ()
	{
		return m_pitch;
	}

	/**
	 * @param roll
	 *            - the roll in radians.
	 */
	public void setRoll (double roll)
	{
		this.m_roll = roll;
	}

	/**
	 * @return the roll in radians.
	 */
	public double getRoll ()
	{
		return m_roll;
	}

	@Override
	protected void paintComponent (Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;

		super.paintComponent (g2d);

		AffineTransform af = g2d.getTransform ();

		//Rotate to account for Roll
		g2d.rotate (-1 * Math.toRadians (this.getRoll ()), super.getWidth () / 2, super.getHeight () / 2);

		//Translate to account for pitch
		int pitchAmount = (int) ((this.getPitch () * 6) / 5);
		g2d.translate (0, pitchAmount);

		//Translate Again to center larger background image
		g2d.translate (-(m_background.getWidth () - super.getWidth ()) / 2,
				(-(m_background.getHeight () - super.getHeight ()) / 2) - 2);

		g2d.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint (RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.drawImage (m_background, 0, 0, null);

		g2d.setTransform (af);

		g2d.drawImage (m_targetMarker, 0, 0, super.getWidth (), super.getHeight (), 0, 0, 100, 100, this);
	}

	/**
	 * TODO: Remove this main method when testing is complete.
	 * 
	 * @param args
	 * @throws java.io.IOException
	 */
	public static void main (String args[])
			throws java.io.IOException
	{
		JFrame frame = new JFrame ("Test Environment");
		frame.setPreferredSize (new Dimension (500, 500));

		final AttitudeIndicator ai = new AttitudeIndicator ();
		JPanel panel = new JPanel ();
		JButton pitchButton = new JButton ("Increase Pitch");
		pitchButton.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				ai.setPitch (ai.getPitch () + 2);
			}
		});

		JButton pitchButtonD = new JButton ("Decrease Pitch");
		pitchButtonD.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				ai.setPitch (ai.getPitch () - 2);
			}
		});

		JButton rollButton = new JButton ("Increase Roll");
		rollButton.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				ai.setRoll (ai.getRoll () + 2);
			}
		});

		JButton rollButtonD = new JButton ("Decrease Roll");
		rollButtonD.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				ai.setRoll (ai.getRoll () - 2);
			}
		});

		panel.add (pitchButton);
		panel.add (pitchButtonD);
		panel.add (rollButton);
		panel.add (rollButtonD);

		frame.add (panel, BorderLayout.SOUTH);
		frame.add (ai);

		frame.pack ();
		frame.setVisible (true);
	}
}
