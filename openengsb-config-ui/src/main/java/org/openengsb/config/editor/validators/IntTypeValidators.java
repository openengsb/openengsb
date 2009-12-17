package org.openengsb.config.editor.validators;

import java.util.ArrayList;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.openengsb.config.jbi.types.IntType;

public class IntTypeValidators {
    public static ArrayList<IntTypeValidator> buildValidators(IntType type) {
        ArrayList<IntTypeValidator> a = new ArrayList<IntTypeValidator>();
        if (type.getMin() != null) {
            a.add(new MinValidator(type.getMin()));
        }
        if (type.getMax() != null) {
            a.add(new MaxValidator(type.getMax()));
        }
        return a;
    }

    public static class MinValidator extends IntTypeValidator {
        private static final long serialVersionUID = 1L;
        private final int min;

        public MinValidator(int min) {
            this.min = min;
        }

        @Override
        protected void validate(IValidatable<String> validatable, int value) {
            if (value < min) {
                ValidationError error = new ValidationError();
                error.addMessageKey("IntType.MinValidator");
                error.setVariable("min", min);
                validatable.error(error);
            }
        }
    }

    public static class MaxValidator extends IntTypeValidator {
        private static final long serialVersionUID = 1L;
        private final int max;

        public MaxValidator(int max) {
            this.max = max;
        }

        @Override
        protected void validate(IValidatable<String> validatable, int value) {
            if (value > max) {
                ValidationError error = new ValidationError();
                error.addMessageKey("IntType.MaxValidator");
                error.setVariable("max", max);
                validatable.error(error);
            }
        }
    }

    public static abstract class IntTypeValidator implements IValidator<String> {
        private static final long serialVersionUID = 1L;

        @Override
        public void validate(IValidatable<String> validatable) {
            String value = validatable.getValue();
            try {
                validate(validatable, Integer.parseInt(value));
            } catch (NumberFormatException e) {
                ValidationError error = new ValidationError();
                error.addMessageKey("IntTypeValidator");
                validatable.error(error);
            }
        }

        protected abstract void validate(IValidatable<String> validatable, int value);
    }
}
