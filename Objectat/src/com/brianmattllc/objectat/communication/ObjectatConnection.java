package com.brianmattllc.objectat.communication;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.io.IOException;
import com.brianmattllc.objectat.events.*;
import com.brianmattllc.objectat.logging.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.regex.*;

public class ObjectatConnection implements Runnable {
	private Socket client;
	private OutputStream out;
	private InputStream in;
	private String messageBuffer = "";
	private ObjectatEvents events;
	private ObjectatLogger logger;
	
	public ObjectatConnection (Socket client, ObjectatEvents events, ObjectatLogger logger) {
		this.client = client;
		this.events = events;
		this.logger = logger;
	}
	
	public void run() {
		Pattern eofPattern = Pattern.compile("\n\n" + (char) 23 + "\n$");
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ObjectatEvent.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		
			while (this.client.isConnected()) {
				try {
					in = this.client.getInputStream();
					char c = (char) 0;
				
					do {
						c = (char) this.client.getInputStream().read();
						messageBuffer += c;
					} while (c != '\n');
								
					Matcher eofMatcher = eofPattern.matcher(messageBuffer);
				
					if (eofMatcher.find()) {
						messageBuffer = messageBuffer.replaceAll("^.*<?xml", "<?xml");
						messageBuffer = messageBuffer.replaceAll("\n\n" + (char) 23 + "\n$", "");
					
						try {
							StringReader xmlReader = new StringReader(messageBuffer);
							ObjectatEvent event = (ObjectatEvent) unmarshaller.unmarshal(xmlReader);
						
							events.addEvent(event);
						} catch (Exception e) {
							// TODO
							// Do something with exception/define more specific exceptions
							this.logger.log(ObjectatLogLevel.ERROR, "Failed to unmarshall XML: " + messageBuffer + "\n\n");
							e.printStackTrace();
						}
					
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
