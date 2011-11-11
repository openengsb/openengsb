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

package org.openengsb.core.security.filter;

import java.util.LinkedList;
import java.util.List;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.openengsb.core.common.remote.RequestMapperFilter;
import org.springframework.security.authentication.AuthenticationManager;

/**
 * /** This factory wraps an existing {@link RequestHandler} to a secure one to be used as final element in a
 * {@link org.openengsb.core.common.remote.FilterChain}.
 * 
 * The resulting FilterAction takes care of message verification and authentication.
 * 
 * DEPRECATED: use manual configuration instead.
 * 
 * This code replaces this class in a blueprint definition:
 * 
 * <pre>
 * {@code
 * <value>org.openengsb.core.security.filter.MessageVerifierFilter</value>
 * <bean id="authenticationFilterFactory" class="org.openengsb.core.security.filter.MessageAuthenticatorFactory">
 *   <property name="authenticationManager" ref="authenticationManager" />
 * </bean>
 * <value>org.openengsb.core.security.filter.WrapperFilter</value>
 * <bean class="org.openengsb.core.common.remote.RequestMapperFilter">
 *   <property name="requestHandler" ref="requestHandler" />
 * </bean>}
 * </pre>
 */

@Deprecated
public class DefaultSecureMethodCallFilterFactory {

    private AuthenticationManager authenticationManager;
    private RequestHandler requestHandler;

    public DefaultSecureMethodCallFilterFactory() {
    }

    public FilterAction create() throws FilterConfigurationException {
        FilterChainFactory<SecureRequest, SecureResponse> factory =
            new FilterChainFactory<SecureRequest, SecureResponse>(SecureRequest.class, SecureResponse.class);
        List<Object> filterFactories = createFilterList();
        factory.setFilters(filterFactories);
        return factory.create();
    }

    private List<Object> createFilterList() {
        List<Object> filterFactories = new LinkedList<Object>();
        filterFactories.add(MessageVerifierFilter.class);
        filterFactories.add(createMessageAuthenticationFactory());
        filterFactories.add(WrapperFilter.class);
        filterFactories.add(new RequestMapperFilter(requestHandler));
        return filterFactories;
    }

    private MessageAuthenticatorFactory createMessageAuthenticationFactory() {
        MessageAuthenticatorFactory messageAuthenticatorFactory = new MessageAuthenticatorFactory();
        messageAuthenticatorFactory.setAuthenticationManager(authenticationManager);
        return messageAuthenticatorFactory;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

}
