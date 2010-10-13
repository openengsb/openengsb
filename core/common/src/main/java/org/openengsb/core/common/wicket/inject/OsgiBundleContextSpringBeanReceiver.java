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

package org.openengsb.core.common.wicket.inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.WicketRuntimeException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationContext;

@SuppressWarnings("serial")
public class OsgiBundleContextSpringBeanReceiver implements OsgiSpringBeanReceiver {

    private static final String SYM_NAME_PROPERTY = "org.springframework.context.service.name";
    private static final String SPRING_APPLICATION_CONTEXT = "org.springframework.context.ApplicationContext";

    private BundleContext bundleContext;
    private Log log = LogFactory.getLog(getClass());

    public OsgiBundleContextSpringBeanReceiver(BundleContext bundleContext) {
        if (bundleContext == null) {
            throw new WicketRuntimeException("Not allowed to use a null bundle context");
        }
        this.bundleContext = bundleContext;
    }

    @Override
    public Object getBean(String springBeanName, String bundleSymbolicName) {
        ServiceReference[] references = getServiceRefsMatchingApplicationContext(bundleSymbolicName);
        ApplicationContext applicationContext = getApplicationContextBySymName(bundleSymbolicName, references);
        Object bean = getSpringBeanFromContext(springBeanName, applicationContext);
        return bean;
    }

    private ServiceReference[] getServiceRefsMatchingApplicationContext(String bundleSymbolicName) {
        ServiceReference[] references = null;
        log.debug("Trying to retrieve ApplicationContext from bundle " + bundleSymbolicName);
        try {
            references = bundleContext.getAllServiceReferences(SPRING_APPLICATION_CONTEXT, null);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Parsing error", e);
        }
        if (references == null) {
            throw new RuntimeException("No service found");
        }
        return references;
    }

    private ApplicationContext getApplicationContextBySymName(String bundleSymbolicName,
            ServiceReference[] references) {
        ServiceReference reference = null;
        for (ServiceReference serviceReference : references) {
            if (serviceRefIsNotNull(serviceReference)
                    && serviceRefPropEqualsSymName(bundleSymbolicName, serviceReference)) {
                reference = serviceReference;
                break;
            }
        }
        if (reference == null) {
            throw new RuntimeException("Bundle " + bundleSymbolicName + " does not export ApplicationContext");
        }
        ApplicationContext applicationContext = (ApplicationContext) bundleContext.getService(reference);
        return applicationContext;
    }

    private boolean serviceRefIsNotNull(ServiceReference serviceReference) {
        return serviceReference.getProperty(SYM_NAME_PROPERTY) != null;
    }

    private boolean serviceRefPropEqualsSymName(String bundleSymbolicName, ServiceReference serviceReference) {
        return serviceReference.getProperty(SYM_NAME_PROPERTY).equals(bundleSymbolicName);
    }

    private Object getSpringBeanFromContext(String springBeanName, ApplicationContext applicationContext) {
        log.debug("Retrieving bean with id " + springBeanName + " from ApplicationContext");
        Object bean = applicationContext.getBean(springBeanName);
        return bean;
    }
}
