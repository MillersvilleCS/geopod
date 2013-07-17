package geopod.gui.styles;

import geopod.constants.UIConstants;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalComboBoxUI;

public class GeopodComboBoxUI
		extends MetalComboBoxUI
{
	@Override
	public void paint (Graphics g, JComponent c)
	{
		Color oldDisabledForeground = UIManager.getColor ("ComboBox.disabledForeground");
		Color oldDisabledBackground = UIManager.getColor ("ComboBox.disabledBackground");

		UIManager.getDefaults ().put ("ComboBox.disabledForeground", Color.black);
		UIManager.getDefaults ().put ("ComboBox.disabledBackground", UIConstants.GEOPOD_GREEN);

		super.paint (g, c);

		UIManager.getDefaults ().put ("ComboBox.disabledForeground", oldDisabledForeground);
		UIManager.getDefaults ().put ("ComboBox.disabledBackground", oldDisabledBackground);
	}
}
