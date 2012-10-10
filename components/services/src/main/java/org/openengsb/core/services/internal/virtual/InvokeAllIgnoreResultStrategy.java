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

package org.openengsb.core.services.internal.virtual;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.openengsb.core.api.CompositeConnectorStrategy;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple and generic {@link CompositeConnectorStrategy} that invokes all services that are passed to it sequentially.
 * The result is always null. As soon as the first invocation throws an Exception it is re-thrown and no more services
 * are invoked.
 */
public class InvokeAllIgnoreResultStrategy implements CompositeConnectorStrategy {

    private static Logger LOGGER = LoggerFactory.getLogger(InvokeAllIgnoreResultStrategy.class);

    private BundleContext bundleContext;

    @Override
    public Object invoke(List<ServiceReference> services, Method method, Object... args) throws Throwable {
        for (ServiceReference ref : services) {
            Object service = bundleContext.getService(ref);
            try {
                method.invoke(service, args);
            } catch (InvocationTargetException e) {
                LOGGER.warn("connector in composition threw an Exception in methodcall %s", method.toString());
                LOGGER.debug("ExceptionDetails: ", e);
            }
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> domainClass) {
        return true;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
