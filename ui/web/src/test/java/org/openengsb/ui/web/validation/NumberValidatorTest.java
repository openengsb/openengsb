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

package org.openengsb.ui.web.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openengsb.core.common.validation.SingleAttributeValidationResult;

public class NumberValidatorTest {

    @Test
    public void passLetterInsteadOfNumber_shouldNotValidateAndGiveErrorMessage() {
        NumberValidator numberValidator = new NumberValidator();
        SingleAttributeValidationResult validate = numberValidator.validate("A");
        assertFalse(validate.isValid());
        assertEquals("validation.number.formating", validate.getErrorMessageId());
    }

    // TODO public void testUsingGermanLocale_shouldReturnGermanMessage() {

    // TODO public void testUsingEnglishLocale_shouldReturnEnglishMessage()

    @Test
    public void validateNumber_shouldReturnValidAndNoErrorMessage() {
        NumberValidator validator = new NumberValidator();
        SingleAttributeValidationResult validate = validator.validate("123");
        assertTrue(validate.isValid());
        assertEquals("", validate.getErrorMessageId());
    }

}
