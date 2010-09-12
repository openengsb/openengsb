package org.openengsb.core.common.validation;


public interface ValidationResult {
    public String getErrorMessageId();
    
    public boolean isValid();
}
