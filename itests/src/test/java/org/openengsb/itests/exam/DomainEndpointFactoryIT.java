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

package org.openengsb.itests.exam;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Hashtable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.AliveState;
import org.openengsb.core.common.DomainEndpointFactory;
import org.openengsb.core.common.context.ContextHolder;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Constants;

@RunWith(JUnit4TestRunner.class)
public class DomainEndpointFactoryIT extends AbstractExamTestHelper {
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
        properties.put("location.root", "foo");
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);

        ContextHolder.get().setCurrentContextId("foo");
        ExampleDomain domainEndpoint = DomainEndpointFactory.getDomainEndpoint(ExampleDomain.class, "foo");
        assertThat(domainEndpoint.getInstanceId(), is("test"));

        service = new DummyService("test2");
        properties = new Hashtable<String, Object>();
        properties.put("id", "test2");
        properties.put("location.foo", "foo2");
        properties.put(Constants.SERVICE_RANKING, 1);

        /* create the proxy before the service is registered */
        ExampleDomain domainEndpoint2 = DomainEndpointFactory.getDomainEndpoint(ExampleDomain.class, "foo2");
        getBundleContext().registerService(ExampleDomain.class.getName(), service, properties);

        assertThat(domainEndpoint2.getInstanceId(), is("test2"));
    }
}
