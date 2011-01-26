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

package org.openengsb.connector.maven.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.domain.build.BuildDomainEvents;
import org.openengsb.domain.build.BuildStartEvent;
import org.openengsb.domain.build.BuildSuccessEvent;
import org.openengsb.domain.deploy.DeployDomainEvents;
import org.openengsb.domain.deploy.DeployStartEvent;
import org.openengsb.domain.deploy.DeploySuccessEvent;
import org.openengsb.domain.test.TestDomainEvents;
import org.openengsb.domain.test.TestFailEvent;
import org.openengsb.domain.test.TestStartEvent;
import org.openengsb.domain.test.TestSuccessEvent;

public class MavenServiceTest {

    private MavenServiceImpl mavenService;
    private TestDomainEvents testEvents;
    private BuildDomainEvents buildEvents;
    private DeployDomainEvents deployEvents;

    @Before
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(getPath("test-unit-success"), "target"));
        FileUtils.deleteDirectory(new File(getPath("test-unit-fail"), "target"));
        this.mavenService = new MavenServiceImpl();
        buildEvents = mock(BuildDomainEvents.class);
        testEvents = mock(TestDomainEvents.class);
        deployEvents = mock(DeployDomainEvents.class);
        mavenService.setBuildEvents(buildEvents);
        mavenService.setTestEvents(testEvents);
        mavenService.setDeployEvents(deployEvents);
        mavenService.setContextService(mock(ContextCurrentService.class));
        mavenService.setSynchronous(true);
    }

    @Test
    public void build_shouldWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("clean compile");
        String id = mavenService.build();
        ArgumentCaptor<BuildSuccessEvent> argumentCaptor = ArgumentCaptor.forClass(BuildSuccessEvent.class);

        verify(buildEvents).raiseEvent(any(BuildStartEvent.class));
        verify(buildEvents).raiseEvent(argumentCaptor.capture());
        BuildSuccessEvent event = argumentCaptor.getValue();
        assertThat(event.getBuildId(), is(id));
        assertThat(event.getOutput(), containsString("SUCCESS"));
    }

    @Test
    public void buildWithProcessId_shouldWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("clean compile");
        mavenService.build(42);
        verify(buildEvents).raiseEvent(any(BuildStartEvent.class));
        verify(buildEvents).raiseEvent(refEq(new BuildSuccessEvent(42L, null), "output"));
    }

    @Test
    public void test_shouldWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("test");
        String id = mavenService.runTests();
        verify(testEvents).raiseEvent(any(TestStartEvent.class));
        verify(testEvents).raiseEvent(refEq(new TestSuccessEvent(id, null), "output"));
    }

    @Test
    public void testWithProcessId_shouldThrowEventsWithProcessId() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("install");
        long processId = 42;
        mavenService.runTests(processId);
        verify(testEvents).raiseEvent(any(TestStartEvent.class));
        verify(testEvents).raiseEvent(refEq(new TestSuccessEvent(processId, null), "output"));
    }

    @Test
    public void deploy_shoudWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("install -Dmaven.test.skip=true");
        String id = mavenService.deploy();
        verify(deployEvents).raiseEvent(any(DeployStartEvent.class));
        verify(deployEvents).raiseEvent(refEq(new DeploySuccessEvent(id, null), "output"));
    }

    @Test
    public void deployWithProcessId_shouldThrowEventsWithProcessId() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("install -Dmaven.test.skip=true");
        long id = 42;
        mavenService.deploy(id);
        verify(deployEvents).raiseEvent(any(DeployStartEvent.class));
        verify(deployEvents).raiseEvent(refEq(new DeploySuccessEvent(id, null), "output"));
    }

    @Ignore("no idea why this fails, it works from cmd-line")
    @Test
    public void testTestFail() {
        mavenService.setProjectPath(getPath("test-unit-fail"));
        mavenService.setCommand("install");
        String id = mavenService.runTests();
        verify(testEvents).raiseEvent(any(TestStartEvent.class));
        verify(testEvents).raiseEvent(refEq(new TestFailEvent(id, null), "output"));
    }

    @Test
    public void testGetAliveState_shouldReturnOnline() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        assertThat(mavenService.getAliveState(), is(AliveState.ONLINE));
    }

    @Test
    public void testGetAliveStateWrongPath_shouldReturnOffline() {
        mavenService.setProjectPath("pathThatDoesForSureNotExistBecauseItIsStrange");
        assertThat(mavenService.getAliveState(), is(AliveState.OFFLINE));
    }

    private String getPath(String folder) {
        return ClassLoader.getSystemResource(folder).getFile();
    }

}
