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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.apache.karaf.features.FeaturesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class DomainInstallIT extends AbstractExamTestHelper {

    @Configuration
    public Option[] configuration() throws Exception {
        return baseConfiguration();
    }

    @Inject
    private FeaturesService featuresService;

    @Test
    public void testInstallDomainShouldWeaveModel() throws Exception {
        featuresService.installFeature("openengsb-domain-example");
        Class<?> loadClass =
            this.getClass().getClassLoader().loadClass("org.openengsb.domain.example.model.ExampleRequestModel");
        assertTrue("ExampleRequestModel has not been woven correctly", OpenEngSBModel.class.isAssignableFrom(loadClass));
    }

    @Test
    public void testInstallDomain_shouldRegisterDomainEvents() throws Exception {
        ServiceReference serviceReference =
            getBundleContext().getServiceReference("org.openengsb.domain.example.ExampleDomainEvents");
        assertThat(serviceReference, is(nullValue()));
        featuresService.installFeature("openengsb-domain-example");
        ServiceTracker tracker =
            new ServiceTracker(getBundleContext(), "org.openengsb.domain.example.ExampleDomainEvents", null);
        tracker.open();
        Object service = tracker.waitForService(10000);
        assertThat(service, not(nullValue()));
    }

}
