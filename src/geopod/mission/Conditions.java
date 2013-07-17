package geopod.mission;

import geopod.devices.FlightDataRecorder;
import geopod.eventsystem.events.DataLoadedEvent;
import geopod.eventsystem.events.FlightEvent;
import geopod.eventsystem.events.GeopodEventId;
import geopod.eventsystem.events.ParticleImageChangedEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Conditions
{
	private Map<GeopodEventId, Integer> m_occurrenceConditions;
	private List<String> m_dataSourceConditions;
	private List<String> m_particleImageConditions;

	public Conditions (Element conditions)
	{
		m_occurrenceConditions = new EnumMap<GeopodEventId, Integer> (GeopodEventId.class);
		readOccurrenceEvents (conditions);

		m_dataSourceConditions = new ArrayList<String> ();
		m_particleImageConditions = new ArrayList<String> ();
		readSpecificEvents (conditions);
	}

	private Conditions (Map<GeopodEventId, Integer> occurrenceConditions, List<String> dataSourceConditions,
			List<String> particleImageConditions)
	{
		m_occurrenceConditions = occurrenceConditions;
		m_dataSourceConditions = dataSourceConditions;
		m_particleImageConditions = particleImageConditions;
	}

	private void readOccurrenceEvents (Element conditions)
	{
		NodeList occurrenceEvents = conditions.getElementsByTagName ("occurrenceEvent");
		for (int i = 0; i < occurrenceEvents.getLength (); i++)
		{
			Element occurrenceEvent = (Element) occurrenceEvents.item (i);
			GeopodEventId eventId = getEventId (occurrenceEvent);
			Integer minNumOccurrences = getMinNumEventOccurrences (occurrenceEvent);
			m_occurrenceConditions.put (eventId, minNumOccurrences);
		}
	}

	private void readSpecificEvents (Element conditions)
	{
		NodeList specificEvents = conditions.getElementsByTagName ("specificEvent");
		for (int i = 0; i < specificEvents.getLength (); i++)
		{
			Element specificEvent = (Element) specificEvents.item (i);
			GeopodEventId eventId = getEventId (specificEvent);
			if (eventId == GeopodEventId.ALL_CHOICES_LOADING_FINISHED)
			{
				List<String> requiredDataSources = getSpecificConditionRequirements (specificEvent, "dataSource");
				m_dataSourceConditions.addAll (requiredDataSources);
			}
			else if (eventId == GeopodEventId.PARTICLE_IMAGED)
			{
				List<String> requiredParticleImageCategories = getSpecificConditionRequirements (specificEvent,
						"imageCategory");
				m_particleImageConditions.addAll (requiredParticleImageCategories);
			}
		}
	}

	private GeopodEventId getEventId (Element occurrenceEvent)
	{
		NodeList eventType = occurrenceEvent.getElementsByTagName ("type");
		String eventTypeName = eventType.item (0).getChildNodes ().item (0).getNodeValue ();
		GeopodEventId eventId = Enum.valueOf (GeopodEventId.class, eventTypeName);
		return (eventId);
	}

	private Integer getMinNumEventOccurrences (Element occurrenceEvent)
	{
		NodeList requiredNumOccurrences = occurrenceEvent.getElementsByTagName ("minNum");
		String numOccurrences = requiredNumOccurrences.item (0).getChildNodes ().item (0).getNodeValue ();
		Integer minNumOccurrences = Integer.valueOf (numOccurrences);
		return (minNumOccurrences);
	}

	private List<String> getSpecificConditionRequirements (Element specificEvent, String requirementName)
	{
		List<String> specificConditionRequirements = new ArrayList<String> ();
		NodeList specificRequirements = specificEvent.getElementsByTagName (requirementName);
		for (int i = 0; i < specificRequirements.getLength (); ++i)
		{
			String specificRequirementName = specificRequirements.item (i).getChildNodes ().item (0).getNodeValue ();
			specificConditionRequirements.add (specificRequirementName);
		}
		return (specificConditionRequirements);
	}

	public boolean haveConditionsBeenMet (FlightDataRecorder dataRecord)
	{
		// Check to see if occurrence conditions have been met
		Set<GeopodEventId> eventSet = m_occurrenceConditions.keySet ();
		List<GeopodEventId> eventList = new ArrayList<GeopodEventId> (eventSet);
		for (int i = 0; i < eventList.size (); i++)
		{
			GeopodEventId eventId = eventList.get (i);
			Integer requiredNumEvents = m_occurrenceConditions.get (eventId);
			Integer actualNumEvents = dataRecord.getNumberOfOccurrences (eventId);
			if (actualNumEvents < requiredNumEvents)
			{
				return (false);
			}
		}

		// Check to see if specific conditions have been met
		boolean specificConditionsMet = areSpecificConditionsMet (dataRecord);

		return (specificConditionsMet);
	}

	private boolean areSpecificConditionsMet (FlightDataRecorder dataRecord)
	{
		boolean dataLoadingConditionsMet = areDataLoadingConditionsMet (dataRecord);
		if (!dataLoadingConditionsMet)
		{
			return (false);
		}

		boolean particleImageConditionsMet = areParticleImageCategoryConditionsMet (dataRecord);
		return (particleImageConditionsMet);
	}

	private boolean areDataLoadingConditionsMet (FlightDataRecorder dataRecord)
	{
		if (m_dataSourceConditions.isEmpty ())
		{
			// There are no specific dataLoadingConditions to be met
			return (true);
		}
		// Get all dataLoadingEvents and extract names of all data sources loaded
		List<FlightEvent> dataLoadedEvents = dataRecord
				.findOccurencesOfEvent (GeopodEventId.ALL_CHOICES_LOADING_FINISHED);
		Set<String> dataSourcesLoaded = new HashSet<String> ();
		for (int i = 0; i < dataLoadedEvents.size (); ++i)
		{
			DataLoadedEvent dataLoadedEvent = (DataLoadedEvent) dataLoadedEvents.get (i);
			String dataSourceLoadedName = dataLoadedEvent.getDataSourceName ();
			dataSourcesLoaded.add (dataSourceLoadedName);
		}
		// See if all the required data source names are among those already loaded 
		boolean conditionsWereMet = dataSourcesLoaded.containsAll (m_dataSourceConditions);
		return (conditionsWereMet);
	}

	private boolean areParticleImageCategoryConditionsMet (FlightDataRecorder dataRecord)
	{
		if (m_particleImageConditions.isEmpty ())
		{
			// There are no specific particleImagedConditions to be met
			return (true);
		}
		// Get all particleImageChanged events and extract names of categories imaged
		List<FlightEvent> particleImagedEvents = dataRecord.findOccurencesOfEvent (GeopodEventId.PARTICLE_IMAGED);
		Set<String> particleCategoriesImaged = new HashSet<String> ();
		for (int i = 0; i < particleImagedEvents.size (); ++i)
		{
			ParticleImageChangedEvent particleImageChangedEvent = (ParticleImageChangedEvent) particleImagedEvents
					.get (i);
			String particleCategoryName = particleImageChangedEvent.getParticleCategory ();
			particleCategoriesImaged.add (particleCategoryName);
		}
		// Check if required categories are among those that have been imaged
		boolean conditionsWereMet = particleCategoriesImaged.containsAll (m_particleImageConditions);
		return (conditionsWereMet);
	}

	public Map<GeopodEventId, Integer> getOccurenceConditions ()
	{
		return (m_occurrenceConditions);
	}

	public List<String> getDataSourceConditions ()
	{
		return (m_dataSourceConditions);
	}

	public List<String> getParticleImageCategoryConditions ()
	{
		return (m_particleImageConditions);
	}

	public static Conditions determineUnmetConditions (Conditions conditions, FlightDataRecorder dataRecord)
	{
		Map<GeopodEventId, Integer> unmetOccurrenceConditions = determineUnmetOccurenceConditions (
				conditions.getOccurenceConditions (), dataRecord);

		List<String> unmetDataSourceConditions = determineUnmetSpecificConditions (
				conditions.getDataSourceConditions (), GeopodEventId.ALL_CHOICES_LOADING_FINISHED, dataRecord);

		List<String> unmetParticleImageConditions = determineUnmetSpecificConditions (
				conditions.getParticleImageCategoryConditions (), GeopodEventId.PARTICLE_IMAGED, dataRecord);

		Conditions unmetConditions = new Conditions (unmetOccurrenceConditions, unmetDataSourceConditions,
				unmetParticleImageConditions);

		return (unmetConditions);
	}

	private static Map<GeopodEventId, Integer> determineUnmetOccurenceConditions (
			Map<GeopodEventId, Integer> occurrenceConditions, FlightDataRecorder dataRecord)
	{
		Map<GeopodEventId, Integer> unmetOccurrenceConditions = new EnumMap<GeopodEventId, Integer> (
				GeopodEventId.class);

		Set<GeopodEventId> eventSet = occurrenceConditions.keySet ();
		List<GeopodEventId> eventList = new ArrayList<GeopodEventId> (eventSet);
		for (int i = 0; i < eventList.size (); i++)
		{
			GeopodEventId eventId = eventList.get (i);
			Integer requiredNumEvents = occurrenceConditions.get (eventId);
			Integer actualNumEvents = dataRecord.getNumberOfOccurrences (eventId);
			if (actualNumEvents < requiredNumEvents)
			{
				Integer numEventsNotCompleted = requiredNumEvents - actualNumEvents;
				unmetOccurrenceConditions.put (eventId, numEventsNotCompleted);
			}
		}

		return (unmetOccurrenceConditions);
	}

	private static List<String> determineUnmetSpecificConditions (List<String> specificConditions,
			GeopodEventId eventType, FlightDataRecorder dataRecord)
	{
		List<FlightEvent> flightEventsOfAppropriateType = dataRecord.findOccurencesOfEvent (eventType);
		Set<String> specificValuesFromFlightEvents = new HashSet<String> ();
		for (int i = 0; i < flightEventsOfAppropriateType.size (); ++i)
		{
			String specificEventValue = "no specific value associated with this event";
			if (eventType == GeopodEventId.ALL_CHOICES_LOADING_FINISHED)
			{
				DataLoadedEvent dataLoadedEvent = (DataLoadedEvent) flightEventsOfAppropriateType.get (i);
				specificEventValue = dataLoadedEvent.getDataSourceName ();
			}
			else if (eventType == GeopodEventId.PARTICLE_IMAGED)
			{
				ParticleImageChangedEvent particleImageChangedEvent = (ParticleImageChangedEvent) flightEventsOfAppropriateType
						.get (i);
				specificEventValue = particleImageChangedEvent.getParticleCategory ();
			}
			specificValuesFromFlightEvents.add (specificEventValue);
		}

		List<String> unmetSpecificConditions = new ArrayList<String> (specificConditions);
		unmetSpecificConditions.removeAll (specificValuesFromFlightEvents);

		return (unmetSpecificConditions);
	}
}
