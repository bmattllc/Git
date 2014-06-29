package com.brianmattllc.objectat.events;

import java.util.Random;

import com.brianmattllc.objectat.logging.*;

public class ObjectatSyntheticEventGenerator implements Runnable {
	private ObjectatEvents events;
	private long frequency = 1L;
	private long maxEvents = 0L;
	private long minEvents = 0L;
	private boolean removeEvents = true;
	private ObjectatLogger logger = new ObjectatLogger(ObjectatLogLevel.FATAL);
	
	public ObjectatSyntheticEventGenerator (ObjectatEvents events, ObjectatLogger logger) {
		this.events = events;
		this.logger = logger;
	}
	
	public void run() {
		boolean done = false;
		
		int i = 0;
		Random r = new Random();
		
		while (!done) {
			if (maxEvents > 0 && i >= maxEvents) { done = true; }
			int rand = r.nextInt();
			
			if (rand % 2 == 0) {
				ObjectatEvent event = new ObjectatEvent(logger);
				event.setKey("SyntheticEvent" + i);
				event.setEventDescription("Synthetic Event " + i);
				event.setFirst(new java.util.Date());
				try {
					events.addEvent(event);
					i++;
				} catch (Exception e) {
					// Something went wrong, exit loop
					done = true;
				}
			} else {
				
				int eventNumber = 0 + (int)Math.random() * ((i - 0) + 1);
				try {
					ObjectatEvent event = events.getAllEvents().get(eventNumber);
					events.removeEvent(event);
				} catch (Exception e) {
					// TODO
					// Better define exception, potentially ArrayIndexOutOfBounds?
				}
			}
			
			try {
				// Pause between synthetic event generation
				Thread.sleep(frequency);
			} catch (Exception e) {
				// Most likely interrupted, exit loop
				done = true;
			}
		}
	}
}
