package org.openengsb.core.edbi.jdbc.names;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openengsb.core.edbi.api.IndexField;

/**
 * SQLIndexFieldNameTranslatorTest
 */
public class SQLIndexFieldNameTranslatorTest {

    @Test
    public void translate_returnsCorrectString() throws Exception {
        IndexField<?> field = mock(IndexField.class);
        when(field.getName()).thenReturn("someProperty");

        assertEquals("SOMEPROPERTY", new SQLIndexFieldNameTranslator().translate(field));
    }

    @Test(expected = IllegalArgumentException.class)
    public void translateNull_throwsException() throws Exception {
        new SQLIndexFieldNameTranslator().translate(null);
    }

}
