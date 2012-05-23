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

import java.util.Map;

import org.apache.shiro.authc.AuthenticationException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.AbstractFilterChainElement;
import org.openengsb.core.security.SecurityContext;
import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.DelegationUtil;
import org.osgi.framework.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter does no actual transformation. It takes a {@link SecureRequest} extracts the
 * {@link org.apache.shiro.authc.AuthenticationInfo} and tries to authenticate. If authentication was successful, the
 * filter-chain will proceed. The result of the next filter is just passed through.
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

    private OsgiUtilsService utilsService;
    private FilterAction next;

    public MessageAuthenticatorFilter(OsgiUtilsService utilsService) {
        this.utilsService = utilsService;
    }

    @Override
    protected SecureResponse doFilter(SecureRequest input, Map<String, Object> metaData) {
        LOGGER.info("recieved authentication info: " + input.getPrincipal() + " " + input.getCredentials());

        String className = input.getCredentials().getClassName();
        Class<? extends Credentials> credentialType;
        try {
            credentialType = loadCredentialsType(className);
        } catch (ClassNotFoundException e) {
            throw new FilterException(e);
        }
        try {
            SecurityContext.login(input.getPrincipal(), input.getCredentials().toObject(credentialType));
        } catch (AuthenticationException e) {
            throw new FilterException(e);
        }

        LOGGER.info("authenticated {}", input.getPrincipal());
        return (SecureResponse) next.filter(input, metaData);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Credentials> loadCredentialsType(String className)
        throws ClassNotFoundException {
        Filter filter = DelegationUtil.createClassProviderFilter(className);
        return (Class<? extends Credentials>) utilsService.getOsgiServiceProxy(filter, ClassProvider.class)
            .loadClass(className);
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, SecureRequest.class, SecureResponse.class);
        this.next = next;
    }

}
