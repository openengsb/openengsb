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

package org.openengsb.ui.common.util;

import javax.servlet.Filter;

import org.ops4j.pax.wicket.api.ConfigurableFilterConfig;
import org.ops4j.pax.wicket.util.AbstractFilterFactory;
import org.osgi.framework.BundleContext;

public class DelegatingSecurityFilterFactory extends AbstractFilterFactory {

    private Filter securityFilterChain;
    
    public DelegatingSecurityFilterFactory(BundleContext bundleContext, String applicationName, Integer priority) {
        super(bundleContext, applicationName, priority);
    }

    @Override
    public Filter createFilter(ConfigurableFilterConfig filterConfig) {
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(DelegatingSecurityFilter.class.getClassLoader());
            DelegatingSecurityFilter filter = DelegatingSecurityFilter.class.newInstance();
            filter.setSecurityFilterChain(securityFilterChain);
            filter.init(filterConfig);
            return filter;
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Filter %s could not be created for application {}",
                DelegatingSecurityFilter.class.getName(), getApplicationName()), e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }
    }
    
    public void setSecurityFilterChain(Filter securityFilterChain) {
        this.securityFilterChain = securityFilterChain;
    }

}
