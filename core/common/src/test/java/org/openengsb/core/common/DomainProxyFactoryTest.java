/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.context.ContextService;
import org.openengsb.core.common.util.AliveState;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class DomainProxyFactoryTest {

    public interface TestInterface extends Domain {
        boolean methodx();
    }

    public static class DefaultImpl implements TestInterface {
        @Override
        public boolean methodx() {
            return true;
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.ONLINE;
        }
    }

    private DefaultImpl targetService;
    private ContextService contextMock;
    private BundleContext bundleContextMock;

    @Before
    public void setUp() throws Exception {
        targetService = new DefaultImpl();
        setupContextMock();
        setupBundleContextMock();
    }

    private void setupBundleContextMock() throws InvalidSyntaxException {
        bundleContextMock = mock(BundleContext.class);
        ServiceReference serviceRefMock = mock(ServiceReference.class);
        when(bundleContextMock.getServiceReferences(anyString(), contains("testService"))).thenReturn(
                new ServiceReference[]{ serviceRefMock });
        when(bundleContextMock.getService(serviceRefMock)).thenReturn(targetService);
    }

    private void setupContextMock() {
        contextMock = mock(ContextService.class);
        when(contextMock.getValue(anyString())).thenReturn("testService");
    }

    @Test
    public void testSelectCorrectConnector() throws Exception {
        DefaultDomainProxyFactoryBean factory = new DefaultDomainProxyFactoryBean();
        factory.setDomainInterface(TestInterface.class);
        factory.setContext(contextMock);
        factory.setDomainName("testDomain");
        factory.setBundleContext(bundleContextMock);

        TestInterface obj = (TestInterface) factory.getObject();

        assertTrue(obj.methodx());
    }

}
