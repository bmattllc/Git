package com.brianmattllc.objectat.communication;

/**
 * @author brian
 *
 */
public class ObjectatCommunicationStatics {
	// Start of Message
	// <ETX><TAB><SPC><TAB><TAB><SPC><SPC>
	private static String startOfMessage = Character.toString((char) 3)
			+ (char) 9 
			+ (char) 32
			+ (char) 9 
			+ (char) 9 
			+ (char) 32
			+ (char) 32;
	
	// End of Message
	// <SPC><SPC><TAB><TAB><SPC><TAB><ETX>
	private static String endOfMessage = Character.toString((char) 32)
			+ (char) 32
			+ (char) 9 
			+ (char) 9 
			+ (char) 32
			+ (char) 9 
			+ Character.toString((char) 3);
	
	private static String messageRegexPattern = "\\x03\\x09\\x20\\x09\\x09\\x20\\x20([^\\x03]+)\\x20\\x20\\x09\\x09\\x20\\x09\\x03";

	/**
	 * @return the startOfMessage
	 */
	public static String getStartOfMessage() {
		return startOfMessage;
	}

	/**
	 * @return the endOfMessage
	 */
	public static String getEndOfMessage() {
		return endOfMessage;
	}

	/**
	 * @return the messageRegexPattern
	 */
	public static String getMessageRegexPattern() {
		return messageRegexPattern;
	}
}
