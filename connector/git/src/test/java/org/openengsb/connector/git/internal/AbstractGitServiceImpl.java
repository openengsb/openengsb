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

package org.openengsb.connector.git.internal;

import java.io.File;

import org.eclipse.jgit.storage.file.FileRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class AbstractGitServiceImpl {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    protected File remoteDirectory;
    protected File localDirectory;
    protected FileRepository remoteRepository;
    protected FileRepository localRepository;

    protected GitServiceImpl service;

    @Before
    public void setup() throws Exception {
        remoteDirectory = tempFolder.newFolder("remote");
        localDirectory = tempFolder.newFolder("local");
        remoteRepository = RepositoryFixture.createRepository(remoteDirectory);
        service = new GitServiceImpl("42");
        service.setLocalWorkspace(localDirectory.getAbsolutePath());
        service.setRemoteLocation(remoteDirectory.toURI().toURL().toExternalForm().replace("%20", " "));
        service.setWatchBranch("master");
    }
}
