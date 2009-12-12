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
    }

    private PrimitiveTypes() {
        throw new AssertionError();
    }

    static Class<?> get(String key) {
        return primitiveTypes.get(key);
    }

    static boolean contains(Class<?> type) {
        return primitiveTypes.containsValue(type);
    }

}
