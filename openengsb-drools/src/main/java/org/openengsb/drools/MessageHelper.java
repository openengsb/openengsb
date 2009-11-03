package org.openengsb.drools;

import java.io.Serializable;

import org.openengsb.drools.model.Action;

public interface MessageHelper extends Serializable {
	public abstract boolean triggerAction(String name, Action arg);
}
