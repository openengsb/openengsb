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

    private static Map<String, Converter> converter = new HashMap<String, Converter>();

    static {
        primitiveTypes.put("byte", byte.class);
        primitiveTypes.put("short", short.class);
        primitiveTypes.put("int", int.class);
        primitiveTypes.put("long", long.class);
        primitiveTypes.put("char", char.class);
        primitiveTypes.put("float", float.class);
        primitiveTypes.put("double", double.class);
        primitiveTypes.put("boolean", boolean.class);
        primitiveTypes.put("void", void.class);
        primitiveTypes.put("java.lang.String", String.class);
        primitiveTypes.put("java.lang.Integer", Integer.class);
        primitiveTypes.put("java.lang.Byte", Byte.class);
        primitiveTypes.put("java.lang.Short", Short.class);
        primitiveTypes.put("java.lang.Long", Long.class);
        primitiveTypes.put("java.lang.Character", Character.class);
        primitiveTypes.put("java.lang.Float", Float.class);
        primitiveTypes.put("java.lang.Double", Double.class);
        primitiveTypes.put("java.lang.Boolean", Boolean.class);

        converter.put("byte", new Converter() {
            @Override
            public Object convert(String value) {
                return Byte.valueOf(value);
            }
        });

        converter.put("short", new Converter() {
            @Override
            public Object convert(String value) {
                return Short.valueOf(value);
            }
        });

        converter.put("int", new Converter() {
            @Override
            public Object convert(String value) {
                return Integer.valueOf(value);
            }
        });

        converter.put("long", new Converter() {
            @Override
            public Object convert(String value) {
                return Long.valueOf(value);
            }
        });

        converter.put("char", new Converter() {
            @Override
            public Object convert(String value) {
                if (value.length() > 1) {
                    throw new IllegalArgumentException("Can't parse char");
                }
                return value.charAt(0);
            }
        });

        converter.put("float", new Converter() {
            @Override
            public Object convert(String value) {
                return Float.valueOf(value);
            }
        });

        converter.put("double", new Converter() {
            @Override
            public Object convert(String value) {
                return Double.valueOf(value);
            }
        });

        converter.put("boolean", new Converter() {
            @Override
            public Object convert(String value) {
                return Boolean.valueOf(value);
            }
        });

        converter.put("void", new Converter() {
            @Override
            public Object convert(String value) {
                return null;
            }
        });

        converter.put("java.lang.String", new Converter() {
            @Override
            public Object convert(String value) {
                return value;
            }
        });

        converter.put("java.lang.Byte", converter.get("byte"));
        converter.put("java.lang.Short", converter.get("short"));
        converter.put("java.lang.Integer", converter.get("int"));
        converter.put("java.lang.Long", converter.get("long"));
        converter.put("java.lang.Character", converter.get("char"));
        converter.put("java.lang.Float", converter.get("float"));
        converter.put("java.lang.Double", converter.get("double"));
        converter.put("java.lang.Boolean", converter.get("boolean"));
        converter.put("java.lang.Void", converter.get("void"));
    }

    private PrimitiveTypes() {
        throw new AssertionError();
    }

    static Object create(String type, String value) {
        Converter conv = converter.get(type);

        if (conv == null) {
            throw new IllegalStateException(String.format("Type '%s' is not supported", type));
        }

        return conv.convert(value);
    }

    static interface Converter {
        Object convert(String value);
    }

    static Class<?> get(String key) {
        return primitiveTypes.get(key);
    }

    static boolean contains(Class<?> type) {
        return primitiveTypes.containsValue(type);
    }
}
