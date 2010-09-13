package org.openengsb.core.common.validation;

import java.util.Map;

public interface MultipleAttributeValidationResult {
    
    boolean isValid();

    Map<String, String> getAttributeErrorMessages();

}
