package com.brianmattllc.objectat.events;

import java.util.Date;
import com.brianmattllc.objectat.security.ObjectatUser;
import com.brianmattllc.objectat.logging.*;

/**
 * @author brian
 *
 * Event model for Objectat.  
 */

public class ObjectatEvent {
	private String key = "";
	private Date first = new Date();
	private Date last = new Date();
	private boolean owned = false;
	private ObjectatUser ownedBy = new ObjectatUser();
	private String eventFrom = "";
	private String eventSource = "";
	private Date eventLastChange = new Date();
	private ObjectatEventType eventType = ObjectatEventType.EVENT;
	private String eventCategory = "";
	private String eventGroup = "";
	private String eventFromVendor = "";
	private String eventFromModel = "";
	private String eventFromComponent = "";
	private String eventFromComponentIdentifier = "";
	private String eventFromPort = "";
	private String eventDescription = "";
	private ObjectatEventClassification eventClassification = new ObjectatEventClassification();
	private ObjectatEventSeverity eventSeverity = ObjectatEventSeverity.NORMAL;
	private ObjectatLogger logger;
	private int eventCount = 0;
	private long eventId = 0L;
	
	public ObjectatEvent () {
		
	}
	
	public ObjectatEvent (ObjectatLogger logger) {
		this.logger = logger;
	}
	
	public boolean deduplicate (ObjectatEvent event) {
		boolean success = false;
		
		// Update last timestamp
		this.setLast(new Date());
		
		// Update count
		this.setEventCount(this.getEventCount() + 1);
		
		if (event.isOwned() != this.isOwned()) {
			this.logger.log(ObjectatLogLevel.DEBUG, "Updating event key " + this.getKey() + " owned from " + this.isOwned() + " to " + event.isOwned());
			this.setOwned(event.isOwned());
		}
		
		if (!event.getOwnedBy().equals(this.getOwnedBy())) {
			this.logger.log(ObjectatLogLevel.DEBUG, "Updating event key " + this.getKey() + " owned by from " + this.getOwnedBy() + " to " + event.getOwnedBy());
			this.setOwnedBy(event.ownedBy);
		}
		
		if (!event.getEventDescription().equals(this.getEventDescription())) {
			this.logger.log(ObjectatLogLevel.DEBUG, "Updating event key " + this.getKey() + " description from " + this.getEventDescription() + " to " + event.getEventDescription());
			this.setEventDescription(event.getEventDescription());
		}
		
		if (event.getEventSeverity().getSeverityInteger() != this.getEventSeverity().getSeverityInteger()) {
			this.logger.log(ObjectatLogLevel.DEBUG, "Updating event key " + this.getKey() + " severity from " + this.getEventSeverity() + " to " + event.getEventSeverity());
			this.setEventSeverity(event.getEventSeverity());
		}

		return success;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Date getFirst() {
		return first;
	}
	public void setFirst(Date first) {
		this.first = first;
	}
	public Date getLast() {
		return last;
	}
	public void setLast(Date last) {
		this.last = last;
	}
	public boolean isOwned() {
		return owned;
	}
	public void setOwned(boolean owned) {
		this.owned = owned;
	}
	public ObjectatUser getOwnedBy() {
		return ownedBy;
	}
	public void setOwnedBy(ObjectatUser ownedBy) {
		this.ownedBy = ownedBy;
	}
	public String getEventFrom() {
		return eventFrom;
	}
	public void setEventFrom(String eventFrom) {
		this.eventFrom = eventFrom;
	}
	public String getEventSource() {
		return eventSource;
	}
	public void setEventSource(String eventSource) {
		this.eventSource = eventSource;
	}
	public Date getEventLastChange() {
		return eventLastChange;
	}
	public void setEventLastChange(Date eventLastChange) {
		this.eventLastChange = eventLastChange;
	}
	public ObjectatEventType getEventType() {
		return eventType;
	}
	public void setEventType(ObjectatEventType eventType) {
		this.eventType = eventType;
	}
	public String getEventCategory() {
		return eventCategory;
	}
	public void setEventCategory(String eventCategory) {
		this.eventCategory = eventCategory;
	}
	public String getEventGroup() {
		return eventGroup;
	}
	public void setEventGroup(String eventGroup) {
		this.eventGroup = eventGroup;
	}
	public String getEventFromVendor() {
		return eventFromVendor;
	}
	public void setEventFromVendor(String eventFromVendor) {
		this.eventFromVendor = eventFromVendor;
	}
	public String getEventFromModel() {
		return eventFromModel;
	}
	public void setEventFromModel(String eventFromModel) {
		this.eventFromModel = eventFromModel;
	}
	public String getEventFromComponent() {
		return eventFromComponent;
	}
	public void setEventFromComponent(String eventFromComponent) {
		this.eventFromComponent = eventFromComponent;
	}
	public String getEventFromComponentIdentifier() {
		return eventFromComponentIdentifier;
	}
	public void setEventFromComponentIdentifier(String eventFromComponentIdentifier) {
		this.eventFromComponentIdentifier = eventFromComponentIdentifier;
	}
	public String getEventFromPort() {
		return eventFromPort;
	}
	public void setEventFromPort(String eventFromPort) {
		this.eventFromPort = eventFromPort;
	}
	public String getEventDescription() {
		return eventDescription;
	}
	public void setEventDescription(String eventDescription) {
		this.eventDescription = eventDescription;
	}
	public ObjectatEventClassification getEventClassification() {
		return eventClassification;
	}
	public void setEventClassification(
			ObjectatEventClassification eventClassification) {
		this.eventClassification = eventClassification;
	}

	public ObjectatEventSeverity getEventSeverity() {
		return eventSeverity;
	}
	public void setEventSeverity(ObjectatEventSeverity eventSeverity) {
		this.eventSeverity = eventSeverity;
	}
	
	public int getEventCount() {
		return eventCount;
	}

	public void setEventCount(int eventCount) {
		this.eventCount = eventCount;
	}

	public long getEventId() {
		return eventId;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	public String toString() {
		return this.getEventId() + ": " + this.getKey() + ": " + this.getEventDescription();
	}
}
