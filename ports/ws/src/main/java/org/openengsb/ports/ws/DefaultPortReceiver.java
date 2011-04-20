package org.openengsb.ports.ws;

import java.io.IOException;

import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.remote.MethodReturn;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.common.marshaling.RequestMapping;
import org.openengsb.core.common.marshaling.ReturnMapping;

public class DefaultPortReceiver implements PortReceiver {

    private RequestHandler requestHandler;

    public DefaultPortReceiver() {
    }

    public DefaultPortReceiver(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public String receive(String message) {
        try {
            RequestMapping readValue = RequestMapping.createFromMessage(message);
            readValue.resetArgs();
            ContextHolder.get().setCurrentContextId(readValue.getMetaData().get("contextId"));
            MethodReturn handleCall = requestHandler.handleCall(readValue);
            String answer = new ReturnMapping(handleCall).convertToMessage();
            return answer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
