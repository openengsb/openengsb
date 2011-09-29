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
package org.openengsb.core.security.internal;

import java.util.Iterator;
import java.util.List;

import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.common.AbstractDelegateStrategy;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.domain.authentication.AuthenticationException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public class AuthenticationProviderStrategy extends AbstractDelegateStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationProviderStrategy.class);

    private static class CompositeAuthenticationProvider extends AbstractOpenEngSBConnectorService implements
            AuthenticationDomain {
        private List<ServiceReference> providers;

        public CompositeAuthenticationProvider(List<ServiceReference> providers) {
            this.providers = providers;
        }

        @Override
        public Authentication authenticate(String username, Credentials credentials) throws AuthenticationException {
            Iterator<AuthenticationDomain> serviceIterator =
                OpenEngSBCoreServices.getServiceUtilsService().getServiceIterator(providers,
                    AuthenticationDomain.class);
            AuthenticationException lastException = null;
            LOGGER.debug("iterating {} authenticationProviderServices", providers.size());
            while (serviceIterator.hasNext()) {
                AuthenticationDomain provider = serviceIterator.next();
                if (provider.supports(credentials)) {
                    LOGGER.info("attempting authentication using provider {}", provider.getClass());
                    try {
                        return provider.authenticate(username, credentials);
                    } catch (AuthenticationException e) {
                        lastException = e;
                    }
                }
            }

            if (lastException == null) {
                lastException =
                    new AuthenticationException("No AuthenticationProvider found, that supports " + credentials);
            }
            throw lastException;
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.ONLINE;
        }

        @Override
        public boolean supports(final Credentials credentials) {
            Iterator<AuthenticationDomain> serviceIterator =
                OpenEngSBCoreServices.getServiceUtilsService().getServiceIterator(providers,
                    AuthenticationDomain.class);
            return Iterators.any(serviceIterator, new Predicate<AuthenticationDomain>() {
                @Override
                public boolean apply(AuthenticationDomain input) {
                    return input.supports(credentials);
                }
            });
        }

    }

    @Override
    protected Object createDelegate(List<ServiceReference> services) {
        return new CompositeAuthenticationProvider(services);
    }

    @Override
    public boolean supports(Class<?> domainClass) {
        return AuthenticationDomain.class.isAssignableFrom(domainClass);
    }

}
