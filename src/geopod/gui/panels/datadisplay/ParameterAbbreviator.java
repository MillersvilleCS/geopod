package geopod.gui.panels.datadisplay;

import geopod.constants.UIConstants;

import java.util.Map;

public class ParameterAbbreviator
{
	private static Map<String, String> m_nameMap;
	
	static 
	{
		m_nameMap = UIConstants.SIMPLE_NAME_MAP;
	}
	
	/**
	 * Method for shortening IDV parameter names so they can be concisely displayed
	 * @param formalParameterName - the long name of the parameter
	 * @return  the shortened version of the parameter name
	 */
	public static String getAbbreviation (String formalParameterName)
	{
		String shortName = null;
		shortName = m_nameMap.get (formalParameterName);
		
		if (shortName == null)
		{
			shortName = formalParameterName.substring (0, 2);
		}
		
		return (shortName);
	}
}
