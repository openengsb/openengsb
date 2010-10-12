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

package org.openengsb.core.common;

import static java.lang.String.format;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.context.ContextService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class ForwardHandler implements InvocationHandler {

    private Log log = LogFactory.getLog(ForwardHandler.class);

    private BundleContext bundleContext;

    private ContextService context;

    private String domainInterfaceName;

    private String domainName;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws IllegalAccessException,
            InvocationTargetException {
        log.info("Forwarding invocation to default connector");
        String connectorId = context.getValue("/domains/" + domainName + "/defaultConnector/id");
        log.debug(format("Default connector for %s is %s", domainName, connectorId));
        ServiceReference serviceRef = getServiceRef(connectorId);
        Object service = bundleContext.getService(serviceRef);
        log.debug("invoking method on serviceObject " + service);
        Object result;
        result = method.invoke(service, args);
        bundleContext.ungetService(serviceRef);
        return result;
    }

    private ServiceReference getServiceRef(String id) {
        String filter = "(id=" + id + ")";
        try {
            ServiceReference[] serviceReferences = bundleContext.getServiceReferences(domainInterfaceName, filter);
            if (serviceReferences == null) {
                throw new IllegalArgumentException("service with id " + id + " not found");
            } else if (serviceReferences.length > 1) {
                throw new IllegalStateException("multiple services found");
            }
            return serviceReferences[0];
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("filter invalid " + filter, e);
        }
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void setContext(ContextService context) {
        this.context = context;
    }

    public void setDomainInterfaceName(String domainInterfaceName) {
        this.domainInterfaceName = domainInterfaceName;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
