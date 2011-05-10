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

/**
 * This object wraps the return values of a remote method call. The different types which could be returned are stored
 * in {@link #type}. The object itself is available via {@link #getArg()}. Since this is the result of an remote call it
 * is possible that additional meta-data was added (describing e.g. context, username, ...) could have been added to
 * this message. Those could be retrieved via {@link #getMetaData()}.
 */
public class MethodResultMessage {

    private MethodResult result;
    private String callId;

    public MethodResultMessage() {
    }

    public MethodResultMessage(MethodResult result, String callId) {
        this.result = result;
        this.callId = callId;
    }

    public MethodResult getResult() {
        return result;
    }

    public void setResult(MethodResult result) {
        this.result = result;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

}
