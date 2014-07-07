package com.brianmattllc.objectat.logging;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

public class ObjectatLogger {
	private ObjectatLogLevel logLevel = ObjectatLogLevel.FATAL;
	
	public ObjectatLogger (ObjectatLogLevel logLevel) {
		this.logLevel = logLevel;
	}
	
	public void log(ObjectatLogLevel logLevel, String log) {
		if (logLevel.getLogLevelInt() <= this.logLevel.getLogLevelInt()) {
			// TODO
			// Update this to be a real log method
			Date logDate = new Date();
			// DateFormat object to make human friendly date
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
			// Display times in GMT for portability
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			
			System.out.println(sdf.format(logDate) + ": " + this.logLevel.toString() + ": " + log);
		}
	}
}
