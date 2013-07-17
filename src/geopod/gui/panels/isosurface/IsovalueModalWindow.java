package geopod.gui.panels.isosurface;

import geopod.constants.UIConstants;
import geopod.gui.components.ButtonFactory;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ucar.unidata.idv.control.ThreeDSurfaceControl;

public class IsovalueModalWindow
		extends JDialog
		implements ActionListener
{

	private static final long serialVersionUID = -8786419803829176289L;

	private static final JFrame NO_OWNER = null;

	private Component m_relative;
	private IsovalueTextField m_textField;

	IsovalueModalWindow (JPanel relative, ThreeDSurfaceControl control)
	{
		super (NO_OWNER, "Change Isovalue", true);
		super.setDefaultCloseOperation (JDialog.DISPOSE_ON_CLOSE);
		super.setResizable (false);

		m_relative = relative;

		buildIsosurfaceChangePanel (control);
	}

	@Override
	public void actionPerformed (ActionEvent e)
	{
		m_textField.displayIsovalue ();
		super.setLocationRelativeTo (m_relative);
		setVisible (true);
	}

	private void buildIsosurfaceChangePanel (ThreeDSurfaceControl control)
	{
		Isosurface surface = new Isosurface (control);

		Container container = getContentPane ();

		JPanel panel = new JPanel ();
		panel.setLayout (new MigLayout ("wrap 1", "[align center]", ""));
		panel.setBackground (UIConstants.GEOPOD_GREEN);

		Font titleFont = UIConstants.GEOPOD_VERDANA;
		titleFont = titleFont.deriveFont (UIConstants.CONTENT_FONT_SIZE);
		titleFont = titleFont.deriveFont (Font.BOLD);

		JLabel header = new JLabel ("Isosurface value for ");
		header.setFont (titleFont);
		JLabel parameter = new JLabel (surface.getParameterName ());
		parameter.setFont (titleFont);
		JLabel unit = new JLabel (surface.getUnitIdentifier ());
		Font labelFont = UIConstants.GEOPOD_VERDANA.deriveFont (UIConstants.CONTENT_FONT_SIZE);
		unit.setFont (labelFont);
		m_textField = new IsovalueTextField (control);
		m_textField.addKeyListener (new KeyAdapter()
		{
			@Override
			public void keyReleased (KeyEvent e)
			{
				if (e.getKeyCode () == KeyEvent.VK_ENTER && m_textField.setSurfaceToValue ())
				{
					setVisible (false);
				}
				else
				{
					m_textField.validateInput();
				}
			}
		});

		JButton saveButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
				UIConstants.GEOPOD_GREEN, false);
		saveButton.setText ("SAVE");
		saveButton.setToolTipText ("Save changes and exit");
		saveButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				if (m_textField.setSurfaceToValue ())
				{
					setVisible (false);
				}
			}
		});

		JButton cancelButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
				UIConstants.GEOPOD_GREEN, false);
		cancelButton.setText ("CANCEL");
		cancelButton.setToolTipText ("Exit without saving changes");
		cancelButton.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				setVisible (false);
			}
		});

		panel.add (header);
		panel.add (parameter, "gapbottom 15");
		panel.add (m_textField, "width 80, gapright 5, split 2");
		panel.add (unit, "gapbottom 10");
		panel.add (saveButton, "gapright 15, split 2");
		panel.add (cancelButton);
		container.add (panel);

		pack ();
	}

}
