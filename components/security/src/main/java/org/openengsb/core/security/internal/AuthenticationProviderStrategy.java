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

import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.common.AbstractDelegateStrategy;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public class AuthenticationProviderStrategy extends AbstractDelegateStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationProviderStrategy.class);

    private OsgiUtilsService utilsService;

    public AuthenticationProviderStrategy() {
    }

    public AuthenticationProviderStrategy(OsgiUtilsService utilsService) {
        this.utilsService = utilsService;
    }

    private static class CompositeAuthenticationProvider implements AuthenticationProvider {
        private List<ServiceReference> providers;
        private OsgiUtilsService utilsService;

        public CompositeAuthenticationProvider(List<ServiceReference> providers, OsgiUtilsService utilsService) {
            this.providers = providers;
            this.utilsService = utilsService;
        }

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            Iterator<AuthenticationProvider> serviceIterator =
                utilsService.getServiceIterator(providers, AuthenticationProvider.class);
            AuthenticationException lastException = null;
            LOGGER.debug("iterating {} authenticationProviderServices", providers.size());
            while (serviceIterator.hasNext()) {
                AuthenticationProvider provider = serviceIterator.next();
                if (provider.supports(authentication.getClass())) {
                    LOGGER.info("attempting authentication using provider {}", provider.getClass());
                    try {
                        return provider.authenticate(authentication);
                    } catch (AuthenticationException e) {
                        lastException = e;
                    }
                }
            }

            if (lastException == null) {
                lastException =
                    new ProviderNotFoundException("No AuthenticationProvider found, that supports "
                            + authentication.getClass());
            }
            throw lastException;
        }

        @Override
        public boolean supports(final Class<? extends Object> authentication) {
            Iterator<AuthenticationProvider> serviceIterator =
                utilsService.getServiceIterator(providers,
                    AuthenticationProvider.class);
            return Iterators.any(serviceIterator, new Predicate<AuthenticationProvider>() {
                @Override
                public boolean apply(AuthenticationProvider input) {
                    return input.supports(authentication);
                }
            });
        }

    }

    @Override
    protected Object createDelegate(List<ServiceReference> services) {
        return new CompositeAuthenticationProvider(services, utilsService);
    }

    @Override
    public boolean supports(Class<?> domainClass) {
        return AuthenticationProvider.class.isAssignableFrom(domainClass);
    }

    public void setUtilsService(OsgiUtilsService utilsService) {
        this.utilsService = utilsService;
    }

}
