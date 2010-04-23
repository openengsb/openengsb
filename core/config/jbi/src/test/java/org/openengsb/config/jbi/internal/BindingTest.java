/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.config.jbi.internal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openengsb.config.jbi.internal.XStreamFactory;
import org.openengsb.config.jbi.types.ChoiceType;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointNameType;
import org.openengsb.config.jbi.types.IntType;
import org.openengsb.config.jbi.types.RefType;
import org.openengsb.config.jbi.types.ServiceNameType;
import org.openengsb.config.jbi.types.StringType;

import com.thoughtworks.xstream.XStream;

public class BindingTest {
    private final XStream x = XStreamFactory.createXStream();

    @Test
    public void parseStringType() throws Exception {
        String xml = "<string name=\"name\" optional=\"true\" maxLength=\"1\" defaultValue=\"a\" />";
        StringType o = (StringType) x.fromXML(xml);
        assertThat(o.getName(), is("name"));
        assertThat(o.isOptional(), is(true));
        assertThat(o.getMaxLength(), is(1));
        assertThat(o.getDefaultValue(), is("a"));
    }

    @Test
    public void parseChoiceType() throws Exception {
        String xml = "<choice values=\"a,b,c\" />";
        ChoiceType o = (ChoiceType) x.fromXML(xml);
        assertThat(o.getValues().length, is(3));
        assertThat(o.getValues()[0], is("a"));
        assertThat(o.getValues()[1], is("b"));
        assertThat(o.getValues()[2], is("c"));
    }

    @Test
    public void parseIntType() throws Exception {
        String xml = "<int min=\"-1\" max=\"2\" />";
        IntType o = (IntType) x.fromXML(xml);
        assertThat(o.getMin(), is(-1));
        assertThat(o.getMax(), is(2));
    }

    @Test
    public void parseRefType() throws Exception {
        String xml = "<ref clazz=\"java.lang.String\" />";
        RefType o = (RefType) x.fromXML(xml);
        assertThat(o.getTheClass(), is("java.lang.String"));
    }

    @Test
    public void beansListIsInitialized() throws Exception {
        String xml = "<component></component>";
        ComponentType o = (ComponentType) x.fromXML(xml);
        assertThat(o.getBeans(), notNullValue());
    }

    @Test
    public void endpointParseTargetAttribute() throws Exception {
        String xml = "<endpointName target=\"true\" />";
        EndpointNameType o = (EndpointNameType) x.fromXML(xml);
        assertThat(o.isTarget(), is(true));
    }

    @Test
    public void serviceParseTargetAttribute() throws Exception {
        String xml = "<serviceName target=\"true\" />";
        ServiceNameType o = (ServiceNameType) x.fromXML(xml);
        assertThat(o.isTarget(), is(true));
    }
}
