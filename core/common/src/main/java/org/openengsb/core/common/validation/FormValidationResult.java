package org.openengsb.core.common.validation;

import java.util.Map;

public interface FormValidationResult {
    
    boolean isValid();

    Map<String, String> getAttributeErrorMessages();

}
