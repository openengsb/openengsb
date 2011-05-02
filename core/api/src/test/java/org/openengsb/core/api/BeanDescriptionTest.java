package org.openengsb.core.api;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openengsb.core.api.model.BeanDescription;

public class BeanDescriptionTest {

    @Test
    public void testWrapSimpleBean() throws Exception {
        SimpleTestBean bean = new SimpleTestBean("42", 42L);
        BeanDescription beanDescription = BeanDescription.fromObject(bean);
        SimpleTestBean bean2 = beanDescription.toObject(SimpleTestBean.class);

        assertThat(bean2.longValue, is(42L));
        assertThat(bean2.stringValue, is("42"));
    }

}
