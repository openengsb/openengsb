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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
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
    public void pollWithEmptyWorkspace_shouldCloneRemoteRepository() throws IOException {
        assertThat(service.poll(), is(true));
        ObjectId remote = service.getRepository().resolve("refs/remotes/origin/master");
        assertThat(remote, notNullValue());
        assertThat(remote, is(remoteRepository.resolve("refs/heads/master")));
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
        Git git = new Git(remoteRepository);
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

    @Test
    public void commitFile_shouldReturnHeadReference() throws IOException {
        File toCommit = new File(localDirectory, "testfile");
        toCommit.createNewFile();
        CommitRef commitRef = service.commit(toCommit, "testcomment");
        assertThat(commitRef, notNullValue());
        localRepository = service.getRepository();
        assertThat(commitRef.getStringRepresentation(), is(localRepository.resolve(Constants.HEAD).name()));
    }

    @Test
    public void commitNonExistingFile_shouldRaiseException() throws IOException {
        File toCommit = new File(localDirectory, "testfile");
        try {
            service.commit(toCommit, "testcomment");
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void commitFileNotAFile_shouldRaiseException() throws IOException {
        File toCommit = new File(localDirectory, "testfile");
        toCommit.mkdirs();
        try {
            service.commit(toCommit, "testcomment");
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void commitFileNotInWorkingfolder_shouldRaiseException() throws IOException {
        File toCommit = tempFolder.newFile("testfile");
        try {
            service.commit(toCommit, "testcomment");
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void commitDirectoryNotADirectory_shouldRaiseException() throws IOException {
        File toCommit = new File(localDirectory, "testfile");
        toCommit.createNewFile();
        try {
            service.commit(toCommit, "testcomment", true);
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void commitDirectoryRecursive_shouldReturnHeadReference() throws IOException {
        File folderTest = new File(localDirectory, "testfolder");
        File folderOne = new File(folderTest, "subfolder1");
        File folderTwo = new File(folderTest, "subfolder2");
        folderOne.mkdirs();
        folderTwo.mkdirs();
        File commitOne = new File(folderOne, "commitOne");
        File commitTwo = new File(folderOne, "commitTwo");
        commitOne.createNewFile();
        commitTwo.createNewFile();
        CommitRef commitRef = service.commit(folderTest, "testcomment", true);
        assertThat(commitRef, notNullValue());
        localRepository = service.getRepository();
        AnyObjectId id = localRepository.resolve(Constants.HEAD);
        assertThat(id.name(), is(commitRef.getStringRepresentation()));
        RevCommit commit = new RevWalk(localRepository).parseCommit(id);
        assertThat("testcomment", is(commit.getFullMessage()));
    }

    @Test
    public void commitDirectoryNonRecursiveWoFilesAtToplevel_shouldReturnNullReference() throws IOException {
        File folderTest = new File(localDirectory, "testfolder");
        File folderOne = new File(folderTest, "subfolder1");
        File folderTwo = new File(folderTest, "subfolder2");
        folderOne.mkdirs();
        folderTwo.mkdirs();
        File commitOne = new File(folderOne, "commitOne");
        File commitTwo = new File(folderOne, "commitTwo");
        commitOne.createNewFile();
        commitTwo.createNewFile();
        CommitRef commitRef = service.commit(folderTest, "testcomment", false);
        assertThat(null, is(commitRef));
    }

    @Test
    public void commitDirectoryNonRecursiveWithFilesAtToplevel_shouldReturnHeadReference() throws IOException {
        File folderTest = new File(localDirectory, "testfolder");
        File folderOne = new File(folderTest, "subfolder1");
        File folderTwo = new File(folderTest, "subfolder2");
        folderOne.mkdirs();
        folderTwo.mkdirs();
        File commitOne = new File(folderTest, "commitOne");
        File commitTwo = new File(folderOne, "commitTwo");
        commitOne.createNewFile();
        commitTwo.createNewFile();
        CommitRef commitRef = service.commit(folderTest, "testcomment", false);
        localRepository = service.getRepository();
        AnyObjectId id = localRepository.resolve(Constants.HEAD);
        assertThat(id.name(), is(commitRef.getStringRepresentation()));
        RevCommit commit = new RevWalk(localRepository).parseCommit(id);
        assertThat("testcomment", is(commit.getFullMessage()));
    }

    @Test
    public void existsFilenameInHeadCommit_shouldReturnTrue() throws IOException {
        File commitOne = new File(localDirectory, "commitOne");
        File commitTwo = new File(localDirectory, "commitTwo");
        commitOne.createNewFile();
        commitTwo.createNewFile();
        service.commit(commitOne, "testcomment");
        service.commit(commitTwo, "testcomment");
        assertThat(service.exists("commitOne"), is(true));
    }

    @Test
    public void existsFilenameInReferencedCommit_shouldReturnTrue() throws IOException {
        File commitOne = new File(localDirectory, "commitOne");
        File commitTwo = new File(localDirectory, "commitTwo");
        commitOne.createNewFile();
        commitTwo.createNewFile();
        CommitRef commitRefOne = service.commit(commitOne, "testcomment");
        service.commit(commitTwo, "testcomment");
        assertThat(service.exists("commitOne", commitRefOne), is(true));
    }

    @Test
    public void existsFilenameOfNotExistingFile_shouldReturnFalse() throws IOException {
        File commitOne = new File(localDirectory, "commitOne");
        File commitTwo = new File(localDirectory, "commitTwo");
        commitOne.createNewFile();
        commitTwo.createNewFile();
        service.commit(commitOne, "testcomment");
        service.commit(commitTwo, "testcomment");
        assertThat(service.exists("commitThree"), is(false));
    }

    @Test
    public void existsFilenameInPriorCommitToFilecommit_shouldReturnFalse() throws IOException {
        File commitOne = new File(localDirectory, "commitOne");
        File commitTwo = new File(localDirectory, "commitTwo");
        commitOne.createNewFile();
        commitTwo.createNewFile();
        CommitRef commitRefOne = service.commit(commitOne, "testcomment");
        service.commit(commitTwo, "testcomment");
        assertThat(service.exists("commitTwo", commitRefOne), is(false));
    }
}
