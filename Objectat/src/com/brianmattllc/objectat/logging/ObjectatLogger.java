package com.brianmattllc.objectat.logging;

public class ObjectatLogger {
	private ObjectatLogLevel logLevel = ObjectatLogLevel.FATAL;
	
	public ObjectatLogger (ObjectatLogLevel logLevel) {
		this.logLevel = logLevel;
	}
	
	public void log(ObjectatLogLevel logLevel, String log) {
		if (logLevel.getLogLevelInt() <= this.logLevel.getLogLevelInt()) {
			// TODO
			// Update this to be a real log method
			System.out.println(this.logLevel.toString() + ": " + log);
		}
	}
}
