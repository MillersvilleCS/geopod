package geopod.gui.panels.distance;

import geopod.constants.UIConstants;
import geopod.gui.components.ButtonFactory;
import geopod.utils.component.LatLonAltTextFieldManager;

import java.awt.Color;
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
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import visad.georef.EarthLocation;

public class DistanceModalWindow
		extends JDialog
		implements ActionListener
{
	private static final long serialVersionUID = -8786419803829176289L;

	private static final String TITLE = "Change Start";
	private static final Color BACKGROUND = UIConstants.GEOPOD_GREEN;
	private static final JFrame NO_OWNER = null;

	private static LatLonAltTextFieldManager m_latLonAltManager;
	private DistancePanel m_distancePanel;

	DistanceModalWindow (DistancePanel distancePanel)
	{
		super (NO_OWNER, TITLE, true);
		super.setDefaultCloseOperation (JDialog.DISPOSE_ON_CLOSE);
		super.setResizable (false);

		m_distancePanel = distancePanel;
		
		buildModalWindow ();
	}

	@Override
	public void actionPerformed (ActionEvent e)
	{
		// We need to get the distance panel's display location when user click's Set in DistancePanel
		m_latLonAltManager.setInputEarthLocation (m_distancePanel.getDisplayLocation ());
		super.setLocationRelativeTo (m_distancePanel);
		super.setVisible (true);
	}

	//	private Font deriveTitleFont ()
	//	{
	//		Font font = UIConstants.GEOPOD_VERDANA;
	//		font = font.deriveFont (UIConstants.CONTENT_FONT_SIZE);
	//		font = font.deriveFont (Font.BOLD);
	//		return (font); 
	//	}

	private void buildModalWindow ()
	{
		Font textFieldFont = UIConstants.GEOPOD_VERDANA;
		textFieldFont = textFieldFont.deriveFont (12.0f);
		Font labelFont = textFieldFont.deriveFont (Font.BOLD);
		//		Font labelFont = UIConstants.GEOPOD_BANDY;
		//		labelFont = labelFont.deriveFont (20.0f);
		Container container = getContentPane ();

		JPanel panel = new JPanel ();
		JLabel latitudeLabel = new JLabel ("Lat:");
		latitudeLabel.setFont (labelFont);
		JLabel longitudeLabel = new JLabel ("Lon:");
		longitudeLabel.setFont (labelFont);
		JLabel altitudeLabel = new JLabel ("Alt:");
		altitudeLabel.setFont (labelFont);

		JTextField latitudeInput = new JTextField ();
		JTextField longitudeInput = new JTextField ();
		JTextField altitudeInput = new JTextField ();

		panel.setLayout (new MigLayout ("wrap 1", "[align center]", ""));
		panel.setBackground (BACKGROUND);

		JButton resetButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
				UIConstants.GEOPOD_GREEN, false);
		resetButton.setText ("SAVE");
		resetButton.setToolTipText ("Change start location and exit");
		resetButton.addActionListener (new ModalWindowActionListener ());

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
				m_latLonAltManager.setInputEarthLocation (m_distancePanel.getDisplayLocation ());
				m_distancePanel.requestFocusOnCanvas ();
			}
		});

		m_latLonAltManager = new LatLonAltTextFieldManager (latitudeInput, longitudeInput, altitudeInput);
		m_latLonAltManager.addDefaultKeyListener ();
		m_latLonAltManager.resetBackgroundColors (Color.WHITE);
		m_latLonAltManager.setInputEarthLocation (m_distancePanel.getDisplayLocation ());
		m_latLonAltManager.addKeyListener (new ChangeListener ());

		panel.add (latitudeLabel, "split 2, width 30");
		panel.add (latitudeInput, "width 90");
		panel.add (longitudeLabel, "split 2, width 30");
		panel.add (longitudeInput, "width 90");
		panel.add (altitudeLabel, "split 2, width 30");
		panel.add (altitudeInput, "width 90");
		panel.add (resetButton, "gapright 15, split 2");
		panel.add (cancelButton);
		container.add (panel);
		pack ();
	}

	private class ChangeListener
			extends KeyAdapter
	{
		@Override
		public void keyReleased (KeyEvent e)
		{
			if (e.getKeyCode () == KeyEvent.VK_ENTER)
			{
				EarthLocation el = m_latLonAltManager.parseInputtedEarthLocation ();
				m_latLonAltManager.setInputEarthLocation (el);
				m_distancePanel.setStartPosition (el);
				setVisible (false);
				m_distancePanel.requestFocusOnCanvas ();
			}
			else
			{
				m_latLonAltManager.validateLatLonAlt ();
			}
		}
	}

	private class ModalWindowActionListener
			implements ActionListener
	{
		@Override
		public void actionPerformed (ActionEvent e)
		{
			if (m_latLonAltManager.attemptValidLatLonAlt ())
			{
				EarthLocation el = m_latLonAltManager.parseInputtedEarthLocation ();
				m_latLonAltManager.setInputEarthLocation (el);
				m_distancePanel.setStartPosition (el);
				setVisible (false);
				m_distancePanel.requestFocusOnCanvas ();
			}
		}
	}
}
