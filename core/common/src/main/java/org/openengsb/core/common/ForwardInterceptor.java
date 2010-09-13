/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.core.common;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.context.ContextService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import static java.lang.String.format;

public class ForwardInterceptor implements MethodInterceptor {

    private Log log = LogFactory.getLog(ForwardInterceptor.class);

    private BundleContext bundleContext;

    private ContextService context;

    private String domainInterfaceName;

    private String domainName;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        log.info("Forwarding invocation to default connector");
        String connectorId = context.getValue("/domains/" + domainName + "/defaultConnector/id");
        log.debug(format("Default connector for %s is %s", domainName, connectorId));
        Object service = getService(connectorId);
        Method methodToInvoke = invocation.getMethod();
        Object[] arguments = invocation.getArguments();
        log.debug("invoking method on serviceObject " + service);
        return methodToInvoke.invoke(service, arguments);
    }

    private Object getService(String id) {
        String filter = "(id=" + id + ")";
        try {
            ServiceReference[] serviceReferences = bundleContext.getServiceReferences(domainInterfaceName, filter);
            if (serviceReferences == null) {
                throw new IllegalArgumentException("service with id " + id + " not found");
            } else if (serviceReferences.length > 1) {
                throw new IllegalStateException("multiple services found");
            }
            return bundleContext.getService(serviceReferences[0]);
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
