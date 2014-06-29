package com.brianmattllc.objectat.communication;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.IOException;
import com.brianmattllc.objectat.events.*;
import com.brianmattllc.objectat.logging.*;

public class ObjectatXMLParser implements Runnable {
	private String xmlString = "";
	private ObjectatEvent event;
	private ObjectatLogger logger;
	
	public ObjectatXMLParser (String xmlString) {
		this.xmlString = xmlString;
	}
	
	public void run() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(xmlString);
			NodeList nodeList = document.getDocumentElement().getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node instanceof Element) {
					event = new ObjectatEvent(logger);
					event.setEventFrom(node.getAttributes().getNamedItem("eventFrom").getNodeValue());
				}
			}
		} catch (IOException e) {
			// TODO
			// Do something with exception
		} catch (Exception e) {
			// TODO
			// Do something with exception
		}
	}
}
