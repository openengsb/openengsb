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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.domain.build.BuildDomainEvents;
import org.openengsb.domain.build.BuildEndEvent;
import org.openengsb.domain.build.BuildStartEvent;
import org.openengsb.domain.deploy.DeployDomainEvents;
import org.openengsb.domain.deploy.DeployEndEvent;
import org.openengsb.domain.deploy.DeployStartEvent;
import org.openengsb.domain.test.TestDomainEvents;
import org.openengsb.domain.test.TestEndEvent;
import org.openengsb.domain.test.TestStartEvent;

public class MavenServiceTest {

    private MavenServiceImpl mavenService;
    private TestDomainEvents testEvents;
    private BuildDomainEvents buildEvents;
    private DeployDomainEvents deployEvents;

    @Before
    public void setUp() {
        this.mavenService = new MavenServiceImpl();
        buildEvents = mock(BuildDomainEvents.class);
        testEvents = mock(TestDomainEvents.class);
        deployEvents = mock(DeployDomainEvents.class);
        mavenService.setBuildEvents(buildEvents);
        mavenService.setTestEvents(testEvents);
        mavenService.setDeployEvents(deployEvents);
        mavenService.setSynchronous(true);
    }

    @Test
    public void build_shouldWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        String id = mavenService.build();
        verify(buildEvents).raiseEvent(any(BuildStartEvent.class));
        verify(buildEvents).raiseEvent(refEq(new BuildEndEvent(id, true, null), "output"));
    }

    @Test
    public void test_shouldWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        String id = mavenService.runTests();
        verify(testEvents).raiseEvent(any(TestStartEvent.class));
        verify(testEvents).raiseEvent(refEq(new TestEndEvent(id, true, null), "output"));
    }

    @Test
    public void deploy_shoudWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        String id = mavenService.deploy();
        verify(deployEvents).raiseEvent(any(DeployStartEvent.class));
        verify(deployEvents).raiseEvent(refEq(new DeployEndEvent(id, true, null), "output"));
    }

    @Test
    public void testTestFail() {
        mavenService.setProjectPath(getPath("test-unit-fail"));
        String id = mavenService.runTests();
        verify(testEvents).raiseEvent(refEq(new TestEndEvent(id, false, null), "output"));
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
        return getClass().getClassLoader().getResource(folder).getPath();
    }

}
