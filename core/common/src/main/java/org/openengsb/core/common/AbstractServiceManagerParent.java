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

import java.util.Hashtable;
import java.util.Map;

import org.openengsb.core.common.context.ContextHolder;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.l10n.BundleStrings;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public abstract class AbstractServiceManagerParent implements BundleContextAware {

    private BundleContext bundleContext;
    private BundleStrings strings;

    public AbstractServiceManagerParent() {
        super();
    }

    protected final BundleStrings getStrings() {
        return strings;
    }

    public final BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        strings = new BundleStrings(bundleContext.getBundle());
    }

    protected Hashtable<String, String> createNotificationServiceProperties(String id, Map<String, String> attributes) {
        Hashtable<String, String> serviceProperties = new Hashtable<String, String>();
        serviceProperties.put("id", id);
        serviceProperties.put("domain", getDomainInterface().getName());
        serviceProperties.put("class", getImplementationClass().getName());
        serviceProperties.put("managerId", getDescriptor().getId());
        createLocation(attributes, serviceProperties);
        if (attributes.containsKey(Constants.SERVICE_RANKING)) {
            serviceProperties.put(Constants.SERVICE_RANKING, attributes.get(Constants.SERVICE_RANKING));
        }

        return serviceProperties;
    }

    protected void createLocation(Map<String, String> attributes, Map<String, String> serviceProperties) {
        if (attributes.containsKey("location")) {
            String[] split = attributes.get("location").split(",");
            StringBuilder builder = new StringBuilder();
            for (String string : split) {
                builder.append("[" + string.trim() + "]");
            }
            serviceProperties.put("location." + ContextHolder.get().getCurrentContextId(), builder.toString());
            attributes.remove("location");
        }
    }

    protected abstract ServiceDescriptor getDescriptor();

    protected abstract Class<? extends Domain> getDomainInterface();

    protected abstract Class<? extends Domain> getImplementationClass();
}
