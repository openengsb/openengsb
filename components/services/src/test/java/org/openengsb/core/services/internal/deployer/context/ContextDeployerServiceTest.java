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

package org.openengsb.core.services.internal.deployer.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.felix.fileinstall.ArtifactInstaller;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.core.api.context.ContextCurrentService;

public class ContextDeployerServiceTest {

    private ContextDeployerService contextDeployerService;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        contextDeployerService = new ContextDeployerService();
    }

    @Test
    public void testContextDeployerService_isAnArtifactListener() {
        ArtifactInstaller contextDeployerService = new ContextDeployerService();

        assertThat(contextDeployerService, is(notNullValue()));
    }

    @Test
    public void testContextDeployerService_canHandleContextFiles() throws IOException {
        File contextFile = temporaryFolder.newFile("example.context");

        assertThat(contextDeployerService.canHandle(contextFile), is(true));
    }

    @Test
    public void testUnknownFiles_shouldNotBeHandledByDeployer() throws IOException {
        File otherFile = temporaryFolder.newFile("other.connector");

        assertThat(contextDeployerService.canHandle(otherFile), is(false));
    }

    @Test
    public void testInstallContextFile_shouldCreateContext() throws Exception {
        File contextFile = temporaryFolder.newFile("newContextId.context");
        ContextCurrentService contextCurrentService = mock(ContextCurrentService.class);
        contextDeployerService.setContextCurrentService(contextCurrentService);

        contextDeployerService.install(contextFile);

        verify(contextCurrentService).createContext("newContextId");
    }

    @Test
    public void testUpdatedContext_shouldNotBeCreatedAgain() throws Exception {
        File contextFile = temporaryFolder.newFile("newContextId.context");
        ContextCurrentService contextCurrentService = mock(ContextCurrentService.class);
        contextDeployerService.setContextCurrentService(contextCurrentService);

        contextDeployerService.install(contextFile);
        when(contextCurrentService.getAvailableContexts()).thenReturn(Arrays.asList("newContextId"));        
        contextDeployerService.update(contextFile);

        verify(contextCurrentService, times(1)).createContext("newContextId");
    }
}
