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

import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

import org.openengsb.core.api.model.BeanDescription;

/**
 * Container for sending {@link MethodCall}s to remote destinations. It contains the necessary routing- and
 * security-information.
 */
@XmlRootElement
public class MethodCallMessage extends MessageBase {

    private static final long serialVersionUID = -484867025274841475L;

    private MethodCall methodCall;
    private boolean answer;
    private String destination;

    private String principal;
    private BeanDescription credentials;

    public MethodCallMessage() {
    }

    public MethodCallMessage(MethodCall methodCall) {
        this(methodCall, UUID.randomUUID().toString());
    }

    public MethodCallMessage(MethodCall methodCall, String callId) {
        this(methodCall, callId, true);
    }

    public MethodCallMessage(MethodCall methodCall, boolean answer) {
        this(methodCall, UUID.randomUUID().toString(), answer);
    }

    public MethodCallMessage(MethodCall methodCall, String callId, boolean answer) {
        super(callId);
        this.methodCall = methodCall;
        this.answer = answer;
    }

    public MethodCall getMethodCall() {
        return methodCall;
    }

    public void setMethodCall(MethodCall methodCall) {
        this.methodCall = methodCall;
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

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public BeanDescription getCredentials() {
        return credentials;
    }

    public void setCredentials(BeanDescription credentials) {
        this.credentials = credentials;
    }

}
