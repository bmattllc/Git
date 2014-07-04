package com.brianmattllc.objectat.events;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import com.brianmattllc.objectat.logging.*;
import com.brianmattllc.objectat.communication.*;

/**
 * @author Brian Matt
 * 
 * Events storage class, this will be the main class for event
 * sorting and storage.
 */

public class ObjectatEvents {
	private String eventsPropertiesFile = "events.properties";
	private Properties eventsProperties = new Properties();
	private ArrayList<ObjectatEvent> arrayListOfEvents = new ArrayList<ObjectatEvent>();
	private HashMap<String,Integer> hashMapOfKeys = new HashMap<String,Integer>();
	private ObjectatLogger logger;
	private long eventId = 0;
	private ObjectatEventsStats eventStats;
	private ArrayList<ObjectatClientWriter> arrayListOfObjectatClientWriters = new ArrayList<ObjectatClientWriter>();
	
	public ObjectatEvents() {
		this.init();
	}
	
	public ObjectatEvents(ObjectatLogger logger) {
		this.logger = logger;
		eventStats = new ObjectatEventsStats(logger);
		new Thread(eventStats).start();
	}
	
	public void init() {
		this.loadProperties();
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
}
