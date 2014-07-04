package com.brianmattllc.objectat.communication;

import java.io.InputStream;

import java.io.OutputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.io.IOException;

import com.brianmattllc.objectat.events.*;
import com.brianmattllc.objectat.logging.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;

import java.io.StringReader;
import java.util.regex.*;
import java.util.ArrayList;

public class ObjectatConnection implements Runnable {
	private Socket client;
	private OutputStream out;
	private InputStream in;
	private String messageBuffer = "";
	private ObjectatEvents events;
	private ObjectatLogger logger;
	private JAXBContext objectatEventJAXBContext;
	private boolean isEventClient = false;
	private ObjectatMessageProcessor objectatMessageProcessor = null;
	private ObjectatClientReader objectatClientReader = null;
	private ObjectatClientWriter objectatClientWriter = null;
	private Thread objectClientWriterThread = null;
	
	public ObjectatConnection (
			Socket client, 
			ObjectatEvents events, 
			ObjectatLogger logger,
			JAXBContext objectatEventJAXBContext
	) {
		this.client = client;
		this.events = events;
		this.logger = logger;
		this.objectatEventJAXBContext = objectatEventJAXBContext;
		this.objectatMessageProcessor = new ObjectatMessageProcessor(this.logger, this.objectatEventJAXBContext);
	}
	
	public void run() {
		try {
			this.objectatClientReader = new ObjectatClientReader(this.client.getInputStream(), this.logger);
			Thread readerThread = new Thread(this.objectatClientReader);
			readerThread.start();
			
			this.objectatClientWriter = new ObjectatClientWriter(this.client.getOutputStream(), this.logger, this.objectatEventJAXBContext);
			
			while (this.client.isConnected()) {
				String message = "";
				while (this.objectatClientReader.getBufferedMessages().size() > 0 && (message = this.objectatClientReader.getBufferedMessages().get(0)) != null) {
					try {
						Object processedMessageObject = this.objectatMessageProcessor.processMessage(objectatClientReader.getBufferedMessages().get(0));
						this.objectatClientReader.getBufferedMessages().remove(0);
						
						if (processedMessageObject instanceof ObjectatEvent) {
							this.events.addEvent((ObjectatEvent) processedMessageObject);
						} else {
							if (message.equals("CLIENT")) {
								if (objectClientWriterThread == null || !objectClientWriterThread.isAlive()) {
									objectClientWriterThread = new Thread(this.objectatClientWriter);
									objectClientWriterThread.start();
								}
								
								this.events.getArrayListOfObjectatClientWriters().add(this.objectatClientWriter);
								this.isEventClient = true;
								ArrayList<ObjectatEvent> allEvents = this.events.getAllEvents();
								Marshaller marshaller = objectatEventJAXBContext.createMarshaller();
								marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
								for (int j = 0; j < allEvents.size(); j++) {
									StringWriter stringWriter = new StringWriter();
									marshaller.marshal(allEvents.get(j), stringWriter);
									String outString = ObjectatCommunicationStatics.getStartOfMessage() 
											+ stringWriter.toString() 
											+ ObjectatCommunicationStatics.getEndOfMessage();
									
									this.objectatClientWriter.writeString(outString);																	
								}
							}
						}
					} catch (JAXBException e) {
						this.logger.log(ObjectatLogLevel.ERROR, this.getClass() + ": Failed to process message from connection.  JAXBException: " + e.getMessage());
					} catch (IOException e) {
						this.logger.log(ObjectatLogLevel.ERROR, this.getClass() + ": Failed to process message from connection.  IOException: " + e.getMessage());
					} catch (Exception e) {
						this.logger.log(ObjectatLogLevel.ERROR, this.getClass() + ": Failed to process message from connection.  Excepion: " + e.getMessage());
					}
				}
			
				try {
					// Pause between iterations to preserve resources
					Thread.sleep(10);
				} catch (Exception e) {
					this.logger.log(ObjectatLogLevel.ERROR, "Sleep between message processing for connection interrupted.  Exception: " + e.getMessage());
				}
			}
		} catch (IOException e) {
			this.logger.log(ObjectatLogLevel.ERROR, "Failed to start reading connection.  IOException: " + e.getMessage());
		}
	}
	
	public Socket getClient () {
		return this.client;
	}
}
