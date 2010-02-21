package org.openengsb.edb.jbi.endpoints.commands;

import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions;

public class EDBFullReset implements EDBEndpointCommand {

    private final EDBHandler handler;
    private final Log log;

    public EDBFullReset(EDBHandler handler, Log log) {
        this.handler = handler;
        this.log = log;
    }

    @Override
    public String execute(NormalizedMessage in) throws Exception {
        String body = null;
        String repoId = XmlParserFunctions.parseFullResetMessage(in);
        log.debug(String.format("Full Reset request for %s received, processing now.", repoId));
        handler.removeRepository();
        body = XmlParserFunctions.buildFullResetResponseBody(String.format("Full Reset request for %s processed.",
                repoId));

        return body;
    }

}
