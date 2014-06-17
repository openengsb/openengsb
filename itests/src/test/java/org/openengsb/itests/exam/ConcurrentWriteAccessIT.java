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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFilePutOption;
import org.apache.karaf.tooling.exam.options.configs.FeaturesCfg;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.ModelWrapper;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.EKBConcurrentException;
import org.openengsb.core.ekb.api.EKBService;
import org.openengsb.domain.example.model.SourceModelA;
import org.openengsb.itests.util.AbstractModelUsingExamTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class ConcurrentWriteAccessIT extends AbstractModelUsingExamTestHelper {
    private static final String CONTEXT = "testcontext";
    private EngineeringDatabaseService edbService;
    private EKBService ekbService;

    @Configuration
    public static Option[] myConfiguration() throws Exception {
        Option[] options = new Option[] {
                new KarafDistributionConfigurationFilePutOption("etc/org.openengsb.ekb.cfg",
                        "modelUpdatePropagationMode", "DEACTIVATED"),
                new KarafDistributionConfigurationFilePutOption("etc/org.openengsb.ekb.cfg",
                        "persistInterfaceLockingMode", "ACTIVATED"),
                mavenBundle().groupId("org.ops4j.pax.tinybundles").artifactId("tinybundles").versionAsInProject(),
                editConfigurationFileExtend(FeaturesCfg.BOOT, ",openengsb-connector-example") };
        return combine(baseConfiguration(), options);
    }

    @Before
    public void setup() throws Exception {
        edbService = getOsgiService(EngineeringDatabaseService.class);
        ekbService = getOsgiService(EKBService.class);
        registerModelProvider();
        ContextHolder.get().setCurrentContextId(CONTEXT);
    }

    @Test(expected = EKBConcurrentException.class)
    public void testIfUnexpectedParentRevisionThrowsException_shouldThrowException() throws Exception {
        SourceModelA model = new SourceModelA();
        model.setEdbId("unexpectedParentRevision/1");
        EKBCommit commit = getTestEKBCommit().addInsert(model);
        ekbService.commit(commit, null);

        model = new SourceModelA();
        model.setEdbId("unexpectedParentRevision/2");
        commit = getTestEKBCommit().addInsert(model);
        // second time throws an exception since the expected parent revision is
        // no longer null
        ekbService.commit(commit, null);
    }

    @Test(expected = EKBConcurrentException.class)
    public void testIfConcurrentWritingInTheSameContextThrowsAnException_shouldThrowException() throws Exception {
        EKBCommit commit = getTestEKBCommit();
        for (int i = 0; i < 30; i++) {
            SourceModelA model = new SourceModelA();
            model.setEdbId("concurrentTest/1/" + i);
            commit.addInsert(model);
        }
        EKBCommit anotherCommit = getTestEKBCommit();
        SourceModelA model = new SourceModelA();
        model.setEdbId("concurrentTest/1/30");
        anotherCommit.addInsert(model);

        ModelCommitThread thread = new ModelCommitThread(ekbService, commit, CONTEXT);
        thread.start();
        Thread.sleep(20);
        // throws an exception since the other commit is still running in the
        // context
        ekbService.commit(anotherCommit);
    }

    @Test
    public void testIfConcurrentWritingInDifferentContextsWorks_shouldWork() throws Exception {
        String otherContext = "a_different_context";
        String oid = "";
        EKBCommit commit = getTestEKBCommit();
        for (int i = 0; i < 15; i++) {
            SourceModelA model = new SourceModelA();
            model.setEdbId("concurrentTest/2/" + i);
            commit.addInsert(model);
            if (i == 14) {
                oid = ModelWrapper.wrap(model).retrieveInternalModelId().toString();
            }
        }
        ModelCommitThread thread = new ModelCommitThread(ekbService, commit, CONTEXT);
        thread.start();
        ModelCommitThread thread2 = new ModelCommitThread(ekbService, commit, otherContext);
        thread2.start();
        thread.join();
        thread2.join();

        ContextHolder.get().setCurrentContextId(CONTEXT);
        assertThat(edbService.getObject(getModelOid(oid)), notNullValue());
        ContextHolder.get().setCurrentContextId(otherContext);
        assertThat(edbService.getObject(getModelOid(oid)), notNullValue());
    }

    @Test
    public void testIfCheckForContextIsDoneCorrectly_shouldWork() throws Exception {
        ContextHolder.get().setCurrentContextId("A");
        EKBCommit commit = getTestEKBCommit();
        SourceModelA model = new SourceModelA();
        model.setEdbId("contextSwitchTest/1");
        String oid = ModelWrapper.wrap(model).retrieveInternalModelId().toString();
        model.setName("A");
        commit.addInsert(model);
        ekbService.commit(commit, ekbService.getLastRevisionId());
        ContextHolder.get().setCurrentContextId("B");
        ekbService.commit(commit, ekbService.getLastRevisionId());
        model.setName("B");
        commit = getTestEKBCommit();
        commit.addUpdate(model);
        ContextHolder.get().setCurrentContextId("A");
        ekbService.commit(commit, ekbService.getLastRevisionId());
        ContextHolder.get().setCurrentContextId("B");
        ekbService.commit(commit, ekbService.getLastRevisionId());

        ContextHolder.get().setCurrentContextId("A");
        EDBObject obj = edbService.getObject(getModelOid(oid));
        assertThat(obj, notNullValue());
        assertThat(obj.getString("name"), is("B"));
        ContextHolder.get().setCurrentContextId("B");
        obj = edbService.getObject(getModelOid(oid));
        assertThat(obj, notNullValue());
        assertThat(obj.getString("name"), is("B"));
    }

    class ModelCommitThread extends Thread {
        private final EKBService ekbService;
        private final EKBCommit commit;
        private final String contextId;

        public ModelCommitThread(EKBService ekbService, EKBCommit commit, String contextId) {
            this.ekbService = ekbService;
            this.commit = commit;
            this.contextId = contextId;
        }

        @Override
        public void run() {
            ContextHolder.get().setCurrentContextId(contextId);
            ekbService.commit(commit);
        }
    }
}
