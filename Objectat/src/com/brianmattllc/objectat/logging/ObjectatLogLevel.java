package com.brianmattllc.objectat.logging;

public enum ObjectatLogLevel {
	FATAL (1),
	ERROR (2),
	WARN (3),
	INFO (4),
	DEBUG (5);
	
	private int level = 0;
	
	ObjectatLogLevel (int level) {
		this.level = level;
	}
	
	public int getLogLevelInt () {
		return level;
	}
	
}
