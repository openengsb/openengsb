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
package org.openengsb.ui.common;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.openengsb.core.api.security.SecurityAttribute;
import org.openengsb.core.api.security.SecurityAttributes;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.SpringSecurityContext;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.domain.authorization.AuthorizationDomain.Access;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class DomainAuthorizationStrategy implements IAuthorizationStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainAuthorizationStrategy.class);

    private AuthorizationDomain authorizer = OpenEngSBCoreServices.getWiringService().getDomainEndpoint(
        AuthorizationDomain.class, "authorization");

    @Override
    public boolean isActionAuthorized(Component arg0, Action arg1) {
        SecurityAttribute[] attributes = getSecurityAttributes(arg0.getClass());
        if (attributes == null) {
            return true;
        }
        Authentication authentication = SpringSecurityContext.getInstance().getAuthentication();
        if (authentication == null) {
            return false;
        }
        String user = authentication.getUsername();
        return authorizer.checkAccess(user,
            ImmutableMap.of("securityAttributes", attributes, "component", arg0, "action", arg1)) == Access.GRANTED;
    }

    private SecurityAttribute[] getSecurityAttributes(Class<? extends Component> componentClass) {
        SecurityAttribute annotation = componentClass.getAnnotation(SecurityAttribute.class);
        if (annotation != null) {
            return new SecurityAttribute[]{ annotation };
        }
        SecurityAttributes annotation2 = componentClass.getAnnotation(SecurityAttributes.class);
        if (annotation2 != null) {
            return annotation2.value();
        }
        return null;
    }

    @Override
    public <T extends Component> boolean isInstantiationAuthorized(Class<T> componentClass) {
        SecurityAttribute annotation = componentClass.getAnnotation(SecurityAttribute.class);
        if (annotation == null) {
            LOGGER.debug("security-attribute-annotation NOT present on {}", componentClass);
            return true;
        }
        LOGGER.trace("security-attribute-annotation present on {}", componentClass);
        Authentication authentication = SpringSecurityContext.getInstance().getAuthentication();
        if (authentication == null) {
            return false;
        }
        String user = authentication.getUsername();
        return authorizer.checkAccess(user, componentClass) == Access.GRANTED;
    }

    public void setAuthorizer(AuthorizationDomain authorizer) {
        this.authorizer = authorizer;
    }
}
