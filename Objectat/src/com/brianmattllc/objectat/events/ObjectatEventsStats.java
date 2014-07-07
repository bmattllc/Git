package com.brianmattllc.objectat.events;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import com.brianmattllc.objectat.logging.*;

/**
 * @author Brian Matt
 * 
 * Statistics class for Objectat.  Currently used to collect data on
 * ObjectatEvent throughput.
 */

public class ObjectatEventsStats implements Runnable {
	private Date startDate = new Date();
	private long eventsProcessed = 0;
	private long eventsLastCheck = 0;
	private long[] arrayOfEventCounts = new long[6];
	private int granularity = 60;
	private double maxEventsPerSecond = 0;
	private Date maxEventsPerSecondDate = new Date();
	private ObjectatLogger logger;
	private boolean done = false;
	private int eventsInObjectat = 0;
	
	/**
	 * Constructor for ObjectatEventsStats object.  Takes input of logger
	 * for logging purposes.
	 * 
	 * @param logger
	 */
	
	public ObjectatEventsStats (ObjectatLogger logger) {
		this.logger = logger;
	}
	
	/**
	 * run() method for Thread.  This is the main method for this class and
	 * goes through an essential infinite loop while Objectat is running.  Thread
	 * sleeps for a duration between each loop to conserve resources, currently
	 * 10 seconds.
	 */
	
	public void run() {
		this.done = false;
		int onIndex = 0;
		while (!this.done) {
			long eventsThisCheck = eventsProcessed - eventsLastCheck;
		
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
				this.done = true;
			}
		}
	}
	
	/**
	 * Calculate current events per second the Objectat is processing.
	 * Granularity is defined with class, but is currently a 60 second
	 * sliding window.  Collection happens every 10 seconds.
	 * 
	 * @return Double value of current events per second over last 60
	 * 	seconds.
	 */
	
	public double getCurrentEventsPerSecond () {
		// Instantiate double value to return
		double currentEventsPerSecond = 0;
		
		// Iterate array of data collected to get sum of events
		// processed over the last minute
		for (int i = 0; i < arrayOfEventCounts.length; i++) {
			currentEventsPerSecond += arrayOfEventCounts[i];
		}
		
		// Divide number of alarms by granularity to get current
		// event rate.
		currentEventsPerSecond /= this.granularity;
		
		return currentEventsPerSecond;
	}
	
	/**
	 * Calculate the mean (average) events per second.
	 * 
	 * @return Double value of mean events per second the Objectat is processing
	 */
	
	public double getMeanEventsPerSecond () {
		// Create double to return
		double meanEventsPerSecond = 0;
		
		// Load total number of events processed
		meanEventsPerSecond = this.eventsProcessed;
		
		// Create Date object for current time, to calculate duration
		// Objectat has been running
		Date curDate = new Date();
		
		// Divide events processed by seconds Objectat has been running
		// getTime is milliseconds so divide by 1000 to get seconds
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
	
	/**
	 * Convert stats to string, presents printable message to end
	 * user
	 */
	
	public String toString() {
		// DateFormat object to make human friendly date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
		// Display times in GMT for portability
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		// Return string of stats
		return "Objectat Events Stats:\n"
				+ "\tStart Date: " + sdf.format(this.startDate) + "\n"
				+ "\tEvents in Objectat: " + this.getEventsInObjectat() + "\n"
				+ "\tEvents Processed: " + this.getEventsProcessed() + "\n"
				+ "\tMax Events Per Second: " + this.getMaxEventsPerSecond() + "\n"
				+ "\tMax Events Per Second Date: " + sdf.format(this.maxEventsPerSecondDate) + "\n"
				+ "\tMean Events Per Second: " + this.getMeanEventsPerSecond() + "\n"
				+ "\tCurrent Events Per Second: " + this.getCurrentEventsPerSecond() + "\n";
	}
	
	public void setDone (boolean done) {
		this.done = done;
	}

	/**
	 * @return the eventsInObjectat
	 */
	public int getEventsInObjectat() {
		return eventsInObjectat;
	}

	/**
	 * @param eventsInObjectat the eventsInObjectat to set
	 */
	public void setEventsInObjectat(int eventsInObjectat) {
		this.eventsInObjectat = eventsInObjectat;
	}
}
