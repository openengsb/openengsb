package org.openengsb.core.common.validation;


public class ValidationResultImpl implements ValidationResult {

    private final String errorMessage;

    private final boolean valid;

    public ValidationResultImpl(boolean valid, String message) {
        super();
        this.valid = valid;
        this.errorMessage = message;
    }

    @Override
    public String getErrorMessageId() {
        return errorMessage;
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }
}
