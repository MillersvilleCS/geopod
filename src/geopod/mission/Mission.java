package geopod.mission;

import geopod.utils.debug.Debug;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * A mission consists of the address of the URL containing the background
 * information for the mission and a list of objectives associated with the
 * mission.
 * 
 * @author geopod
 * 
 */
public class Mission
		extends Failable
{
	private String m_missionId;
	private String m_missionTitle;
	private String m_backgroundText;
	private List<Objective> m_objectives;

	/**
	 * Creates a new mission with the information located in the file at the
	 * given location. Missions can only be initialized from XML files with the
	 * appropriate structure. If the mission location does not exist or contains
	 * invalid information, mission initialization will fail.
	 * 
	 * @param missionLocation
	 *            - address of xml file containing mission information
	 */
	public Mission (String missionLocation)
	{
		try
		{
			Document document = createDocument (missionLocation);

			readId (document);

			readTitle (document);

			readBackground (document);

			readObjectives (document);
		}
		catch (Exception e)
		{
			recordInitializationFailure ();
			if (Debug.isDebuggingOn ())
			{
				e.printStackTrace ();
			}
		}
	}

	private Document createDocument (String missionFileLocation)
			throws Exception
	{
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance ();
		DocumentBuilder builder = builderFactory.newDocumentBuilder ();

		File missionFile = new File (missionFileLocation);
		String encodedMission = FileUtils.readFileToString (missionFile);

		MissionEncrypter missionEncrypter = MissionEncrypter.getEncrypter ();
		String missionInXml = missionEncrypter.decrypt (encodedMission);

		StringReader stringReader = new StringReader (missionInXml);
		InputSource inputSource = new InputSource (stringReader);

		Document document = builder.parse (inputSource);
		document.getDocumentElement ().normalize ();

		return (document);
	}

	private void readId (Document document)
	{
		NodeList missionIdElements = document.getDocumentElement ().getElementsByTagName ("missionId");
		if (missionIdElements.getLength () == 1)
		{
			m_missionId = missionIdElements.item (0).getChildNodes ().item (0).getNodeValue ();
		}
		else
		{
			recordInitializationFailure ("Mission initialization failed: missing mission id");
		}
	}

	private void readTitle (Document document)
	{
		NodeList missionTitleElements = document.getDocumentElement ().getElementsByTagName ("missionTitle");
		if (missionTitleElements.getLength () == 1)
		{
			m_missionTitle = missionTitleElements.item (0).getChildNodes ().item (0).getNodeValue ();
		}
		else
		{
			recordInitializationFailure ("Mission initialization failed: no mission title");
		}
	}

	private void readBackground (Document document)
	{
		NodeList backgroundTextElements = document.getDocumentElement ().getElementsByTagName ("background");
		if (backgroundTextElements.getLength () == 1)
		{
			try
			{
				// get the content of the backgroud text element, preserving html tags from the ckeditor
				Transformer xmlToStringTransformer = TransformerFactory.newInstance ().newTransformer ();

				DOMSource source = new DOMSource (backgroundTextElements.item (0));
				StreamResult result = new StreamResult (new StringWriter ());
				xmlToStringTransformer.setOutputProperty (OutputKeys.OMIT_XML_DECLARATION, "yes");
				xmlToStringTransformer.transform (source, result);

				m_backgroundText = result.getWriter ().toString ();
			}
			catch (Exception e)
			{
				// load 'plain text' mode, probably not what we wanted, but at least it's something
				m_backgroundText = backgroundTextElements.item (0).getTextContent ();
				e.printStackTrace ();
			}
		}
		else
		{
			recordInitializationFailure ("Mission initialization failed: error reading background");
		}
	}

	private void readObjectives (Document document)
	{
		m_objectives = new ArrayList<Objective> ();

		NodeList missionObjectives = document.getElementsByTagName ("objectives");
		if (missionObjectives.getLength () == 1)
		{
			Element objectives = (Element) missionObjectives.item (0);

			NodeList objectivesList = objectives.getElementsByTagName ("objective");
			for (int objectiveNum = 0; objectiveNum < objectivesList.getLength () && !m_initializationFailed; ++objectiveNum)
			{
				Element objectiveElement = (Element) objectivesList.item (objectiveNum);
				Objective newObjective = new Objective (objectiveElement, objectiveNum + 1);
				m_objectives.add (newObjective);
				checkForFailure (newObjective);
			}
		}
		else
		{
			recordInitializationFailure ("Mission initialization failed: no objectives element");
		}
	}

	public String getMissionId ()
	{
		return (m_missionId);
	}

	public String getMissionTitle ()
	{
		return (m_missionTitle);
	}

	public String getBackgroundText ()
	{
		return (m_backgroundText);
	}

	public List<Objective> getObjectives ()
	{
		return (m_objectives);
	}
}
