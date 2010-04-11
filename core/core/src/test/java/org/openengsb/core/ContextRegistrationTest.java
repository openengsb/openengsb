/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.Resource;
import javax.jbi.servicedesc.ServiceEndpoint;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openengsb.contextcommon.ContextHelperImpl;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:../test-classes/SUProperties.xml" })
public class ContextRegistrationTest {
    private ContextHelperImpl contextHelper;

    @Resource
    private HashMap<String, HashMap<String, String>> properties;

    @Before
    public void setUp() {
        contextHelper = Mockito.mock(ContextHelperImpl.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNoRegistering() throws Exception {
        OpenEngSBEndpoint endpoint = new OpenEngSBEndpoint(new OpenEngSBComponent(), Mockito
                .mock(ServiceEndpoint.class));
        setContextHelper(endpoint);
        callMethod(endpoint, "register");
        callMethod(endpoint, "unregister");

        Mockito.verify(contextHelper, Mockito.never()).store(Mockito.anyMap());
        Mockito.verify(contextHelper, Mockito.never()).remove(Mockito.anyList());
    }

    @Test
    public void testSULifecycle() throws Exception {
        OpenEngSBEndpoint endpoint = new OpenEngSBEndpoint(new OpenEngSBComponent(), Mockito
                .mock(ServiceEndpoint.class));
        endpoint.setEndpoint("testendpoint");
        endpoint.setContextProperties(properties);
        setContextHelper(endpoint);
        callMethod(endpoint, "register");

        for (String key : properties.keySet()) {
            Mockito.verify(contextHelper, Mockito.times(1)).setContext(Mockito.eq(key));
            Mockito.verify(contextHelper, Mockito.times(1)).store(
                    Mockito.eq(addSource(properties.get(key), "SU/testendpoint")));
        }

        clearContextHelper(endpoint);
        callMethod(endpoint, "unregister");

        for (String key : properties.keySet()) {
            Mockito.verify(contextHelper, Mockito.times(1)).setContext(Mockito.eq(key));
            Mockito.verify(contextHelper, Mockito.times(1)).remove(
                    Mockito.eq(addSource(properties.get(key).keySet(), "SU/testendpoint")));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSELifecycle() throws Exception {
        OpenEngSBComponent component = new OpenEngSBComponent();
        OpenEngSBEndpoint endpoint = new OpenEngSBEndpoint(component, Mockito.mock(ServiceEndpoint.class));
        component.setContextProperties(properties);
        setContextHelper(endpoint);
        callMethod(endpoint, "register");

        for (String key : properties.keySet()) {
            Mockito.verify(contextHelper, Mockito.times(1)).store(Mockito.eq(addSource(properties.get(key), "SE")));
        }

        clearContextHelper(endpoint);
        callMethod(endpoint, "register");

        Mockito.verify(contextHelper, Mockito.never()).store(Mockito.anyMap());

        clearContextHelper(endpoint);
        callMethod(endpoint, "unregister");

        Mockito.verify(contextHelper, Mockito.never()).store(Mockito.anyMap());

        clearContextHelper(endpoint);
        callMethod(endpoint, "unregister");

        for (String key : properties.keySet()) {
            Mockito.verify(contextHelper, Mockito.times(1)).setContext(Mockito.eq(key));
            Mockito.verify(contextHelper, Mockito.times(1)).remove(
                    Mockito.eq(addSource(properties.get(key).keySet(), "SE")));
        }
    }

    private void clearContextHelper(OpenEngSBEndpoint endpoint) throws IllegalArgumentException, SecurityException,
            IllegalAccessException, NoSuchFieldException {
        contextHelper = Mockito.mock(ContextHelperImpl.class);
        setContextHelper(endpoint);
    }

    private HashMap<String, String> addSource(HashMap<String, String> properties, String src) {
        HashMap<String, String> newProperties = new HashMap<String, String>(properties.size());
        for (String key : properties.keySet()) {
            int pos = key.lastIndexOf("/");
            String path = key.substring(0, pos);
            String name = key.substring(pos);

            newProperties.put(path + "/" + src + name, properties.get(key));
        }
        return newProperties;
    }

    private ArrayList<String> addSource(Set<String> keys, String src) {
        ArrayList<String> newKeys = new ArrayList<String>(keys.size());
        for (String key : keys) {
            int pos = key.lastIndexOf("/");
            String path = key.substring(0, pos);
            String name = key.substring(pos);

            newKeys.add(path + "/" + src + name);
        }
        return newKeys;
    }

    private void setContextHelper(OpenEngSBEndpoint endpoint) throws IllegalArgumentException, IllegalAccessException,
            SecurityException, NoSuchFieldException {
        Field field = OpenEngSBEndpoint.class.getDeclaredField("contextHelper");
        field.setAccessible(true);
        field.set(endpoint, contextHelper);
    }

    private void callMethod(Object target, String name) throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = target.getClass().getDeclaredMethod(name, new Class[0]);
        method.setAccessible(true);
        method.invoke(target, new Object[0]);
    }
}
