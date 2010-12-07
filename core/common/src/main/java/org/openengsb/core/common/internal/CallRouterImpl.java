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

package org.openengsb.core.common.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.OpenEngSBService;
import org.openengsb.core.common.communication.CallRouter;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.MethodReturn;
import org.openengsb.core.common.communication.Port;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.BundleContextAware;

public class CallRouterImpl implements CallRouter, BundleContextAware {

    private Log log = LogFactory.getLog(CallRouterImpl.class);

    private ExecutorService executor = Executors.newCachedThreadPool();

    private class PortHandler implements Runnable {
        private Port port;

        public PortHandler(Port port) {
            this.port = port;
        }

        @Override
        public void run() {
            while (true) {
                MethodCall call = port.receive();
                String serviceId = call.getServiceId();

                ServiceReference[] allServiceReferences;
                try {
                    allServiceReferences =
                        bundleContext.getServiceReferences(OpenEngSBService.class.getName(), "(id=" + serviceId
                                + ")");
                } catch (InvalidSyntaxException e1) {
                    throw new RuntimeException(e1);
                }
                if (allServiceReferences == null) {
                    throw new IllegalArgumentException("service with id " + serviceId + " not found");
                }
                if (allServiceReferences.length != 1) {
                    throw new IllegalStateException("mutliple services matching (id=" + serviceId + ")");
                }
                Object service = bundleContext.getService(allServiceReferences[0]);
                Class<?>[] argTypes = getArgTypes(call.getArgs());

                Method method;
                try {
                    method = service.getClass().getMethod(call.getMethodName(), argTypes);
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException(e);
                }
                try {
                    method.invoke(service, call.getArgs());
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                log.info(call);
            }
        }

        private Class<?>[] getArgTypes(Object[] args) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private Map<String, Port> ports = new HashMap<String, Port>();
    private BundleContext bundleContext;

    @Override
    public void registerPort(String scheme, Port port) {
        ports.put(scheme, port);
        PortHandler portHandler = new PortHandler(port);
        executor.submit(portHandler);
    }

    @Override
    public void call(String portId, final URI destination, final MethodCall call) {
        final Port port = ports.get(portId);
        Runnable callHandler = new Runnable() {
            @Override
            public void run() {
                port.send(destination, call);
            }
        };
        executor.submit(callHandler);
    }

    @Override
    public MethodReturn callSync(String portId, URI destination, MethodCall call) {
        // TODO Auto-generated method stub
        return null;
    }

    public void start() {

    }

    public void stop() {
        executor.shutdownNow();
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
