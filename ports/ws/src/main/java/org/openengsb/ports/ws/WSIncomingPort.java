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

import java.util.Hashtable;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerLifeCycleListener;
import org.apache.cxf.endpoint.ServerLifeCycleManager;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.openengsb.core.common.remote.FilterChain;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceException;
import org.osgi.service.http.HttpService;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

public class WSIncomingPort {

    private static final String[] CXF_CONFIG = new String[]{
        "classpath:META-INF/cxf/cxf.xml",
    };
    private static final String CONTEXT_ROOT = "/ws";

    private BundleContext bundleContext;
    private HttpService httpService;
    private FilterChain filterChain;

    public void start() {
        Bus cxfBus = createCxfBus();
        registerServletForBus(cxfBus);
        JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
        registerFactoryForBus(factory, cxfBus);
        startServer(factory, cxfBus);
    }

    private void startServer(JaxWsServerFactoryBean factory, Bus cxfBus) {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ServerFactoryBean.class.getClassLoader());
            Server server = factory.create();
            server.getEndpoint().put("WS", CONTEXT_ROOT);
            Thread.currentThread().setContextClassLoader(ServerFactoryBean.class.getClassLoader());
            ServerLifeCycleListener stopHook = new ServerLifeCycleListener() {
                @Override
                public void stopServer(Server s) {
                    Object contextProperty = s.getEndpoint().get("WS");
                    if (contextProperty != null && contextProperty.equals(CONTEXT_ROOT)) {
                        httpService.unregister(CONTEXT_ROOT);
                    }
                }

                @Override
                public void startServer(Server server) {
                }
            };
            ServerLifeCycleManager mgr = cxfBus.getExtension(ServerLifeCycleManager.class);
            if (mgr != null) {
                mgr.registerListener(stopHook);
            }
            server.start();
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    private void registerFactoryForBus(JaxWsServerFactoryBean factory, Bus cxfBus) {
        factory.setBus(cxfBus);
        factory.setServiceClass(PortReceiver.class);
        factory.setAddress("/receiver/");
        factory.setServiceBean(new DefaultPortReceiver(filterChain));
    }

    private void registerServletForBus(Bus cxfBus) {
        CXFNonSpringServlet cxf = new CXFNonSpringServlet();
        cxf.setBus(cxfBus);
        try {
            httpService.registerServlet(CONTEXT_ROOT, cxf, new Hashtable<String, String>(),
                httpService.createDefaultHttpContext());
        } catch (Exception e) {
            throw new ServiceException("CXF: problem registering CXF HTTP Servlet", e);
        }
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

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void setFilterChain(FilterChain filterChain) {
        this.filterChain = filterChain;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
