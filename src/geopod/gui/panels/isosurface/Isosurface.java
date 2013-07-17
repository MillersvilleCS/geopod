package geopod.gui.panels.isosurface;

import geopod.utils.math.VisadUtility;

import java.util.Map;
import java.util.TreeMap;

import ucar.unidata.idv.control.ThreeDSurfaceControl;
import visad.Unit;

/**
 * An encapsulation for Idv ThreeDSurfaceControls
 * 
 */

public class Isosurface
{
	private ThreeDSurfaceControl m_surface;
	// A way to correct the unit identifers for isosurfaces, map old abreviation to new one.
	private static Map<String, String> m_unitAbbreviations;
	
	static 
	{
		m_unitAbbreviations = new TreeMap<String, String> ();
		m_unitAbbreviations.put ("celsius", "Cel");
		m_unitAbbreviations.put ("fahrenheit", "F");
		m_unitAbbreviations.put ("kelvin", "K");
	}

	public Isosurface ()
	{
	}

	/**
	 * Constructs an Isosurface object from a ThreeDSurfaceControl
	 * 
	 * @param ThreeDSurfaceControl
	 *            surface
	 */
	public Isosurface (ThreeDSurfaceControl surface)
	{
		m_surface = surface;
	}

	private double getSurfaceValue ()
	{
		if (m_surface == null)
		{
			return (Double.NaN);
		}

		return (VisadUtility.getValue (m_surface.getSurfaceValue (), m_surface.getRawDataUnit (),
				m_surface.getDisplayUnit ()));
	}

	public double getValue ()
	{
		return (getSurfaceValue ());
	}

	public Unit getUnit ()
	{
		return (m_surface.getDisplayUnit ());
	}

	public String getUnitIdentifier ()
	{
		String unitName = "";
		
		if (m_surface == null)
		{
			return unitName;
		}
		
		unitName = getUnit ().getIdentifier ();
		
		if (m_unitAbbreviations.containsKey (unitName.toLowerCase ()))
		{
			unitName = m_unitAbbreviations.get (unitName);
		}

		return (unitName);
	}

	public String getParameterName ()
	{
		String parameter = null;
		try
		{
			parameter = m_surface.getDataChoice ().toString ();
		}
		catch (NullPointerException exp)
		{
			parameter = "";
		}
		return (parameter);
	}
}
