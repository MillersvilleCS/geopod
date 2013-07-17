package geopod.gui.components;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.border.Border;

/*
 * @author Romain Guy <romain.guy@mac.com>
 */
public class PulsatingBorder
		implements Border
{
	private float thickness = 0.0f;
	private JComponent c;

	public PulsatingBorder (JComponent c)
	{
		this.c = c;
	}

	@Override
	public void paintBorder (Component c, Graphics g, int x, int y, int width, int height)
	{
		Graphics2D g2 = (Graphics2D) g.create ();
		g2.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke (new BasicStroke (4.0f * getThickness ()));
		g2.setComposite (AlphaComposite.SrcOver.derive (getThickness ()));
		g2.setColor (new Color (0x54A4DE));
		g2.fillRect (0, 0, 100, 100);
	}

	@Override
	public Insets getBorderInsets (Component c)
	{
		return new Insets (2, 2, 2, 2);
	}

	@Override
	public boolean isBorderOpaque ()
	{
		return false;
	}

	public float getThickness ()
	{
		return thickness;
	}

	public void setThickness (float thickness)
	{
		this.thickness = thickness;
		c.repaint ();
	}
}