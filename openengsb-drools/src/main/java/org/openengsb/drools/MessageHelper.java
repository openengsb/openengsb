package org.openengsb.drools;

import java.io.Serializable;
import java.util.Collection;

import org.openengsb.drools.model.Action;

public interface MessageHelper extends Serializable {
	public abstract boolean call(String name, Collection<Object> args);
}
