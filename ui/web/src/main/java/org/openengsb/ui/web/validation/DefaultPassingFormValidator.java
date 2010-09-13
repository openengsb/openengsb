package org.openengsb.ui.web.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.common.validation.FormValidationResult;
import org.openengsb.core.common.validation.FormValidationResultImpl;
import org.openengsb.core.common.validation.FormValidator;
import org.openengsb.core.common.validation.FieldValidationResult;
import org.openengsb.core.common.validation.ValidationResultImpl;

public class DefaultPassingFormValidator implements FormValidator {
    @Override
    public FormValidationResult validate(Map<String, String> attributes) {
        return new FormValidationResultImpl(true, new HashMap<String, String>());
    }

    @Override
    public List<String> fieldsToValidate() {
        return new ArrayList<String>();
    }
}
