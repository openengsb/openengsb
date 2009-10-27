package org.openengsb.drools.model;

public class Event {
	private String name;
	private boolean handled;

	public Event(String name) {
		super();
		this.name = name;
		this.handled = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isHandled() {
		return handled;
	}

	public void setHandled(boolean handled) {
		this.handled = handled;
	}

}
