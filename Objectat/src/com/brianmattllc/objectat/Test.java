package com.brianmattllc.objectat;

import com.brianmattllc.objectat.events.*;
import java.util.ArrayList;
import com.brianmattllc.objectat.logging.*;

public class Test {
	public static void main (String[] args) {
		ObjectatLogger logger = new ObjectatLogger(ObjectatLogLevel.DEBUG);
		ObjectatEvents e = new ObjectatEvents(logger);
		
		
		while (true) {
			System.out.print("> ");
			String cmd = "";
			char c = 'a';
			do {
				try {
					c = (char) System.in.read();
					cmd += c;
				} catch (Exception ex) {
					
				}
			} while (c != '\n');
			
			cmd = cmd.replace("\n", "");
			cmd = cmd.replace("\r", "");
			String[] cmdArgs = cmd.split(" ");
			System.out.println("Found " + cmdArgs.length + " args");
		
			
			if (cmdArgs[0] != null) {
				System.out.println("Command '" + cmdArgs[0] + "'");
				if (cmdArgs[0].equals("add")) {
					if (cmdArgs[1] != null && cmdArgs[2] != null) {
						System.out.println("Attempting to add event with key " + cmdArgs[1] + " and description '" + cmdArgs[2] + "'");
						ObjectatEvent event = new ObjectatEvent(logger);
						event.setKey(cmdArgs[1]);
						event.setEventDescription(cmdArgs[2]);
						try {
							e.addEvent(event);
							System.out.println("Event successfully added");
						} catch (Exception ex) {
							System.out.println("Failed ot add event " + ex.getMessage());
						}
					}
				} else if (cmdArgs[0].equals("remove")) {
					if (cmdArgs[1] != null) {
						System.out.println("Removing event with key " + cmdArgs[1]);
						ObjectatEvent event = new ObjectatEvent(logger);
						event.setKey(cmdArgs[1]);
						try {
							e.removeEvent(event);
							System.out.println("Successfully removed event");
						} catch (Exception ex) {
							ex.printStackTrace();
							System.out.println("Failed to remove event " + ex.getMessage());
						}
					}
				} else if (cmdArgs[0].equals("list")) {
					System.out.println("Retrieving all events...");
					ArrayList<ObjectatEvent> events = new ArrayList<ObjectatEvent>();
				
					events = e.getAllEvents();
				
					System.out.println("Found " + events.size() + " event(s)");
					for (int i = 0; i < events.size(); i++) {
						System.out.println(events.get(i).toString());
					}
				} else if (cmdArgs[0].equals("get")) {
					if (cmdArgs[1] != null) {
						System.out.println("Retrieving event with key " + cmdArgs[1]);
						ObjectatEvent event = new ObjectatEvent(logger);
						try {
							event = e.getEventByKey(cmdArgs[1]);
							System.out.println("Successfully retrieved event");
							System.out.println(event.toString());
						} catch (Exception ex) {
							System.out.println("Failed to retrieve event " + ex.getMessage());
						}
					}
				}
			}
		}
		
	}
}
