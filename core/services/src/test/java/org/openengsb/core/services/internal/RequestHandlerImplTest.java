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

import static org.hamcrest.Matchers.is;
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

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.osgi.framework.BundleContext;

import com.google.common.collect.ImmutableMap;

public class RequestHandlerImplTest extends AbstractOsgiMockServiceTest {

    private DefaultOsgiUtilsService serviceUtils;
    private RequestHandler requestHandler;

    public static interface TestInterface {
        Integer test(Integer arg);
    }

    @Before
    public void setup() throws Exception {
        requestHandler = new RequestHandlerImpl();
    }

    private TestInterface mockServiceWithProps(Map<String, Object> propData) {
        TestInterface mockService = mock(TestInterface.class);
        Dictionary<String, Object> props = new Hashtable<String, Object>(propData);
        registerService(mockService, props, TestInterface.class);
        return mockService;
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

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
    }

}
