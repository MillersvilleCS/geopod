package geopod.gui.components;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import org.jdesktop.swingx.painter.GlossPainter;

/**
 * Package private class to handle painting Geopod buttons. Unlike
 * {@link GlossPainter}, ButtonGlossPainter produces a gloss effect more
 * consistent with standard button gloss.
 * 
 */
class ButtonGlossPainter
		extends GlossPainter
{

	public ButtonGlossPainter ()
	{
		super ();
	}

	public ButtonGlossPainter (GlossPosition position)
	{
		super (position);
	}

	public ButtonGlossPainter (Paint paint, GlossPosition position)
	{
		super (paint, position);
	}

	public ButtonGlossPainter (Paint paint)
	{
		super (paint);
	}

	@Override
	protected void doPaint (Graphics2D g, Object component, int width, int height)
	{
		if (getPaint () != null)
		{
			Ellipse2D ellipse = new Ellipse2D.Double (-width / 8f, -height / 2f, 1.25f * width, height);

			Area gloss = new Area (ellipse);
			//			if (getPosition () == GlossPosition.TOP)
			//			{
			//				Area area = new Area (new Rectangle (0, 0, width, height));
			//				area.subtract (new Area (ellipse));
			//				gloss = area;
			//			}

			g.setPaint (getPaint ());
			g.fill (gloss);
		}
	}

}
