package org.openengsb.edb.jbi.endpoints.commands;

import javax.jbi.messaging.NormalizedMessage;

public interface EDBEndpointCommand {
	public String execute(NormalizedMessage in) throws Exception;
}
