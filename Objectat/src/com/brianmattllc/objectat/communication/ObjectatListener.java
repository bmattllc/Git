package com.brianmattllc.objectat.communication;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;

import com.brianmattllc.objectat.events.*;
import com.brianmattllc.objectat.logging.*;

public class ObjectatListener implements Runnable {
	private ServerSocket serverSocket;
	private Socket connection = null;
	private int listenPort = 5100;
	private int backlog = 10;
	private int maxConnections = 0;
	private ArrayList<ObjectatConnection> arrayListOfObjectatConnection = new ArrayList<ObjectatConnection>();
	private ObjectatEvents events;
	private ObjectatLogger logger;
	private JAXBContext objectatEventJAXBContext;
	private boolean done = false;
	
	public ObjectatListener (
			ObjectatEvents events, 
			ObjectatLogger logger,
			JAXBContext objectatEventJAXBContext
	) {
		this.events = events;
		this.logger = logger;
		this.objectatEventJAXBContext = objectatEventJAXBContext;
	}
	
	public void run() {
		try {
			serverSocket = new ServerSocket(listenPort, backlog);
			this.done = false;
			while (!this.done) {
				if (maxConnections > 0 && arrayListOfObjectatConnection.size() >= maxConnections) {
					// TODO
					// Add code to wait for connection to leave
				} else {
					connection = serverSocket.accept();
									
					ObjectatConnection client = new ObjectatConnection(connection, events, logger, objectatEventJAXBContext);
					arrayListOfObjectatConnection.add(client);
					new Thread(client).start();
					
					// Check for dead connections
					for (int i = 0; i < arrayListOfObjectatConnection.size(); i++) {
						if (arrayListOfObjectatConnection.get(i).getClient().isClosed()) {
							arrayListOfObjectatConnection.remove(i);
						}
					}
				}
				
				try {
					// Prevent while loop from eating CPU
					Thread.sleep(1000);
				} catch (Exception e) {
					// Interrupted, leave loop
					this.done = true;
				}
			}
		} catch (IOException e) {
			
		}
	}
	
	public void setDone (boolean done) {
		this.done = done;
	}
	
	public ArrayList<ObjectatConnection> getConnections () {
		return this.arrayListOfObjectatConnection;
	}
}
