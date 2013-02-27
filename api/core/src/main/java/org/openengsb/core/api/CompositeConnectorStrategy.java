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

package org.openengsb.core.api;

import java.lang.reflect.Method;
import java.util.List;

import org.osgi.framework.ServiceReference;

/**
 * implements a Strategy used for handling invocations on composite connectors. The implementation decides which
 * services are invoked and how it is done (sequential, concurrent, ...). When implementing a strategy the developer
 * must also think of a way for providing a suitable result.
 *
 * In order for the strategy to be usable it must be registered as an OSGi-service. The registered service must have the
 * "strategy-name" property in order to be discoverable.
 */
public interface CompositeConnectorStrategy {

    /**
     * handles an invocation for a composite connector. The list of services is provided as {@link ServiceReference}s so
     * that the implementation can reason on the services properties. Utility-methods in {@link OsgiUtilsService} can be
     * used to resolve the service to its object.
     *
     */
    Object invoke(List<ServiceReference> services, Method method, Object... args) throws Throwable;

    /**
     * returns true if the specified domain represented by the class is supported by this strategy. Strategies that
     * support any domain may just return true here. Implementations of domain specific strategies should behave
     * accordingly.
     */
    boolean supports(Class<?> domainClass);

}
