/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */

package org.openengsb.core.methodcalltransformation;

import java.util.HashMap;
import java.util.Map;

class PrimitiveTypes {

    private static Map<String, Class<?>> primitiveTypes = new HashMap<String, Class<?>>();

    static {
        primitiveTypes.put("byte", byte.class);
        primitiveTypes.put("short", short.class);
        primitiveTypes.put("int", int.class);
        primitiveTypes.put("long", long.class);
        primitiveTypes.put("char", char.class);
        primitiveTypes.put("float", float.class);
        primitiveTypes.put("double", double.class);
        primitiveTypes.put("boolean", boolean.class);
        primitiveTypes.put("java.lang.String", String.class);
        primitiveTypes.put("java.lang.Integer", Integer.class);
        primitiveTypes.put("java.lang.Byte", Byte.class);
        primitiveTypes.put("java.lang.Short", Short.class);
        primitiveTypes.put("java.lang.Long", Long.class);
        primitiveTypes.put("java.lang.Character", Character.class);
        primitiveTypes.put("java.lang.Float", Float.class);
        primitiveTypes.put("java.lang.Double", Double.class);
        primitiveTypes.put("java.lang.Boolean", Boolean.class);
    }

    private PrimitiveTypes() {
        throw new AssertionError();
    }

    static Object create(String type, String value) {
        if (type.equals("java.lang.String")) {
            return value;
        }

        if (type.equals("byte") || type.equals("java.lang.Byte")) {
            return Byte.valueOf(value);
        }

        if (type.equals("short") || type.equals("java.lang.Short")) {
            return Short.valueOf(value);
        }

        if (type.equals("int") || type.equals("java.lang.Integer")) {
            return Integer.valueOf(value);
        }

        if (type.equals("long") || type.equals("java.lang.Long")) {
            return Long.valueOf(value);
        }

        if (type.equals("char") || type.equals("java.lang.Character")) {
            if (value.length() > 1) {
                throw new IllegalArgumentException("Can't parse char");
            }
            return value.charAt(0);
        }

        if (type.equals("float") || type.equals("java.lang.Float")) {
            return Float.valueOf(value);
        }

        if (type.equals("double") || type.equals("java.lang.Double")) {
            return Double.valueOf(value);
        }

        if (type.equals("boolean") || type.equals("java.lang.Boolean")) {
            return Boolean.valueOf(value);
        }

        throw new IllegalStateException(String.format("Type '%s' is not supported", type));
    }

    static Class<?> get(String key) {
        return primitiveTypes.get(key);
    }

    static boolean contains(Class<?> type) {
        return primitiveTypes.containsValue(type);
    }

}
