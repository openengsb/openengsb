package org.openengsb.drools;

import java.io.Serializable;

public interface MessageHelper extends Serializable {
	public abstract boolean triggerAction(String name, Object... args);
}
