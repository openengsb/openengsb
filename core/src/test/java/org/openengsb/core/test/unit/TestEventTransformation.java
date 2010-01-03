/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */

package org.openengsb.core.test.unit;

import junit.framework.Assert;

import org.junit.Test;
import org.openengsb.core.model.Event;
import org.openengsb.core.test.unit.SomeEvent.Bean;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public class TestEventTransformation {

    @Test
    public void testEvent() throws SerializationException {
        Event input = new Event("domain", "name");
        input.setValue("foo", 42);
        input.setValue("bar", "42");
        input.setValue("buz", new Integer(42));

        String xml = Transformer.toXml(input);

        Event result = Transformer.toEvent(xml);

        checkFields(input, result);
        checkKeys(input, result);
    }

    @Test
    public void testEventSubclass() throws SerializationException {
        SomeEvent input = new SomeEvent();
        input.setBean(new Bean("foo", null));

        String xml = Transformer.toXml(input);
        Event result = Transformer.toEvent(xml);

        checkFields(input, result);
        checkKeys(input, result);

        SomeEvent actual = (SomeEvent) result;
        Assert.assertEquals(input.getBean(), actual.getBean());
        Assert.assertNull(actual.getBean().getBean());
    }

    @Test
    public void testEventSubclassCircular() throws SerializationException {
        SomeEvent input = new SomeEvent();

        Bean beanA = new Bean("foo", null);
        Bean beanB = new Bean("bar", beanA);
        Bean beanC = new Bean("buz", beanB);
        beanA.setBean(beanC);

        input.setBean(beanA);

        String xml = Transformer.toXml(input);
        Event result = Transformer.toEvent(xml);

        checkFields(input, result);
        checkKeys(input, result);

        SomeEvent actual = (SomeEvent) result;
        Bean actualBeanA = actual.getBean();
        Assert.assertEquals(input.getBean(), actualBeanA);
        Assert.assertTrue(actualBeanA == actualBeanA.getBean().getBean().getBean());
    }

    private void checkFields(Event expected, Event actual) {
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getDomain(), actual.getDomain());
        Assert.assertEquals(expected.getToolConnector(), actual.getToolConnector());
    }

    private void checkKeys(Event expected, Event actual) {
        Assert.assertEquals(expected.getKeys(), actual.getKeys());
        for (String key : expected.getKeys()) {
            Assert.assertEquals(expected.getValue(key), actual.getValue(key));
        }
    }
}
