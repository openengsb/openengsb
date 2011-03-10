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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * The OpenEngSB, and its projects can contain quite complex proxing of methods. This abstract class handles some of the
 * common problems which can occur working in such situations.
 */
public abstract class AbstractOpenEngSBInvocationHandler implements InvocationHandler {

    private boolean handleObjectMethodsItself;

    public AbstractOpenEngSBInvocationHandler() {
        this(false);
    }

    public AbstractOpenEngSBInvocationHandler(boolean handleObjectMethodsItself) {
        this.handleObjectMethodsItself = handleObjectMethodsItself;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (handleObjectMethodsItself) {
            for (Method objectMethod : Object.class.getMethods()) {
                if (objectMethod.getName().equals(method.getName())
                        && Arrays.deepEquals(objectMethod.getParameterTypes(), method.getParameterTypes())) {
                    if (Proxy.isProxyClass(proxy.getClass())) {
                        if (Proxy.getInvocationHandler(proxy) instanceof AbstractOpenEngSBInvocationHandler) {
                            return method.invoke(Proxy.getInvocationHandler(proxy), args);
                        }
                    }
                }
            }
        }
        return handleInvoke(proxy, method, args);
    }

    protected abstract Object handleInvoke(Object proxy, Method method, Object[] args) throws Throwable;

}
