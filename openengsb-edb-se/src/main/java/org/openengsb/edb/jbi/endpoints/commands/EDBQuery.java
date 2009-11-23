package org.openengsb.edb.jbi.endpoints.commands;

import java.util.ArrayList;
import java.util.List;

import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.openengsb.edb.core.api.EDBException;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions;

public class EDBQuery implements EDBEndpointCommand {

	private EDBHandler handler;
	private Log log;
	
	public EDBQuery(EDBHandler handler, Log log) {
		this.handler = handler;
		this.log = log;
	}
	
	@Override
	public String execute(NormalizedMessage in) throws Exception {
		String body = null;
		final List<String> terms = XmlParserFunctions.parseQueryMessage(in);
		List<GenericContent> foundSignals = new ArrayList<GenericContent>();

		try {
			for (final String term : terms) {
				final List<GenericContent> result = handler.query(term,
						false);
				foundSignals.addAll(result);
			}

		} catch (final EDBException e) {
			// TODO build error message
			e.printStackTrace();
			foundSignals = new ArrayList<GenericContent>();
		}
		body = XmlParserFunctions.buildQueryBody(foundSignals);
		return body;
	}

}
