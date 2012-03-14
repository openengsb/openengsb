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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.ArrayUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.openengsb.core.api.security.service.AccessDeniedException;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.domain.authorization.AuthorizationDomain.Access;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This interceptor is used to enforce access control on services. Secure services are supposed to be advised with this
 * interceptor.
 * 
 * Either the {@link org.openengsb.core.api.ConnectorRegistrationManager} or the developer himself must take care of
 * this. To configure an advice in blueprint you may use {@link org.openengsb.core.common.ProxyFactoryBean}.
 */
public class SecurityInterceptor implements MethodInterceptor {

    private AuthorizationDomain authorizer;

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityInterceptor.class);

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        LOGGER.debug("intercepting method {}", mi.getMethod());
        // don't control access to Object-methods like toString or hashcode
        if (ArrayUtils.contains(Object.class.getMethods(), mi.getMethod())) {
            LOGGER.info("is Object-method; skipping");
            return mi.proceed();
        }
        Subject subject = ThreadContext.getSubject();
        if (subject == null || !subject.isAuthenticated()) {
            throw new AccessDeniedException("no authentication was found in context");
        }

        if (subject.getPrincipal().getClass().equals(Object.class)) {
            // this action is executed in a root-context
            return mi.proceed();
        }

        String username = (String) subject.getPrincipal();
        Access decisionResult = authorizer.checkAccess(username, mi);
        if (decisionResult != Access.GRANTED) {
            LOGGER.warn("Access denied because result was {}", decisionResult);
            throw new AccessDeniedException();
        }
        LOGGER.debug("Access was granted");
        return mi.proceed();
    }

    public void setAuthorizer(AuthorizationDomain authorizer) {
        this.authorizer = authorizer;
    }

}
