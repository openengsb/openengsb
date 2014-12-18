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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.configs.FeaturesCfg;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class TransformationDeployIT extends AbstractExamTestHelper {

    private TransformationEngine transformationEngine;

    private TinyBundle providerTinyBundle;

    @Configuration
    public static Option[] configuration() throws Exception {
        return combine(baseConfiguration(),
                editConfigurationFileExtend(FeaturesCfg.BOOT, ",openengsb-connector-example"),
                mavenBundle().groupId("org.ops4j.pax.tinybundles").artifactId("tinybundles").versionAsInProject());
    }

    @Before
    public void setUp() throws Exception {
        waitForOsgiBundle("org.openengsb.framework.ekb.transformation.wonderland");
        InputStream transformationInputStream = getClass().getClassLoader()
                .getResourceAsStream("test-transformations/testDescription.transformation");
        providerTinyBundle = bundle()
                .add("META-INF/transformations/test.transformation", transformationInputStream)
                .set(Constants.BUNDLE_SYMBOLICNAME, "test.transformation.provider")
                .set(Constants.BUNDLE_VERSION, "1.0.0");
        transformationEngine = queryOsgiService(TransformationEngine.class, null, 25000, true);
    }

    @Test
    public void testInstallBundleWithTransformations_shouldRegisterTransformations() throws Exception {
        Bundle providerBundle = getBundleContext()
                .installBundle("test://testlocation/test.provider.transformation.jar", providerTinyBundle.build());
        providerBundle.start();
        assertTrue("transformation is not possible", transformationEngine.isTransformationPossible(
                new ModelDescription(ExampleResponseModel.class.getName(), getOsgiProjectVersion()),
                new ModelDescription(ExampleRequestModel.class.getName(), getOsgiProjectVersion()))
        );
    }

    @Ignore
    @Test
    public void testRemoveBundleWithTransformations_shouldUnregisterTransformations() throws Exception {
        Bundle providerBundle = getBundleContext()
                .installBundle("test://testlocation/test.provider.transformation.jar", providerTinyBundle.build());
        providerBundle.start();
        providerBundle.stop();
        providerBundle.uninstall();
        assertFalse("transformation still possible. It has not been removed",
                transformationEngine.isTransformationPossible(
                        new ModelDescription(ExampleResponseModel.class.getName(), getOsgiProjectVersion()),
                        new ModelDescription(ExampleRequestModel.class.getName(), getOsgiProjectVersion()))
        );
    }

    @Ignore
    @Test
    public void testModifyBundleWithTransformations_shouldUnregisterTransformations() throws Exception {
        Bundle providerBundle = getBundleContext()
                .installBundle("test://testlocation/test.provider.transformation.jar", providerTinyBundle.build());
        providerBundle.start();
        providerTinyBundle.removeResource("test.transformation");
        providerBundle.update(providerTinyBundle.build());
        providerBundle.start();
        assertFalse("transformation still possible. It has not been removed",
                transformationEngine.isTransformationPossible(
                        new ModelDescription(ExampleResponseModel.class.getName(), getOsgiProjectVersion()),
                        new ModelDescription(ExampleRequestModel.class.getName(), getOsgiProjectVersion()))
        );
    }


}
