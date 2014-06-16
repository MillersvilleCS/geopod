package geopod.gui.components;

import javax.swing.JTextField;
import javax.swing.JToolTip;

/**
 * We extend JTextField to override createToolTip and getToolTipLocation. We
 * ensure that tooltips are not displayed outside the bounds of the window.
 * 
 * @author Geopod Team
 * 
 */
public class GeopodTextField
		extends JTextField
{
	/**
	 * Cached tooltip;
	 */
	JToolTip m_toolTip;

	/**
	 * 
	 */
	private static final long serialVersionUID = 7909077512484624767L;

	/**
	 * Empty constructor.
	 */
	public GeopodTextField ()
	{
		super ();
	}

	/**
	 * Constructs a new empty <code>TextField</code> with the specified number
	 * of columns. A default model is created and the initial string is set to
	 * <code>null</code>.
	 * 
	 * @param columns
	 *            the number of columns to use to calculate the preferred width;
	 *            if columns is set to zero, the preferred width will be
	 *            whatever naturally results from the component implementation
	 */
	public GeopodTextField (int columns)
	{
		super (columns);
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