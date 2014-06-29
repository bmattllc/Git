package com.brianmattllc.objectat.events;

import java.util.Date;
import java.text.DateFormat;
import com.brianmattllc.objectat.logging.*;

public class ObjectatEventsStats implements Runnable {
	private Date startDate = new Date();
	private long eventsProcessed = 0;
	private long eventsLastCheck = 0;
	private long[] arrayOfEventCounts = new long[6];
	private int granularity = 60;
	private double maxEventsPerSecond = 0;
	private Date maxEventsPerSecondDate = new Date();
	private ObjectatLogger logger;
	
	public ObjectatEventsStats (ObjectatLogger logger) {
		this.logger = logger;
	}
	
	public void run() {
		boolean done = false;
		int onIndex = 0;
		while (!done) {
			long eventsThisCheck = eventsProcessed - eventsLastCheck;
			this.logger.log(ObjectatLogLevel.DEBUG, "Events this check: " + eventsThisCheck);
		
			if (onIndex < arrayOfEventCounts.length) {
				arrayOfEventCounts[onIndex] = eventsThisCheck;
				onIndex++;
			} else {
				// Shift counts down an index, dropping the oldest
				for (int i = 1; i < arrayOfEventCounts.length; i++) {
					arrayOfEventCounts[(i - 1)] = arrayOfEventCounts[i];
				}
				
				arrayOfEventCounts[(onIndex - 1)] = eventsThisCheck;				
			}
			
			this.eventsLastCheck = this.getEventsProcessed();
			
			double currentEventsPerSecond = this.getCurrentEventsPerSecond();
			if (currentEventsPerSecond > this.maxEventsPerSecond) {
				this.maxEventsPerSecond = currentEventsPerSecond;
				this.maxEventsPerSecondDate = new Date();
			}
			
			try {
				// Stats collection every 10 seconds
				Thread.sleep(10000);
			} catch (Exception e) {
				done = true;
			}
		}
	}
	
	public double getCurrentEventsPerSecond () {
		double currentEventsPerSecond = 0;
		
		for (int i = 0; i < arrayOfEventCounts.length; i++) {
			currentEventsPerSecond += arrayOfEventCounts[i];
		}
		
		currentEventsPerSecond /= this.granularity;
		
		return currentEventsPerSecond;
	}
	
	public double getMeanEventsPerSecond () {
		double meanEventsPerSecond = 0;
		meanEventsPerSecond = this.eventsProcessed;
		Date curDate = new Date();
		meanEventsPerSecond /= ((curDate.getTime() - this.startDate.getTime()) / 1000);
		return meanEventsPerSecond;
	}
	
	public double getMaxEventsPerSecond () {
		return this.maxEventsPerSecond;
	}

	/**
	 * @return the eventsProcessed
	 */
	public long getEventsProcessed() {
		return eventsProcessed;
	}

	/**
	 * @param eventsProcessed the eventsProcessed to set
	 */
	public void setEventsProcessed(long eventsProcessed) {
		this.eventsProcessed = eventsProcessed;
	}
	
	public String toString() {
		DateFormat df = DateFormat.getInstance();
		
		return "Objectat Events Stats:\n"
				+ "\tStart Date: " + df.format(this.startDate) + "\n"
				+ "\tEvents Processed: " + this.getEventsProcessed() + "\n"
				+ "\tMax Events Per Second: " + this.getMaxEventsPerSecond() + "\n"
				+ "\tMax Events Per Second Date: " + df.format(this.maxEventsPerSecondDate) + "\n"
				+ "\tMean Events Per Second: " + this.getMeanEventsPerSecond() + "\n"
				+ "\tCurrent Events Per Second: " + this.getCurrentEventsPerSecond() + "\n";
	}
}
