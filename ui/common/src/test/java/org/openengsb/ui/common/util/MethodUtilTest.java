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

package org.openengsb.ui.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.AttributeDefinition.Builder;
import org.openengsb.core.api.descriptor.AttributeDefinition.Option;
import org.openengsb.core.api.l10n.LocalizableString;
import org.openengsb.core.api.l10n.StringLocalizer;
import org.openengsb.core.test.DummyModel;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;

public class MethodUtilTest {

    public interface HiddenInterface {
        void hiddenMethod();
    }

    public class TestClass extends NullDomainImpl implements HiddenInterface {
        @Override
        public void hiddenMethod() {
        }

        public void dootherstuff() {
        }
    }

    public abstract class AbstractTestClass {
        public abstract void dootherstuff();
    }

    public class SubTestClass extends AbstractTestClass implements NullDomain {

        @Override
        public String getInstanceId() {
            return null;
        }

        @Override
        public void dootherstuff() {
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }

        @Override
        public void nullMethod() {
        }

        @Override
        public Object nullMethod(Object o) {
            return o;
        }

        @Override
        public Object nullMethod(Object o, String b) {
            return null;
        }

        @Override
        public void commitModel(DummyModel model) {

        }

    }

    public interface TestInterface2 extends Domain {
        void dootherstuff();
    }

    public static class MultiClass implements NullDomain, TestInterface2 {

        @Override
        public String getInstanceId() {
            return null;
        }

        @Override
        public void dootherstuff() {
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }

        @Override
        public void nullMethod() {
        }

        @Override
        public Object nullMethod(Object o) {
            return o;
        }

        @Override
        public Object nullMethod(Object o, String b) {
            return null;
        }

        @Override
        public void commitModel(DummyModel model) {

        }
<<<<<<< HEAD

=======
>>>>>>> master
    }

    public static enum TestEnum {
            ONE, TWO
    }

    @Test
    public void addEnumValues() {
        StringLocalizer mock = Mockito.mock(StringLocalizer.class);
        LocalizableString mock2 = Mockito.mock(LocalizableString.class);
        LocalizableString mock3 = Mockito.mock(LocalizableString.class);
        when(mock2.getKey()).thenReturn("123");
        when(mock2.getString(Locale.getDefault())).thenReturn("ONE");
        when(mock2.getKey()).thenReturn("123");
        when(mock3.getString(Locale.getDefault())).thenReturn("TWO");
        when(mock.getString("ONE")).thenReturn(mock2);
        when(mock.getString("TWO")).thenReturn(mock2);

        Builder builder = AttributeDefinition.builder(mock);
        builder.name("ONE").id("123");
        MethodUtil.addEnumValues(TestEnum.class, builder);
        AttributeDefinition build = builder.build();
        List<Option> options = build.getOptions();
        Option option0 = options.get(0);
        assertThat(option0.getLabel().getString(Locale.getDefault()), equalTo(TestEnum.ONE.toString()));
        assertThat(option0.getValue().toString(), equalTo(TestEnum.ONE.toString()));
        Option option1 = options.get(1);
        assertThat(option1.getLabel().getString(Locale.getDefault()), equalTo(TestEnum.ONE.toString()));
        assertThat(option1.getValue().toString(), equalTo(TestEnum.TWO.toString()));
    }
}
