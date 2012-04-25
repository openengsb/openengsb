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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;
import org.osgi.framework.BundleContext;

public class OsgiServiceUtilsTest extends AbstractOsgiMockServiceTest {

    private class BlockingAnswer implements Answer<Object> {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            sync.acquire();
            return 42;
        }
    }

    private class BlockingNullDomain extends NullDomainImpl {
        @Override
        public Object nullMethod(Object o, String b) {
            try {
                sync.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return super.nullMethod(o, b);
        }
    }

    private OsgiUtilsService utils;
    private BlockingAnswer blockingAnswer;
    private Semaphore sync = new Semaphore(0);

    @Before
    public void setUp() throws Exception {
        DefaultOsgiUtilsService defaultOsgiUtilsService = new DefaultOsgiUtilsService();
        defaultOsgiUtilsService.setBundleContext(bundleContext);
        utils = defaultOsgiUtilsService;
        BlockingNullDomain service = new BlockingNullDomain();
        registerServiceViaId(service, "foo", NullDomain.class);
    }

    @Test(expected = OsgiServiceNotAvailableException.class)
    public void getProxyForNonExistingservice_shouldThrowNotAvailableException() throws Exception {
        NullDomain osgiServiceProxy = utils.getOsgiServiceProxy(NullDomain.class, 1);
        osgiServiceProxy.getAliveState();
    }

    @Test
    public void handleParallelProxyCalls() throws Exception {
        final NullDomain service = utils.getOsgiServiceProxy(NullDomain.class);

        Callable<Object> normalCall = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return service.nullMethod(42);
            }
        };

        Callable<Object> blockingCall = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return service.nullMethod(42, "foo");
            }
        };

        FutureTask<Object> normalFuture = new FutureTask<Object>(normalCall);
        new Thread(normalFuture).start();
        FutureTask<Object> blockingFuture = new FutureTask<Object>(blockingCall);
        new Thread(blockingFuture).start();

        Object normalResult = normalFuture.get();

        // verify(serviceMock).nullMethod(any());
        /* getAnswer-call is finished */
        assertThat((Integer) normalResult, is(42));
        try {
            blockingFuture.get(200, TimeUnit.MILLISECONDS);
            fail("blocking method returned premature");
        } catch (TimeoutException e) {
            // ignore, this is expceted
        }

        sync.release();
        Object blockingResult = blockingFuture.get();
        assertThat((Integer) blockingResult, is(42));
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
    }

}
