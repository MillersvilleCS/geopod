package geopod.gui.components;

import geopod.constants.UIConstants;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * GeopodIntegerTextField is loosely based off JFormattedTextField. As such,
 * this text field handles all validation of user input and maintains the most
 * recent valid value that was entered.
 * 
 * @author Geopod Team
 * 
 */
public class GeopodIntegerTextField
		extends GeopodTextField
		implements FocusListener
{
	private static final long serialVersionUID = 5703217436429099112L;
	private int m_value;
	private int m_lowerBounds;
	private int m_upperBounds;

	/**
	 * Constructs a GeopodTextField with a default valid range of
	 * Integer.MIN_VALUE to Integer.MAX_VALUE
	 */
	public GeopodIntegerTextField ()
	{
		super ();
		super.addFocusListener (this);
		setDefaultValidRange ();
	}

	/**
	 * Constructs a GeopodTextField with a default valid range of
	 * Integer.MIN_VALUE to Integer.MAX_VALUE with the specified number of
	 * columns
	 * 
	 * @param columns
	 *            the number of columns
	 */
	public GeopodIntegerTextField (int columns)
	{
		super (columns);
		super.addFocusListener (this);
		setDefaultValidRange ();
	}

	/**
	 * Sets the text to the specified value and validates the text field. The
	 * current value will reflect this value iff this value is inside the valid
	 * range
	 * 
	 * @param value
	 *            the value to be set
	 */
	public void setValue (int value)
	{
		super.setText (Integer.toString (value));
		validateTextField ();
	}

	/**
	 * Sets the default valid range on this GeopodIntegerTextField
	 */
	private void setDefaultValidRange ()
	{
		m_lowerBounds = Integer.MIN_VALUE;
		m_upperBounds = Integer.MAX_VALUE;
	}

	/**
	 * Sets a new valid range to the specified lower bounds and upper bounds
	 * 
	 * @param lowerBounds
	 *            exclusive lower bounds to be set
	 * @param upperBounds
	 *            inclusive upper bounds to be set
	 */
	public void setValidRange (int lowerBounds, int upperBounds)
	{
		m_lowerBounds = lowerBounds;
		m_upperBounds = upperBounds;
	}

	/**
	 * Returns the most recent valid edit
	 */
	public int getValue ()
	{
		return (m_value);
	}

	/**
	 * Validates the TextField and sets colors to indicate validity. If a valid
	 * edit has been made, the current value is updated to reflect this change.
	 */
	public void validateTextField ()
	{
		if (isEditValid ())
		{
			super.setBackground (UIConstants.GEOPOD_GREEN);
		}
		else
		{
			super.setBackground (UIConstants.GEOPOD_RED);
		}
	}

	/**
	 * Updates the current value to the specified valid value.
	 * 
	 * @param validValue
	 *            the specified value
	 */
	private void commitEdit (int validValue)
	{
		m_value = validValue;
	}

	/**
	 * Returns whether or not the edit is valid. An edit is valid iff it can be
	 * parsed and it is inside the valid range
	 * 
	 * @return true is valid edit, false otherwise
	 */
	private boolean isEditValid ()
	{
		try
		{
			int value = Integer.parseInt (super.getText ());
			boolean isValid = isInsideValidRange (value);

			if (isValid)
			{
				commitEdit (value);
			}

			return (isValid);
		}
		catch (NumberFormatException e)
		{
			// Parse failed; therefore invalid
			return (false);
		}
	}

	/**
	 * Returns whether or not the specified value is inside the valid range.
	 * 
	 * @param value
	 *            the specified value
	 * @return true if inside the range, false otherwise
	 */
	private boolean isInsideValidRange (int value)
	{
		return (value > m_lowerBounds && value <= m_upperBounds);
	}

	@Override
	public void focusGained (FocusEvent e)
	{
		super.selectAll ();
	}

	@Override
	public void focusLost (FocusEvent e)
	{
		// Do nothing.
	}
}
