package org.openengsb.core.common;

import org.junit.Test;
import org.openengsb.core.common.util.AliveEnum;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DomainTest {

    public interface TestInterface extends Domain {
        @Override
        public AliveEnum getAliveState();

        public void doSomething();
    }

    public interface FactoryMethodsInterface {
        public boolean connect();

        public boolean update();
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

    private class TestClass implements TestInterface {
        private AliveEnum state;
        FactoryMethodsInterface fm;

        TestClass(FactoryMethodsInterface fm) {
            this.fm = fm;
            init();
        }

        private void init() {
            if (fm.connect()) {
                state = AliveEnum.CONNECTING;
            } else {
                state = AliveEnum.DISCONNECTED;
            }
        }

        @Override
        public AliveEnum getAliveState() {
            return this.state;
        }

        @Override
        public void doSomething() {

            if (fm.update()) {
                state = AliveEnum.ONLINE;
            } else {
                state = AliveEnum.OFFLINE;
            }
        }
    }


    @Test
    public void checkPossibleStatesOfAnDomainWhichWasJustCreated_ShouldReturnConnecting() {
        FactoryMethodsInterface fmi = mock(FactoryMethods.class);
        when(fmi.connect()).thenReturn(true);
        TestClass test = new TestClass(fmi);
        assertThat(test.getAliveState(), is(AliveEnum.CONNECTING));
    }

    @Test
    public void checkPossibleStatesOfAnDomainWhichWasJustCreatedButFailed_ShouldReturnDisconnected() {
        FactoryMethodsInterface fmi = mock(FactoryMethods.class);
        when(fmi.connect()).thenReturn(false);
        TestClass test = new TestClass(fmi);
        assertThat(test.getAliveState(), is(AliveEnum.DISCONNECTED));
    }

    @Test
    public void checkStatesOfAnDomainWhichDoesSomethingCorrect_ShouldReturnOnline() {
        FactoryMethodsInterface fmi = mock(FactoryMethods.class);
        when(fmi.connect()).thenReturn(true);
        when(fmi.update()).thenReturn(true);
        TestClass test = new TestClass(fmi);
        test.doSomething();
        assertThat(test.getAliveState(), is(AliveEnum.ONLINE));
    }

        @Test
    public void checkStatesOfAnDomainWhichDoesSomethingAndAnErrorOccurred_ShouldReturnOffline() {
        FactoryMethodsInterface fmi = mock(FactoryMethods.class);
        when(fmi.connect()).thenReturn(true);
        when(fmi.update()).thenReturn(false);
        TestClass test = new TestClass(fmi);
        test.doSomething();
        assertThat(test.getAliveState(), is(AliveEnum.OFFLINE));
    }
}
