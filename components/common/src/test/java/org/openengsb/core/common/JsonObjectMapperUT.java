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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectMapper.DefaultTypeResolverBuilder;
import org.codehaus.jackson.map.ObjectMapper.DefaultTyping;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.type.JavaType;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.JsonObjectSerializer.DelegatingSubtypeResolver;
import org.openengsb.core.common.JsonObjectSerializer.DelegatingTypeIdResolver;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.Constants;
import org.openengsb.labs.delegation.service.Provide;
import org.openengsb.labs.delegation.service.internal.ClassProviderImpl;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class JsonObjectMapperUT extends AbstractOsgiMockServiceTest {

    private DelegatedClassLoadingHelper classLoadingHelper;

    public interface ITest {
        String getValue();
    }

    @Provide(alias = "TEST")
    public static class TestImpl implements ITest {
        private String value;

        public TestImpl() {
        }

        public TestImpl(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            TestImpl other = (TestImpl) obj;
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            } else if (!value.equals(other.value)) {
                return false;
            }
            return true;
        }

    }

    @Provide(alias = "MSG")
    public static class MessageImpl {
        private String id;
        private ITest content;
        private Map<String, String> metadata;

        public MessageImpl(String id, ITest content) {
            this.id = id;
            this.content = content;
            metadata = Maps.newHashMap();
            metadata.put("id", id);
        }

        public MessageImpl() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public ITest getContent() {
            return content;
        }

        public void setContent(ITest content) {
            this.content = content;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id, content, metadata);
        }

        @Override
        public boolean equals(Object obj) {
            if (getClass().isAssignableFrom(obj.getClass())) {
                MessageImpl other = (MessageImpl) obj;
                return Objects.equal(id, other.id)
                        && Objects.equal(content, other.content)
                        && Objects.equal(metadata, other.metadata);
            }
            return false;
        }
    }

    @Before
    public void setUp() throws Exception {
        ClassProviderImpl classloadingDelegateImpl =
            new ClassProviderImpl(bundle, TestImpl.class.getName());

        Dictionary<String, Object> props =
            new Hashtable<String, Object>(ImmutableMap.of(
                Constants.PROVIDED_CLASSES_KEY, new String[]{ TestImpl.class.getName(), "TEST" }));
        registerService(classloadingDelegateImpl, props, ClassProvider.class);

        classLoadingHelper = new DelegatedClassLoadingHelper(bundleContext);
    }

    @Test
    public void testName() throws Exception {
        MessageImpl messageImpl = new MessageImpl("foo", new TestImpl("value"));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);
        // objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        TypeResolverBuilder<?> typer = new DefaultTypeResolverBuilder(DefaultTyping.OBJECT_AND_NON_CONCRETE) {
            @Override
            public boolean useForType(JavaType t) {
                return super.useForType(t) && !t.isContainerType();
            }
        };
        typer = typer.init(JsonTypeInfo.Id.NAME, new DelegatingTypeIdResolver(classLoadingHelper));
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        objectMapper.setDefaultTyping(typer);
        objectMapper.setSubtypeResolver(new DelegatingSubtypeResolver(classLoadingHelper));

        ObjectMapper plainMapper = new ObjectMapper();
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

        TestImpl testImpl = new TestImpl("test");

        String writeValueAsString = writer.writeValueAsString(messageImpl);
        System.out.println(writeValueAsString);

        MessageImpl readValue = objectMapper.readValue(writeValueAsString, MessageImpl.class);
        assertThat(readValue, is(messageImpl));
    }
}
