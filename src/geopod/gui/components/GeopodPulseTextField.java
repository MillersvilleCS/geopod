package geopod.gui.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class GeopodPulseTextField
		extends GeopodTextField
{
	private static final long serialVersionUID = -8377048478492472734L;
	private float backgroundAlpha = 0.0f;

	public GeopodPulseTextField (int numColumns)
	{
		super (numColumns);
	}

	public void setBackgroundAlpha (float backgroundAlpha)
	{
		this.backgroundAlpha = backgroundAlpha;
		this.repaint ();
	}

	public float getBackgroundAlpha ()
	{
		return backgroundAlpha;
	}

	@Override
	protected void paintComponent (Graphics g)
	{
		super.paintComponent (g);

		Graphics2D g2 = (Graphics2D) g.create ();
		g2.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setComposite (AlphaComposite.SrcOver.derive (getBackgroundAlpha () * .75f));
		g2.setColor (Color.WHITE);
		g2.fillRect (0, 0, super.getWidth (), super.getHeight ());
	}

}
