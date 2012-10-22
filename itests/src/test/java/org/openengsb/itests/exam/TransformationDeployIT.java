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

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import java.io.InputStream;

import javax.inject.Inject;

import org.apache.karaf.tooling.exam.options.configs.FeaturesCfg;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class TransformationDeployIT extends AbstractExamTestHelper {

    @Inject
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
        InputStream transformationInputStream =
                getClass().getClassLoader().getResourceAsStream("transformations/testDescription.transformation.txt");
        providerTinyBundle = bundle()
                .add("test.transformation", transformationInputStream)
                .set(Constants.BUNDLE_SYMBOLICNAME, "test.transformation.provider")
                .set(Constants.BUNDLE_VERSION, "1.0.0");
    }

    @Test
    public void testInstallBundleWithTransformations_shouldRegisterTransformations() throws Exception {
        Bundle providerBundle =
                getBundleContext().installBundle("test://testlocation/test.provider.transformation.jar", providerTinyBundle.build());
        providerBundle.start();
        assertTrue("transformation is not possible", transformationEngine.isTransformationPossible(
                new ModelDescription(ExampleResponseModel.class.getName(), "3.0.0.SNAPSHOT"),
                new ModelDescription(ExampleRequestModel.class.getName(), "3.0.0.SNAPSHOT"))
        );
    }

    @Test
    public void testRemoveBundleWithTransformations_shouldUnregisterTransformations() throws Exception {
        Bundle providerBundle =
                getBundleContext().installBundle("test://testlocation/test.provider.transformation.jar", providerTinyBundle.build());
        providerBundle.start();
        providerBundle.stop();
        providerBundle.uninstall();
        assertFalse("transformation still possible. It has not been removed", transformationEngine.isTransformationPossible(
                new ModelDescription(ExampleResponseModel.class.getName(), "3.0.0.SNAPSHOT"),
                new ModelDescription(ExampleRequestModel.class.getName(), "3.0.0.SNAPSHOT"))
        );
    }


}
