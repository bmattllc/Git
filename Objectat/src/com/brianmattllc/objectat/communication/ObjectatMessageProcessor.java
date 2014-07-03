package com.brianmattllc.objectat.communication;

import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.brianmattllc.objectat.events.*;
import com.brianmattllc.objectat.logging.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;

public class ObjectatMessageProcessor {
	private ObjectatLogger logger = null;
	private JAXBContext objectatEventJAXBContext = null;
	
	
	public ObjectatMessageProcessor(
			ObjectatLogger logger,
			JAXBContext objectatEventJAXBContext
	) {
		this.logger = logger;
		this.objectatEventJAXBContext = objectatEventJAXBContext;		
	}

	public Object processMessage (
			String message
	) {
		try {
			Unmarshaller objectatEventUnmarshaller = objectatEventJAXBContext.createUnmarshaller();
		
			if (message.contains("xml")) {
				if (message.contains("objectatEvent key")) {
					try {	
						StringReader xmlReader = new StringReader(message);
						ObjectatEvent event = (ObjectatEvent) objectatEventUnmarshaller.unmarshal(xmlReader);
						
						return event;
					} catch (Exception e) {
						// TODO
						// Do something with exception/define more specific exceptions
						this.logger.log(ObjectatLogLevel.ERROR, "Failed to unmarshall XML: " + message + "\n\n");
						e.printStackTrace();
					}	
				}
			} else if (message.equals("CLIENT")) {
				return message;
			}
		} catch (JAXBException e) {
			this.logger.log(ObjectatLogLevel.ERROR, "Failed to process Objectat message.  JAXBExceptin: " + e.getMessage());
		}	
		
		return null;
	}
}
