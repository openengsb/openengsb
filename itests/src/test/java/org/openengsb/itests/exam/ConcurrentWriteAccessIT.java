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

import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFilePutOption;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.EKBConcurrentException;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.itests.exam.models.PrimitivePropertyModelDecorator;
import org.openengsb.itests.exam.models.SubModelDecorator;
import org.openengsb.itests.exam.models.TestModelDecorator;
import org.openengsb.itests.util.AbstractModelUsingExamTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class ConcurrentWriteAccessIT extends AbstractModelUsingExamTestHelper {
    private final static String CONTEXT = "testcontext";
    private EngineeringDatabaseService edbService;
    private QueryInterface query;
    private PersistInterface persist;

    @Configuration
    public static Option[] myConfiguration() throws Exception {
        Option[] options = new Option[]{
            new KarafDistributionConfigurationFilePutOption(
                "etc/org.openengsb.ekb.cfg",
                "modelUpdatePropagationMode", "DEACTIVATED"),
            new KarafDistributionConfigurationFilePutOption(
                "etc/org.openengsb.ekb.cfg",
                "persistInterfaceLockingMode", "BOTH"),
            mavenBundle().groupId("org.ops4j.pax.tinybundles").artifactId("tinybundles").versionAsInProject()
        };
        return combine(baseConfiguration(), options);
    }

    @Before
    public void setup() throws Exception {
        edbService = getOsgiService(EngineeringDatabaseService.class);
        query = getOsgiService(QueryInterface.class);
        persist = getOsgiService(PersistInterface.class);
        registerModelProvider();
        ContextHolder.get().setCurrentContextId(CONTEXT);
    }
    
    @Test(expected = EKBConcurrentException.class)
    public void testIfUnexpectedParentRevisionThrowsException_shouldThrowException() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("unexpectedParentRevision/1");
        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        persist.commit(commit);
        
        model = getTestModelDecorator();
        model.setEdbId("unexpectedParentRevision/2");
        commit = getTestEKBCommit().addInsert(model.getModel());
        // second time throws an exception since the expected parent revision is no longer null
        persist.commit(commit);
    }

    private TestModelDecorator loadTestModel(String oid) throws Exception {
        return new TestModelDecorator(query.getModel(getTestModel(), getModelOid(oid)));
    }

    private SubModelDecorator loadSubModel(String oid) throws Exception {
        return new SubModelDecorator(query.getModel(getSubModel(), getModelOid(oid)));
    }

    private TestModelDecorator getTestModelDecorator() throws Exception {
        return new TestModelDecorator(getTestModel().newInstance());
    }

    private SubModelDecorator getSubModelDecorator() throws Exception {
        return new SubModelDecorator(getSubModel().newInstance());
    }

    private PrimitivePropertyModelDecorator getPrimitivePropertyModelDecorator() throws Exception {
        return new PrimitivePropertyModelDecorator(getPrimitivePropertyModel().newInstance());
    }
}
