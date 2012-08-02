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

package org.openengsb.core.common;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;

import java.lang.reflect.Method;

import org.junit.Test;
import org.openengsb.core.test.AbstractOpenEngSBTest;

public class AbstractOpenEngSBInvocationHandlerTest extends AbstractOpenEngSBTest {

    @Test
    public void testUseDefaultConstructor_shouldForwardAllCalls() throws Throwable {
        Object testObject = new Object();
        AbstractOpenEngSBInvocationHandler abstractOpenEngSBInvocationHandler =
            createDefaultAbstractOpenEngSBInvocationHandler(testObject);
        Method method = Object.class.getMethod("toString", (Class<?>[]) null);
        assertSame(testObject, abstractOpenEngSBInvocationHandler.invoke(testObject, method, null));
    }

    @Test
    public void testUseConstructurToHandleObjectMethods_shouldHandleToString() throws Throwable {
        Object testObject = new Object() {
            @Override
            public String toString() {
                return "teststring";
            }
        };
        AbstractOpenEngSBInvocationHandler abstractOpenEngSBInvocationHandler =
            createAbstractOpenEngSBInvocationHandlerHandlingObjectMethods(testObject);
        Method method = Object.class.getMethod("toString", (Class<?>[]) null);
        String proxyRetVal = abstractOpenEngSBInvocationHandler.invoke(testObject, method, null).toString();
        assertEquals(proxyRetVal, "teststring");
    }

    private AbstractOpenEngSBInvocationHandler createDefaultAbstractOpenEngSBInvocationHandler(final Object test) {
        AbstractOpenEngSBInvocationHandler abstractOpenEngSBInvocationHandler =
            new AbstractOpenEngSBInvocationHandler() {
                @Override
                protected Object handleInvoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return test;
                }
            };
        return abstractOpenEngSBInvocationHandler;
    }

    private AbstractOpenEngSBInvocationHandler createAbstractOpenEngSBInvocationHandlerHandlingObjectMethods(
            final Object test) {
        AbstractOpenEngSBInvocationHandler abstractOpenEngSBInvocationHandler =
            new AbstractOpenEngSBInvocationHandler(true) {
                @Override
                protected Object handleInvoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return test;
                }
            };
        return abstractOpenEngSBInvocationHandler;
    }

}
