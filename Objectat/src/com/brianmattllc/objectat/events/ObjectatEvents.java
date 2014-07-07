package com.brianmattllc.objectat.events;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;

import com.brianmattllc.objectat.communication.ObjectatClientWriter;
import com.brianmattllc.objectat.logging.ObjectatLogLevel;
import com.brianmattllc.objectat.logging.ObjectatLogger;

/**
 * @author Brian Matt
 * 
 * Events storage class, this will be the main class for event
 * sorting and storage.
 */

@XmlRootElement
public class ObjectatEvents {
	private String objectatName = "Objectat";
	private String eventsSnapshotFile = "ObjectatEvents.xml";
	private String eventsPropertiesFile = "events.properties";
	private Properties eventsProperties = new Properties();
	private JAXBContext objectatEventsJAXBContext = null;
	private Marshaller objectatEventsJAXBMarshaller = null;
	private Unmarshaller objectatEventsJAXBUnmarshaller = null;
	private ArrayList<ObjectatEvent> arrayListOfEvents = new ArrayList<ObjectatEvent>();
	private HashMap<String,Integer> hashMapOfKeys = new HashMap<String,Integer>();
	
	private ObjectatLogger logger = new ObjectatLogger(ObjectatLogLevel.ERROR);
	private long eventId = 0;
	private ObjectatEventsStats eventStats;
	private Thread eventStatsThread;
	private ArrayList<ObjectatClientWriter> arrayListOfObjectatClientWriters = new ArrayList<ObjectatClientWriter>();
	private ObjectatEventsDiskWriter eventsDiskWriter;	
	private Thread eventsDiskWriterThread;
	
	public ObjectatEvents() {
		// This constructor is called by JAXB and should only be used for 
		// that purpose.  If used otherwise, consider calling the init method
		// after invocation.		
	}
	
	public ObjectatEvents(ObjectatLogger logger) {
		this.logger = logger;
		this.eventStats = new ObjectatEventsStats(logger);
		this.eventStatsThread = new Thread(this.eventStats);
		this.eventStatsThread.start();
		
		this.eventsDiskWriter = new ObjectatEventsDiskWriter(this, this.logger);
		this.eventsDiskWriterThread = new Thread(this.eventsDiskWriter);
		
		try {
			this.objectatEventsJAXBContext = JAXBContext.newInstance(ObjectatEvents.class);
		} catch (JAXBException e) {
			this.logger.log(ObjectatLogLevel.DEBUG, this.getClass() + ": Error while creating ObjectatEvents object.  JAXBException: " + e.getMessage());
		}
		this.init();
	}
	
	public void init() {
		//this.loadProperties();
		this.logger.log(ObjectatLogLevel.DEBUG, this.getClass() + ": Initializing ObjectatEvents Object.");
		
		try {
			this.objectatEventsJAXBMarshaller = this.objectatEventsJAXBContext.createMarshaller();
			this.objectatEventsJAXBMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			this.objectatEventsJAXBUnmarshaller = this.objectatEventsJAXBContext.createUnmarshaller();
			
			File objectatEventsDiskImage = new File(this.getEventsSnapshotFile());
			
			try {
				FileReader objectatEventsFileReader = new FileReader(objectatEventsDiskImage);
				this.logger.log(ObjectatLogLevel.DEBUG, this.getClass() + ": Reading ObjectatEvents image from disk.");
				
				ObjectatEvents objectatEventsImage = (ObjectatEvents) this.objectatEventsJAXBUnmarshaller.unmarshal(objectatEventsFileReader);
				
				if (objectatEventsImage != null) {
					this.logger.log(ObjectatLogLevel.DEBUG, this.getClass() + ": Successfully unmarshalled disk image of Objectat Events.  Image contained " + objectatEventsImage.getAllEvents().size() + " event(s)");
					this.arrayListOfEvents = objectatEventsImage.getArrayListOfEvents();
					this.hashMapOfKeys = objectatEventsImage.getHashMapOfKeys();
					this.eventStats.setEventsInObjectat(this.getArrayListOfEvents().size());
				}
			} catch (JAXBException e) {
				this.logger.log(ObjectatLogLevel.DEBUG, this.getClass() + ": Failed to process disk image of ObjectatEvent.  JAXBException: " + e.getMessage());
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				this.logger.log(ObjectatLogLevel.DEBUG, this.getClass() + ": No image of ObjectatEvents found on disk.");
			}
		} catch (JAXBException e) {
			this.logger.log(ObjectatLogLevel.FATAL, this.getClass() + ": Failed to get ObjectatEvents JAXB Context.  This will prevent marshalling of ObjectatEvents object and prevent snapshots from being generated.  JAXBException: " + e.getMessage());
		}
		
		// Start disk writer
		this.eventsDiskWriterThread.start();
	}
	
	public void destroy() {
		this.eventStats.setDone(true);
	}
	
	public boolean loadProperties () {
		boolean success = false;
		try {
			FileInputStream in = new FileInputStream(eventsPropertiesFile);
			eventsProperties.load(in);
			
			success = true;
		} catch (FileNotFoundException e) {
		
		} catch (IOException e) {
			
		}
		
		return success;
	}
	
	public synchronized boolean addEvent (ObjectatEvent event) throws Exception {
		boolean success = false;
		if (hashMapOfKeys.containsKey(event.getKey())) {
			int eventIndex = hashMapOfKeys.get(event.getKey());
			ObjectatEvent deduplicateEvent = arrayListOfEvents.get(eventIndex);
			
			if (!deduplicateEvent.getKey().equals(event.getKey())) {
				this.logger.log(ObjectatLogLevel.WARN, "Index of keys is invalid before deduplicating event, attempting to re-index event keys.");
				this.reindexEventKeys();
				
				deduplicateEvent = arrayListOfEvents.get(hashMapOfKeys.get(event.getKey()));
				if (!deduplicateEvent.getKey().equals(event.getKey())) {
					// TODO
					// Define custom exception(s)
					this.logger.log(ObjectatLogLevel.FATAL, "Indexing of event keys failed, Objectat is corrupt.");
					Exception e = new Exception("Indexing of event keys failed, Objectat is corrupt.");
					throw e;
				}
			}
			
			success = deduplicateEvent.deduplicate(event);
		} else {
			event.setFirst(new Date());
			event.setLast(new Date());
			long thisEventId = ++eventId;
			event.setEventId(thisEventId);
			int newEventIndex = arrayListOfEvents.size();
			arrayListOfEvents.add(event);
			hashMapOfKeys.put(event.getKey(), newEventIndex);
			
			if (!arrayListOfEvents.get(hashMapOfKeys.get(event.getKey())).getKey().equals(event.getKey())) {
				// Key mapping is invalid, re-index keys
				this.logger.log(ObjectatLogLevel.WARN, "Index of keys is invalid after adding event, attempting to re-index event keys.");
				this.reindexEventKeys();
				
				if (!arrayListOfEvents.get(hashMapOfKeys.get(event.getKey())).getKey().equals(event.getKey())) {
					// TODO
					// Define custom exception(s)
					this.logger.log(ObjectatLogLevel.FATAL, "Indexing of event keys failed, Objectat is corrupt.");
					Exception e = new Exception("Indexing of event keys failed, Objectat is corrupt.");
					throw e;
				}
			}
			
			success = true;
		}
		
		this.eventStats.setEventsProcessed(this.eventStats.getEventsProcessed() + 1);
		this.eventStats.setEventsInObjectat(this.getArrayListOfEvents().size());
		
		if (success) {
			for (int i = 0; i < this.getArrayListOfObjectatClientWriters().size(); i++) {
				this.getArrayListOfObjectatClientWriters().get(i).addObjectToQueue(
						arrayListOfEvents.get(hashMapOfKeys.get(event.getKey()))
				);
			}
		}
		
		return success;
	}
	
	public synchronized boolean removeEvent (ObjectatEvent event) throws Exception {
		boolean success = false;
		
		if (hashMapOfKeys.containsKey(event.getKey())) {
			if (!arrayListOfEvents.get(hashMapOfKeys.get(event.getKey())).getKey().equals(event.getKey())) {
				// Key mapping is invalid, re-index keys
				this.logger.log(ObjectatLogLevel.WARN, "Index of keys is invalid before removing event, attempting to re-index event keys.");
				this.reindexEventKeys();
				if (!arrayListOfEvents.get(hashMapOfKeys.get(event.getKey())).getKey().equals(event.getKey())) {
					// TODO
					// Define custom exception(s)
					this.logger.log(ObjectatLogLevel.FATAL, "Indexing of event keys failed, Objectat is corrupt.");
					Exception e = new Exception("Indexing of event keys failed, Objectat is corrupt.");
					throw e;
				}
			}
			
			int removeEventIndex = hashMapOfKeys.get(event.getKey());
			arrayListOfEvents.remove(removeEventIndex);
			hashMapOfKeys.remove(event.getKey());
			
			// Shift key index down
			for (int i = removeEventIndex; i < arrayListOfEvents.size(); i++) {
				hashMapOfKeys.remove(arrayListOfEvents.get(i).getKey());
				hashMapOfKeys.put(arrayListOfEvents.get(i).getKey(), i);
			}
			
			// Check consistency of key index
			int lastEventIndex = arrayListOfEvents.size() - 1;
			if (
					!hashMapOfKeys.get(arrayListOfEvents.get(removeEventIndex).getKey()).equals(removeEventIndex) 
					|| !hashMapOfKeys.get(arrayListOfEvents.get(lastEventIndex).getKey()).equals(lastEventIndex)
			) {
				// Index of keys is invalid, re-index keys
				this.logger.log(ObjectatLogLevel.WARN, "Index of keys is invalid after removing event, attempting to re-index event keys.");
				this.reindexEventKeys();
				if (
						!hashMapOfKeys.get(arrayListOfEvents.get(removeEventIndex).getKey()).equals(removeEventIndex) 
						|| !hashMapOfKeys.get(arrayListOfEvents.get(lastEventIndex).getKey()).equals(lastEventIndex)
				) {
					// TODO
					// Define custom exception(s)
					this.logger.log(ObjectatLogLevel.FATAL, "Indexing of event keys failed, Objectat is corrupt.");
					Exception e = new Exception("Indexing of event keys failed, Objectat is corrupt.");
					throw e;
				}
			}
		}
		
		this.eventStats.setEventsInObjectat(this.getArrayListOfEvents().size());
		
		if (success) {
			for (int i = 0; i < this.getArrayListOfObjectatClientWriters().size(); i++) {
				this.getArrayListOfObjectatClientWriters().get(i).addObjectToQueue(
						arrayListOfEvents.get(hashMapOfKeys.get(event.getKey()))
				);
			}
		}
		
		return success;
	}
	
	public void reindexEventKeys() {
		this.logger.log(ObjectatLogLevel.DEBUG, "Re-index of event keys initiated.");
		hashMapOfKeys.clear();
		for (int i = 0; i < arrayListOfEvents.size(); i++) {
			this.logger.log(ObjectatLogLevel.DEBUG, "Key " + arrayListOfEvents.get(i).getKey() + " indexed at " + i);
			hashMapOfKeys.put(arrayListOfEvents.get(i).getKey(), i);
		}
	}
	
	public ArrayList<ObjectatEvent> getAllEvents () {
		return this.arrayListOfEvents;
	}
	
	public ObjectatEvent getEventByKey (String key) throws Exception {
		ObjectatEvent event = null;
		
		if (hashMapOfKeys.containsKey(key)) {
			if (!arrayListOfEvents.get(hashMapOfKeys.get(key)).getKey().equals(key)) {
				this.logger.log(ObjectatLogLevel.WARN, "Index of keys is invalid while attempting to retrieve event by key, attempting to re-index keys.");
				this.reindexEventKeys();
				
				if (!arrayListOfEvents.get(hashMapOfKeys.get(key)).getKey().equals(key)) {
					// TODO
					// Add custom exception(s)
					this.logger.log(ObjectatLogLevel.FATAL, "Indexing of event keys failed, Objectat is corrupt.");
					Exception e = new Exception("Indexing of event keys failed, Objectat is corrupt.");
					throw e;
				}
			}
			
			event = arrayListOfEvents.get(hashMapOfKeys.get(key));
		}
		
		return event;
	}
	
	public ArrayList<ObjectatEvent> getMatchingEvents (ObjectatEvent event) {
		ArrayList<ObjectatEvent> arrayListOfEvents = new ArrayList<ObjectatEvent>();
		
		
		
		return arrayListOfEvents;
	}
	
	public ObjectatEventsStats getEventsStats () {
		return this.eventStats;
	}

	public ArrayList<ObjectatClientWriter> getArrayListOfObjectatClientWriters() {
		return arrayListOfObjectatClientWriters;
	}

	public String getEventsSnapshotFile() {
		return eventsSnapshotFile;
	}

	public void setEventsSnapshotFile(String eventsSnapshotFile) {
		this.eventsSnapshotFile = eventsSnapshotFile;
	}
	
	public synchronized boolean writeObjectatEventsToDisk () {
		// TODO
		// Write to intermediate file to prevent file corruption in the event
		// the Objectat process is killed mid marshal
		
		boolean success = false;
		
		try {
			File tempFile = new File(this.getEventsSnapshotFile() + ".tmp");
			File diskImageFile = new File(this.getEventsSnapshotFile());
			this.objectatEventsJAXBMarshaller.marshal(this, tempFile);
			if (diskImageFile.exists()) {
				diskImageFile.delete();
			}
			tempFile.renameTo(diskImageFile);
		} catch (JAXBException e) {
			this.logger.log(ObjectatLogLevel.ERROR, this.getClass() + ": Failed to marshall ObjectatEvents to XML file " + this.getEventsSnapshotFile() + ".  JAXBException: " + e.getMessage());
		}
		
		return success;
	}

	/**
	 * @return the arrayListOfEvents
	 */
	public ArrayList<ObjectatEvent> getArrayListOfEvents() {
		return arrayListOfEvents;
	}

	/**
	 * @param arrayListOfEvents the arrayListOfEvents to set
	 */
	@XmlElement
	public void setArrayListOfEvents(ArrayList<ObjectatEvent> arrayListOfEvents) {
		this.arrayListOfEvents = arrayListOfEvents;
	}

	/**
	 * @return the hashMapOfKeys
	 */
	public HashMap<String, Integer> getHashMapOfKeys() {
		return hashMapOfKeys;
	}

	/**
	 * @param hashMapOfKeys the hashMapOfKeys to set
	 */
	@XmlElement
	public void setHashMapOfKeys(HashMap<String, Integer> hashMapOfKeys) {
		this.hashMapOfKeys = hashMapOfKeys;
	}

	/**
	 * @return the objectatName
	 */
	public String getObjectatName() {
		return objectatName;
	}

	/**
	 * @param objectatName the objectatName to set
	 */
	@XmlAttribute
	public void setObjectatName(String objectatName) {
		this.objectatName = objectatName;
	}
}
