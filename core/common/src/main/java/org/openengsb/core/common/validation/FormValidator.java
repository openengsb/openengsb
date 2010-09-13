package org.openengsb.core.common.validation;

import java.util.List;
import java.util.Map;

public interface FormValidator {
    FormValidationResult validate(Map<String, String> attributes);
    
    List<String> fieldsToValidate();
}
