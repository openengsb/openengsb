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

import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.remote.RemoteCommunicationException;
import org.openengsb.core.common.remote.FilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutgoingPortImpl implements OutgoingPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutgoingPortImpl.class);

    private FilterChain filterChain;

    @Override
    public void send(MethodCallMessage call) throws RemoteCommunicationException {
        HashMap<String, Object> metaData = getMetaDataMap(call);
        LOGGER.info("sending methodcall {} away with metadata {}", call, metaData);
        filterChain.filter(call, metaData);
    }

    private HashMap<String, Object> getMetaDataMap(MethodCallMessage call) {
        HashMap<String, Object> metaData = new HashMap<String, Object>();
        metaData.put("callId", call.getCallId());
        metaData.put("destination", call.getDestination());
        return metaData;
    }

    @Override
    public MethodResultMessage sendSync(MethodCallMessage call) throws RemoteCommunicationException {
        HashMap<String, Object> metaData = getMetaDataMap(call);
        metaData.put("answer", true);
        LOGGER.info("sending methodcall {} away with metadata {}", call, metaData);
        return (MethodResultMessage) filterChain.filter(call, metaData);
    }

    public void setFilterChain(FilterChain filterChain) {
        this.filterChain = filterChain;
    }

}
