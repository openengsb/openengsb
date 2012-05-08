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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.remote.OutgoingPortUtilService;

public class DefaultOutgoingPortUtilService implements OutgoingPortUtilService {

    private OsgiUtilsService utilsService;

    public DefaultOutgoingPortUtilService() {
    }
    
    public DefaultOutgoingPortUtilService(OsgiUtilsService utilsService) {
        this.utilsService = utilsService;
    }

    private final class SendMethodCallTask implements Runnable {
        private final OutgoingPort port;
        private final MethodCallMessage request;

        private SendMethodCallTask(OutgoingPort port, MethodCallMessage request) {
            this.port = port;
            this.request = request;
        }

        @Override
        public void run() {
            port.send(request);
        }
    }

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void sendMethodCall(String portId, String destination, MethodCall call) {
        OutgoingPort port = getPort(portId);
        MethodCallMessage request = new MethodCallMessage(call, false);
        request.setDestination(destination);
        Runnable callHandler = new SendMethodCallTask(port, request);
        executor.execute(callHandler);
    }

    @Override
    public MethodResult sendMethodCallWithResult(String portId, String destination, MethodCall call) {
        OutgoingPort port = getPort(portId);
        MethodCallMessage request = new MethodCallMessage(call, true);
        request.setDestination(destination);
        MethodResultMessage requestResult = port.sendSync(request);
        return requestResult.getResult();
    }

    private OutgoingPort getPort(String portId) throws OsgiServiceNotAvailableException {
        return utilsService.getServiceWithId(OutgoingPort.class, portId);
    }
    
    public void setUtilsService(OsgiUtilsService utilsService) {
        this.utilsService = utilsService;
    }

}
