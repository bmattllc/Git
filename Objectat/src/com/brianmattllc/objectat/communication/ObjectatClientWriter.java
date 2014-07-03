package com.brianmattllc.objectat.communication;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import com.brianmattllc.objectat.logging.*;

public class ObjectatClientWriter implements Runnable {
	private ObjectOutputStream objectOutputStream = null;
	private ObjectatLogger logger = null;
	
	public ObjectatClientWriter (OutputStream outputStream, ObjectatLogger logger) {
		this.logger = logger;
		
		try {
			this.objectOutputStream = new ObjectOutputStream(outputStream);
		} catch (IOException e) {
			this.logger.log(ObjectatLogLevel.FATAL, "Failed to create ObjectOutputStream for ObjectatClientWriter. IOException: " + e.getMessage());
		}
	}
	
	public boolean writeString (String string) {
		boolean success = false;
		
		try {
			// Add start/end delimiter
			string = ObjectatCommunicationStatics.getStartOfMessage() 
					+ string 
					+ ObjectatCommunicationStatics.getEndOfMessage();
			
			this.objectOutputStream.writeObject(string);
			
			success = true;
		} catch (IOException e) {
			this.logger.log(ObjectatLogLevel.ERROR, "Failed to write to ObjectOutputStream in ObjectatClientWriter.  IOException: " + e.getMessage());
		}
		
		return success;
	}
	
	public void run() {
		// TODO
		// Create thread logic or remove
	}
}
