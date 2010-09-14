package org.openengsb.ui.web.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;
import org.openengsb.core.common.validation.FormValidator;
import org.openengsb.core.common.validation.SingleAttributeValidationResult;
import org.openengsb.core.common.validation.ValidationResultImpl;

public class DefaultPassingFormValidator implements FormValidator {
    @Override
    public MultipleAttributeValidationResult validate(Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

    @Override
    public List<String> fieldsToValidate() {
        return new ArrayList<String>();
    }
}
