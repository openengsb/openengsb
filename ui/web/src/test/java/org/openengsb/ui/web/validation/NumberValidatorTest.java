package org.openengsb.ui.web.validation;

import static org.junit.Assert.*;

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
