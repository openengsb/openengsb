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
package org.openengsb.core.common.composite;

import java.lang.reflect.Method;
import java.util.List;

import org.openengsb.core.api.CompositeConnectorStrategy;
import org.osgi.framework.ServiceReference;

/**
 * Abstract utility class that can be used to delegate the invocation to a delegate implementation of the Domain
 * interface
 */
public abstract class AbstractDelegateStrategy implements CompositeConnectorStrategy {

    @Override
    public Object invoke(List<ServiceReference> services, Method method, Object... args) throws Throwable {
        return method.invoke(createDelegate(services), args);
    }

    protected abstract Object createDelegate(List<ServiceReference> services);

}
