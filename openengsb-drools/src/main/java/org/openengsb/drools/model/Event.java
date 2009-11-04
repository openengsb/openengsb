package org.openengsb.drools.model;

public class Event {
	private String name;
	private String contextId;
	private String toolDomainId;

	public Event(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	public String getToolDomainId() {
		return toolDomainId;
	}

	public void setToolDomainId(String toolDomainId) {
		this.toolDomainId = toolDomainId;
	}

}
