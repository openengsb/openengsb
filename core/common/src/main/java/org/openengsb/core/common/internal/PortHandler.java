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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openengsb.core.common.OpenEngSBService;
import org.openengsb.core.common.communication.IncomingPort;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.MethodReturn;
import org.openengsb.core.common.communication.MethodReturn.ReturnType;
import org.openengsb.core.common.util.OsgiServiceUtils;
import org.osgi.framework.BundleContext;

class PortHandler implements Runnable {
    private BundleContext bundleContext;
    private IncomingPort port;
    private ExecutorService executor = Executors.newCachedThreadPool();

    public PortHandler(BundleContext bundleContext, IncomingPort port) {
        this.bundleContext = bundleContext;
        this.port = port;
    }

    @Override
    public void run() {
        while (true) {
            final UUID id = UUID.randomUUID();
            final MethodCall call = port.listen(id);
            Runnable callhandler = new Runnable() {
                @Override
                public void run() {
                    handleMethodCall(id, call);
                }
            };
            executor.execute(callhandler);
        }
    }

    private void handleMethodCall(UUID id, MethodCall call) {
        final String filter = "(id=" + call.getServiceId() + ")";
        OpenEngSBService service = OsgiServiceUtils.getService(bundleContext, OpenEngSBService.class, filter);

        Object[] args = call.getArgs();
        Method method = findMethod(service, call.getMethodName(), getArgTypes(args));

        MethodReturn resultContainer = invokeMethod(service, method, args);
        port.sendResponse(id, resultContainer);
    }

    private MethodReturn invokeMethod(OpenEngSBService service, Method method, Object[] args) {
        MethodReturn resultContainer = new MethodReturn();
        try {
            Object result = method.invoke(service, args);
            resultContainer.setType(ReturnType.Object);
            resultContainer.setArg(result);
        } catch (InvocationTargetException e) {
            resultContainer.setType(ReturnType.Exception);
            resultContainer.setArg(e.getCause());
        } catch (IllegalAccessException e) {
            resultContainer.setType(ReturnType.Exception);
            resultContainer.setArg(e);
        }
        return resultContainer;
    }

    private Method findMethod(OpenEngSBService service, String methodName, Class<?>[] argTypes) {
        Method method;
        try {
            method = service.getClass().getMethod(methodName, argTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
        return method;
    }

    private Class<?>[] getArgTypes(Object[] args) {
        Class<?>[] result = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = args[i].getClass();
        }
        return result;
    }
}
