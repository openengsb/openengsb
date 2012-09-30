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

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.util.ArrayList;
import java.util.List;

import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFilePutOption;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.itests.util.AbstractModelUsingExamTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class EngineeringObjectIT extends AbstractModelUsingExamTestHelper {
    private QueryInterface query;
    private PersistInterface persist;

    @Configuration
    public static Option[] myConfiguration() throws Exception {
        Option[] options = new Option[]{
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "url", "jdbc:h2:mem:itests"),
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "driverClassName", "org.h2.jdbcx.JdbcDataSource"),
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "username", ""),
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "password", ""),
            mavenBundle().groupId("org.ops4j.pax.tinybundles").artifactId("tinybundles").versionAsInProject()
        };
        return combine(baseConfiguration(), options);
    }

    @Before
    public void setup() throws Exception {
        query = getOsgiService(QueryInterface.class);
        persist = getOsgiService(PersistInterface.class);
        TransformationEngine engine = getOsgiService(TransformationEngine.class);
        List<TransformationDescription> descriptions = generateTransformationDescriptions();
        engine.saveDescriptions(descriptions);
        registerModelProvider();
    }

    private List<TransformationDescription> generateTransformationDescriptions() {
        List<TransformationDescription> descriptions = new ArrayList<TransformationDescription>();
        TransformationDescription desc =
            new TransformationDescription(getSourceModelADescription(), getEOModelDescription());
        desc.forwardField("name", "nameA");
        descriptions.add(desc);
        desc = new TransformationDescription(getSourceModelBDescription(), getEOModelDescription());
        desc.forwardField("name", "nameB");
        descriptions.add(desc);
        desc = new TransformationDescription(getEOModelDescription(), getSourceModelADescription());
        desc.forwardField("nameA", "name");
        descriptions.add(desc);
        desc = new TransformationDescription(getEOModelDescription(), getSourceModelBDescription());
        desc.forwardField("nameB", "name");
        descriptions.add(desc);
        return descriptions;
    }

}
