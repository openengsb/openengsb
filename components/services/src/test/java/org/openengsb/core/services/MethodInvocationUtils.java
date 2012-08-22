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
package org.openengsb.core.services;

import org.aopalliance.intercept.MethodInvocation;
import org.openengsb.core.services.internal.SimpleMethodInvocation;

public final class MethodInvocationUtils {

    public static MethodInvocation create(Object object, String methodName) {
        return new SimpleMethodInvocation(object, methodName);
    }

    public static MethodInvocation create(Object object, String methodName, Object... objects) {
        return new SimpleMethodInvocation(object, methodName, objects);
    }
    
    private MethodInvocationUtils() {
    }
}
