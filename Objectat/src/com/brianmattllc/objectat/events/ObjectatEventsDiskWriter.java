package com.brianmattllc.objectat.events;

import com.brianmattllc.objectat.logging.*;

public class ObjectatEventsDiskWriter implements Runnable {
	private long writeFrequency = 10000;
	private ObjectatEvents objectatEvents = null;
	private ObjectatLogger logger = null;
	private boolean done = false;
	
	public ObjectatEventsDiskWriter (ObjectatEvents objectatEvents, ObjectatLogger logger) {
		this.objectatEvents = objectatEvents;
		this.logger = logger;
	}
	
	/**
	 * @return the writeFrequency
	 */
	public long getWriteFrequency() {
		return writeFrequency;
	}

	/**
	 * @param writeFrequency the writeFrequency to set
	 */
	public void setWriteFrequency(long writeFrequency) {
		this.writeFrequency = writeFrequency;
	}

	public void run() {
		while (!this.done) {
			try {
				Thread.sleep(this.writeFrequency);
			} catch (Exception e) {
				this.logger.log(ObjectatLogLevel.ERROR, this.getClass() + ": ObjectatEventDiskWriter interrupted, thread exiting.  Exception: " + e.getMessage());
			}
			
			this.logger.log(ObjectatLogLevel.DEBUG, this.getClass() + ": Writing Objectat to disk.");
			
			this.objectatEvents.writeObjectatEventsToDisk();
			
			this.logger.log(ObjectatLogLevel.DEBUG, this.getClass() + ": Objectat disk write completed.");
		}
	}
	
	public void setDone (boolean done) {
		this.done = done;
	}
}
