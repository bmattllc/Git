package com.brianmattllc.objectat.communication;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.brianmattllc.objectat.events.*;
import com.brianmattllc.objectat.logging.*;

public class ObjectatClient implements Runnable {
	private Socket client = null;
	private String objectatHost = "";
	private int objectatPort = 5100;
	private ObjectatLogger logger;
	private JAXBContext objectatEventJAXBContext;
	private ObjectOutputStream out;
	private InputStream in;
	private boolean clientMode = false;
	private boolean serverMode = false;
	private ObjectatClientReader objectatClientReader = null;
	private ObjectatClientWriter objectatClientWriter = null;
	
	public ObjectatClient (
			ObjectatLogger logger,
			JAXBContext objectatEventJAXBContext,
			boolean clientMode,
			boolean serverMode
	) {
		this.logger = logger;
		this.objectatEventJAXBContext = objectatEventJAXBContext;
		this.clientMode = clientMode;
		this.serverMode = serverMode;
	}
	
	public void run() {
		boolean done = false;
	
		try {
			Marshaller jaxbMarshaller = objectatEventJAXBContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			Unmarshaller jaxbUnmarshaller = objectatEventJAXBContext.createUnmarshaller();
			
			String messageRegex = ObjectatCommunicationStatics.getMessageRegexPattern();
			Pattern messagePattern = Pattern.compile(messageRegex);
			
			if (serverMode) {
				client = new Socket(objectatHost, objectatPort);
		
				out = new ObjectOutputStream(client.getOutputStream());
			
								StringWriter stringWriter = new StringWriter();
				String eventXML = "";
			
				while (!done) {
			
				}
			}
			
			if (clientMode) {
				client = new Socket(objectatHost, objectatPort);
				
				objectatClientReader = new ObjectatClientReader(client.getInputStream(), this.logger);
				objectatClientWriter = new ObjectatClientWriter(client.getOutputStream(), this.logger);
				
				
				in = new ObjectInputStream(client.getInputStream());
				out = new ObjectOutputStream(client.getOutputStream());
				
				out.writeObject(ObjectatCommunicationStatics.getStartOfMessage() + "CLIENT" + ObjectatCommunicationStatics.getEndOfMessage());
	
				String messageBuffer = "";
				
				while (!done) {
					char c = (char) 0;
					do {
						c = (char) in.read();
						messageBuffer += c;
					} while (c != (char) 3);
					
					Matcher messageMatcher = messagePattern.matcher(messageBuffer);
					boolean messageFound = false;
					
					if (messageMatcher.find()) {
						messageFound = true;
						
						for (int i = 1; i <= messageMatcher.groupCount(); i++) {
							String message = messageMatcher.group(i);
							
							if (message.contains("xml")) {
								if (message.contains("objectatEvent key")) {
									
								}
							}
						}
					}
					
					if (messageFound) { messageBuffer = ""; }
				}
			}
		} catch (Exception e) {
			// TODO
			// Catch/make more specific exception
		}
	}
}
