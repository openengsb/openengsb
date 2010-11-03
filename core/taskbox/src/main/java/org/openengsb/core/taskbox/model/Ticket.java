package org.openengsb.core.taskbox.model;

public class Ticket implements Task {
	private String id;
	private String type;
	
	public Ticket(String id) {
		super();
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
