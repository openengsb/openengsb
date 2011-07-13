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

import java.lang.reflect.Method;
import java.util.Collection;

import org.aopalliance.intercept.MethodInvocation;
import org.openengsb.core.api.security.Public;
import org.openengsb.core.security.internal.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class PublicAnnotationVoter extends AbstractAccessDecisionVoter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicAnnotationVoter.class);

    private static final Predicate<Method> PREDICATE = new Predicate<Method>() {
        @Override
        public boolean apply(Method input) {
            return input.isAnnotationPresent(Public.class);
        };
    };

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {

        MethodInvocation invocation = (MethodInvocation) object;
        if (Iterables.any(ReflectionUtils.getAllMethodDeclarations(invocation), PREDICATE)) {
            LOGGER.trace("granting access for {} because it is annotated @Public", invocation);
            return ACCESS_GRANTED;
        }
        return ACCESS_ABSTAIN;
    }
}
