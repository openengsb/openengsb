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

package org.openengsb.domains.scm.git.internal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.storage.file.FileRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GitServiceImplTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File remoteDirectory;
    private File localDirectory;
    private FileRepository repository;

    private GitServiceImpl service;

    @Before
    public void setup() throws Exception {
        remoteDirectory = tempFolder.newFolder("remote");
        localDirectory = tempFolder.newFolder("local");
        repository = RepositoryFixture.createRepository(remoteDirectory);
        service = new GitServiceImpl();
        service.setLocalWorkspace(localDirectory.getAbsolutePath());
        service.setRemoteLocation(remoteDirectory.toURI().toURL().toExternalForm());
        service.setWatchBranch("master");
    }

    @Test
    public void pollWithEmptyWorkspace_shouldCloneRemoteRepository() throws IOException {
        assertThat(service.poll(), is(true));
        ObjectId remote = service.getRepository().resolve("refs/remotes/origin/master");
        assertThat(remote, notNullValue());
        assertThat(remote,
            is(repository.resolve("refs/heads/master")));
    }

    @Test
    public void pollAgainFromSameRepoState_shouldReturnFalseFromPoll() {
        assertThat(service.poll(), is(true));
        assertThat(service.poll(), is(false));
    }

    @Test
    public void poll_shouldPullChangesIntoLocalBranch() {
        assertThat(service.poll(), is(true));
        assertThat(new File(localDirectory, "testfile").isFile(), is(true));
    }

    @Test
    public void pollFromUpdatedRemote_shouldUpdateLocal() throws Exception {
        assertThat(service.poll(), is(true));
        Git git = new Git(repository);
        RepositoryFixture.addFile(git, "second");
        RepositoryFixture.commit(git, "second commit");
        assertThat(new File(localDirectory, "second").isFile(), is(false));
        assertThat(service.poll(), is(true));
        assertThat(new File(localDirectory, "second").isFile(), is(true));
        assertThat(service.poll(), is(false));
    }

    @Test
    public void pollWithNoExistingWatchBranch_shouldReturnFalse() {
        service.setWatchBranch("unknown");
        assertThat(service.poll(), is(false));
    }

    @Test
    public void exportRepository_shouldCreateFullCopyOfCurrentRepoState() {
        service.poll();
        assertThat(new File(localDirectory, "testfile").isFile(), is(true));
        File exportDirectory = tempFolder.newFolder("export");
        service.export(exportDirectory);
        assertThat(new File(exportDirectory, "testfile").isFile(), is(true));
        assertThat(new File(exportDirectory, ".git").isDirectory(), is(false));
    }
}
