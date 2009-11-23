package org.openengsb.edb.jbi.endpoints.commands;

import javax.jbi.messaging.NormalizedMessage;

public interface EDBEndpointCommand {
	public void execute(NormalizedMessage in) throws Exception;
}
