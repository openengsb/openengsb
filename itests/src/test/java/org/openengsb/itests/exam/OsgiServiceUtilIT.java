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

package org.openengsb.itests.exam;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Hashtable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.common.util.FilterUtils;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Constants;

@RunWith(JUnit4TestRunner.class)
// This one will run each test in it's own container (slower speed)
// @ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class OsgiServiceUtilIT extends AbstractPreConfiguredExamTestHelper {

    @Test
    public void testOsgiServiceUtilMethods() throws Exception {
        DomainProvider provider = getServiceUtils().getService(DomainProvider.class);
        assertThat(provider, notNullValue());
        provider =
            (DomainProvider) getServiceUtils().getService(FilterUtils.makeFilter(DomainProvider.class,
                String.format("(%s=example)", org.openengsb.core.api.Constants.DOMAIN_KEY)));
        assertThat(provider, notNullValue());

        assertThat(provider.getId(), is("example"));
    }

    @Test
    public void testOsgiServiceProxy() throws Exception {
        ConnectorProvider proxy =
            getServiceUtils().getOsgiServiceProxy(
                FilterUtils.makeFilter(ConnectorProvider.class,
                    String.format("(%s=example)", org.openengsb.core.api.Constants.CONNECTOR_KEY)),
                ConnectorProvider.class);
        assertThat(proxy.getId(), is("example"));
    }

    private static class DummyService extends AbstractOpenEngSBService implements ExampleDomain {

        public DummyService(String instanceId) {
            super(instanceId);
        }

        @Override
        public String doSomethingWithMessage(String message) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public String doSomethingWithLogEvent(LogEvent event) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public AliveState getAliveState() {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public ExampleResponseModel doSomethingWithModel(ExampleRequestModel model) {
            throw new UnsupportedOperationException("Not yet implemented");
        }


    }

    @Test
    public void testLocationUtils() throws Exception {
        ExampleDomain service = new DummyService("test");
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("id", "test");
        properties.put(Constants.SERVICE_RANKING, -1);
        properties.put("location.root", new String[]{ "foo" });
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);

        service = new DummyService("test2");
        properties = new Hashtable<String, Object>();
        properties.put("id", "test2");
        properties.put("location.foo", new String[]{ "foo" });
        properties.put(Constants.SERVICE_RANKING, 1);
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);

        ExampleDomain service2 = getServiceUtils().getService(ExampleDomain.class);
        assertThat(service2.getInstanceId(), is("test2"));

        ContextHolder.get().setCurrentContextId("foo");
        ExampleDomain serviceForLocation =
            (ExampleDomain) getServiceUtils().getServiceForLocation("foo");
        assertThat(serviceForLocation.getInstanceId(), is("test2"));

        ContextHolder.get().setCurrentContextId("foo2");
        serviceForLocation = (ExampleDomain) getServiceUtils().getServiceForLocation("foo");
        assertThat(serviceForLocation.getInstanceId(), is("test"));

        serviceForLocation =
            (ExampleDomain) getServiceUtils().getServiceForLocation("foo", "foo");
        assertThat(serviceForLocation.getInstanceId(), is("test2"));

        serviceForLocation = getServiceUtils().getServiceForLocation(ExampleDomain.class, "foo");
        assertThat(serviceForLocation.getInstanceId(), is("test"));
    }

    @Test
    public void testMutlipleLocations() throws Exception {
        ExampleDomain service = new DummyService("test");
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("id", "test");
        properties.put(Constants.SERVICE_RANKING, -1);
        properties.put("location.root", new String[]{ "main/foo", "main/foo2" });
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);

        ExampleDomain fooService = (ExampleDomain) getServiceUtils().getServiceForLocation("main/foo");
        assertThat(fooService.getInstanceId(), is("test"));

        ExampleDomain foo2Service = (ExampleDomain) getServiceUtils().getServiceForLocation("main/foo2");
        assertThat(foo2Service.getInstanceId(), is("test"));
    }

    private OsgiUtilsService getServiceUtils() {
        return new DefaultOsgiUtilsService(getBundleContext());
    }
}
