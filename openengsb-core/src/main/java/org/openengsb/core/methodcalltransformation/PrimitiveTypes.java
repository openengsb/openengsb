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
