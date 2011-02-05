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

package org.openengsb.ui.web.model;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class MethodId implements Serializable {
    private String name;
    private List<String> argumentTypes = new ArrayList<String>();

    private static final Map<String, Class<?>> PRIMITIVES = new HashMap<String, Class<?>>();
    {
        PRIMITIVES.put("int", int.class);
        PRIMITIVES.put("long", long.class);
        PRIMITIVES.put("boolean", boolean.class);
        PRIMITIVES.put("byte", byte.class);
        PRIMITIVES.put("short", short.class);
        PRIMITIVES.put("char", char.class);
        PRIMITIVES.put("float", float.class);
        PRIMITIVES.put("double", double.class);
        PRIMITIVES.put("void", void.class);
    }

    public MethodId(Method method) {
        setName(method.getName());
        for (Class<?> type : method.getParameterTypes()) {
            argumentTypes.add(type.getName());
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getArgumentTypes() {
        return this.argumentTypes;
    }

    public void setArgumentTypes(List<String> argumentTypes) {
        this.argumentTypes = argumentTypes;
    }

    public Class<?>[] getArgumentTypesAsClasses() {
        Class<?>[] result = new Class<?>[argumentTypes.size()];
        int i = 0;
        for (String s : argumentTypes) {
            try {
                result[i] = PRIMITIVES.get(s);
                if (result[i] == null) {
                    result[i] = Class.forName(s);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            i++;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(name);
        result.append('(');
        boolean first = true;
        for (String s : argumentTypes) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            result.append(s);
        }
        result.append(')');
        return result.toString();
    }

}
