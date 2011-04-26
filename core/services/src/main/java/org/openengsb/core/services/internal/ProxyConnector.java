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

package org.openengsb.core.services.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.remote.CallRouter;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;
import org.openengsb.core.common.AbstractOpenEngSBService;

/**
 * Representation of a connector that forwards all method-calls to a remote connector. Communication is done using a
 * port-implementation (like jms+json)
 */
public class ProxyConnector extends AbstractOpenEngSBService implements InvocationHandler {

    /**
     * methods declared in these classes are always handled by the invocation handler itself rather than forwarding it
     * to the remote object
     */
    @SuppressWarnings("unchecked")
    private static final List<Class<? extends Object>> SELF_HANDLED_CLASSES = Arrays.asList(Object.class,
        OpenEngSBService.class);

    private String portId;
    private String destination;
    private final Map<String, String> metadata = new HashMap<String, String>();

    private CallRouter callRouter;

    public ProxyConnector() {
    }

    public ProxyConnector(String instanceId) {
        super(instanceId);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (SELF_HANDLED_CLASSES.contains(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        MethodReturn callSync =
            callRouter.callSync(portId, destination, new MethodCall(method.getName(), args, metadata));
        switch (callSync.getType()) {
            case Object:
                return callSync.getArg();
            case Void:
                return null;
            case Exception:
                throw new RuntimeException(callSync.getArg().toString());
            default:
                throw new IllegalStateException("Return Type has to be either Void, Object or Exception");
        }
    }

    public final void setPortId(String id) {
        portId = id;
    }

    public final void setDestination(String destination) {
        this.destination = destination;
    }

    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }

    public final void setCallRouter(CallRouter callRouter) {
        this.callRouter = callRouter;
    }

    public final String getPortId() {
        return portId;
    }

    public final String getDestination() {
        return destination;
    }

    public final CallRouter getCallRouter() {
        return callRouter;
    }
}
