package com.brianmattllc.objectat.communication;

import java.io.InputStream;
import java.io.ObjectOutputStream;
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
	private ObjectOutputStream out;
	private InputStream in;
	private String messageBuffer = "";
	private ObjectatEvents events;
	private ObjectatLogger logger;
	private JAXBContext objectatEventJAXBContext;
	private boolean isEventClient = false;
	
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
	}
	
	public void run() {
		String messageRegex = ObjectatCommunicationStatics.getMessageRegexPattern();
		Pattern eofPattern = Pattern.compile(messageRegex);
		
		try {
			Unmarshaller unmarshaller = objectatEventJAXBContext.createUnmarshaller();
		
			while (this.client.isConnected()) {
				try {
					in = this.client.getInputStream();
					char c = (char) 0;
				
					do {
						c = (char) this.client.getInputStream().read();
						messageBuffer += c;
					} while (c != (char) 3);
								
					Matcher eofMatcher = eofPattern.matcher(messageBuffer);
					boolean messageProcessed = false;
					
					while (eofMatcher.find()) {
						messageProcessed = true;
						for (int i = 1; i <= eofMatcher.groupCount(); i++) {
							String message = eofMatcher.group(i);
							
							if (message.contains("xml")) {
								if (message.contains("objectatEvent key")) {
									try {
										StringReader xmlReader = new StringReader(message);
										ObjectatEvent event = (ObjectatEvent) unmarshaller.unmarshal(xmlReader);
										events.addEvent(event);
									} catch (Exception e) {
										// TODO
										// Do something with exception/define more specific exceptions
										this.logger.log(ObjectatLogLevel.ERROR, "Failed to unmarshall XML: " + message + "\n\n");
										e.printStackTrace();
									}
								}
							} else if (message.equals("CLIENT")) {
								this.isEventClient = true;
								ArrayList<ObjectatEvent> allEvents = this.events.getAllEvents();
								Marshaller marshaller = objectatEventJAXBContext.createMarshaller();
								marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
								out = (ObjectOutputStream) this.client.getOutputStream();
								for (int j = 0; j < allEvents.size(); j++) {
									StringWriter stringWriter = new StringWriter();
									marshaller.marshal(allEvents.get(j), stringWriter);
									out.writeObject(
											ObjectatCommunicationStatics.getStartOfMessage() 
											+ stringWriter.toString() 
											+ ObjectatCommunicationStatics.getEndOfMessage()
									);									
								}
							}
						}
					}	
					
					if (messageProcessed) {
						messageBuffer = "";
					}
				} catch (IOException e) {
					// 	TODO
					// Do something with this exception
				}
			} 
		} catch (Exception e) {
			// TODO
			// Do something with this exception
		}
	}
	
	public Socket getClient () {
		return this.client;
	}
}
