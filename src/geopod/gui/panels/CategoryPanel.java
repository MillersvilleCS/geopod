package geopod.gui.panels;

import geopod.constants.UIConstants;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * A transparent panel with an opaque label. The label is used to display a 
 * particle image's name.
 * 
 * @author Geopod Team
 */
public class CategoryPanel
		extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 180630171993339644L;
	private JLabel m_categoryLabel;
	
	public CategoryPanel (String text)
	{
		super ();
		setUpPanel ();
		addCategoryLabel (text);
	}
	
	public CategoryPanel ()
	{
		this ("");
	}

	// ctor helper method
	private void setUpPanel ()
	{
		this.setLayout (new MigLayout ("bottomtotop, fillx", "center"));
		this.setOpaque (false);
		this.setFocusable (false);
	}
	
	// ctor helper method
	private void addCategoryLabel (String text)
	{
		m_categoryLabel = new JLabel (" " + text + " ");
		m_categoryLabel.setBackground (UIConstants.GEOPOD_GREEN);
		// A trick to make a non-opaque label blend into a black background
		m_categoryLabel.setForeground (Color.BLACK);
		m_categoryLabel.setVisible (false);
		this.add (m_categoryLabel);
	}
	
	protected void setImageCategory (String category)
	{
		m_categoryLabel.setText (" " + category + " ");
	}

	protected void setCategoryVisible (boolean isVisible)
	{
		m_categoryLabel.setVisible (isVisible);
	}
	
	protected void repaintCategoryLabel ()
	{
		m_categoryLabel.repaint ();
	}
	
	protected void setCategoryOpaque (boolean isOpaque)
	{
		m_categoryLabel.setOpaque (isOpaque);
	}
}
