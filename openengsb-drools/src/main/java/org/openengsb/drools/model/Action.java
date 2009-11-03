package org.openengsb.drools.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

public class Action implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7015379310266603386L;

	@SuppressWarnings("unchecked")
	public Action(String name, Object... args) {
		super();
		this.name = name;
		this.args = Arrays.asList(args);
	}

	public Action(String name, Collection<Object> args) {
		super();
		this.name = name;
		this.args = args;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<Object> getArgs() {
		return args;
	}

	public void setArgs(Collection<Object> args) {
		this.args = args;
	}

	protected String name;
	protected Collection<Object> args;

}
