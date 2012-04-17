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

package org.openengsb.core.api.remote;

import java.io.Serializable;
import java.util.UUID;

import com.google.common.base.Objects;

/**
 * Representation of a most general method call containing a {@link #methodName}, {@link #args} you want to give to the
 * method and so called {@link #metaData}. Since the target system often requires additional information for calling
 * specific methods (e.g. context setup, target thread, security, active user, ...) it is allowed to add additional
 * information to each method call to make. Finally this abstraction can extract all {@link Class} objects in the
 * {@link #getClasses()} required to load this method call correctly into the class loader. The classes are used to
 * identify the right method.
 */
public class MethodCallRequest implements Serializable {

    private static final long serialVersionUID = -484867025274841475L;

    private MethodCall methodCall;
    private String callId;
    private boolean answer;
    private String destination;

    public MethodCallRequest() {
    }

    public MethodCallRequest(MethodCall methodCall, String callId) {
        this(methodCall, callId, true);
    }

    public MethodCallRequest(MethodCall methodCall) {
        this(methodCall, UUID.randomUUID().toString());
    }

    public MethodCallRequest(MethodCall methodCall, boolean answer) {
        this(methodCall, UUID.randomUUID().toString(), answer);
    }

    public MethodCallRequest(MethodCall methodCall, String callId, boolean answer) {
        this.methodCall = methodCall;
        this.callId = callId;
        this.answer = answer;
    }

    public MethodCall getMethodCall() {
        return methodCall;
    }

    public void setMethodCall(MethodCall methodCall) {
        this.methodCall = methodCall;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public boolean isAnswer() {
        return answer;
    }

    public void setAnswer(boolean answer) {
        this.answer = answer;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("callId", callId)
            .add("destination", destination)
            .add("answer", answer)
            .add("methodCall", methodCall)
            .toString();
    }
}
