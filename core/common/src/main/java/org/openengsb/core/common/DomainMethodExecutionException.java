package org.openengsb.core.common;

@SuppressWarnings("serial")
public class DomainMethodExecutionException extends RuntimeException {

    public DomainMethodExecutionException() {
    }

    public DomainMethodExecutionException(String message) {
        super(message);
    }

    public DomainMethodExecutionException(Throwable cause) {
        super(cause);
    }

    public DomainMethodExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

}
