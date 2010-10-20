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

package org.openengsb.ui.web;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.util.AliveState;

public class MethodUtilTest {

    public interface HiddenInterface {
        void hiddenMethod();
    }

    public interface Testinterface extends Domain {
        void dosomething();
    }

    public class TestClass implements Testinterface, HiddenInterface {
        @Override
        public void dosomething() {
        }

        public void dootherstuff() {

        }

        @Override
        public void hiddenMethod() {

        }

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }
    }

    public abstract class AbstractTestClass {
        public abstract void dootherstuff();
    }

    public class SubTestClass extends AbstractTestClass implements Testinterface {
        @Override
        public void dootherstuff() {
        }

        @Override
        public void dosomething() {
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }
    }

    public interface TestInterface2 extends Domain {
        void dootherstuff();
    }

    public static class MultiClass implements Testinterface, TestInterface2 {
        @Override
        public void dootherstuff() {
        }

        @Override
        public void dosomething() {
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }
    }

    @Test
    public void testOnlyInterface() throws Exception {
        List<Method> methods = MethodUtil.getServiceMethods(new TestClass());
        Assert.assertTrue(methods.contains(Testinterface.class.getMethod("dosomething")));
        Assert.assertFalse(methods.contains(TestClass.class.getMethod("dootherstuff")));
    }

    @Test
    public void testAbstractClass() throws Exception {
        List<Method> methods = MethodUtil.getServiceMethods(new SubTestClass());
        Assert.assertTrue(methods.contains(Testinterface.class.getMethod("dosomething")));
        Assert.assertFalse(methods.contains(SubTestClass.class.getMethod("dootherstuff")));
    }

    @Test
    public void testMultipleInterfaces() throws Exception {
        List<Method> methods = MethodUtil.getServiceMethods(new MultiClass());
        Assert.assertTrue(methods.contains(Testinterface.class.getMethod("dosomething")));
        Assert.assertTrue(methods.contains(TestInterface2.class.getMethod("dootherstuff")));
    }

    @Test
    public void onlyDomainMethodsAreReturned() throws Exception {
        List<Method> methods = MethodUtil.getServiceMethods(new TestClass());
        Method hidden = HiddenInterface.class.getMethod("hiddenMethod");
        Assert.assertFalse(methods.contains(hidden));
    }
}
