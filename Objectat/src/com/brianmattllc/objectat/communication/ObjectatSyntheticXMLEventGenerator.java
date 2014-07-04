package com.brianmattllc.objectat.communication;

import com.brianmattllc.objectat.logging.*;
import com.brianmattllc.objectat.events.*;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class ObjectatSyntheticXMLEventGenerator implements Runnable {
	private long frequency = 10L;
	private long maxEvents = 0L;
	private long minEvents = 0L;
	private boolean removeEvents = true;
	private ObjectatLogger logger = new ObjectatLogger(ObjectatLogLevel.FATAL);
	private String objectatHost = "127.0.0.1";
	private int objectatPort = 5100;
	private Socket client;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String baseKey = "SyntheticEvent";
	private JAXBContext objectatEventJAXBContext;
	private boolean done = false;
	
	public ObjectatSyntheticXMLEventGenerator (ObjectatLogger logger, JAXBContext objectatEventJAXBContext) {
		this.logger = logger;
		this.objectatEventJAXBContext = objectatEventJAXBContext;
	}
	
	public void run() {
		this.done = false;
		
		try {
			client = new Socket(objectatHost, objectatPort);
		
			out = new ObjectOutputStream(client.getOutputStream());
			
			int i = 0;
			
			Marshaller jaxbMarshaller = objectatEventJAXBContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter stringWriter = new StringWriter();
			String eventXML = "";
			
			while (!this.done) {
				if (maxEvents > 0 && i >= maxEvents) { this.done = true; }
				
				ObjectatEvent event = new ObjectatEvent(logger);
				event.setKey(this.baseKey + i);
				event.setEventDescription("Synthetic Event " + i);
				event.setFirst(new java.util.Date());
				try {
					
					stringWriter = new StringWriter();
					jaxbMarshaller.marshal(event, stringWriter);						
					eventXML = ObjectatCommunicationStatics.getStartOfMessage() + stringWriter.toString() + ObjectatCommunicationStatics.getEndOfMessage();
					out.writeObject(eventXML);
						
					i++;
				} catch (Exception e) {
					// 	Something went wrong, exit loop
					e.printStackTrace();
					this.done = true;
				}
			
				try {
					// Pause between synthetic event generation
					Thread.sleep(frequency);
				} catch (Exception e) {
					// Most likely interrupted, exit loop
					this.done = true;
				}
			}
		} catch (Exception e) {
			
		}
	}
	
	public void setDone (boolean done) {
		this.done = done;
	}

	public String getBaseKey() {
		return baseKey;
	}

	public void setBaseKey(String baseKey) {
		this.baseKey = baseKey;
	}
	
	
}
