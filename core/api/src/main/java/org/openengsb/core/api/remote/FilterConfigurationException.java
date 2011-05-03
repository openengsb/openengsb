package org.openengsb.core.api.remote;

public class FilterConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FilterConfigurationException() {
    }

    public FilterConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterConfigurationException(String message) {
        super(message);
    }

    public FilterConfigurationException(Throwable cause) {
        super(cause);
    }

}
