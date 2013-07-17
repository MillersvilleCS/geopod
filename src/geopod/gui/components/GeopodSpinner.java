package geopod.gui.components;

import geopod.constants.UIConstants;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;

public class GeopodSpinner
		extends JComponent
{
	private static final long serialVersionUID = -2525650531330215900L;

	private static int INCREMENT_AMOUNT;

	static
	{
		INCREMENT_AMOUNT = 1;
	}

	// Model State
	private int m_currentValue;
	private int m_minValue;
	private int m_maxValue;
	private int m_incrementAmount;

	// Gui
	private GeopodTextField m_textField;
	private GeopodButton m_incrementButton;
	private GeopodButton m_decrementButton;

	public GeopodSpinner (int defaultValue, int minValue, int maxValue)
	{
		this (defaultValue, minValue, maxValue, INCREMENT_AMOUNT);
	}

	public GeopodSpinner (int defaultValue, int minValue, int maxValue, int incrementAmount)
	{
		m_currentValue = defaultValue;
		m_minValue = minValue;
		m_maxValue = maxValue;
		m_incrementAmount = incrementAmount;

		this.buildGui ();
	}

	private void buildGui ()
	{
		super.setLayout (new BoxLayout (this, BoxLayout.PAGE_AXIS));
		//super.setPreferredSize (new Dimension (36,90));
		//super.setMaximumSize (new Dimension (36, 90));

		String pathUp = "//Resources/Images/User Interface/Spinner/spinButtonTop.png";
		String pathDown = "//Resources/Images/User Interface/Spinner/spinButtonTopDown.png";
		String pathGlow = "//Resources/Images/User Interface/Spinner/spinButtonTopHover.png";

		m_incrementButton = new GeopodButton (pathUp, pathUp, pathDown, pathDown, pathGlow, pathGlow);
		m_incrementButton.setAlignmentX (JComponent.CENTER_ALIGNMENT);

		super.add (m_incrementButton);
		m_incrementButton.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				incrementValue ();
			}
		});

		m_textField = new GeopodTextField (0);
		final float textFieldFontSize = 15.0f;
		Font textFieldFont = UIConstants.GEOPOD_VERDANA.deriveFont (Font.BOLD, textFieldFontSize);
		m_textField.setFont (textFieldFont);
		m_textField.setText (m_currentValue + "");
		m_textField.setEditable (false);
		m_textField.setFocusable (false);
		m_textField.setHighlighter (null);
		final int textFieldWidth = 26;
		m_textField.setPreferredSize (new Dimension (textFieldWidth, 24));
		m_textField.setMaximumSize (new Dimension (textFieldWidth, 24));
		m_textField.setMinimumSize (new Dimension (textFieldWidth, 24));
		m_textField.setBorder (javax.swing.BorderFactory.createEmptyBorder ());
		m_textField.setAlignmentX (JComponent.CENTER_ALIGNMENT);

		super.add (m_textField);

		pathUp = "//Resources/Images/User Interface/Spinner/spinButtonBottom.png";
		pathDown = "//Resources/Images/User Interface/Spinner/spinButtonBottomDown.png";
		pathGlow = "//Resources/Images/User Interface/Spinner/spinButtonBottomHover.png";
		m_decrementButton = new GeopodButton (pathUp, pathUp, pathDown, pathDown, pathGlow, pathGlow);
		m_decrementButton.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				decrementValue ();
			}
		});
		m_decrementButton.setAlignmentX (JComponent.CENTER_ALIGNMENT);

		super.add (m_decrementButton);
	}

	public int getValue ()
	{
		return (m_currentValue);
	}

	public void setValue (int value)
	{
		if (value > m_minValue && value <= m_maxValue)
		{
			m_currentValue = value;
			updateTextField ();
		}
	}

	public void setToolTips (String upButtonTip, String downButtonTip, String textFieldTip)
	{
		m_incrementButton.setToolTipText (upButtonTip);
		m_decrementButton.setToolTipText (downButtonTip);
		m_textField.setToolTipText (textFieldTip);
	}

	public void incrementValue ()
	{
		int previousValue = m_currentValue;

		m_currentValue += m_incrementAmount;
		m_currentValue %= m_maxValue;

		//Skip Over Zero
		if (m_currentValue == 0)
		{
			incrementValue ();
			return;
		}

		firePropertyChange ("value", previousValue, m_currentValue);
		updateTextField ();
	}

	public void decrementValue ()
	{
		int previousValue = m_currentValue;

		m_currentValue -= m_incrementAmount;
		m_currentValue = (m_currentValue + m_maxValue);
		m_currentValue %= m_maxValue;

		//Skip Over Zero
		if (m_currentValue == 0)
		{
			decrementValue ();
			return;
		}

		firePropertyChange ("value", previousValue, m_currentValue);
		updateTextField ();
	}

	private void updateTextField ()
	{
		m_textField.setText (m_currentValue + "");
	}
}
