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

package org.openengsb.core.common.remote;

import java.util.Map;

import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.RequestHandler;

/**
 * This filter takes a {@link MethodCallMessage} and handles it using a {@link RequestHandler}. The result is then
 * wrapped to a {@link MethodResultMessage} and returned.
 *
 * <code>
 * <pre>
 *      [MethodCallRequest]   > Filter > [MethodCall]
 *                                             |
 *                                             v
 *                                       RequestHandler
 *                                             |
 *                                             v
 *      [MethodResultMessage] < Filter < [MethodResult]
 * </pre>
 * </code>
 */
public class RequestMapperFilter extends AbstractFilterAction<MethodCallMessage, MethodResultMessage> {

    private RequestHandler requestHandler;

    public RequestMapperFilter() {
    }

    public RequestMapperFilter(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    protected MethodResultMessage doFilter(MethodCallMessage input, Map<String, Object> metadata) {
        metadata.put("callId", input.getCallId());
        metadata.put("answer", input.isAnswer());
        MethodResult result = requestHandler.handleCall(input.getMethodCall());
        return new MethodResultMessage(result, input.getCallId());
    }

    public void setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }
}
