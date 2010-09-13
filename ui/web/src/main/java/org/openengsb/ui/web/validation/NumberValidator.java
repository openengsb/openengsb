package org.openengsb.ui.web.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openengsb.core.common.validation.FieldValidator;
import org.openengsb.core.common.validation.FieldValidationResult;
import org.openengsb.core.common.validation.ValidationResultImpl;

public class NumberValidator implements FieldValidator {

    @Override
    public FieldValidationResult validate(String validate) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(validate);
        if (!matcher.matches()) {
            ValidationResultImpl validationResultImpl = new ValidationResultImpl(false, "validation.number.formating");
            return validationResultImpl;
        } else {
            return new ValidationResultImpl(true, "");
        }
    }
}
