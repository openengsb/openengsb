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

package org.openengsb.core.services.internal.virtual;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.OutgoingPortUtilService;
import org.openengsb.core.common.VirtualConnector;
import org.openengsb.core.common.transformations.TransformationUtils;
import org.openengsb.core.ekb.api.TransformationEngine;

/**
 * Representation of a connector that forwards all method-calls to a remote connector. Communication is done using a
 * port-implementation (like jms+json)
 */
public class ProxyConnector extends VirtualConnector {

    private String portId;
    private String destination;
    private final Map<String, String> metadata = new HashMap<String, String>();
    private String domainId;
    private String connectorId;

    private OutgoingPortUtilService portUtil;
    private ProxyRegistration registration;

    private TransformationEngine transformationEngine;
    private Class<?> connectorInterface;

    public ProxyConnector(String instanceId, OutgoingPortUtilService portUtil, ProxyRegistration registration) {
        super(instanceId);
        this.portUtil = portUtil;
        this.registration = registration;
    }

    @Override
    public Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Connector.class)) {
            return this.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(this, args);
        }
        Method targetMethod = method;
        Object[] targetArgs = args;
        if (connectorInterface != null) {
            targetMethod = TransformationUtils.findTargetMethod(method, connectorInterface);
            targetArgs = transformArguments(args, targetMethod.getParameterTypes());
        }
        List<Class<?>> paramList = Arrays.asList(targetMethod.getParameterTypes());
        List<String> paramTypeNames = new ArrayList<String>();
        for (Class<?> paramType : paramList) {
            paramTypeNames.add(paramType.getName());
        }

        MethodCall methodCall = new MethodCall(targetMethod.getName(), targetArgs, metadata, paramTypeNames);

        if (!registration.isRegistered()) {
            if (targetMethod.getName().equals("getAliveState")) {
                return AliveState.OFFLINE;
            }
            throw new IllegalArgumentException("not registered");
        }

        MethodResult callResult =
            portUtil.sendMethodCallWithResult(registration.getPortId(), registration.getDestination(), methodCall);
        switch (callResult.getType()) {
            case Object:
                Object result = callResult.getArg();
                if(connectorInterface == null){
                    return result;
                }
                return transformArgument(result, method.getReturnType());
            case Void:
                return null;
            case Exception:
                throw new RuntimeException(callResult.getArg().toString());
            default:
                throw new IllegalStateException("Return Type has to be either Void, Object or Exception");
        }
    }

    private Object[] transformArguments(Object[] args, Class<?>[] parameterTypes) {
        if (args == null) {
            return null;
        }
        Object[] transformed = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            transformed[i] = transformArgument(args[i], parameterTypes[i]);
        }
        return transformed;
    }

    private Object transformArgument(Object arg, Class<?> targetType) {
        if (!(OpenEngSBModel.class.isInstance(arg) && OpenEngSBModel.class.isAssignableFrom(targetType))) {
            return arg;
        }
        return transformationEngine.performTransformation(TransformationUtils.toModelDescription(arg.getClass()),
                TransformationUtils.toModelDescription(targetType), arg);
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

    public final void setOutgoingPortUtilService(OutgoingPortUtilService portUtil) {
        this.portUtil = portUtil;
    }

    public final String getPortId() {
        return portId;
    }

    public final String getDestination() {
        return destination;
    }

    public final OutgoingPortUtilService getCallRouter() {
        return portUtil;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public void setConnectorInterface(Class<?> connectorInterface) {
        this.connectorInterface = connectorInterface;
    }

    public void setTransformationEngine(TransformationEngine transformationEngine) {
        this.transformationEngine = transformationEngine;
    }
}
