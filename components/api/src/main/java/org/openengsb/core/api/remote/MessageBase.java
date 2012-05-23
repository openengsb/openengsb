package org.openengsb.core.api.remote;

import java.io.Serializable;

/**
 * Abstract baseclass for {@link MethodCallMessage} and {@link MethodResultMessage} with the common attributes.
 */
public abstract class MessageBase implements Serializable {

    private static final long serialVersionUID = 3338696894401864461L;

    protected String callId;
    protected Long timestamp = System.currentTimeMillis();

    public MessageBase() {
    }

    public MessageBase(String callId) {
        this.callId = callId;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

}
