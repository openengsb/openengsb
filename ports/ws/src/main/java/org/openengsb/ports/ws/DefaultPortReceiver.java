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

import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.remote.MethodReturn;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.common.marshaling.RequestMapping;
import org.openengsb.core.common.marshaling.ReturnMapping;

public class DefaultPortReceiver implements PortReceiver {

    private RequestHandler requestHandler;

    public DefaultPortReceiver() {
    }

    public DefaultPortReceiver(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public String receive(String message) {
        try {
            RequestMapping readValue = RequestMapping.createFromMessage(message);
            readValue.resetArgs();
            ContextHolder.get().setCurrentContextId(readValue.getMetaData().get("contextId"));
            MethodReturn handleCall = requestHandler.handleCall(readValue);
            String answer = new ReturnMapping(handleCall).convertToMessage();
            return answer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
