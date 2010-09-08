package org.openengsb.core.common.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResultImpl implements ValidationResult {

    private final List<String> errorMessages = new ArrayList<String>();

    private final boolean valid;

    public ValidationResultImpl(boolean valid) {
        super();
        this.valid = valid;
    }

    public void addErrorMessage(String message) {
        this.errorMessages.add(message);
    }

    @Override
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }
}
