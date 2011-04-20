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

import java.io.IOException;
import java.util.Hashtable;

import org.apache.cxf.Bus;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerLifeCycleListener;
import org.apache.cxf.endpoint.ServerLifeCycleManager;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.common.marshaling.RequestMapping;
import org.openengsb.core.common.marshaling.ReturnMapping;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceException;
import org.osgi.service.http.HttpService;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

public class WSPort implements OutgoingPort {

    private static final String[] CXF_CONFIG = new String[]{
        "classpath:META-INF/cxf/cxf.xml",
    };
    private static final String CONTEXT_ROOT = "/ws";

    private RequestHandler requestHandler;
    private BundleContext bundleContext;
    private HttpService httpService;

    @Override
    public void send(String destination, MethodCall call) {
        try {
            forwardToReceiver(destination, new RequestMapping(call).convertToMessage());
        } catch (IOException e) {
            throw new RuntimeException("Not possible to forward message", e);
        }
    }

    @Override
    public MethodReturn sendSync(String destination, MethodCall call) {
        String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        RequestMapping mapping = new RequestMapping(call);
        mapping.setAnswer(true);
        mapping.setCallId(currentTimeMillis);
        String receiveAndConvert;
        try {
            receiveAndConvert = forwardToReceiver(destination, mapping.convertToMessage());
        } catch (IOException e) {
            throw new RuntimeException("Not possible to forward message", e);
        }
        return createMethodReturn(receiveAndConvert);
    }

    private MethodReturn createMethodReturn(String receiveAndConvert) {
        try {
            return ReturnMapping.createFromMessage(receiveAndConvert);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String forwardToReceiver(String destination, String message) {
        PortReceiver service = retrieveProxyReceiverForDestination(destination);
        return service.receive(message);
    }

    private PortReceiver retrieveProxyReceiverForDestination(String destination) {
        Bus bus = createCxfBus();
        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setBus(bus);
        jaxWsProxyFactoryBean.setServiceClass(PortReceiver.class);
        jaxWsProxyFactoryBean.setDataBinding(new AegisDatabinding());
        jaxWsProxyFactoryBean.setAddress(destination);
        jaxWsProxyFactoryBean.setWsdlURL(destination + "?wsdl");
        PortReceiver service = jaxWsProxyFactoryBean.create(PortReceiver.class);
        return service;
    }

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
        factory.getServiceFactory().setDataBinding(new AegisDatabinding());
        factory.setServiceBean(new DefaultPortReceiver(requestHandler));
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

    public void stop() {
    }

    public void setRequestHandler(RequestHandler handler) {
        requestHandler = handler;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

}
