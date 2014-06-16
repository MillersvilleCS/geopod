package geopod.gui.styles;

import geopod.constants.UIConstants;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

public class GeopodTabbedPaneUI
		extends MetalTabbedPaneUI
{
	private Color m_tabColor;
	private Color m_selectedTabColor;
	private Color m_highlightColor;
	private Color m_textColor;
	private Font m_font;

	public GeopodTabbedPaneUI ()
	{
		super ();
		m_tabColor = new Color (163, 163, 160);
		m_selectedTabColor = new Color (100, 100, 100);
		m_highlightColor = Color.DARK_GRAY;
		m_textColor = UIConstants.GEOPOD_GREEN;
		m_font = UIConstants.GEOPOD_VERDANA.deriveFont (11.0f);
	}

	public GeopodTabbedPaneUI (Color defaultTabColor, Color selectedTabColor, Color tabHighlightColor)
	{
		super ();
		m_tabColor = defaultTabColor;
		m_selectedTabColor = selectedTabColor;
		m_highlightColor = tabHighlightColor;
		m_textColor = UIConstants.GEOPOD_GREEN;
		m_font = UIConstants.GEOPOD_VERDANA.deriveFont (11.0f);
	}

	public GeopodTabbedPaneUI (Color defaultTabColor, Color selectedTabColor, Color tabHighlightColor,
			Color tabTextColor, Font tabFont)
	{
		super ();
		m_tabColor = defaultTabColor;
		m_selectedTabColor = selectedTabColor;
		m_highlightColor = tabHighlightColor;
		m_textColor = tabTextColor;
		m_font = tabFont;
	}

	@Override
	protected void installDefaults ()
	{
		tabPane.setBackground (m_tabColor);
		tabPane.setForeground (m_textColor);
		tabPane.setFont (m_font);

		super.installDefaults ();

		// Focus painting color, set same as selected tab color so tab never appears to be focus painted
		super.focus = m_selectedTabColor;
		// Color of the face of the tab when selected
		super.selectColor = m_selectedTabColor;
		//super.lightHighlight = Color.DARK_GRAY; //cannot determine effect
		//super.shadow = Color.DARK_GRAY; //cannot determine effect
		// Color around outside of white border on unselected tab
		super.darkShadow = m_highlightColor;
		//Color around outside of selected tab, inside blue border
		super.selectHighlight = m_highlightColor;
	}

	@Override
	protected void paintContentBorder (Graphics g, int tabPlacement, int selectedIndex)
	{
		// Content area color is border around tabbed pane, set same as selectedTabColor before painting,
		// then reset 
		Color oldColor = UIManager.getColor ("TabbedPane.contentAreaColor");
		UIManager.getDefaults ().put ("TabbedPane.contentAreaColor", m_selectedTabColor);
		super.paintContentBorder (g, tabPlacement, selectedIndex);
		UIManager.getDefaults ().put ("TabbedPane.contentAreaColor", oldColor);
	}
}