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
	private String objectatHost = "localhost";
	private int objectatPort = 5100;
	private ObjectatLogger logger;
	private JAXBContext objectatEventJAXBContext;
	private boolean clientMode = false;
	private boolean serverMode = false;
	private ObjectatClientReader objectatClientReader = null;
	private ObjectatClientWriter objectatClientWriter = null;
	
	public ObjectatClient (
			String objectatHost,
			int objectatPort,
			ObjectatLogger logger,
			JAXBContext objectatEventJAXBContext,
			boolean clientMode,
			boolean serverMode
	) {
		this.objectatHost = objectatHost;
		this.objectatPort = objectatPort;
		this.logger = logger;
		this.objectatEventJAXBContext = objectatEventJAXBContext;
		this.clientMode = clientMode;
		this.serverMode = serverMode;
	}
	
	public void run() {
		boolean done = false;
	
		try {			
			if (serverMode) {
				client = new Socket(objectatHost, objectatPort);
				
				StringWriter stringWriter = new StringWriter();
				String eventXML = "";
				while (!done) {
			
				}
			}
			
			if (clientMode) {
				this.client = new Socket(objectatHost, objectatPort);
				
				if (this.client.isConnected()) {
					this.logger.log(ObjectatLogLevel.DEBUG, this.getClass() + ": Starting client reader");					
					this.objectatClientReader = new ObjectatClientReader(this.client.getInputStream(), this.logger);
					Thread objectatClientReaderThread = new Thread(this.objectatClientReader);
					objectatClientReaderThread.start();
				
					this.logger.log(ObjectatLogLevel.DEBUG, this.getClass() + ": Starting client writer");
					this.objectatClientWriter = new ObjectatClientWriter(this.client.getOutputStream(), this.logger, this.objectatEventJAXBContext);
										
					this.logger.log(ObjectatLogLevel.DEBUG, this.getClass() + ": Sending client message");
					this.objectatClientWriter.writeString("CLIENT");
					
					while (!done) {
						while (this.objectatClientReader.getBufferedMessages().size() > 0) {
							//this.logger.log(ObjectatLogLevel.DEBUG, this.getClass() + ": " + this.objectatClientReader.getBufferedMessages().get(0));
							this.objectatClientReader.getBufferedMessages().remove(0);
						}
						
						try {
							Thread.sleep(10);							
						} catch (Exception e) {
							
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO
			// Catch/make more specific exception
			this.logger.log(ObjectatLogLevel.ERROR, this.getClass() + ": Exception while starting client.  Exception: " + e.getMessage());
		}
	}
}
