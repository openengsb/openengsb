/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.security;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;

public class SecurityInterceptor extends MethodSecurityInterceptor {

    private Log log = LogFactory.getLog(SecurityInterceptor.class);

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        log.info("intercepting method " + mi.getMethod());
        if (ArrayUtils.contains(Object.class.getMethods(), mi.getMethod())) {
            logger.info("is Object-method; skipping");
            return mi.proceed();
        }
        return super.invoke(mi);
    }

    @Override
    protected InterceptorStatusToken beforeInvocation(Object object) {
        MethodInvocation o = (MethodInvocation) object;

        return super.beforeInvocation(object);
    }

}
