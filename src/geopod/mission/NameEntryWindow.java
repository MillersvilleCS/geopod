package geopod.mission;

import geopod.constants.UIConstants;
import geopod.gui.components.ButtonFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class NameEntryWindow
		extends JDialog
		implements ActionListener
{
	private static final long serialVersionUID = 5381848495341230195L;
	private static final JFrame NO_OWNER = null;
	
	private AssessmentPanel m_assessmentPanel;
	private JTextField m_nameEntryField;
	private JCheckBox m_preserveNameCheckbox;

	public NameEntryWindow (AssessmentPanel assessmentPanel)
	{
		super (NO_OWNER, "Enter Name", true);
		super.setDefaultCloseOperation (JDialog.DISPOSE_ON_CLOSE);
		super.setResizable (false);
		
		m_assessmentPanel = assessmentPanel;

		addContent ();
		super.setLocationRelativeTo (m_assessmentPanel);
	}
	
	private void addContent()
	{
		JPanel content = new JPanel ();
		content.setBackground (UIConstants.GEOPOD_GREEN);
		content.setLayout (new MigLayout ("", "[align center]", ""));

		JLabel instructions = new JLabel ("Please enter a name before submitting your assessment.", JLabel.CENTER);
		content.add (instructions, "wrap");

		m_nameEntryField = new JTextField ();
		m_preserveNameCheckbox = new JCheckBox ();
		JPanel nameEntryPabel = m_assessmentPanel.createNameEntryPanel (m_nameEntryField, m_preserveNameCheckbox);
		m_preserveNameCheckbox.setSelected (true);
		content.add (nameEntryPabel, "wrap");

		JButton submitButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
				UIConstants.GEOPOD_GREEN, false);
		submitButton.setText ("SUBMIT");
		submitButton.setActionCommand ("SUBMIT");
		submitButton.addActionListener (this);
		
		content.add (submitButton, "split 2");

		JButton cancelButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
				UIConstants.GEOPOD_GREEN, false);
		cancelButton.setText ("CANCEL");
		cancelButton.setActionCommand ("CANCEL");
		cancelButton.addActionListener (this);
		
		content.add (cancelButton);

		this.getContentPane ().add (content);
		super.pack ();
	}

	@Override
	public void actionPerformed (ActionEvent e)
	{
		String command = e.getActionCommand ();
		if (command.equals ("SUBMIT"))
		{
			String name = m_nameEntryField.getText ();
			if(!name.isEmpty ())
			{
				m_assessmentPanel.setUserEnteredName (name);
				boolean preserveName = m_preserveNameCheckbox.isSelected ();
				m_assessmentPanel.setPreserveName (preserveName);
				dispose();
			}
		}
		else if (command.equals ("CANCEL"))
		{
			dispose ();
		}
		
	}

}
