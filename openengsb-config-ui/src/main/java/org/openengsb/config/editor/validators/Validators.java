package org.openengsb.config.editor.validators;

import java.util.ArrayList;

import org.apache.wicket.validation.IValidator;
import org.openengsb.config.jbi.types.AbstractType;
import org.openengsb.config.jbi.types.IntType;

public class Validators {
    public static IValidator<String>[] buildValidators(AbstractType type) {
        ArrayList<IValidator<String>> v = new ArrayList<IValidator<String>>();
        if (type.getClass().equals(IntType.class)) {
            v.addAll(IntTypeValidators.buildValidators((IntType)type));
        }
        return v.toArray(new IValidator[v.size()]);
    }
}
