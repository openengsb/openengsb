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
import org.openengsb.domain.deploy.DeployDomainEvents;
import org.openengsb.domain.test.TestDomainEvents;
import org.openengsb.domain.test.TestEndEvent;
import org.openengsb.domain.test.TestStartEvent;

public class MavenServiceTest {

    private MavenServiceImpl mavenService;
    private TestDomainEvents testEvents;

    @Before
    public void setUp() {
        this.mavenService = new MavenServiceImpl();
        mavenService.setBuildEvents(Mockito.mock(BuildDomainEvents.class));
        testEvents = Mockito.mock(TestDomainEvents.class);
        mavenService.setTestEvents(testEvents);
        mavenService.setDeployEvents(Mockito.mock(DeployDomainEvents.class));
    }

    @Test
    public void testTestSuccess() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        assertThat(mavenService.runTests(), is(true));
        Mockito.verify(testEvents).raiseEvent(Mockito.any(TestStartEvent.class));
        Mockito.verify(testEvents).raiseEvent(Mockito.any(TestEndEvent.class));
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
