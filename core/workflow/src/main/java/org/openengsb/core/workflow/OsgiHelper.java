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

package org.openengsb.core.workflow;

import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.OutgoingPort;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.util.OsgiServiceNotAvailableException;
import org.openengsb.core.common.util.OsgiServiceUtils;
import org.openengsb.core.common.workflow.model.RemoteEvent;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

public class OsgiHelper implements BundleContextAware {

    private BundleContext bundleContext;
    private ContextCurrentService contextService;

    public void sendRemoteEvent(String portId, String destination, RemoteEvent e) throws PortNotAvailableException {
        sendRemoteEvent(portId, destination, e, new HashMap<String, String>());
    }

    public void sendRemoteEvent(String portId, String destination, RemoteEvent e, Map<String, String> metaData)
        throws PortNotAvailableException {
        OutgoingPort port;
        try {
            port = OsgiServiceUtils.getServiceWithId(bundleContext, OutgoingPort.class, portId);
        } catch (OsgiServiceNotAvailableException e1) {
            throw new PortNotAvailableException("Port with id " + portId + " not available", e1);
        }
        MethodCall call = new MethodCall("processRemoteEvent", new Object[]{ e }, metaData);
        port.send(destination, call);
    }

    public void sendRemoteEvent(String portId, String destination, RemoteEvent e, String serviceId)
        throws PortNotAvailableException {
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("serviceId", serviceId);
        sendRemoteEvent(portId, destination, e, metaData);
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setContextService(ContextCurrentService contextService) {
        this.contextService = contextService;
    }

}
