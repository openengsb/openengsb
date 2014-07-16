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

package org.openengsb.core.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.List;

import org.apache.commons.lang.reflect.MethodUtils;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Preconditions;

public final class JsonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectMapper MODEL_MAPPER = new ObjectMapper();

    static {
        // adding the additional deserializer needed to deserialize models
        MODEL_MAPPER.registerModule(new SimpleModule().addDeserializer(Object.class,
            new OpenEngSBModelEntryDeserializer()));
    }

    /**
     * Converts an object in JSON format to the given class. Throws an IOException if the conversion could not be
     * performed.
     */
    public static <T> T convertObject(String json, Class<T> clazz) throws IOException {
        try {
            if (clazz.isAnnotationPresent(Model.class)) {
                return MODEL_MAPPER.readValue(json, clazz);
            }
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            String error = String.format("Unable to parse given json '%s' into class '%s'.", json, clazz.getName());
            LOGGER.error(error, e);
            throw new IOException(error, e);
        }
    }

    private static Object convertArgument(String className, Object arg) {
        try {
            Class<?> type = findType(className);
            if (type.isAnnotationPresent(Model.class)) {
                return MODEL_MAPPER.convertValue(arg, type);
            }
            return MAPPER.convertValue(arg, type);
        } catch (ClassNotFoundException e) {
            LOGGER.error("could not convert argument " + arg, e);
            return arg;
        }
    }

    private static Class<?> findType(String className) throws ClassNotFoundException {
        if (className.startsWith("[L")) {
            Class<?> componentType = findType(className.substring(2, className.length() - 1));
            return Array.newInstance(componentType, 0).getClass();
        }
        return JsonUtils.class.getClassLoader().loadClass(className);
    }

    public static void convertAllArgs(MethodCall call) {
        Object[] args = call.getArgs();
        List<String> classes = call.getClasses();
        Preconditions.checkArgument(args.length == classes.size());
        for (int i = 0; i < args.length; i++) {
            args[i] = convertArgument(classes.get(i), args[i]);
        }
    }

    public static void convertResult(MethodResult result) {
        Object convertArgument = convertArgument(result.getClassName(), result.getArg());
        result.setArg(convertArgument);
    }

    public static void convertAllArgs(MethodCallMessage request) {
        convertAllArgs(request.getMethodCall());
    }

    public static void convertResult(MethodResultMessage message) {
        convertResult(message.getResult());
    }

    public static ObjectMapper createObjectMapperWithIntroSpectors() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector primaryIntrospector = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondaryIntrospector = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        AnnotationIntrospector introspector =
            new AnnotationIntrospectorPair(primaryIntrospector, secondaryIntrospector);
        mapper.getDeserializationConfig().withAppendedAnnotationIntrospector(introspector);
        mapper.getSerializationConfig().withAppendedAnnotationIntrospector(introspector);
        return mapper;
    }

    private JsonUtils() {
    }

    /**
     * The OpenEngSBModelEntryDeserializer class is needed in order to be able to transform the list of
     * OpenEngSBModelEntry elements, which is contained in every model tail, from a JSON string into a list of actual
     * elements.
     */
    @SuppressWarnings("serial")
    private static class OpenEngSBModelEntryDeserializer extends StdScalarDeserializer<OpenEngSBModelEntry> {
        public OpenEngSBModelEntryDeserializer() {
            super(OpenEngSBModelEntry.class);
        }

        @Override
        public OpenEngSBModelEntry deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonToken token = jp.getCurrentToken();
            OpenEngSBModelEntry entry = new OpenEngSBModelEntry();
            if (token != JsonToken.START_OBJECT) {
                return null;
            } else {
                // skip the JsonToken.START_OBJECT token
                token = jp.nextValue();
            }
            do {
                if (token == JsonToken.END_OBJECT) {
                    Object value = createValueOfEntry(entry);
                    if (value != null) {
                        entry.setValue(value);
                    }
                    return entry;
                } else {
                    if (jp.getCurrentName().equals("key")) {
                        entry.setKey(jp.getValueAsString());
                    } else if (jp.getCurrentName().equals("value")) {
                        entry.setValue(jp.getValueAsString());
                    } else if (jp.getCurrentName().equals("type")) {
                        try {
                            entry.setType(findType(jp.getValueAsString()));
                        } catch (ClassNotFoundException e) {
                            LOGGER.error("Did not find class of type " + jp.getValueAsString(), e);
                            break;
                        }
                    }
                }
                token = jp.nextValue();
            } while (token != null);
            return null;
        }

        /**
         * Converts the string located in the value property of the entry in the correct data format and returns it.
         */
        private Object createValueOfEntry(OpenEngSBModelEntry entry) {
            if (entry.getType().equals(String.class)) {
                return entry.getValue();
            }
            Object element = null;
            if (entry.getType() == null) {
                LOGGER.error("Unknown type for model entry with key {}", entry.getKey());
                return element;
            }
            try {
                Class<?> clazz = entry.getType();
                Constructor<?> constr = ClassUtils.getConstructorIfAvailable(clazz, String.class);
                if (constr != null) {
                    element = constr.newInstance(entry.getValue());
                } else {
                    element = MethodUtils.invokeStaticMethod(clazz, "valueOf", entry.getValue());
                }
            } catch (Exception e) {
                LOGGER.error("Unable to convert value with the key {} to correct type", entry.getKey());
            }
            return element;
        }
    }
}
