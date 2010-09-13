package org.openengsb.core.common.validation;


public interface FieldValidationResult {
    public String getErrorMessageId();
    
    public boolean isValid();
}
