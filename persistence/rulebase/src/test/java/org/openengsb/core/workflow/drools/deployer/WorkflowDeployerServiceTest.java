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
package org.openengsb.core.workflow.drools.deployer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.core.workflow.api.RuleManager;
import org.openengsb.core.workflow.api.model.RuleBaseElementId;
import org.openengsb.core.workflow.api.model.RuleBaseElementType;
import org.openengsb.core.workflow.drools.internal.WorkflowDeployerService;
import org.openengsb.core.workflow.drools.persistence.util.PersistenceTestUtil;
import org.slf4j.Logger;

import antlr.debug.Event;

public class WorkflowDeployerServiceTest {

    private static final String PROCESS_EXAMPLE = "flowtest.rf";
    private static final String RULE_EXAMPLE = "rulebase/org/openengsb/hello1.rule";

    private WorkflowDeployerService workflowDeployer;
    private RuleManager ruleManager;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        workflowDeployer = new WorkflowDeployerService();
        ruleManager = mock(RuleManager.class);
        workflowDeployer.setRuleManager(ruleManager);
    }

    @Test
    public void testWorkflowDeployerService_isAnArtifactListener() {
        assertThat(workflowDeployer instanceof ArtifactInstaller, is(true));
    }

    @Test
    public void testWorkflowDeployerService_canHandleWorkflowFiles() throws IOException {
        File processFile = temporaryFolder.newFile("process.rf");
        File ruleFile = temporaryFolder.newFile("rule.rule");
        File functionFile = temporaryFolder.newFile("function.function");
        File importFile = temporaryFolder.newFile("test1.import");
        File globalFile = temporaryFolder.newFile("test1.global");

        assertThat(workflowDeployer.canHandle(processFile), is(true));
        assertThat(workflowDeployer.canHandle(ruleFile), is(true));
        assertThat(workflowDeployer.canHandle(functionFile), is(true));
        assertThat(workflowDeployer.canHandle(importFile), is(true));
        assertThat(workflowDeployer.canHandle(globalFile), is(true));
    }

    @Test
    public void testWorkflowDeployerService_shouldNotHandleFiles() throws IOException {
        File fileWithoutExtension = temporaryFolder.newFile("process");
        File dictionary = temporaryFolder.newFolder("dictionary.rf");

        assertThat(workflowDeployer.canHandle(fileWithoutExtension), is(false));
        assertThat(workflowDeployer.canHandle(dictionary), is(false));
    }

    @Test
    public void testWorkflowDeployerService_shouldInstallArtifacts() throws Exception {
        File ruleFile = readExampleRuleFile();
        File processFile = readExampleProcessFile();

        workflowDeployer.install(ruleFile);
        workflowDeployer.install(processFile);

        RuleBaseElementId idRule = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "rule");
        RuleBaseElementId idProcess = new RuleBaseElementId(RuleBaseElementType.Process, "org.openengsb", "process");

        verify(ruleManager, times(1)).addOrUpdate(idRule, FileUtils.readFileToString(ruleFile));
        verify(ruleManager, times(1)).addOrUpdate(idProcess, FileUtils.readFileToString(processFile));
    }

    @Test
    public void testWorkflowDeployerService_shouldUpdateArtifacts() throws Exception {
        File ruleFile = readExampleRuleFile();
        File processFile = readExampleProcessFile();

        workflowDeployer.install(ruleFile);
        workflowDeployer.install(processFile);

        String modifiedRule = "it doesnt matter";
        FileUtils.writeStringToFile(ruleFile, modifiedRule);
        RuleBaseElementId idRule = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "rule");

        workflowDeployer.update(ruleFile);
        verify(ruleManager, times(1)).addOrUpdate(idRule, modifiedRule);

        String process = FileUtils.readFileToString(processFile);
        String modifiedProcess = process.replace("org.openengsb", "new.package");
        FileUtils.writeStringToFile(processFile, modifiedProcess);

        workflowDeployer.update(processFile);
        RuleBaseElementId idProcess = new RuleBaseElementId(RuleBaseElementType.Process, "org.openengsb", "process");
        verify(ruleManager, times(1)).delete(idProcess);
        idProcess.setPackageName("new.package");
        verify(ruleManager, times(1)).addOrUpdate(idProcess, modifiedProcess);
    }

    @Test
    public void testWorkflowDeployerService_shouldDeleteArtifacts() throws Exception {
        File ruleFile = readExampleRuleFile();
        File processFile = readExampleProcessFile();

        workflowDeployer.install(ruleFile);
        workflowDeployer.install(processFile);

        workflowDeployer.uninstall(ruleFile);
        workflowDeployer.uninstall(processFile);

        RuleBaseElementId idRule = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "rule");
        RuleBaseElementId idProcess = new RuleBaseElementId(RuleBaseElementType.Process, "org.openengsb", "process");

        verify(ruleManager, times(1)).delete(idRule);
        verify(ruleManager, times(1)).delete(idProcess);
    }

    @Test
    public void testInstallImports_shouldImportClasses() throws Exception {
        File importFile = temporaryFolder.newFile("test1.import");
        FileUtils.writeLines(importFile, Arrays.asList(
            "java.util.List",
            ""));

        workflowDeployer.install(importFile);

        verify(ruleManager).addImport("java.util.List");
    }

    @Test
    public void testInstallGlobals_shouldAddGlobals() throws Exception {
        File globalfile = temporaryFolder.newFile("test1.global");
        FileUtils.writeLines(globalfile, Arrays.asList(
            Logger.class.getName() + " logger",
            ""));

        workflowDeployer.install(globalfile);

        verify(ruleManager).addGlobal(Logger.class.getName(), "logger");
    }

    @Test
    public void testInstallImportsAndGlobalsRequiredByRule_shouldParseCorrectly() throws Exception {
        setupWithRealCompiler();

        File importFile = temporaryFolder.newFile("test1.import");
        FileUtils.writeLines(importFile, Arrays.asList(
            Event.class.getName(),
            BigInteger.class.getName(),
            ""));
        workflowDeployer.install(importFile);

        File globalFile = temporaryFolder.newFile("test1.global");
        FileUtils.writeLines(globalFile, Arrays.asList(
            Logger.class.getName() + " logger",
            ""));
        workflowDeployer.install(globalFile);

        File testRuleFile = temporaryFolder.newFile("test1.rule");
        FileUtils.writeLines(testRuleFile, Arrays.asList(
            "when",
            "   Event()",
            "then",
            "   BigInteger i = new BigInteger(1);",
            "   logger.info(i.toString());",
            ""));

        workflowDeployer.install(testRuleFile);
    }

    @Test
    public void testInstallWrongOrder_shouldAttemptInstallAgain() throws Exception {
        setupWithRealCompiler();

        final File importFile = temporaryFolder.newFile("test1.import");
        FileUtils.writeLines(importFile, Arrays.asList(
            Event.class.getName(),
            BigInteger.class.getName(),
            Logger.class.getName(),
            ""));

        final File globalFile = temporaryFolder.newFile("test1.global");
        FileUtils.writeLines(globalFile, Arrays.asList(
            List.class.getName() + " listtest",
            Logger.class.getSimpleName() + " logger",
            ""));

        final File testRuleFile = temporaryFolder.newFile("test1.rule");
        FileUtils.writeLines(testRuleFile, Arrays.asList(
            "when",
            "   Event()",
            "then",
            "   BigInteger i = new BigInteger(1);",
            "   logger.info(i.toString());",
            ""));

        workflowDeployer.install(testRuleFile);
        workflowDeployer.install(globalFile);
        workflowDeployer.install(importFile);

        assertThat(ruleManager.get(new RuleBaseElementId(RuleBaseElementType.Rule, "test1")), not(nullValue()));
    }

    @Test
    public void uninstallGlobal_RemoveGlobal() throws Exception {
        final File globalFile = temporaryFolder.newFile("test1.global");
        FileUtils.writeLines(globalFile, Arrays.asList(
            Logger.class.getName() + " logger",
            ""));

        workflowDeployer.install(globalFile);
        globalFile.delete();
        workflowDeployer.uninstall(globalFile);

        assertThat(ruleManager.listGlobals().keySet(), not(hasItem("logger")));
    }

    @Test
    public void uninstallImport_shouldRemoveImport() throws Exception {
        File importFile = temporaryFolder.newFile("test1.import");
        FileUtils.writeLines(importFile, Arrays.asList(
            Event.class.getName(),
            BigInteger.class.getName(),
            ""));

        workflowDeployer.install(importFile);
        importFile.delete();
        workflowDeployer.uninstall(importFile);

        assertThat(ruleManager.listImports(), not(hasItem(BigInteger.class.getName())));
    }

    private void setupWithRealCompiler() throws Exception {
        ruleManager = PersistenceTestUtil.getRuleManager(temporaryFolder);
        workflowDeployer.setRuleManager(ruleManager);
    }

    private File readExampleProcessFile() throws IOException {
        URL processURL = ClassLoader.getSystemResource(PROCESS_EXAMPLE);
        File processFile = FileUtils.toFile(processURL);
        File target = temporaryFolder.newFile("process.rf");
        FileUtils.copyFile(processFile, target);
        return target;
    }

    private File readExampleRuleFile() throws IOException {
        URL processURL = ClassLoader.getSystemResource(RULE_EXAMPLE);
        File processFile = FileUtils.toFile(processURL);
        File target = temporaryFolder.newFile("rule.rule");
        FileUtils.copyFile(processFile, target);
        return target;
    }

}
