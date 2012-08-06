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

package org.openengsb.core.workflow.drools;

import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.api.Event;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.OutgoingPortUtilService;
import org.openengsb.core.workflow.api.model.RemoteEvent;

public final class OsgiHelper {

    private static OsgiUtilsService utilsService;

    public static void sendRemoteEvent(String portId, String destination, RemoteEvent e)
        throws PortNotAvailableException {
        sendRemoteEvent(portId, destination, e, new HashMap<String, String>());
    }

    public static void sendRemoteEvent(String portId, String destination, RemoteEvent e, Map<String, String> metaData)
        throws PortNotAvailableException {
        MethodCall methodCall = new MethodCall("processRemoteEvent", new Object[]{ e }, metaData);
        OutgoingPortUtilService portUtilService = utilsService.getOsgiServiceProxy(OutgoingPortUtilService.class);
        portUtilService.sendMethodCall(portId, destination, methodCall);
    }

    public static void sendRemoteEvent(String portId, String destination, RemoteEvent e, String serviceId)
        throws PortNotAvailableException {
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("serviceId", serviceId);
        sendRemoteEvent(portId, destination, e, metaData);
    }

    public static <T> T getResponseProxy(Event e, Class<T> targetClass) {
        String origin = e.getOrigin();
        String filter = String.format("(%s=%s)", org.osgi.framework.Constants.SERVICE_PID, origin);
        return utilsService.getOsgiServiceProxy(filter, targetClass);
    }

    public static void setUtilsService(OsgiUtilsService utilsService) {
        OsgiHelper.utilsService = utilsService;
    }

    private OsgiHelper() {
    }

}
