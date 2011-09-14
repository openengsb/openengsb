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
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Constants;

@RunWith(JUnit4TestRunner.class)
// This one will run each test in it's own container (slower speed)
// @ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class DomainEndpointFactoryIT extends AbstractPreConfiguredExamTestHelper {
    private static class DummyService extends AbstractOpenEngSBService implements ExampleDomain {

        public DummyService(String instanceId) {
            super(instanceId);
        }

        @Override
        public String doSomething(ExampleEnum exampleEnum) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public String doSomething(String message) {
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

    }

    @Test
    public void testSingleMethodProxies() throws Exception {
        ExampleDomain service = new DummyService("test");
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("id", "test");
        properties.put(Constants.SERVICE_RANKING, -1);
        properties.put("location.root", new String[]{ "foo" });
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);

        ContextHolder.get().setCurrentContextId("foo");
        ExampleDomain domainEndpoint =
            OpenEngSBCoreServices.getWiringService().getDomainEndpoint(ExampleDomain.class, "foo");
        assertThat(domainEndpoint.getInstanceId(), is("test"));

        service = new DummyService("test2");
        properties = new Hashtable<String, Object>();
        properties.put("id", "test2");
        properties.put("location.foo", new String[]{ "foo2", });
        properties.put(Constants.SERVICE_RANKING, 1);

        /* create the proxy before the service is registered */
        ExampleDomain domainEndpoint2 =
            OpenEngSBCoreServices.getWiringService().getDomainEndpoint(ExampleDomain.class, "foo2");
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);

        assertThat(domainEndpoint2.getInstanceId(), is("test2"));
    }

    @Test
    public void testListMethod() throws Exception {
        ExampleDomain service = new DummyService("test");
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("id", "test");
        properties.put(Constants.SERVICE_RANKING, -1);
        properties.put("location.foo", new String[]{ "test/foo", "main/foo", "main/bla", });
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);

        service = new DummyService("test2");
        properties = new Hashtable<String, Object>();
        properties.put("id", "test2");
        properties.put("location.foo", new String[]{ "main/foo2" });
        properties.put(Constants.SERVICE_RANKING, 1);
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);

        service = new DummyService("test3");
        properties = new Hashtable<String, Object>();
        properties.put("id", "test3");
        properties.put("location.foo", new String[]{ "test/foo" });
        properties.put(Constants.SERVICE_RANKING, 1);
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);

        ContextHolder.get().setCurrentContextId("foo");
        List<ExampleDomain> domainEndpoints =
            OpenEngSBCoreServices.getWiringService().getDomainEndpoints(ExampleDomain.class, "main/*");
        List<String> ids = new ArrayList<String>();
        for (ExampleDomain endpoint : domainEndpoints) {
            ids.add(endpoint.getInstanceId());
        }
        assertThat(ids, hasItems("test", "test2"));
        assertThat(ids, not(hasItem("test3")));
    }

    @Test
    public void testServiceDoesExist() throws Exception {
        ExampleDomain service = new DummyService("test");
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("id", "test");
        properties.put(Constants.SERVICE_RANKING, -1);
        properties.put("location.foo", new String[]{ "test/foo", "main/foo", "main/bla", });
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);

        assertThat(OpenEngSBCoreServices.getWiringService().isConnectorCurrentlyPresent(ExampleDomain.class), is(true));
    }
}
