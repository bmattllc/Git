package com.brianmattllc.objectat.communication;

import java.io.InputStream;
import java.io.IOException;

import com.brianmattllc.objectat.logging.*;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectatClientReader implements Runnable {
	private ObjectatLogger logger = null;
	private ArrayList<String> bufferedMessages = new ArrayList<String>();
	private InputStream inputStream = null;
	
	public ObjectatClientReader (InputStream inputStream, ObjectatLogger logger) {
		this.logger = logger;
		this.inputStream = inputStream;		
	}
	
	public void run() {
		String input = "";
		String messageRegex = ObjectatCommunicationStatics.getMessageRegexPattern();
		Pattern eofPattern = Pattern.compile(messageRegex);
		
		boolean done = false;
		
		while (!done) {
			try {
			
				char c = (char) 0;
			
				do {
					c = (char) this.inputStream.read();
					input += c;
				} while (c != (char) 3);
				
				Matcher eofMatcher = eofPattern.matcher(input);
			
				while (eofMatcher.find()) {
					for (int i = 1; i <= eofMatcher.groupCount(); i++) {
						String message = eofMatcher.group(i);
						this.bufferedMessages.add(message);
					}
				
					input = "";
				}
			
			} catch (IOException e) {
				this.logger.log(ObjectatLogLevel.ERROR, this.getClass() + ": Failed to read input stream in ObjectatClientReader. IOException: " + e.getMessage());
				done = true;
			}
		}
	}
	
	public ArrayList<String> getBufferedMessages () {
		return this.bufferedMessages;
	}
}
