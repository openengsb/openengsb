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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.OpenEngSBService;
import org.openengsb.core.common.communication.CallRouter;
import org.openengsb.core.common.communication.IncomingPort;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.MethodReturn;
import org.openengsb.core.common.communication.MethodReturn.ReturnType;
import org.openengsb.core.common.communication.OutgoingPort;
import org.openengsb.core.common.util.OsgiServiceUtils;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

public class CallRouterImpl implements CallRouter, BundleContextAware {

    private Log log = LogFactory.getLog(CallRouterImpl.class);

    private ExecutorService executor = Executors.newCachedThreadPool();

    private class PortHandler implements Runnable {
        private IncomingPort port;

        public PortHandler(IncomingPort port) {
            this.port = port;
        }

        @Override
        public void run() {
            while (true) {
                UUID id = UUID.randomUUID();
                MethodCall call = port.listen(id);
                String serviceId = call.getServiceId();

                OpenEngSBService service =
                    OsgiServiceUtils.getService(bundleContext, OpenEngSBService.class, "(id=" + serviceId + ")");
                Class<?>[] argTypes = getArgTypes(call.getArgs());

                Method method;
                try {
                    method = service.getClass().getMethod(call.getMethodName(), argTypes);
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException(e);
                }
                MethodReturn resultContainer = new MethodReturn();
                try {
                    Object result = method.invoke(service, call.getArgs());
                    resultContainer.setType(ReturnType.Object);
                    resultContainer.setArg(result);
                } catch (InvocationTargetException e) {
                    resultContainer.setType(ReturnType.Exception);
                    resultContainer.setArg(e.getCause());
                } catch (IllegalAccessException e) {
                    resultContainer.setType(ReturnType.Exception);
                    resultContainer.setArg(e);
                }

                port.sendResponse(id, resultContainer);
            }
        }

        private Class<?>[] getArgTypes(Object[] args) {
            Class<?>[] result = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                result[i] = args[i].getClass();
            }
            return result;
        }
    }

    private Map<String, OutgoingPort> ports = new HashMap<String, OutgoingPort>();
    private BundleContext bundleContext;

    @Override
    public void registerIncomingPort(IncomingPort port) {
        PortHandler portHandler = new PortHandler(port);
        executor.execute(portHandler);
    }

    @Override
    public void registerOutgoingPort(String scheme, OutgoingPort port) {
        ports.put(scheme, port);
    }

    @Override
    public void call(String portId, final URI destination, final MethodCall call) {
        final OutgoingPort port = ports.get(portId);
        Runnable callHandler = new Runnable() {
            @Override
            public void run() {
                port.send(destination, call);
            }
        };
        executor.execute(callHandler);
    }

    @Override
    public MethodReturn callSync(String portId, final URI destination, final MethodCall call) {
        final OutgoingPort port = ports.get(portId);
        return port.sendSync(destination, call);
    }

    public void stop() {
        executor.shutdownNow();
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
