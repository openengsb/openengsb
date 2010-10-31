package org.openengsb.core.taskbox.model;

public class Ticket {
	private String ID;
	private String Type;
	
	public Ticket(String id) {
		super();
		ID = id;
	}
	
	public Ticket(String id, String type) {
		super();
		ID = id;
		Type = type;
	}

	public String getID() {
		return ID;
	}

	public void setID(String id) {
		ID = id;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}
}
