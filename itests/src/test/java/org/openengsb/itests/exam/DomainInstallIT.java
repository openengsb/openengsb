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
