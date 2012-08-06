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

package org.openengsb.core.api.descriptor;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.api.l10n.PassThroughLocalizableString;
import org.openengsb.core.api.l10n.StringLocalizer;

public class ServiceDescriptorTest {

    private StringLocalizer strings;
    private Builder builder;
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setup() {
        strings = mock(StringLocalizer.class);
        builder = ServiceDescriptor.builder(strings);

        when(strings.getString("nameKey")).thenReturn(new PassThroughLocalizableString("name"));
        when(strings.getString("descKey")).thenReturn(new PassThroughLocalizableString("desc"));
        builder.id("a");
        builder.implementationType(NullDomainImpl.class);
        builder.name("nameKey");
        builder.description("descKey");
    }

    protected void expectMessage(String expectedMessage) {
        expected.expect(IllegalStateException.class);
        expected.expectMessage(expectedMessage);
    }

    @Test
    public void testBuilderShouldLocalizeNameAndDescription_shouldWork() {
        assertThat(builder.build().getName().getString(Locale.getDefault()), is("name"));
        assertThat(builder.build().getDescription().getString(Locale.getDefault()), is("desc"));
    }

    @Test
    public void testBuildWithouthId_shouldThrowIllegalStateException() {
        expectMessage("id");
        builder.id(null);
        builder.build();
    }

    @Test
    public void testBuildWithoutName_shouldThrowIllegalStateException() {
        expectMessage("name");
        builder.name("unknownKey");
        builder.build();
    }

    @Test
    public void testBuildWithoutDescription_shouldThrowIllegalStateException() {
        expectMessage("description");
        builder.description("unknownKey");
        builder.build();
    }

    public interface NullDomain extends Domain {

        Object nullMethod(Object o);
    }

    public class NullDomainImpl implements NullDomain {

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }

        @Override
        public Object nullMethod(Object o) {
            return o;
        }

        @Override
        public String getInstanceId() {
            return null;
        }

    }

    public class DomainImpl implements Domain {
        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }

        @Override
        public String getInstanceId() {
            return null;
        }
    }

}
