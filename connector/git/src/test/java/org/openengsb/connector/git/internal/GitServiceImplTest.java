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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.compress.archivers.zip.ZipFile;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.connector.git.domain.GitCommitRef;
import org.openengsb.connector.git.domain.GitTagRef;
import org.openengsb.domain.scm.CommitRef;
import org.openengsb.domain.scm.ScmException;
import org.openengsb.domain.scm.TagRef;

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
    public void exportRepository_shouldReturnZipFileWithRepoEntries() throws Exception {
        localRepository = RepositoryFixture.createRepository(localDirectory);

        String dir = "testDirectory";
        String file = "myTestFile";
        File parent = new File(localDirectory, dir);
        parent.mkdirs();
        File child = new File(parent, file);
        FileWriter fw = new FileWriter(child);
        fw.write(file + "\n");
        fw.close();

        String pattern = dir + "/" + file;
        Git git = new Git(localRepository);
        git.add().addFilepattern(pattern).call();
        git.commit().setMessage("My msg").call();

        ZipFile zipFile = new ZipFile(service.export());
        assertThat(zipFile.getEntry("testfile").getName(), is("testfile"));
        assertThat(zipFile.getEntry(dir + "/").getName(), is(dir + "/"));
        assertThat(zipFile.getEntry(dir + "\\" + file).getName(), is(dir + "\\" + file));
    }

    public void exportRepositoryByRef_shouldReturnZipFileWithRepoEntries() throws Exception {
        localRepository = RepositoryFixture.createRepository(localDirectory);

        String dir = "testDirectory";
        String file = "myTestFile";
        File parent = new File(localDirectory, dir);
        parent.mkdirs();
        File child = new File(parent, file);
        FileWriter fw = new FileWriter(child);
        fw.write(file + "\n");
        fw.close();

        String pattern = dir + "/" + file;
        Git git = new Git(localRepository);
        git.add().addFilepattern(pattern).call();
        git.commit().setMessage("My msg").call();

        AnyObjectId headId = localRepository.resolve(Constants.HEAD);
        RevWalk rw = new RevWalk(localRepository);
        RevCommit head = rw.parseCommit(headId);
        rw.release();

        ZipFile zipFile = new ZipFile(service.export(new GitCommitRef(head)));
        assertThat(zipFile.getEntry("testfile").getName(), is("testfile"));
        assertThat(zipFile.getEntry(dir + "/").getName(), is(dir + "/"));
        assertThat(zipFile.getEntry(dir + "\\" + file).getName(), is(dir + "\\" + file));
    }

    @Test
    public void getFileFromHeadCommit_shouldReturnFileWithCorrectContent() throws Exception {
        localRepository = RepositoryFixture.createRepository(localDirectory);

        String fileName = "myFile";
        Git git = new Git(localRepository);
        RepositoryFixture.addFile(git, fileName);
        RepositoryFixture.commit(git, "Commited my file");

        File file = service.get(fileName);
        String content = new BufferedReader(new FileReader(file)).readLine();
        assertThat(content, is(fileName));
    }

    @Test
    public void getFileFromCommitByRef_shouldReturnFileWithCorrectContent() throws Exception {
        String fileName = "myFile";

        localRepository = RepositoryFixture.createRepository(localDirectory);
        AnyObjectId head = localRepository.resolve(Constants.HEAD);
        RevWalk rw = new RevWalk(localRepository);
        RevCommit headCommit = rw.parseCommit(head);
        rw.release();
        Git git = new Git(localRepository);
        RepositoryFixture.addFile(git, fileName);
        RepositoryFixture.commit(git, "Commited my file");

        File file = service.get("testfile", new GitCommitRef(headCommit));
        String content = new BufferedReader(new FileReader(file)).readLine();
        assertThat(content, is("testfile"));
    }

    @Test(expected = ScmException.class)
    public void getFileFromCommitByNonExistingRef_shouldThrowSCMException() throws Exception {
        localRepository = RepositoryFixture.createRepository(localDirectory);

        File file = service.get("testfile", new GitCommitRef(null));
        String content = new BufferedReader(new FileReader(file)).readLine();
        assertThat(content, is("testfile"));
    }

    @Test
    public void addFile_shouldReturnHeadReference() throws IOException {
        File toCommit = new File(localDirectory, "testfile");
        toCommit.createNewFile();
        CommitRef commitRef = service.add("testcomment", toCommit);
        assertThat(commitRef, notNullValue());
        localRepository = service.getRepository();
        assertThat(commitRef.getStringRepresentation(), is(localRepository.resolve(Constants.HEAD).name()));
    }

    @Test
    public void addNonExistingFile_shouldRaiseException() throws IOException {
        File toCommit = new File(localDirectory, "testfile");
        try {
            service.add("testcomment", toCommit);
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void addDirectory_shouldReturnObjectIdForChild() throws Exception {
        localRepository = RepositoryFixture.createRepository(localDirectory);

        String dir = "testDirectory";
        String file = "testFile";
        File parent = new File(localDirectory, dir);
        parent.mkdirs();
        File child = new File(parent, file);
        FileWriter fw = new FileWriter(child);
        fw.write(file + "\n");
        fw.close();

        CommitRef commitRef = service.add("testComment", parent);
        assertThat(commitRef, is(notNullValue()));

        AnyObjectId id = localRepository.resolve(Constants.HEAD);
        RevCommit commit = new RevWalk(localRepository).parseCommit(id);
        String search = dir + "/" + file;
        TreeWalk treeWalk = TreeWalk.forPath(localRepository, search, new AnyObjectId[] { commit.getTree() });
        assertThat(treeWalk, is(notNullValue()));

        ObjectId objectId = treeWalk.getObjectId(treeWalk.getTreeCount() - 1);
        assertThat(objectId, not(ObjectId.zeroId()));
    }

    @Test
    public void addFileNotInWorkingfolder_shouldRaiseException() throws IOException {
        File toCommit = tempFolder.newFile("testfile");
        toCommit.createNewFile();
        try {
            service.add("testcomment", toCommit);
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void removeFileFromRepository_shouldReturnNewCommitRefAndDeleteFile() throws Exception {
        localRepository = RepositoryFixture.createRepository(localDirectory);
        AnyObjectId headId = localRepository.resolve(Constants.HEAD);
        RevWalk rw = new RevWalk(localRepository);
        RevCommit head = rw.parseCommit(headId);
        rw.release();

        CommitRef ref = service.remove("remove", new File(localDirectory, "testfile"));
        assertThat(head.name(), not(ref.getStringRepresentation()));

        File removed = new File(localDirectory, "testfile");
        assertThat(removed.exists(), is(false));
    }

    @Test
    public void removeDirectoryFromRepository_shouldReturnNewCommitRefAndDeleteFiles() throws Exception {
        localRepository = RepositoryFixture.createRepository(localDirectory);

        String dir = "testDirectory";
        String file = "testFile";
        File parent = new File(localDirectory, dir);
        parent.mkdirs();
        File child = new File(parent, file);
        FileWriter fw = new FileWriter(child);
        fw.write(file + "\n");
        fw.close();
        assertThat(child.exists(), is(true));

        Git git = new Git(localRepository);
        git.add().addFilepattern(dir + "/" + file).call();
        git.commit().setMessage("comment").call();

        AnyObjectId headId = localRepository.resolve(Constants.HEAD);
        RevWalk rw = new RevWalk(localRepository);
        RevCommit head = rw.parseCommit(headId);
        rw.release();

        CommitRef ref = service.remove("remove", new File(localDirectory, dir));
        assertThat(head.name(), not(ref.getStringRepresentation()));

        File removed = new File(localDirectory, dir + "/" + file);
        assertThat(removed.exists(), is(false));
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

    @Test
    public void tagHeadWithName_shouldReturnTagRefWithName() throws Exception {
        localRepository = RepositoryFixture.createRepository(localDirectory);
        String tagName = "newTag";
        TagRef tag = service.tagRepo(tagName);
        assertThat(tag, notNullValue());
        assertThat(tagName, is(tag.getTagName()));
        AnyObjectId tagId = localRepository.resolve(tagName);
        assertThat(tagId.name(), is(tag.getStringRepresentation()));
        RevTag revTag = new RevWalk(localRepository).parseTag(tagId);
        AnyObjectId head = localRepository.resolve(Constants.HEAD);
        assertThat(revTag.getObject().name(), is(head.name()));
    }

    @Test(expected = ScmException.class)
    public void tagHeadAgainWithSameName_shouldThrowSCMException() throws Exception {
        localRepository = RepositoryFixture.createRepository(localDirectory);
        String tagName = "newTag";
        TagRef tag = service.tagRepo(tagName);
        assertThat(tag, notNullValue());
        assertThat(tagName, is(tag.getTagName()));
        service.tagRepo(tagName);
    }

    @Test(expected = ScmException.class)
    public void tagEmptyRepoWithName_shouldThrowSCMException() throws Exception {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        localRepository = builder.setWorkTree(localDirectory).build();
        localRepository.create();
        service.tagRepo("newTag");
    }

    @Test
    public void tagCommitRefWithName_shouldReturnTagRefWithName() throws Exception {
        localRepository = RepositoryFixture.createRepository(localDirectory);
        RevWalk walk = new RevWalk(localRepository);
        RevCommit head = walk.lookupCommit(localRepository.resolve(Constants.HEAD));
        CommitRef ref = new GitCommitRef(head);
        String tagName = "newTag";
        TagRef tag = service.tagRepo(tagName, ref);
        assertThat(tag, notNullValue());
        assertThat(tagName, is(tag.getTagName()));
        AnyObjectId tagId = localRepository.resolve(tagName);
        assertThat(tagId.name(), is(tag.getStringRepresentation()));
        RevTag revTag = new RevWalk(localRepository).parseTag(tagId);
        assertThat(revTag.getObject().name(), is(head.name()));
    }

    @Test
    public void getCommitRefForTagRef_shouldReturnTaggedCommitRef() throws Exception {
        localRepository = RepositoryFixture.createRepository(localDirectory);
        RevWalk walk = new RevWalk(localRepository);
        RevCommit head = walk.lookupCommit(localRepository.resolve(Constants.HEAD));
        String tagName = "newTag";
        TagCommand tagCommand = new Git(localRepository).tag();
        TagRef tag = new GitTagRef(tagCommand.setName(tagName).call());
        assertThat(tag, notNullValue());
        assertThat(tagName, is(tag.getTagName()));
        CommitRef commitRef = service.getCommitRefForTag(tag);
        assertThat(head.name(), is(commitRef.getStringRepresentation()));
    }
}
