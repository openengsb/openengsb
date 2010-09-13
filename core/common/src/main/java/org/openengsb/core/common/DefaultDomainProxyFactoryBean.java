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

import org.openengsb.core.common.context.ContextService;
import org.osgi.framework.BundleContext;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.osgi.context.BundleContextAware;

@SuppressWarnings("serial")
public class DefaultDomainProxyFactoryBean extends ProxyFactoryBean implements BundleContextAware {

    private ForwardInterceptor interceptor;

    private String domainInterfaceName;

    public DefaultDomainProxyFactoryBean() {
        addInterface(Domain.class);
        interceptor = new ForwardInterceptor();
        addAdvice(interceptor);
    }

    public void setDomainInterface(Class<?> domainInterface) {
        addInterface(domainInterface);
        domainInterfaceName = domainInterface.getName();
        interceptor.setDomainInterfaceName(domainInterfaceName);
    }

    public void setDomainName(String domainName) {
        interceptor.setDomainName(domainName);
    }

    public void setContext(ContextService context) {
        interceptor.setContext(context);
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        interceptor.setBundleContext(bundleContext);
    }
}
