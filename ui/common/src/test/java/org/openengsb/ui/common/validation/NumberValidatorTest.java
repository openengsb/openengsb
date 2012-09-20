/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.common.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openengsb.core.api.validation.SingleAttributeValidationResult;
import org.openengsb.ui.common.editor.NumberValidator;

public class NumberValidatorTest {

    @Test
    public void testPassLetterInsteadOfNumber_shouldNotValidateAndGiveErrorMessage() throws Exception {
        NumberValidator numberValidator = new NumberValidator();
        SingleAttributeValidationResult validate = numberValidator.validate("A");
        assertFalse(validate.isValid());
        assertEquals("validation.number.formating", validate.getErrorMessageId());
    }

    // TODO: [OPENENGSB-1250] public void testUsingGermanLocale_shouldReturnGermanMessage() {

    // TODO: [OPENENGSB-1250] public void testUsingEnglishLocale_shouldReturnEnglishMessage()

    @Test
    public void testValidateNumber_shouldReturnValidAndNoErrorMessage() throws Exception {
        NumberValidator validator = new NumberValidator();
        SingleAttributeValidationResult validate = validator.validate("123");
        assertTrue(validate.isValid());
        assertEquals("", validate.getErrorMessageId());
    }
}
