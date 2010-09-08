package org.openengsb.core.common.validation;

import java.util.List;

public interface ValidationResult {
    public List<String> getErrorMessages();
    
    public boolean isValid();
}
