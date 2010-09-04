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
package org.openengsb.core.config.descriptor;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openengsb.core.config.Domain;
import org.openengsb.core.config.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.config.util.BundleStrings;

public class ServiceDescriptorTest {

    private static interface DummyDomain extends Domain {
    }

    private static class DummyInstance implements DummyDomain {
    }

    private static class OtherInstance implements Domain {
    }

    private Locale locale;
    private BundleStrings strings;
    private Builder builder;
    @Rule
    public final ExpectedException expected = ExpectedException.none();

    @Before
    public void setup() {
        locale = new Locale("en");
        strings = mock(BundleStrings.class);
        builder = ServiceDescriptor.builder(locale, strings);

        when(strings.getString("nameKey", locale)).thenReturn("name");
        when(strings.getString("descKey", locale)).thenReturn("desc");
        builder.id("a");
        builder.serviceType(DummyDomain.class);
        builder.implementationType(DummyInstance.class);
        builder.name("nameKey");
        builder.description("descKey");
    }

    protected void expectMessage(String expectedMessage) {
        expected.expect(IllegalStateException.class);
        expected.expectMessage(expectedMessage);
    }

    @Test
    public void builderShouldLocalizeNameAndDescription() {
        assertThat(builder.build().getName(), is("name"));
        assertThat(builder.build().getDescription(), is("desc"));
    }

    @Test
    public void buildWithouthId_shouldThrowISE() {
        expectMessage("id");
        builder.id(null);
        builder.build();
    }

    @Test
    public void buildWithoutServiceTyp_shouldThrowISE() {
        expectMessage("service");
        builder.serviceType(null);
        builder.build();
    }

    @Test
    public void buildWithoutImplType_shouldThrowISE() {
        expectMessage("implementation");
        builder.implementationType(null);
        builder.build();
    }

    @Test
    public void implementationTypeDoesNotImplementServiceType_shouldThrowISE() {
        expectMessage("service");
        builder.implementationType(OtherInstance.class);
        builder.build();
    }

    @Test
    public void buildWithoutName_shouldThrowISE() {
        expectMessage("name");
        builder.name("unknownKey");
        builder.build();
    }

    @Test
    public void buildWithoutDescription_shouldThrowISE() {
        expectMessage("description");
        builder.description("unknownKey");
        builder.build();
    }
}
