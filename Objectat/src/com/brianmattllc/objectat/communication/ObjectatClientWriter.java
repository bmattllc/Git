package com.brianmattllc.objectat.communication;

import java.util.ArrayList;
import java.io.OutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.brianmattllc.objectat.logging.*;
import com.brianmattllc.objectat.events.*;

import javax.xml.bind.Marshaller;

public class ObjectatClientWriter implements Runnable {
	private OutputStream outputStream = null;
	private ObjectatLogger logger = null;
	private ArrayList<Object> objectQueue = new ArrayList<Object>();
	private JAXBContext objectatEventJAXBContext;
	
	public ObjectatClientWriter (OutputStream outputStream, ObjectatLogger logger, JAXBContext objectatEventJAXBContext) {
		this.logger = logger;
		this.outputStream = outputStream;
		this.objectatEventJAXBContext = objectatEventJAXBContext; 
	}
	
	public boolean writeString (String string) {
		boolean success = false;
		
		try {
			// Add start/end delimiter
			string = ObjectatCommunicationStatics.getStartOfMessage() 
					+ string 
					+ ObjectatCommunicationStatics.getEndOfMessage();
			
			for (int i = 0; i < string.length(); i++) {
				this.outputStream.write(string.charAt(i));
			}
			
			success = true;
		} catch (IOException e) {
			this.logger.log(ObjectatLogLevel.ERROR, "Failed to write to ObjectOutputStream in ObjectatClientWriter.  IOException: " + e.getMessage());
		}
		
		return success;
	}
	
	public void run() {
		boolean done = false;
		
		while (!done) {
			while (this.objectQueue.size() > 0) {
				Object sendObject = this.objectQueue.get(0);
				this.objectQueue.remove(0);
				
				if (sendObject instanceof ObjectatEvent) {
					try {
						Marshaller jaxbMarshaller = objectatEventJAXBContext.createMarshaller();
						jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
						StringWriter stringWriter = new StringWriter();
						jaxbMarshaller.marshal((ObjectatEvent) sendObject, stringWriter);
						this.writeString(stringWriter.toString());
					} catch (JAXBException e) {
						this.logger.log(ObjectatLogLevel.DEBUG, this.getClass() + ": Failed to send ObjectatEvent to client.  JAXBException: " + e.getMessage());
					}					
				} else {
					this.writeString(sendObject.toString());
				}
			}
			
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				done = true;
			}
		}
	}
	
	public void addObjectToQueue (Object object) {
		this.objectQueue.add(object);
	}
}
