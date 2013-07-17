package geopod.gui.components;

import javax.swing.JLabel;
import javax.swing.JToolTip;

// Used for IsovalueViewPanel

public class GeopodLabel
		extends JLabel
{
	/**
	 * Serial id
	 */
	private static final long serialVersionUID = -3525468432198243348L;

	JToolTip m_toolTip;

	/**
	 * Empty constructor
	 */

	public GeopodLabel ()
	{
		super ();
	}

	/**
	 * Creates a new <tt>GeopodLabel</tt> and sets its text to the specified
	 * text String.
	 * 
	 * @param text
	 */

	public GeopodLabel (String text)
	{
		super (text);
	}

	/**
	 * Creates a new <tt>GeopodLabel</tt> with the specified text and horizontal
	 * alignment.
	 * 
	 * @param text
	 * @param horizontalAlignment
	 */
	public GeopodLabel (String text, int horizontalAlignment)
	{
		super (text, horizontalAlignment);
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
