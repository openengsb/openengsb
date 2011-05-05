package org.openengsb.core.api.remote;

/**
 * Indicates that an Exception has occured during Filter execution. So this Exception is used to wrap it.
 */
public class FilterException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FilterException() {
        super();
    }

    public FilterException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterException(String message) {
        super(message);
    }

    public FilterException(Throwable cause) {
        super(cause);
    }

}
