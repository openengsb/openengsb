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

package org.openengsb.core.workflow;

import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.workflow.model.RemoteEvent;
import org.openengsb.core.common.OpenEngSBCoreServices;

public final class OsgiHelper {

    private static ContextCurrentService contextService;

    public static void sendRemoteEvent(String portId, String destination, RemoteEvent e)
        throws PortNotAvailableException {
        sendRemoteEvent(portId, destination, e, new HashMap<String, String>());
    }

    public static void sendRemoteEvent(String portId, String destination, RemoteEvent e, Map<String, String> metaData)
        throws PortNotAvailableException {
        OutgoingPort port;
        try {
            port = OpenEngSBCoreServices.getServiceUtilsService().getServiceWithId(OutgoingPort.class, portId);
        } catch (OsgiServiceNotAvailableException e1) {
            throw new PortNotAvailableException("Port with id " + portId + " not available", e1);
        }
        MethodCall methodCall = new MethodCall("processRemoteEvent", new Object[]{ e }, metaData);
        MethodCallRequest request = new MethodCallRequest(methodCall);
        port.send(destination, request);
    }

    public static void sendRemoteEvent(String portId, String destination, RemoteEvent e, String serviceId)
        throws PortNotAvailableException {
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("serviceId", serviceId);
        sendRemoteEvent(portId, destination, e, metaData);
    }

    public static void setContextService(ContextCurrentService contextService) {
        OsgiHelper.contextService = contextService;
    }

    private OsgiHelper() {
    }

}
