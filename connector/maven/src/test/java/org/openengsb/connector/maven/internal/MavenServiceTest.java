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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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
        buildEvents = Mockito.mock(BuildDomainEvents.class);
        testEvents = Mockito.mock(TestDomainEvents.class);
        deployEvents = Mockito.mock(DeployDomainEvents.class);
        mavenService.setBuildEvents(buildEvents);
        mavenService.setTestEvents(testEvents);
        mavenService.setDeployEvents(deployEvents);
    }

    @Test
    public void build_shouldWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        assertThat(mavenService.build(), is(true));
        Mockito.verify(buildEvents).raiseEvent(Mockito.any(BuildStartEvent.class));
        Mockito.verify(buildEvents).raiseEvent(Mockito.any(BuildEndEvent.class));
    }

    @Test
    public void test_shouldWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        assertThat(mavenService.runTests(), is(true));
        Mockito.verify(testEvents).raiseEvent(Mockito.any(TestStartEvent.class));
        Mockito.verify(testEvents).raiseEvent(Mockito.any(TestEndEvent.class));
    }

    @Test
    public void deploy_shoudWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        assertThat(mavenService.deploy(), is(true));
        Mockito.verify(deployEvents).raiseEvent(Mockito.any(DeployStartEvent.class));
        Mockito.verify(deployEvents).raiseEvent(Mockito.any(DeployEndEvent.class));
    }

    @Test
    public void testTestFail() {
        mavenService.setProjectPath(getPath("test-unit-fail"));
        assertThat(mavenService.runTests(), is(false));
    }

    private String getPath(String folder) {
        return getClass().getClassLoader().getResource(folder).getPath();
    }

}
