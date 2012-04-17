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
package org.openengsb.core.common;

import java.io.IOException;
import java.util.Collection;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectMapper.DefaultTypeResolverBuilder;
import org.codehaus.jackson.map.ObjectMapper.DefaultTyping;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.SubtypeResolver;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.type.SimpleType;
import org.codehaus.jackson.type.JavaType;
import org.openengsb.core.api.remote.GenericObjectSerializer;
import org.openengsb.labs.delegation.service.Provide;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class JsonObjectSerializer implements GenericObjectSerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonObjectSerializer.class);

    private ObjectMapper mapper;
    private ObjectWriter writer;
    private BundleContext bundleContext;

    public JsonObjectSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    public JsonObjectSerializer() {
    }

    public void init() {
        mapper = createMapperWithDefaults(bundleContext);
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    @Override
    public byte[] serializeToByteArray(Object object) throws IOException {
        return writer.writeValueAsBytes(object);
    }

    @Override
    public String serializeToString(Object object) throws IOException {
        return writer.writeValueAsString(object);
    }

    @Override
    public <T> T parse(String data, Class<T> type) throws IOException {
        return mapper.readValue(data, type);
    }

    @Override
    public <T> T parse(byte[] data, Class<T> type) throws IOException {
        return mapper.readValue(data, type);
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private static ObjectMapper createMapperWithDefaults(BundleContext bundleContext) {
        ObjectMapper mapper = new ObjectMapper();
        DelegatedClassLoadingHelper classLoadingHelper = new DelegatedClassLoadingHelper(bundleContext);

        TypeResolverBuilder<?> typer = new DefaultTypeResolverBuilder(DefaultTyping.OBJECT_AND_NON_CONCRETE) {
            @Override
            public boolean useForType(JavaType t) {
                /*
                 * skip typing for containers
                 * 
                 * This is required to avoid inclusion of specific type for maps and other container types.
                 * 
                 * Example:
                 * 
                 * {
                 * 
                 * "@type" : "java.util.HashMap", <-- we don't want that
                 * 
                 * "id" : "foo"
                 * 
                 * }
                 */
                return super.useForType(t) && !t.isContainerType();
            }
        };

        typer = typer.init(JsonTypeInfo.Id.NAME, new DelegatingTypeIdResolver(classLoadingHelper));
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        mapper.setDefaultTyping(typer);

        mapper.setSubtypeResolver(new DelegatingSubtypeResolver(classLoadingHelper));

        return mapper;
    }

    static final class DelegatingSubtypeResolver extends SubtypeResolver {

        private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingSubtypeResolver.class);

        private DelegatedClassLoadingHelper helper;

        public DelegatingSubtypeResolver(DelegatedClassLoadingHelper helper) {
            this.helper = helper;
        }

        @Override
        public void registerSubtypes(Class<?>... classes) {
            throw new UnsupportedOperationException("This method should not be used in this resolver");
        }

        @Override
        public void registerSubtypes(NamedType... types) {
            throw new UnsupportedOperationException("This method should not be used in this resolver");
        }

        @Override
        public Collection<NamedType> collectAndResolveSubtypes(AnnotatedClass basetype, MapperConfig<?> config,
                AnnotationIntrospector ai) {
            String typeName = basetype.getName();
            LOGGER.info("resolving {}", typeName);
            return getKnownSubclasses(typeName);
        }

        private Collection<NamedType> getKnownSubclasses(String typeName) {
            Collection<Class<?>> allKnownSubTypes = helper.getAllKnownSubTypes(typeName);
            return Collections2.transform(allKnownSubTypes, new Function<Class<?>, NamedType>() {
                @Override
                public NamedType apply(Class<?> input) {
                    Provide annotation = input.getAnnotation(Provide.class);
                    if (annotation != null && (!annotation.alias()[0].isEmpty())) {
                        return new NamedType(input, annotation.alias()[0]);
                    }
                    return new NamedType(input);
                }
            });
        }

        @Override
        public Collection<NamedType> collectAndResolveSubtypes(AnnotatedMember property, MapperConfig<?> config,
                AnnotationIntrospector ai) {
            LOGGER.info("resolving {}", property);
            return getKnownSubclasses(property.getDeclaringClass().getName());
        }
    }

    static final class DelegatingTypeIdResolver implements TypeIdResolver {
        private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingTypeIdResolver.class);

        private DelegatedClassLoadingHelper helper;

        public DelegatingTypeIdResolver(DelegatedClassLoadingHelper helper) {
            this.helper = helper;
        }

        @Override
        public JavaType typeFromId(String id) {
            try {
                LOGGER.info("resolving type from id {}", id);
                Class<?> clazz = helper.loadClass(id);
                LOGGER.info("-> resolved {}", clazz.getName());
                return SimpleType.construct(clazz);
            } catch (ClassNotFoundException e) {
                LOGGER.error("could not load class {}", id, e);
                return null;
            }
        }

        @Override
        public void init(JavaType baseType) {
            LOGGER.info("init TypeIdResolver ", baseType);
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            LOGGER.info("get id from type {} - {}", value, suggestedType);
            Provide annotation = suggestedType.getAnnotation(Provide.class);
            if (annotation != null) {
                LOGGER.info("got {} from annotation", annotation.alias());
                return annotation.alias()[0];
            }
            LOGGER.info("using class name");
            return suggestedType.getName();
        }

        @Override
        public String idFromValue(Object value) {
            LOGGER.info("resolving idFromValue for {}", value);
            return idFromValueAndType(value, value.getClass());
        }

        @Override
        public Id getMechanism() {
            return Id.NAME;
        }
    }

}
