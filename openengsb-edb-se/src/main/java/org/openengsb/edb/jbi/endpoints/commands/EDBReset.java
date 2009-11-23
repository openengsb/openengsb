package org.openengsb.edb.jbi.endpoints.commands;

import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.openengsb.edb.core.api.EDBException;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions.RequestWrapper;

public class EDBReset implements EDBEndpointCommand {
	private EDBHandler handler;
	private Log log;

	public EDBReset(EDBHandler handler, Log log) {
		this.handler = handler;
		this.log = log;
	}

	@Override
	public String execute(NormalizedMessage in) throws Exception {
		String body = null;
		final RequestWrapper req = XmlParserFunctions.parseResetMessage(in);
		try {
			body = XmlParserFunctions.buildResetBody(handler.reset(req
					.getHeadId(), req.getDepth()));
		} catch (final EDBException e) {
			body = XmlParserFunctions.buildResetErrorBody(e.getMessage(), e
					.getStackTrace().toString());
		}
		return body;
	}

}
