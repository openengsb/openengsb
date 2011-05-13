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

package org.openengsb.core.common;

import java.util.HashMap;

import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.remote.RemoteCommunicationException;
import org.openengsb.core.common.remote.FilterChain;

public class OutgoingPortImpl implements OutgoingPort {

    private FilterChain filterChain;

    @Override
    public void send(MethodCallRequest call) throws RemoteCommunicationException {
        HashMap<String, Object> metaData = getMetaDataMap(call);
        filterChain.filter(call, metaData);
    }

    private HashMap<String, Object> getMetaDataMap(MethodCallRequest call) {
        HashMap<String, Object> metaData = new HashMap<String, Object>();
        metaData.put("callId", call.getCallId());
        metaData.put("destination", call.getDestination());
        return metaData;
    }

    @Override
    public MethodResultMessage sendSync(MethodCallRequest call) throws RemoteCommunicationException {
        HashMap<String, Object> metaData = getMetaDataMap(call);
        metaData.put("answer", true);
        return (MethodResultMessage) filterChain.filter(call, metaData);
    }

    public void setFilterChain(FilterChain filterChain) {
        this.filterChain = filterChain;
    }

}
