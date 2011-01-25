/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.common.wicket.editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openengsb.core.common.validation.FieldValidator;
import org.openengsb.core.common.validation.SingleAttributeValidationResult;
import org.openengsb.core.common.validation.ValidationResultImpl;

/**
 * FieldValidator, that is used to check if the value is a number
 */
@SuppressWarnings("serial")
public class NumberValidator implements FieldValidator {

    @Override
    public SingleAttributeValidationResult validate(String validate) {
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
