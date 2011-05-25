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

package org.openengsb.ports.ws;

import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.openengsb.core.common.remote.AbstractFilterAction;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

public class WSOutgoingPort extends AbstractFilterAction<String, String> {

    private static final String[] CXF_CONFIG = new String[]{
        "classpath:META-INF/cxf/cxf.xml",
    };
    private BundleContext bundleContext;

    public WSOutgoingPort() {
        super(String.class, String.class);
    }

    @Override
    protected String doFilter(String input, Map<String, Object> metaData) {
        String destination = (String) metaData.get("destination");
        PortReceiver service = retrieveProxyReceiverForDestination(destination);
        return service.receive(input);
    }

    private PortReceiver retrieveProxyReceiverForDestination(String destination) {
        Bus bus = createCxfBus();
        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setBus(bus);
        jaxWsProxyFactoryBean.setServiceClass(PortReceiver.class);
        jaxWsProxyFactoryBean.setAddress(destination);
        jaxWsProxyFactoryBean.setWsdlURL(destination + "?wsdl");
        PortReceiver service = jaxWsProxyFactoryBean.create(PortReceiver.class);
        return service;
    }

    private Bus createCxfBus() {
        ApplicationContext ctx = createSpringCxfContext();
        SpringBusFactory fact = new SpringBusFactory(ctx);
        return fact.createBus();
    }

    private ApplicationContext createSpringCxfContext() {
        OsgiBundleXmlApplicationContext ctx = new OsgiBundleXmlApplicationContext(CXF_CONFIG);
        ctx.setPublishContextAsService(false);
        ctx.setBundleContext(bundleContext);
        ctx.refresh();
        return ctx;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
