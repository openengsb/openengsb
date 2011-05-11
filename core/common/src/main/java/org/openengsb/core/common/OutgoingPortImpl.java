package org.openengsb.core.common;

import java.util.HashMap;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.remote.RemoteCommunicationException;

public class OutgoingPortImpl implements OutgoingPort {

    private FilterAction filterChain;

    @Override
    public void send(MethodCallRequest call) throws RemoteCommunicationException {
        HashMap<String, Object> metaData = getMetaDataMap(call);
        filterChain.filter(call, metaData);
    }

    private HashMap<String, Object> getMetaDataMap(MethodCallRequest call) {
        HashMap<String, Object> metaData = new HashMap<String, Object>();
        metaData.put("callId", call.getCallId());
        metaData.put("destination", call.getDestination());
        return metaData;
    }

    @Override
    public MethodResultMessage sendSync(MethodCallRequest call) throws RemoteCommunicationException {
        HashMap<String, Object> metaData = getMetaDataMap(call);
        metaData.put("answer", true);
        return (MethodResultMessage) filterChain.filter(call, metaData);
    }

    public void setFilterChain(FilterAction filterChain) {
        this.filterChain = filterChain;
    }

}
