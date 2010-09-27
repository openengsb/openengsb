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

package org.openengsb.domains.jms;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
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
        JSONSerialisationInvocationHandler handler = new JSONSerialisationInvocationHandler(sender);
        newProxyInstance =
            (TestInterface) Proxy.newProxyInstance(TestInterface.class.getClassLoader(),
                new Class[]{TestInterface.class}, handler);
    }

    @Test
    public void invokeWithSimpleIntAndString_shouldSerializeToJSONAndCallSender() {
        when(sender.send(Mockito.anyString(), Mockito.anyObject())).thenReturn("\"worked\"");
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

    @Test
    public void senderReturnsMappableReturnValue_shouldDeserializeAndReturnObject() throws IOException {
        when(sender.send(Mockito.anyString(), Mockito.anyObject())).thenReturn(
            "[{\"a\":{\"string\":\"a\",\"i\":1}}, {\"b\":{\"string\":\"b\",\"i\":2}}]");
        List<Map<String, TestObject>> returnValueTestMethod = newProxyInstance.returnValueTestMethod();
        assertThat(2, equalTo(returnValueTestMethod.size()));
        Map<String, TestObject> map1 = returnValueTestMethod.get(0);
        assertThat("a", equalTo(map1.get("a").getString()));
        assertThat(1, equalTo(map1.get("a").getI()));
        Map<String, TestObject> map2 = returnValueTestMethod.get(1);
        assertThat("b", equalTo(map2.get("b").getString()));
        assertThat(2, equalTo(map2.get("b").getI()));
    }

    private static interface TestInterface {
        void test(Map<String, TestObject> testObject);

        String test(int i, String s);

        List<Map<String, TestObject>> returnValueTestMethod();
    }

    public static class TestObject {
        private String string;
        private int i;

        public TestObject() {
            // TODO Auto-generated constructor stub
        }

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

        public void setString(String string) {
            this.string = string;
        }

        public void setI(int i) {
            this.i = i;
        }

    }
}
