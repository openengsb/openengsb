package org.openengsb.core.events;

@SuppressWarnings("serial")
public class EventProxyException extends RuntimeException {

    public EventProxyException() {
        super();
    }

    public EventProxyException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventProxyException(String message) {
        super(message);
    }

    public EventProxyException(Throwable cause) {
        super(cause);
    }

}
