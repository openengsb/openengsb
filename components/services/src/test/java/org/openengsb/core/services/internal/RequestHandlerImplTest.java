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

package org.openengsb.core.services.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.remote.CustomJsonMarshaller;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.api.remote.UseCustomJasonMarshaller;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class RequestHandlerImplTest extends AbstractOsgiMockServiceTest {

    private RequestHandler requestHandler;

    public interface TestInterface {
        Integer test(Integer arg);
    }

    public static class CustomMarshaller implements CustomJsonMarshaller<Integer> {

        @Override
        public Integer transformArg(Object arg) {
            Preconditions.checkArgument(arg instanceof Integer);
            return (Integer) arg + 1;
        }

    }

    @Before
    public void setup() throws Exception {
        RequestHandlerImpl requestHandlerImpl = new RequestHandlerImpl();
        requestHandlerImpl.setUtilsService(new DefaultOsgiUtilsService(bundleContext));
        requestHandler = requestHandlerImpl;
    }

    private TestInterface mockServiceWithProps(Map<String, Object> propData) {
        TestInterface mockService = mock(TestInterface.class);
        Dictionary<String, Object> props = new Hashtable<String, Object>(propData);
        registerService(mockService, props, TestInterface.class);
        return mockService;
    }

    private TestInterface registerServiceWithProps(TestInterface mockService, Map<String, Object> propData) {
        Dictionary<String, Object> props = new Hashtable<String, Object>(propData);
        registerService(mockService, props, TestInterface.class);
        return mockService;
    }

    @Test
    public void testMethodCallWithNullParameters_shouldBeSerialized() throws Exception {
        MethodCall methodCall = new MethodCall("test", new Object[]{ 1, null, 2 });
        ObjectMapper objectMapper = new ObjectMapper();
        String writeValueAsString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(methodCall);
        System.out.println(writeValueAsString);
        MethodCall readValue = objectMapper.readValue(writeValueAsString, MethodCall.class);
        assertThat(readValue, is(methodCall));
    }

    @Test
    public void testCallByServiceId_shouldCallService() throws Exception {
        TestInterface mockService = mockService(TestInterface.class, "testid");
        when(mockService.test(anyInt())).thenReturn(21);

        Map<String, String> metaData = ImmutableMap.of("serviceId", "testid");
        MethodCall c = new MethodCall("test", new Object[]{ 42 }, metaData);
        MethodResult result = requestHandler.handleCall(c);

        verify(mockService).test(42);
        assertThat(result.getClassName(), is(Integer.class.getName()));
        assertThat((Integer) result.getArg(), is(21));
    }

    @Test
    public void testCallByFilter_shouldCallService() throws Exception {
        Map<String, Object> propData = ImmutableMap.of("testprop", (Object) new String[]{ "bla", "bleh", });
        TestInterface mockService = mockServiceWithProps(propData);
        when(mockService.test(anyInt())).thenReturn(21);

        Map<String, String> metaData = ImmutableMap.of("serviceFilter", "(testprop=bla)");
        MethodCall c = new MethodCall("test", new Object[]{ 42 }, metaData);
        MethodResult result = requestHandler.handleCall(c);

        verify(mockService).test(42);
        assertThat(result.getClassName(), is(Integer.class.getName()));
        assertThat((Integer) result.getArg(), is(21));
    }

    @Test
    public void testCallByFilterAndServiceId_shouldCallService() throws Exception {
        Map<String, Object> propData =
            ImmutableMap.of("testprop", (Object) new String[]{ "bla", "bleh", }, "id", "xxx");
        TestInterface mockService = mockServiceWithProps(propData);
        when(mockService.test(anyInt())).thenReturn(21);

        Map<String, String> metaData = ImmutableMap.of("serviceFilter", "(testprop=bla)", "serviceId", "xxx");
        MethodCall c = new MethodCall("test", new Object[]{ 42 }, metaData);
        MethodResult result = requestHandler.handleCall(c);

        verify(mockService).test(42);
        assertThat(result.getClassName(), is(Integer.class.getName()));
        assertThat((Integer) result.getArg(), is(21));
    }

    @Test
    public void testCallByFilterAndServiceIdWithCustomMarshaller_shouldCallService() throws Exception {
        Map<String, Object> propData =
            ImmutableMap.of("testprop", (Object) new String[]{ "blub", "bleh", }, "id", "xxx");
        TestInterface realObject = new TestInterface() {
            @Override
            public Integer test(@UseCustomJasonMarshaller(CustomMarshaller.class) Integer arg) {
                return arg;
            }
        };
        registerServiceWithProps(realObject, propData);

        Map<String, String> metaData = ImmutableMap.of("serviceFilter", "(testprop=blub)", "serviceId", "xxx");
        MethodCall c = new MethodCall("test", new Object[]{ 42 }, metaData);
        MethodResult result = requestHandler.handleCall(c);

        assertThat(result.getClassName(), is(Integer.class.getName()));
        assertThat((Integer) result.getArg(), is(43));
    }

    @Test
    public void testCallButBothAttrsNull_shouldThrowException() throws Exception {
        Map<String, Object> propData =
            ImmutableMap.of("testprop", (Object) new String[]{ "bla", "bleh", });
        TestInterface mockService = mockServiceWithProps(propData);

        MethodCall c = new MethodCall("test", new Object[]{ 42 });
        try {
            requestHandler.handleCall(c);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            verifyZeroInteractions(mockService);
        }
    }

}
