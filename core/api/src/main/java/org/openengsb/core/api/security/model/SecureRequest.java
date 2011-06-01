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

import org.openengsb.core.api.model.BeanDescription;
import org.openengsb.core.api.remote.MethodCallRequest;

/**
 * serves as a container for a {@link MethodCallRequest} and adds attributes relevant for security. Namely
 * verification-information handled by {@link AbstractSecureMessage} and authenticationInformation.
 *
 */
public class SecureRequest extends AbstractSecureMessage<MethodCallRequest> {

    private static final long serialVersionUID = -2350090113804167120L;

    private BeanDescription authenticationData;

    public static SecureRequest create(MethodCallRequest original, BeanDescription auth) {
        SecureRequest secureRequest = new SecureRequest();
        secureRequest.setMessage(original);
        long time = System.currentTimeMillis();
        secureRequest.setTimestamp(time);
        secureRequest.setAuthenticationData(auth);
        return secureRequest;
    }

    public void setAuthenticationData(BeanDescription authentiationData) {
        this.authenticationData = authentiationData;
    }

    public BeanDescription getAuthenticationData() {
        return authenticationData;
    }

    public AuthenticationInfo retrieveAuthenticationInfo() {
        return authenticationData.toObject(AuthenticationInfo.class);
    }

    protected SecureRequest() {
    }

}
