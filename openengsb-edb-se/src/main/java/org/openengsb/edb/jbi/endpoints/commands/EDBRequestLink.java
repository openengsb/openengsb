package org.openengsb.edb.jbi.endpoints.commands;

import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.openengsb.edb.core.api.EDBHandler;

public class EDBRequestLink implements EDBEndpointCommand {

	private EDBHandler handler;
	private Log log;
	
	public EDBRequestLink(EDBHandler handler, Log log) {
		this.handler = handler;
		this.log = log;
	}
	
	@Override
	public String execute(NormalizedMessage in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
