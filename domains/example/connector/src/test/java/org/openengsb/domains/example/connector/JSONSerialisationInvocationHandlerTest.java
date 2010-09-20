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

package org.openengsb.domains.example.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class JSONSerialisationInvocationHandlerTest {

    private TestInterface newProxyInstance;
    private Sender sender;

    @Before
    public void setUp() {
        sender = mock(Sender.class);
        when(sender.send(Mockito.anyString(), Mockito.anyObject())).thenReturn("worked");
        JSONSerialisationInvocationHandler handler = new JSONSerialisationInvocationHandler(sender);
        newProxyInstance =
            (TestInterface) Proxy.newProxyInstance(TestInterface.class.getClassLoader(),
                new Class[]{TestInterface.class}, handler);
    }

    @Test
    public void invokeWithSimpleIntAndString_shouldSerializeToJSONAndCallSender() throws Throwable {
        String log = newProxyInstance.test(1, "zwei");
        assertThat(log, equalTo("worked"));
        verify(sender).send("test", "[1,\"zwei\"]");
    }

    @Test
    public void invokeWithMap_ShouldSerializeToJSONAndCallSender() {
        Map<String, TestObject> map = new HashMap<String, TestObject>();
        map.put("b", new TestObject("zwei", 1));
        map.put("a", new TestObject("vier", 3));
        newProxyInstance.test(map);
        verify(sender).send("test", "[{\"b\":{\"string\":\"zwei\",\"i\":1},\"a\":{\"string\":\"vier\",\"i\":3}}]");
    }

    private static interface TestInterface {
        public void test(Map<String, TestObject> testObject);

        public String test(int i, String s);
    }

    private static class TestObject {
        private final String string;
        private final int i;

        public TestObject(String string, int i) {
            super();
            this.string = string;
            this.i = i;
        }

        public String getString() {
            return string;
        }

        public int getI() {
            return i;
        }
    }
}
