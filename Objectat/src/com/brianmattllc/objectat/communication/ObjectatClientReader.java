package com.brianmattllc.objectat.communication;

import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.IOException;
import com.brianmattllc.objectat.logging.*;

public class ObjectatClientReader implements Runnable {
	private ObjectInputStream objectInputStream = null;
	private ObjectatLogger logger = null;
	
	public ObjectatClientReader (InputStream inputStream, ObjectatLogger logger) {
		this.logger = logger;
		
		try {
			this.objectInputStream = new ObjectInputStream(inputStream);
		} catch (IOException e) {
			this.logger.log(ObjectatLogLevel.FATAL, "Failed to create ObjectInputStream for ObjectatClientReader. IOException: " + e.getMessage());
		}
	}
	
	public void run() {
		String input = "";
		try {
			while ((input = (String) this.objectInputStream.readObject()) != null) {
				
			}
		} catch (ClassNotFoundException e) {
			this.logger.log(ObjectatLogLevel.ERROR, "Failed to read input stream in ObjectatClientReader. ClassNotFoundException: " + e.getMessage());
		} catch (IOException e) {
			this.logger.log(ObjectatLogLevel.ERROR, "Failed to read input stream in ObjectatClientReader. IOException: " + e.getMessage());
		}
	}
}
