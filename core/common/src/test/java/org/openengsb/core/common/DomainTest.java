/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openengsb.core.common.util.AliveState;

public class DomainTest {

    public interface TestInterface extends Domain {
        @Override
        AliveState getAliveState();

        void doSomething();
    }

    public interface FactoryMethodsInterface {
        boolean connect();

        boolean update();
    }

    private class FactoryMethods implements FactoryMethodsInterface {

        @Override
        public boolean connect() {
            return false;
        }

        @Override
        public boolean update() {
            return false;
        }

    }

    private static class TestClass implements TestInterface {
        private AliveState state;
        private final FactoryMethodsInterface fm;

        TestClass(FactoryMethodsInterface fm) {
            this.fm = fm;
            init();
        }

        private void init() {
            if (fm.connect()) {
                state = AliveState.CONNECTING;
            } else {
                state = AliveState.DISCONNECTED;
            }
        }

        @Override
        public AliveState getAliveState() {
            return this.state;
        }

        @Override
        public void doSomething() {

            if (fm.update()) {
                state = AliveState.ONLINE;
            } else {
                state = AliveState.OFFLINE;
            }
        }
    }

    @Test
    public void checkPossibleStatesOfAnDomainWhichWasJustCreated_ShouldReturnConnecting() {
        FactoryMethodsInterface fmi = mock(FactoryMethods.class);
        when(fmi.connect()).thenReturn(true);
        TestClass test = new TestClass(fmi);
        assertThat(test.getAliveState(), is(AliveState.CONNECTING));
    }

    @Test
    public void checkPossibleStatesOfAnDomainWhichWasJustCreatedButFailed_ShouldReturnDisconnected() {
        FactoryMethodsInterface fmi = mock(FactoryMethods.class);
        when(fmi.connect()).thenReturn(false);
        TestClass test = new TestClass(fmi);
        assertThat(test.getAliveState(), is(AliveState.DISCONNECTED));
    }

    @Test
    public void checkStatesOfAnDomainWhichDoesSomethingCorrect_ShouldReturnOnline() {
        FactoryMethodsInterface fmi = mock(FactoryMethods.class);
        when(fmi.connect()).thenReturn(true);
        when(fmi.update()).thenReturn(true);
        TestClass test = new TestClass(fmi);
        test.doSomething();
        assertThat(test.getAliveState(), is(AliveState.ONLINE));
    }

    @Test
    public void checkStatesOfAnDomainWhichDoesSomethingAndAnErrorOccurred_ShouldReturnOffline() {
        FactoryMethodsInterface fmi = mock(FactoryMethods.class);
        when(fmi.connect()).thenReturn(true);
        when(fmi.update()).thenReturn(false);
        TestClass test = new TestClass(fmi);
        test.doSomething();
        assertThat(test.getAliveState(), is(AliveState.OFFLINE));
    }
}
