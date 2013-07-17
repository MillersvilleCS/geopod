package geopod.gui.panels.isosurface;

import geopod.constants.UIConstants;
import geopod.gui.components.GeopodPulseTextField;
import geopod.utils.math.MathUtility;
import geopod.utils.math.VisadUtility;

import java.awt.Color;

import javax.swing.SwingConstants;

import ucar.unidata.idv.control.ThreeDSurfaceControl;
import ucar.unidata.util.Range;
import visad.Real;
import visad.Unit;
import visad.VisADException;

public class IsovalueTextField
		extends GeopodPulseTextField
{
	private static final long serialVersionUID = -1012666322769725736L;

	private ThreeDSurfaceControl m_surfaceControl;
	private static final String FORMAT_STRING;
	private static final Color NORMAL_BACKGROUND;
	private static final Color ERROR_BACKGROUND;

	static
	{
		FORMAT_STRING = "%4.2f";
		NORMAL_BACKGROUND = Color.WHITE;
		ERROR_BACKGROUND = UIConstants.GEOPOD_RED;
	}

	public IsovalueTextField (ThreeDSurfaceControl surfaceControl)
	{
		super (6);
		super.setBackground (NORMAL_BACKGROUND);
		super.setHorizontalAlignment (SwingConstants.RIGHT);
		super.setEditable (true);

		m_surfaceControl = surfaceControl;
	}

	/**
	 * 
	 * @return a range representing the max and min parameter values allowed for
	 *         this text field's isosurface
	 */
	private Range getValidDataRange ()
	{
		// Don't modify the validValueRange directly!!
		Range validValueRange = m_surfaceControl.getDataRange ();

		double minValue = toSurfaceDisplayUnit (validValueRange.min);
		double maxValue = toSurfaceDisplayUnit (validValueRange.max);

		Range rangeInDisplayUnits = new Range (minValue, maxValue);
		return (rangeInDisplayUnits);
	}

	/**
	 * Display the surface isovalue in the text field.
	 */
	public void displayIsovalue ()
	{
		super.setBackground (NORMAL_BACKGROUND);

		double surfaceValue = this.getSurfaceValue ();
		if (surfaceValue == Double.NaN)
		{
			super.setText ("N/A");
		}
		else
		{
			super.setText (String.format (FORMAT_STRING, surfaceValue));
		}

		Range validValueRange = getValidDataRange ();
		// truncate, rather than round, otherwise, could display numbers that would be not valid if entered
		String minAllowed = String.format (FORMAT_STRING, Math.ceil (validValueRange.min * 100) / 100);
		String maxAllowed = String.format (FORMAT_STRING, Math.floor (validValueRange.max * 100) / 100);
		super.setToolTipText (" Enter a number between " + minAllowed + " and " + maxAllowed + " ");
	}

	/**
	 * Returns the isovalue associated with the text field's isosurface as a
	 * Real.
	 * 
	 * @return double surfaceValue
	 */
	private double getSurfaceValue ()
	{
		if (m_surfaceControl == null)
		{
			return (Double.NaN);
		}
		double surfaceValue = m_surfaceControl.getSurfaceValue ();
		double valueInDisplayUnit = toSurfaceDisplayUnit (surfaceValue);
		return (valueInDisplayUnit);
	}

	private double toSurfaceDisplayUnit (double value)
	{
		Unit rawUnit = m_surfaceControl.getRawDataUnit ();
		Unit displayUnit = m_surfaceControl.getDisplayUnit ();

		try
		{
			Real valueInDisplayUnits = new Real (visad.RealType.Generic, rawUnit.toThat (value, displayUnit),
					displayUnit);
			double displayUnitsDouble = valueInDisplayUnits.getValue ();
			return (displayUnitsDouble);
		}
		catch (VisADException e)
		{
			return (Double.NaN);
		}

	}

	private void updateBackground (boolean contentValid)
	{
		if (contentValid)
		{
			super.setBackground (NORMAL_BACKGROUND);
		}
		else
		{
			super.setBackground (ERROR_BACKGROUND);
		}
	}

	public void validateInput ()
	{
		boolean contentValid = checkTextboxContent ();
		updateBackground (contentValid);
	}

	/**
	 * Returns true if this text field's current text is valid, that is, a
	 * number within the allowed range of parameter values
	 * 
	 * @return whether or not the current text is a valid value
	 */
	public boolean checkTextboxContent ()
	{
		String displayedText = super.getText ();
		// The new value is in the display, parse it
		double displayValue = MathUtility.parseDouble (displayedText);

		return (!Double.isNaN (displayValue) && valueInRange (displayValue));
	}

	/**
	 * Determines if a value would be a valid parameter value for a this text
	 * field's isosurface
	 * 
	 * @param valueToTest
	 * @return whether or not the value is in the valid range
	 */
	private boolean valueInRange (double valueToTest)
	{
		Range validValueRange = getValidDataRange ();
		boolean isInRange = (valueToTest >= validValueRange.min && valueToTest <= validValueRange.max);
		return (isInRange);
	}

	/**
	 * Attempt to set the current isovalue of this text field's isosurface to be
	 * the value currently in the textbox. If is successful, returns true. If
	 * fails, sets textbox to error background and returns false;
	 * 
	 * @return true if and only if we actually changed the isovalue
	 */
	public boolean setSurfaceToValue ()
	{
		boolean successfullySet = (m_surfaceControl != null && checkTextboxContent () && setToDisplayValue ());
		updateBackground (successfullySet);
		return (successfullySet);
	}

	/**
	 * Attempt to set the isovalue with the value displayed in this text box.
	 * 
	 * @return true if we actually changed the isosurface value.
	 */
	private boolean setToDisplayValue ()
	{
		double rawValue = 0.0;
		double displayValue = MathUtility.parseDouble (super.getText ());
		Unit rawUnit = m_surfaceControl.getRawDataUnit ();
		Unit displayUnit = m_surfaceControl.getDisplayUnit ();
		try
		{
			rawValue = VisadUtility.getValue (displayValue, displayUnit, rawUnit);
			m_surfaceControl.setLevelWithRawValue (rawValue);
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}
}
