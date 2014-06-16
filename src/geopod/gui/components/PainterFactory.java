package geopod.gui.components;

import java.awt.Color;
import java.awt.LinearGradientPaint;

import org.jdesktop.swingx.painter.MattePainter;

public class PainterFactory
{
	private PainterFactory ()
	{
		// Static class
	}

	/**
	 * TODO: expand argument descriptions.
	 * 
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param fractions
	 * @param colors
	 * @return a new matte painter.
	 */
	public static MattePainter createMattePainter (float startX, float startY, float endX, float endY,
			float[] fractions, Color[] colors)
	{
		LinearGradientPaint gradientPaint = new LinearGradientPaint (startX, startY, endX, endY, fractions, colors);
		MattePainter mattePainter = new MattePainter (gradientPaint);

		return (mattePainter);
	}

	/**
	 * Construct a standard matte painter.
	 * 
	 * TODO: expand argument descriptions.
	 * 
	 * @param endX
	 *            -
	 * @param endY
	 *            -
	 * @return a new standard matte painter.
	 */
	public static MattePainter createStandardMattePainter (float endX, float endY)
	{
		Color[] colors = { Color.DARK_GRAY, Color.LIGHT_GRAY, Color.LIGHT_GRAY, Color.DARK_GRAY };
		float[] fractions = { 0.0f, 0.2f, 0.8f, 1.0f };

		MattePainter mattePainter = createMattePainter (0, 0, endX, endY, fractions, colors);

		return (mattePainter);
	}

	/**
	 * @return - a standard Geopod background painter.
	 */
	public static MattePainter createStandardGradientBackground ()
	{
		Color color1 = Color.DARK_GRAY;
		Color color2 = Color.LIGHT_GRAY;

		LinearGradientPaint gradientPaint = new LinearGradientPaint (0.0f, 0.0f, 400, 200, new float[] { 0.0f, 1.0f },
				new Color[] { color1, color2 });
		MattePainter mattePainter = new MattePainter (gradientPaint);

		return (mattePainter);
	}
}