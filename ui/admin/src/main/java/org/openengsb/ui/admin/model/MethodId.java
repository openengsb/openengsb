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

package org.openengsb.ui.admin.model;

import java.io.Serializable;
import java.lang.reflect.Method;

@SuppressWarnings("serial")
public class MethodId implements Serializable {
    private String name;
    private Class<?>[] argumentTypes;

    public MethodId(Method method) {
        name = method.getName();
        argumentTypes = method.getParameterTypes();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?>[] getArgumentTypes() {
        return argumentTypes;
    }

    public void setArgumentTypes(Class<?>[] argumentTypes) {
        this.argumentTypes = argumentTypes;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(name);
        result.append('(');
        boolean first = true;
        for (Class<?> s : argumentTypes) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            result.append(s.getSimpleName());
        }
        result.append(')');
        return result.toString();
    }

}
