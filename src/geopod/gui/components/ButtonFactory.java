package geopod.gui.components;

import geopod.constants.UIConstants;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LinearGradientPaint;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.GlossPainter;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * ButtonFactory produces buttons
 * 
 * @author Geopod Team
 * 
 */
public class ButtonFactory
{
	private ButtonFactory ()
	{
		// Static class
	}

	/**
	 * Returns a standard {@link GeopodButton} with the specified text and font
	 * color.
	 * 
	 * @param text
	 *            the text label of the button
	 * @param fontColor
	 *            the color of the font to be used
	 * @return the GeopodButton
	 */
	public static GeopodButton createStandardButton (String text, Color fontColor)
	{
		GeopodButton button = new GeopodButton ();

		button.setText (text);
		button.setForeground (fontColor);

		return (button);
	}

	/**
	 * Returns a {@link GeopodButton} with the specified font size, font color,
	 * and bold property. This button will feature a gradient background and
	 * uses the Bandy true type font.
	 * 
	 * @param fontSize
	 *            the desired font size
	 * @param fontColor
	 *            the desired font color
	 * @param isBold
	 *            text boldness
	 * @return the GeopodButton
	 */
	public static GeopodButton createGradientButton (float fontSize, Color fontColor, boolean isBold)
	{
		GeopodButton button = new GeopodButton ();

		button.setMargin (new Insets (0, 5, 0, 5));

		Font geopodFont = UIConstants.GEOPOD_BANDY;
		if (isBold)
		{
			geopodFont = geopodFont.deriveFont (Font.BOLD, fontSize);
		}
		else
		{
			geopodFont = geopodFont.deriveFont (fontSize);
		}

		button.setFont (geopodFont);

		GlossPainter gloss = new ButtonGlossPainter (new Color (1.0f, 1.0f, 1.0f, 0.35f),
				GlossPainter.GlossPosition.TOP);

		Color startGradientColor = Color.DARK_GRAY;
		Color endGradientColor = Color.GRAY;

		LinearGradientPaint gradientPaint = new LinearGradientPaint (0.0f, 0.0f, 100, 100, new float[] { 0.0f, 1.0f },
				new Color[] { startGradientColor, endGradientColor });

		MattePainter mattePainter = new MattePainter (gradientPaint);

		button.setBackgroundPainter (new CompoundPainter<JXButton> (mattePainter, gloss));
		button.setForeground (fontColor);
		button.setFocusPainted (false);

		return (button);
	}
}
