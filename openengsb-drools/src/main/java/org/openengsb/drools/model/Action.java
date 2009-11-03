package org.openengsb.drools.model;

import java.util.Collection;

import edu.emory.mathcs.backport.java.util.Arrays;

public class Action {

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
