package geopod.gui.components;

import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * This factory produces borders
 * 
 * @author Geopod Team
 * 
 */
public class BorderFactory
{
	private BorderFactory ()
	{
		// Static class
	}

	/**
	 * Returns the standard border
	 * 
	 * @return the border
	 */
	public static Border createStandardBorder ()
	{
		Border border = javax.swing.BorderFactory.createCompoundBorder (
				javax.swing.BorderFactory.createBevelBorder (BevelBorder.RAISED),
				javax.swing.BorderFactory.createBevelBorder (BevelBorder.LOWERED));

		return (border);
	}
}
