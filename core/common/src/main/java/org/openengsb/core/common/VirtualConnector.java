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
import java.util.Arrays;
import java.util.List;

import org.openengsb.core.api.OpenEngSBService;

/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The AASTI licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

public abstract class VirtualConnector extends AbstractOpenEngSBService implements InvocationHandler {

    /**
     * methods declared in these classes are always handled by the invocation handler itself rather than forwarding it
     * to the remote object
     */
    private static final List<Class<?>> SELF_HANDLED_CLASSES = Arrays.asList(new Class<?>[]{ Object.class,
        OpenEngSBService.class });

    protected VirtualConnector(String instanceId) {
        super(instanceId);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (SELF_HANDLED_CLASSES.contains(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        return doInvoke(proxy, method, args);
    }

    protected abstract Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable;

}
