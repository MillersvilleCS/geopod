package geopod.gui.components;

import geopod.constants.UIConstants;
import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;

import java.awt.Dimension;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class PaddedToolTip
		extends JToolTip
{
	private static final long serialVersionUID = 4492553285595086486L;

	/**
	 * The minimum tool-tip width to prevent mixing bugs.
	 */
	private static final int MINIMUM_TOOL_TIP_WIDTH = 151;
	private static final int MINIMUM_TOOL_TIP_HEIGHT = 18;

	public PaddedToolTip ()
	{
		setBackground (UIConstants.GEOPOD_GREEN);
	}

	@Override
	public void setTipText (String tipText)
	{
		// Using a minimum tool-tip width to prevent the j3d slow down bug.
		Dimension stringBounds = estimateBounds (tipText);
		Dimension suggestedBounds = new Dimension (stringBounds);
		suggestedBounds.width = Math.max (MINIMUM_TOOL_TIP_WIDTH, suggestedBounds.width + 5);
		suggestedBounds.height = Math.max (MINIMUM_TOOL_TIP_HEIGHT, suggestedBounds.height);

		setPreferredSize (suggestedBounds);

		// Pad tooltip text with spaces to center it.
		FontUIResource fontResource = (FontUIResource) UIManager.get ("ToolTip.font");
		FontRenderContext frc = new FontRenderContext (null, false, false);
		double spaceWidth = fontResource.getStringBounds (" ", frc).getWidth ();
		int emptySpace = suggestedBounds.width - stringBounds.width;
		double numSpaces = emptySpace / spaceWidth;
		int leftSpaceCount = (int) numSpaces / 2;
		char[] spaceChars = new char[leftSpaceCount];
		Arrays.fill (spaceChars, ' ');
		String padding = new String (spaceChars);
		super.setTipText (padding + tipText);

		Debug.printf (DebugLevel.HIGH, "%d\n", suggestedBounds.width);
	}

	/**
	 * Returns the approximate number of pixels wide the current tooltip will
	 * be.
	 * 
	 * @return the approximate length of the tooltip
	 */
	private Dimension estimateBounds (String tipText)
	{
		FontUIResource fontResource = (FontUIResource) UIManager.get ("ToolTip.font");
		FontRenderContext frc = new FontRenderContext (null, false, false); //this.getGraphics ().getFontMetrics ().getFontRenderContext ();
		Rectangle2D bounds = fontResource.getStringBounds (tipText, frc);
		return new Dimension ((int) bounds.getWidth (), (int) bounds.getHeight ());
	}

}
