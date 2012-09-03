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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.remote.MethodCall;

import com.google.common.collect.ImmutableMap;

public class JsonUtilTest {

    private ObjectMapper objectMapper;

    public static class TestBean {
        private String x;

        public TestBean() {
        }

        public TestBean(String x) {
            this.x = x;
        }

        public String getX() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
        }
    }

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testConvertBeanArgument_shouldConvertToBean() throws Exception {
        MethodCall methodCall = new MethodCall("test", new Object[]{ ImmutableMap.of("x", "foo") },
            Arrays.asList(TestBean.class.getName()));
        JsonUtils.convertAllArgs(methodCall);
        Object object = methodCall.getArgs()[0];
        assertThat(object, is(TestBean.class));
        assertThat(((TestBean) object).x, is("foo"));
    }

    @Test
    public void testConvertListArguments_shouldConvertListValue() throws Exception {
        MethodCall methodCall = new MethodCall("test", new Object[]{ new TestBean[]{ new TestBean("foo") } });
        String stringValue = objectMapper.writeValueAsString(methodCall);
        methodCall = objectMapper.readValue(stringValue, MethodCall.class);
        JsonUtils.convertAllArgs(methodCall);
        Object object = methodCall.getArgs()[0];
        assertThat(object, is(TestBean[].class));
        TestBean[] arg = (TestBean[]) object;
        assertThat(arg[0].x, is("foo"));
    }
}
