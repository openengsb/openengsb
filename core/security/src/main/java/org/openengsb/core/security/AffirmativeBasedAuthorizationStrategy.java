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
package org.openengsb.core.security;

import java.util.Iterator;
import java.util.List;

import org.openengsb.core.api.AliveState;
import org.openengsb.core.common.AbstractDelegateStrategy;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AffirmativeBasedAuthorizationStrategy extends AbstractDelegateStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AffirmativeBasedAuthorizationStrategy.class);

    private static class CompositeAccessControlProvider extends AbstractOpenEngSBConnectorService
            implements AuthorizationDomain {
        private List<ServiceReference> providers;

        public CompositeAccessControlProvider(List<ServiceReference> providers) {
            this.providers = providers;
        }

        @Override
        public Access checkAccess(final String user, Object action) {
            Iterator<AuthorizationDomain> serviceIterator =
                OpenEngSBCoreServices.getServiceUtilsService().getServiceIterator(providers,
                    AuthorizationDomain.class);
            LOGGER.debug("iterating {} authenticationProviderServices", providers.size());
            boolean granted = false;
            while (serviceIterator.hasNext()) {
                AuthorizationDomain provider = serviceIterator.next();
                Access checkAccess = provider.checkAccess(user, action);
                if (checkAccess == Access.GRANTED) {
                    granted = true;
                } else if (checkAccess == Access.DENIED) {
                    return Access.DENIED;
                }
            }
            return granted ? Access.GRANTED : Access.ABSTAINED;
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.ONLINE;
        }
    }

    @Override
    protected Object createDelegate(List<ServiceReference> services) {
        return new CompositeAccessControlProvider(services);
    }

    @Override
    public boolean supports(Class<?> domainClass) {
        return AuthorizationDomain.class.isAssignableFrom(domainClass);
    }

}
