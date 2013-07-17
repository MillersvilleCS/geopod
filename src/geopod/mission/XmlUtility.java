package geopod.mission;

import geopod.utils.debug.Debug;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class XmlUtility
{
	private XmlUtility ()
	{

	}

	public static Document createDocument ()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
			DocumentBuilder documentBuilder = factory.newDocumentBuilder ();

			Document document = documentBuilder.newDocument ();
			return (document);
		}
		catch (ParserConfigurationException e)
		{
			if (Debug.isDebuggingOn ())
				e.printStackTrace ();
			return (null);
		}
	}

	public static void generateXmlElement (Document document, Element elementToAppendTo, String tagName, String tagData)
	{
		Element element = document.createElement (tagName);
		Text content = document.createTextNode (tagData);
		element.appendChild (content);
		elementToAppendTo.appendChild (element);
	}

}
