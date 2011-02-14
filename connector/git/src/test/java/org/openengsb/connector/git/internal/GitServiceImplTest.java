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

package org.openengsb.connector.git.internal;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.storage.file.FileRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.domain.scm.CommitRef;

public class GitServiceImplTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File remoteDirectory;
    private File localDirectory;
    private FileRepository remoteRepository;
    private FileRepository localRepository;

    private GitServiceImpl service;

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

    @Test
    public void updateWithEmptyWorkspace_shouldCloneRemoteRepository() throws Exception {
        List<CommitRef> commits = service.update();
        assertThat(commits.size(), is(1));
        ObjectId remote = service.getRepository().resolve("refs/remotes/origin/master");
        assertThat(remote, notNullValue());
        assertThat(remote, is(remoteRepository.resolve("refs/heads/master")));
        assertThat(commits.get(0).getStringRepresentation(), is(service.getRepository().resolve(Constants.HEAD).name()));
    }

    @Test
    public void updateWithEmptyWorkspace_shouldCloneRemoteSSHRepository() throws Exception {
        service.setRemoteLocation("git@github.com:Mercynary/myTestRepo.git");
        List<CommitRef> commits = service.update();
        assertThat(commits.size(), greaterThan(0));
        assertThat(commits.get(0).getStringRepresentation(), is(service.getRepository().resolve(Constants.HEAD).name()));
    }

    @Test
    public void updateAgainFromSameRepoState_shouldReturnFalseFromPoll() {
        List<CommitRef> updateOne = service.update();
        assertThat(updateOne.size(), is(1));
        List<CommitRef> updateTwo = service.update();
        assertThat(updateTwo.size(), is(0));
    }

    @Test
    public void update_shouldPullChangesIntoLocalBranch() {
        List<CommitRef> updateOne = service.update();
        assertThat(updateOne.size(), is(1));
        assertThat(new File(localDirectory, "testfile").isFile(), is(true));
    }

    @Test
    public void updateFromUpdatedRemote_shouldUpdateLocal() throws Exception {
        List<CommitRef> updateOne = service.update();
        assertThat(updateOne.size(), is(1));
        Git git = new Git(remoteRepository);
        RepositoryFixture.addFile(git, "second");
        RepositoryFixture.commit(git, "second commit");
        assertThat(new File(localDirectory, "second").isFile(), is(false));
        List<CommitRef> updateTwo = service.update();
        assertThat(updateTwo.size(), is(1));
        assertThat(new File(localDirectory, "second").isFile(), is(true));
        List<CommitRef> updateThree = service.update();
        assertThat(updateThree.size(), is(0));
    }

    @Test
    public void updateWithNoExistingWatchBranch_shouldReturnFalse() {
        service.setWatchBranch("unknown");
        List<CommitRef> updateOne = service.update();
        assertThat(updateOne, nullValue());
    }

    @Test
    public void exportRepository_shouldCreateFullCopyOfCurrentRepoState() {
        service.update();
        assertThat(new File(localDirectory, "testfile").isFile(), is(true));
        File exportDirectory = tempFolder.newFolder("export");
        service.export(exportDirectory);
        assertThat(new File(exportDirectory, "testfile").isFile(), is(true));
        assertThat(new File(exportDirectory, ".git").isDirectory(), is(false));
    }

    @Test
    public void commitFile_shouldReturnHeadReference() throws IOException {
        File toCommit = new File(localDirectory, "testfile");
        toCommit.createNewFile();
        CommitRef commitRef = service.add("testcomment", toCommit);
        assertThat(commitRef, notNullValue());
        localRepository = service.getRepository();
        assertThat(commitRef.getStringRepresentation(), is(localRepository.resolve(Constants.HEAD).name()));
    }

    @Test
    public void commitNonExistingFile_shouldRaiseException() throws IOException {
        File toCommit = new File(localDirectory, "testfile");
        try {
            service.add("testcomment", toCommit);
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void commitFileNotAFile_shouldRaiseException() throws IOException {
        File toCommit = new File(localDirectory, "testfile");
        toCommit.mkdirs();
        try {
            service.add("testcomment", toCommit);
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void commitFileNotInWorkingfolder_shouldRaiseException() throws IOException {
        File toCommit = tempFolder.newFile("testfile");
        try {
            service.add("testcomment", toCommit);
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void existsFilenameInHeadCommit_shouldReturnTrue() throws IOException {
        File commitOne = new File(localDirectory, "commitOne");
        File commitTwo = new File(localDirectory, "commitTwo");
        commitOne.createNewFile();
        commitTwo.createNewFile();
        service.add("testcomment", commitOne);
        service.add("testcomment", commitTwo);
        assertThat(service.exists("commitOne"), is(true));
    }

    @Test
    public void existsFilenameInReferencedCommit_shouldReturnTrue() throws IOException {
        File commitOne = new File(localDirectory, "commitOne");
        File commitTwo = new File(localDirectory, "commitTwo");
        commitOne.createNewFile();
        commitTwo.createNewFile();
        CommitRef commitRefOne = service.add("testcomment", commitOne);
        service.add("testcomment", commitTwo);
        assertThat(service.exists("commitOne", commitRefOne), is(true));
    }

    @Test
    public void existsFilenameOfNotExistingFile_shouldReturnFalse() throws IOException {
        File commitOne = new File(localDirectory, "commitOne");
        File commitTwo = new File(localDirectory, "commitTwo");
        commitOne.createNewFile();
        commitTwo.createNewFile();
        service.add("testcomment", commitOne);
        service.add("testcomment", commitTwo);
        assertThat(service.exists("commitThree"), is(false));
    }

    @Test
    public void existsFilenameInPriorCommitToFilecommit_shouldReturnFalse() throws IOException {
        File commitOne = new File(localDirectory, "commitOne");
        File commitTwo = new File(localDirectory, "commitTwo");
        commitOne.createNewFile();
        commitTwo.createNewFile();
        CommitRef commitRefOne = service.add("testcomment", commitOne);
        service.add("testcomment", commitTwo);
        assertThat(service.exists("commitTwo", commitRefOne), is(false));
    }
}
