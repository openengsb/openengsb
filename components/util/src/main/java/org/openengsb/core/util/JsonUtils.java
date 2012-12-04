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

import java.lang.reflect.Array;
import java.util.List;

import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Preconditions;

public final class JsonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static Object convertArgument(String className, Object arg) {
        try {
            Class<?> type = findType(className);
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
}
