package geopod.gui.panels;

import geopod.gui.components.PaddedToolTip;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JToolTip;

/**
 * Particle Image Panel is an Image Panel that implements a fixed image aspect
 * ratio.
 * 
 * @author Geopod Team
 */
public class ParticleImagePanel
		extends ImagePanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2948364909018446552L;

	private static final double DEFAULT_WIDTH;
	private static final  double DEFAULT_HEIGHT;
	private static double m_widthToHeightRatio;
	private static double m_heightToWidthRatio;
	private CategoryPanel m_categoryPanel;
	
	static
	{
		DEFAULT_WIDTH = 430.0;
		DEFAULT_HEIGHT = 280.0;
	}
	
	public ParticleImagePanel ()
	{
		super ();
		this.setUpPanel ();
		this.setAspectRatio (DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public ParticleImagePanel (String imagePath)
	{
		super (imagePath);
		this.setUpPanel ();
		BufferedImage image = getImage ();
		this.setAspectRatio ((double) image.getWidth (), (double) image.getHeight ());
	}

	private void setUpPanel ()
	{
		this.setLayout (new BorderLayout ());
		m_categoryPanel = new CategoryPanel ();
		this.add (m_categoryPanel, BorderLayout.CENTER);
		this.addMouseListener ();
	}
	
	private void setAspectRatio (double width, double height)
	{
		m_widthToHeightRatio = width/height;
		m_heightToWidthRatio = height/width;
	}
	
	private void addMouseListener ()
	{
		this.addMouseListener (new MouseAdapter ()
		{			
			@Override
			public void mouseEntered (MouseEvent e)
			{
				m_categoryPanel.setCategoryVisible (true);
				m_categoryPanel.repaintCategoryLabel ();
			}

			@Override
			public void mouseExited (MouseEvent e)
			{
				m_categoryPanel.setCategoryVisible (false);
				m_categoryPanel.repaintCategoryLabel ();
			}
		});
	}
	
	@Override
	public void setImage (BufferedImage image)
	{
		super.setImage (image);
		this.setAspectRatio ((double) image.getWidth (), (double) image.getHeight ());
	}
	
	@Override
	protected void drawBufferedImage (Graphics g)
	{
		BufferedImage image = getImage ();
		if (image != null)
		{
			Graphics2D g2d = (Graphics2D) g;
			int destinationWidth = 0;
			int destinationHeight = 0;
			int width = super.getWidth ();
			int height = super.getHeight ();

			if ((int) (width * m_heightToWidthRatio) <= height)
			{
				destinationWidth = width;
				destinationHeight = (int) (width * m_heightToWidthRatio);
			}
			else
			{
				destinationWidth = (int) (height * m_widthToHeightRatio);
				destinationHeight = height;
			}

			int dw = Math.max ((width - destinationWidth) / 2, 0);
			int dh = Math.max ((height - destinationHeight) / 2, 0);

			g2d.drawImage (image, dw, dh, destinationWidth + dw, destinationHeight + dh, 0, 0, image.getWidth (),
					image.getHeight (), this);
		}
	}

	public void setImageCategory (String category)
	{
		m_categoryPanel.setImageCategory (category);
	}
	
	public void setCategoryLabelOpaque (boolean isOpaque)
	{
		m_categoryPanel.setCategoryOpaque (isOpaque);
	}
	
	public void repaintCategory ()
	{
		m_categoryPanel.repaintCategoryLabel ();
	}
}
