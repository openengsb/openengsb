package org.openengsb.core.services.internal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

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

    public static interface TestInterface {
        Integer test(Integer arg);
    }

    @Test
    public void testCallByServiceId_shouldCallService() throws Exception {
        TestInterface mockService = mockService(TestInterface.class, "testid");
        when(mockService.test(anyInt())).thenReturn(21);
        RequestHandler requestHandler = new RequestHandlerImpl();

        Map<String, String> metaData = ImmutableMap.of("serviceId", "testid");
        MethodCall c = new MethodCall("test", new Object[]{ 42 }, metaData);
        MethodResult result = requestHandler.handleCall(c);

        verify(mockService).test(42);
        assertThat(result.getClassName(), is(Integer.class.getName()));
        assertThat((Integer) result.getArg(), is(21));
    }

    @Test
    public void testCallByFilter_shouldCallService() throws Exception {
        TestInterface mockService = mock(TestInterface.class);
        ImmutableMap<String, String[]> propData = ImmutableMap.of("testprop", new String[]{ "bla", "bleh", });
        Dictionary<String, Object> props = new Hashtable<String, Object>(propData);
        registerService(mockService, props, TestInterface.class);

        when(mockService.test(anyInt())).thenReturn(21);
        RequestHandler requestHandler = new RequestHandlerImpl();

        Map<String, String> metaData = ImmutableMap.of("serviceFilter", "(testprop=bla)");
        MethodCall c = new MethodCall("test", new Object[]{ 42 }, metaData);
        MethodResult result = requestHandler.handleCall(c);

        verify(mockService).test(42);
        assertThat(result.getClassName(), is(Integer.class.getName()));
        assertThat((Integer) result.getArg(), is(21));
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
    }

}
