package geopod.gui.components;

import geopod.constants.UIConstants;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.JToolTip;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SpeedControlSlider
		extends JSlider
{

	/**
	 * Cached tooltip.
	 */
	JToolTip m_toolTip;

	private static final long serialVersionUID = -6329041958843830744L;

	public SpeedControlSlider (BoundedRangeModel rangeModel)
	{
		super (rangeModel);

		// Set initial tooltip
		int value = (int) (((float) rangeModel.getValue ()) / rangeModel.getMaximum () * 100.0f);
		String text = "Speed: " + value + "%";
		setToolTipText (text);

		//Tooltips disabled due to Java3D slow-down issue.
		rangeModel.addChangeListener (new ChangeListener ()
		{
			@Override
			public void stateChanged (ChangeEvent e)
			{
				BoundedRangeModel rm = (BoundedRangeModel) e.getSource ();
				int value = (int) (((float) rm.getValue ()) / rm.getMaximum () * 100.0f);
				String text = "Speed: " + value + "%";
				SpeedControlSlider.this.setToolTipText (text);
			}
		});
	}

	@Override
	public void paintComponent (Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Draw with a green->red gradient
		// ESG: 59 - Determining appropriate color scheme
		
		// Original: (Before Edit)
		//g2d.setPaint (new GradientPaint (this.getX (), this.getY (), Color.red, this.getX (), (float) this.getY ()
		//+ this.getHeight () - 2, Color.green, true));
		
		// New set to Color.green to dark green (0, 128, 0)
		// Looks much more appealing than Geopod Color constants. -ESG
		g2d.setPaint (new GradientPaint (this.getX (), this.getY (), Color.green, this.getX (), (float) this.getY ()
				+ this.getHeight () - 2, new Color(0.0f, 0.5f, 0.0f), true));

		// Alternate
		// Yuck. Kept for judging by impartial eye. -ESG
		//g2d.setPaint (new GradientPaint (this.getX (), this.getY (), UIConstants.GEOPOD_DARK_GREEN, this.getX (), (float) this.getY ()
		//		+ this.getHeight () - 2, UIConstants.GEOPOD_GREEN, true));
		
		Polygon p = new Polygon ();
		p.addPoint (0, getWidth () / 2); // upper left point
		p.addPoint (getWidth (), getWidth () / 2); // upper right point
		p.addPoint (getWidth () / 2 + 3, getHeight () - 8); // lower right
		p.addPoint (getWidth () / 2 - 3, getHeight () - 8); // lower left
		g2d.fillPolygon (p);

		g2d.fillOval (0, 0, getWidth (), getWidth ());

		super.paintComponent (g);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JToolTip createToolTip ()
	{
		return (m_toolTip == null) ? new PaddedToolTip () : m_toolTip;
	}
}
