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

package org.openengsb.core.common.security.filter;

import java.util.Map;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.security.model.AuthenticationInfo;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.AbstractFilterChainElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * This filter does no actual transformation. It takes a {@link SecureRequest} extracts the
 * {@link AuthenticationInfo} and tries to authenticate. If authentication was succesful, the filter-chain will proceed.
 * The result of the next filter is just passed through.
 *
 * This filter is intended for incoming ports.
 *
 * <code>
 * <pre>
 *      [SecureRequest]  > Filter > [SecureRequest]    > ...
 *                                                        |
 *                                                        v
 *      [SecureResponse] < Filter < [SecureResponse]   < ...
 * </pre>
 * </code>
 */
public class MessageAuthenticatorFilter extends AbstractFilterChainElement<SecureRequest, SecureResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageAuthenticatorFilter.class);

    private FilterAction next;
    private AuthenticationManager authenticationManager;

    public MessageAuthenticatorFilter(AuthenticationManager authenticationManager) {
        super(SecureRequest.class, SecureResponse.class);
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected SecureResponse doFilter(SecureRequest input, Map<String, Object> metaData) {
        AuthenticationInfo authenticationInfo = input.retrieveAuthenticationInfo();
        LOGGER.debug("recieved authentication info: " + authenticationInfo);
        Authentication authentication = authenticationInfo.toSpringSecurityAuthentication();
        LOGGER.debug("trying to authenticate using spring-security authentication-manager");
        Authentication authenticated = authenticationManager.authenticate(authentication);
        LOGGER.debug("authenticated: {}", authenticated.isAuthenticated());
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        return (SecureResponse) next.filter(input, metaData);
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, SecureRequest.class, SecureResponse.class);
        this.next = next;
    }
}
