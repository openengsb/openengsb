package org.openengsb.core.common.validation;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

public class ValidationResultImplTest {

    @Test
    public void testAddErrorMessage() {
        ValidationResultImpl validationResultImpl = new ValidationResultImpl(true);
        String a = "A";
        validationResultImpl.addErrorMessage(a);
        String b = "B";
        validationResultImpl.addErrorMessage(b);
        Iterator<String> iterator = validationResultImpl.getErrorMessages().iterator();
        assertEquals(a, iterator.next());
        assertEquals(b, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testIsValid() {
        assertTrue(new ValidationResultImpl(true).isValid());
        assertFalse(new ValidationResultImpl(false).isValid());
    }

}
