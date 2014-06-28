package com.brianmattllc.objectat.events;

public enum ObjectatEventSeverity {
	NORMAL (0),
	UNKNOWN (1),
	WARNING (2),
	MINOR (3),
	MAJOR (4),
	CRITICAL (5),
	MAINTENANCE(1000);
	
	private int severityInteger = 0;
	
	ObjectatEventSeverity (int severityInteger) {
		this.severityInteger = severityInteger;
	}
	
	public int getSeverityInteger () { return this.severityInteger; }
}
