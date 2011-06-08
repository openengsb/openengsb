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

package org.openengsb.core.api.security.model;

import org.openengsb.core.api.remote.MethodResultMessage;

/**
 * wraps the result of a MethodCall to a response-object containing security-relevant information handled by
 * {@link AbstractSecureMessage}
 */
public class SecureResponse extends AbstractSecureMessage<MethodResultMessage> {

    private static final long serialVersionUID = 4052853600342752517L;

    protected SecureResponse() {
    }

    public static SecureResponse create(MethodResultMessage original) {
        SecureResponse secureResponse = new SecureResponse();
        secureResponse.setMessage(original);
        long time = System.currentTimeMillis();
        secureResponse.setTimestamp(time);
        return secureResponse;
    }
}
