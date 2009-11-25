package org.openengsb.edb.jbi.endpoints;

import org.openengsb.edb.core.api.EDBHandlerFactory;

public class EDBEndPointConfig {
	private EDBHandlerFactory factory;

	private String linkStorage;
	
	public String getLinkStorage() {
		return linkStorage;
	}

	public void setLinkStorage(String linkStorage) {
		this.linkStorage = linkStorage;
	}

	public EDBHandlerFactory getFactory() {
		return factory;
	}

	public void setFactory(EDBHandlerFactory factory) {
		this.factory = factory;
	}
	
}
