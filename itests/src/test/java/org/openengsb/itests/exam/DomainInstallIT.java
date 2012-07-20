package org.openengsb.itests.exam;

import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.apache.karaf.features.FeaturesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
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

}
