/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.connector.git.internal;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.eclipse.jgit.lib.Constants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.domain.scm.CommitRef;

public class UserGitServiceImplUT {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File remoteDirectory;
    private File localDirectory;

    private GitServiceImpl service;

    @Before
    public void setup() throws Exception {
        remoteDirectory = tempFolder.newFolder("remote");
        localDirectory = tempFolder.newFolder("local");
        service = new GitServiceImpl("42");
        service.setLocalWorkspace(localDirectory.getAbsolutePath());
        service.setRemoteLocation(remoteDirectory.toURI().toURL().toExternalForm().replace("%20", " "));
        service.setWatchBranch("master");
    }

    @Test
    public void updateWithEmptyWorkspace_shouldCloneRemoteSSHRepository() throws Exception {
        service.setRemoteLocation("git@github.com:Mercynary/myTestRepo.git");
        List<CommitRef> commits = service.update();
        assertThat(commits.size(), greaterThan(0));
        assertThat(commits.get(0).getStringRepresentation(),
            is(service.getRepository().resolve(Constants.HEAD).name()));
    }

}
