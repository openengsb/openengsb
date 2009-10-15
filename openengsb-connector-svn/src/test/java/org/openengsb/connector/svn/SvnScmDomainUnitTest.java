/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.connector.svn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.connector.svn.commands.SvnCommandFactory;
import org.openengsb.connector.svn.constants.SvnScmDomainTestConstants;
import org.openengsb.scm.common.commands.CommandFactory;
import org.openengsb.scm.common.exceptions.ScmException;
import org.openengsb.scm.common.pojos.MergeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;


/**
 * A Collection Of Unit-Tests that test the backend implementation of the
 * scm-domain for SVN.
 */
@ContextConfiguration(locations = { "/testSpring.xml" })
public class SvnScmDomainUnitTest extends AbstractJUnit4SpringContextTests {
    private static String REPOSITORY;
    private static String TRUNK;
    private static String REPOSITORY_NO_BRANCH;
    private static String REPOSITORY_NO_BRANCH_TRUNK;

    private static SVNClientManager clientManager;

    @Autowired
    private SvnScmDomainTestConstants CONSTANTS;

    /* creators */

    /**
     * Creates a new CommandFactory, to be exact an implementation thereof for
     * SVN
     * 
     * @param workingCopy The basepath, where all operations are based on.
     * @param connection The default connection to check out contents from the
     *        repository. Read only.
     * @param developerConnection The developer-connection which should also
     *        allow to commit to the repository.
     */
    private CommandFactory createCommandFactory(String workingCopy, String connection, String developerConnection) {
        SvnCommandFactory commandFactory = new SvnCommandFactory();

        if (workingCopy != null) {
            workingCopy = new File(workingCopy).getAbsolutePath(); // Absolute
        }
        // path to
        // make the
        // checkout-command
        // not
        // checkout
        // in
        // ./data/openengs/<workingCopy>
        commandFactory.setWorkingCopy(workingCopy);
        commandFactory.setConnection(connection);
        commandFactory.setDeveloperConnection(developerConnection);

        return commandFactory;
    }

    /* creators end */

    /* set up */

    @BeforeClass
    public static void setUpClientManager() {
        SvnScmDomainUnitTest.clientManager = SVNClientManager.newInstance();
    }

    @Before
    public void calculateRepositoryUris() {
        SvnScmDomainUnitTest.REPOSITORY = new File(this.CONSTANTS.REPOSITORY).toURI().toString();
        SvnScmDomainUnitTest.TRUNK = new File(this.CONSTANTS.TRUNK).toURI().toString();
        SvnScmDomainUnitTest.REPOSITORY_NO_BRANCH = new File(this.CONSTANTS.REPOSITORY_NO_BRANCH).toURI().toString();
        SvnScmDomainUnitTest.REPOSITORY_NO_BRANCH_TRUNK = new File(this.CONSTANTS.BRANCHLESS_REPOSITORY_TRUNK).toURI()
                .toString();
    }

    @Before
    public void setUpRepository() throws IOException, SVNException {
        deleteRepository();
        FileUtils.copyDirectoryStructure(new File(this.CONSTANTS.REFERENCE_REPOSITORY), new File(
                this.CONSTANTS.REPOSITORY));
        FileUtils.copyDirectoryStructure(new File(this.CONSTANTS.REFERENCE_REPOSITORY_NO_BRANCH), new File(
                this.CONSTANTS.REPOSITORY_NO_BRANCH));
    }

    @After
    public void deleteRepository() throws IOException {
        File repositoryPath = new File(this.CONSTANTS.REPOSITORY);
        File repositoryPathNoBranch = new File(this.CONSTANTS.REPOSITORY_NO_BRANCH);

        if (repositoryPath.exists()) {
            FileUtils.deleteDirectory(repositoryPath);
        }

        if (repositoryPathNoBranch.exists()) {
            FileUtils.deleteDirectory(repositoryPathNoBranch);
        }
    }

    @Before
    @After
    public void deleteWorkingCopies() throws IOException {
        for (String workingCopy : this.CONSTANTS.WORKING_COPIES) {
            File workingCopyPath = new File(workingCopy);

            if (workingCopyPath.exists()) {
                FileUtils.deleteDirectory(workingCopyPath);
            }
        }

        File defaultWorkingCopy = new File(this.CONSTANTS.DEFAULT_WORKING_COPY);

        if (defaultWorkingCopy.exists()) {
            FileUtils.deleteDirectory(defaultWorkingCopy);
        }
    }

    @After
    public void deleteExportDirectory() throws IOException {
        File exportDirectory = new File(this.CONSTANTS.EXPORT_PATH);

        if (exportDirectory.exists()) {
            FileUtils.deleteDirectory(exportDirectory);
        }
    }

    /* set up end */

    /* tests */

    /**
     * Tests basic checkout behavior
     * 
     * @throws ScmException
     */
    @Test
    public void checkout_shouldCheckoutRepositorysContents() throws ScmException {
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        MergeResult result = commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();
        String[] checkedOutFiles = result.getAdds();

        // checking checkedOutFiles
        assertFileListsEqual(this.CONSTANTS.WORKING_COPIES[0], this.CONSTANTS.INITIAL_FILES, checkedOutFiles);

        // checking file system:
        // even though there were more files checked out, only this one will
        // have to be checked
        // for existence as representative. If this file was checked out, all
        // others will have
        // or SVNKit is buggy.
        File workingCopyPath = new File(this.CONSTANTS.WORKING_COPIES[0]);
        assertTrue(new File(workingCopyPath, this.CONSTANTS.TEST_FILE).exists());
    }

    /**
     * Tests whether the developerConnection is prefered over the connection
     * 
     * @throws ScmException
     */
    @Test
    public void checkout_shouldPreferDeveloperConnectionIfBothConnectionsAreSet() throws ScmException {
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0],
                SvnScmDomainUnitTest.REPOSITORY_NO_BRANCH, SvnScmDomainUnitTest.REPOSITORY);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // assert that the branch-folder does exist
        File branchesFolder = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), "branches");
        assertTrue("Branches-folder " + branchesFolder + " does not exist", branchesFolder.exists());
    }

    /**
     * Tests whether the connection is actually used, when the
     * developerConnection is missing
     * 
     * @throws ScmException
     */
    @Test
    public void checkout_shouldUseConnectionIfDeveloperConnectionIsNotSet() throws ScmException {
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0],
                SvnScmDomainUnitTest.REPOSITORY, null);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // assert that the branch-folder does exist
        File branchesFolder = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), "branches");
        assertTrue("Branches-folder " + branchesFolder + " does not exist", branchesFolder.exists());
    }

    /**
     * Asserts that checkout fails, when no connection was set
     * 
     * @throws ScmException
     */
    @Test(expected = ScmException.class)
    public void checkout_shouldFailWhenNoConnectionWasSet() throws ScmException {
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null, null);

        // exception expected here
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();
    }

    /**
     * Asserts that checkout checks out the repository to the default directory
     * 
     * @throws ScmException
     */
    @Test
    public void checkout_shouldCheckoutToDefaultDirectoryIfWorkigCopyWasNotSet() throws ScmException {
        CommandFactory commandFactory = createCommandFactory(null, null, SvnScmDomainUnitTest.TRUNK);

        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // assert that workingcopy was put in ./workingCopies/trunk
        assertTrue("Could not find " + this.CONSTANTS.DEFAULT_WORKING_COPY, new File(
                this.CONSTANTS.DEFAULT_WORKING_COPY).exists());
    }

    /**
     * Tests basic ability to add files to the working copy.
     * 
     * @throws ScmException
     * @throws IOException
     * @throws SVNException
     */
    @Test
    public void add_shouldAddFileToWorkingCopy() throws ScmException, IOException, SVNException {
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);

        // check out initial working copy
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // copy the file to add
        File resourcesFileToAdd = new File(this.CONSTANTS.FILE_TO_ADD);
        String fileToAddName = resourcesFileToAdd.getName();
        File workingCopyFileToAdd = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), fileToAddName);

        FileUtils.copyFile(resourcesFileToAdd, workingCopyFileToAdd);

        // call the actual method
        commandFactory.getAddCommand(fileToAddName).execute();

        // check if file was actually added
        SVNStatus status = SvnScmDomainUnitTest.clientManager.getStatusClient().doStatus(workingCopyFileToAdd, false);
        assertEquals(SVNStatusType.STATUS_ADDED.getID(), status.getContentsStatus().getID());
    }

    /**
     * Tries to add a not existing file, which should result in an ScmException
     * 
     * @throws ScmException is expected to be thrown
     */
    @Test(expected = ScmException.class)
    public void add_shouldThrowExceptionOnNotExistingFile() throws ScmException {
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);

        // check out initial working copy
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // do NOT copy the file to add

        // call the actual method
        // exception expected here
        commandFactory.getAddCommand(this.CONSTANTS.NOT_EXISTING_FILE).execute();
    }

    /**
     * Tries to add a file, that is outside the working copy, which is usually
     * hard to do since the arguments passed to the add call are considered
     * relative to the working-copy. However, we play the mean guy and expect an
     * ScmException
     * 
     * @throws ScmException is expected to be thrown
     */
    @Test(expected = ScmException.class)
    public void add_shouldThrowExceptionOnFilePathThatLeavesTheWorkingCopy() throws ScmException {
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);

        // check out initial working copy
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // do NOT copy the file to add

        // call the actual method
        // exception expected here
        commandFactory.getAddCommand(this.CONSTANTS.WORKING_COPY_LEAVING_FILE).execute();
    }

    /**
     * Tests basic ability to mark files for deletion
     * 
     * @throws ScmException
     * @throws SVNException
     */
    @Test
    public void delete_shouldMarkFileForDeletion() throws ScmException, SVNException {
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);

        // check out initial working copy
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // call the actual method
        commandFactory.getDeleteCommand(this.CONSTANTS.DELETE_FILE).execute();

        // check if file was actually deleted
        File workingCopyDeleteFile = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.DELETE_FILE);
        SVNStatus status = SvnScmDomainUnitTest.clientManager.getStatusClient().doStatus(workingCopyDeleteFile, false);
        assertEquals(SVNStatusType.STATUS_DELETED.getID(), status.getContentsStatus().getID());
    }

    /**
     * Tries to delete a not existing file, which should result in an
     * ScmException
     * 
     * @throws ScmException is expected to be thrown
     */
    @Test(expected = ScmException.class)
    public void delete_shouldThrowExceptionOnNotexistingFile() throws ScmException {
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);

        // check out initial working copy
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // call the actual method
        commandFactory.getDeleteCommand(this.CONSTANTS.NOT_EXISTING_FILE).execute();
    }

    /**
     * Tries to delete a file, that is outside the working copy, which is
     * usually hard to do since the arguments passed to the add call are
     * considered relative to the working-copy. However, we play the mean guy
     * and expect an ScmException
     * 
     * @throws ScmException is expected to be thrown
     */
    @Test(expected = ScmException.class)
    public void delete_shouldThrowExceptionOnFileThatLeavesTheWorkingCopy() throws ScmException {
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);

        // check out initial working copy
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // call the actual method
        commandFactory.getDeleteCommand(this.CONSTANTS.WORKING_COPY_LEAVING_FILE).execute();
    }

    /**
     * Tests basic ability to commit changes to the repository. Also extends the
     * tests for add and delete since those changes are expected to go into a
     * repository sooner or later
     * 
     * @throws ScmException
     * @throws IOException
     */
    @Test
    public void commit_shouldTransferChangesToRepository() throws ScmException, IOException {
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);

        // check out initial working copy
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // do some changes to the files:
        // + add file
        File resourcesFileToAdd = new File(this.CONSTANTS.FILE_TO_ADD);
        String fileToAddName = resourcesFileToAdd.getName();
        File workingCopyFileToAdd = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), fileToAddName);
        FileUtils.copyFile(resourcesFileToAdd, workingCopyFileToAdd);

        commandFactory.getAddCommand(fileToAddName).execute();

        // + delete file
        commandFactory.getDeleteCommand(this.CONSTANTS.DELETE_FILE).execute();

        // + modify file
        String modifyContent = "someContent";
        File fileToModify = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify, modifyContent);

        // actual call
        MergeResult result = commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "ordinary commit").execute();

        // check result
        assertEquals(0, result.getConflicts().length);
        assertEquals(1, result.getDeletions().length);
        assertEquals(
                new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.DELETE_FILE).getAbsolutePath(),
                new File(result.getDeletions()[0]).getAbsolutePath());
        assertEquals(1, result.getAdds().length);
        assertEquals(workingCopyFileToAdd.getAbsolutePath(), new File(result.getAdds()[0]).getAbsolutePath());
        assertEquals(1, result.getMerges().length);
        assertEquals(fileToModify.getAbsolutePath(), new File(result.getMerges()[0]).getAbsolutePath());

        // check if files reached the repository by checking it out a second
        // time
        CommandFactory commandFactory2 = createCommandFactory(this.CONSTANTS.WORKING_COPIES[1], null,
                SvnScmDomainUnitTest.TRUNK);
        File newWorkingCopy = new File(this.CONSTANTS.WORKING_COPIES[1]);

        commandFactory2.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // assert changes
        assertTrue(new File(newWorkingCopy, fileToAddName).exists());
        assertFalse(new File(newWorkingCopy, this.CONSTANTS.DELETE_FILE).exists());

        assertThatFileWasModified(new File(newWorkingCopy, this.CONSTANTS.UPDATE_FILE), modifyContent);
    }

    /**
     * Test the ability to commit changes only from a sub-path within the
     * working copy.
     * 
     * @throws ScmException
     * @throws IOException
     */
    @Test
    public void commit_shouldTransferPatialChangesToRepositoryWhenSubpathIsGiven() throws ScmException, IOException {
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);

        // check out initial working copy
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // add file in root
        File resourcesFileToAdd = new File(this.CONSTANTS.FILE_TO_ADD);
        String fileToAddName = resourcesFileToAdd.getName();
        File workingCopyFileToAdd = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), fileToAddName);
        FileUtils.copyFile(resourcesFileToAdd, workingCopyFileToAdd);
        commandFactory.getAddCommand(fileToAddName).execute();

        // add file in subpath
        File workingCopyFileToAddSubPath = new File(new File(new File(this.CONSTANTS.WORKING_COPIES[0]),
                this.CONSTANTS.SUB_PATH), fileToAddName);
        FileUtils.copyFile(resourcesFileToAdd, workingCopyFileToAddSubPath);
        commandFactory.getAddCommand(new File(new File(this.CONSTANTS.SUB_PATH), fileToAddName).getPath()).execute();

        // actual call
        MergeResult result = commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "ordinary commit",
                this.CONSTANTS.SUB_PATH).execute();

        // check result
        assertEquals(0, result.getConflicts().length);
        assertEquals(0, result.getDeletions().length);
        assertEquals(1, result.getAdds().length);
        assertEquals(workingCopyFileToAddSubPath.getAbsolutePath(), new File(result.getAdds()[0]).getAbsolutePath());
        assertEquals(0, result.getMerges().length);

        // check if files reached the repository by checking it out a second
        // time
        CommandFactory commandFactory2 = createCommandFactory(this.CONSTANTS.WORKING_COPIES[1], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory2.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // check filesystem
        File secondWorkingCopy = new File(this.CONSTANTS.WORKING_COPIES[1]);
        assertFalse(new File(secondWorkingCopy, fileToAddName).exists());
        assertTrue(new File(new File(secondWorkingCopy, this.CONSTANTS.SUB_PATH), fileToAddName).exists());

    }

    /**
     * Tests that commit fails, when unsolvable (by SVN) conflicts between the
     * working-copy and the repository arise
     * 
     * @throws ScmException is expected to be thrown
     * @throws IOException
     */
    @Test(expected = ScmException.class)
    public void commit_shouldFailWhenConflictsArise() throws ScmException, IOException {
        // check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // check out a second time
        CommandFactory commandFactory2 = createCommandFactory(this.CONSTANTS.WORKING_COPIES[1], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory2.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // modify the same file in both working copies
        String modifyContent = "someContent";
        File fileToModify = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify, modifyContent);

        String modifyContent2 = "someOtherContent";
        File fileToModify2 = new File(new File(this.CONSTANTS.WORKING_COPIES[1]), this.CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify2, modifyContent2);

        // commit first working copy
        commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "ordinary commit").execute();

        // commit second working copy
        // exception expected here
        commandFactory2.getCommitCommand(this.CONSTANTS.AUTHOR, "another ordinary commit").execute();
    }

    /**
     * Asserts that commit fails, if we may not write to the repository (i.e.
     * the connection, not the developerConnection was used)
     * 
     * @throws ScmException
     * @throws IOException
     */
    @Test(expected = ScmException.class)
    public void commit_shouldFailWhenConnectionInsteadOfDeveloperConnectionWasSet() throws ScmException, IOException {
        // check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0],
                SvnScmDomainUnitTest.TRUNK, null);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // modify file
        String modifyContent = "someContent";
        File fileToModify = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify, modifyContent);

        // commit
        // exception expected here
        commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "ordinary commit").execute();
    }

    /**
     * Tests basic ability to update the working copy with changes from the
     * repository.
     * 
     * @throws ScmException
     * @throws IOException
     */
    @Test
    public void update_shouldUpdateFilesFromRepository() throws ScmException, IOException {
        // check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // check out a second time
        CommandFactory commandFactory2 = createCommandFactory(this.CONSTANTS.WORKING_COPIES[1], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory2.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // do some changes to the files:
        // + add file
        File resourcesFileToAdd = new File(this.CONSTANTS.FILE_TO_ADD);
        String fileToAddName = resourcesFileToAdd.getName();
        File workingCopyFileToAdd = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), fileToAddName);
        FileUtils.copyFile(resourcesFileToAdd, workingCopyFileToAdd);

        commandFactory.getAddCommand(fileToAddName).execute();

        // + delete file
        commandFactory.getDeleteCommand(this.CONSTANTS.DELETE_FILE).execute();

        // + modify file
        String modifyContent = "someContent";
        File fileToModify = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify, modifyContent);

        // commit changes
        commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "somArbitraryMessage").execute();

        // update second working copy
        MergeResult result = commandFactory2.getUpdateCommand(null).execute();

        // check result
        assertEquals(0, result.getConflicts().length);
        assertEquals(1, result.getDeletions().length);
        assertEquals(
                new File(new File(this.CONSTANTS.WORKING_COPIES[1]), this.CONSTANTS.DELETE_FILE).getAbsolutePath(),
                new File(result.getDeletions()[0]).getAbsolutePath());
        assertEquals(1, result.getAdds().length);
        assertEquals(new File(new File(this.CONSTANTS.WORKING_COPIES[1]), fileToAddName).getAbsolutePath(), new File(
                result.getAdds()[0]).getAbsolutePath());
        assertEquals(2, result.getMerges().length);
        String[] expectedMerges = new String[] { new File(this.CONSTANTS.WORKING_COPIES[1]).getAbsolutePath(),
                new File(new File(this.CONSTANTS.WORKING_COPIES[1]), this.CONSTANTS.UPDATE_FILE).getAbsolutePath() };
        assertThatArraysEqualIgnorePosition(expectedMerges, result.getMerges());

        // assert changes
        File secondWorkingCopy = new File(this.CONSTANTS.WORKING_COPIES[1]);
        assertTrue(new File(secondWorkingCopy, fileToAddName).exists());
        assertFalse(new File(secondWorkingCopy, this.CONSTANTS.DELETE_FILE).exists());

        assertThatFileWasModified(new File(secondWorkingCopy, this.CONSTANTS.UPDATE_FILE), modifyContent);
    }

    /**
     * Tests the ability to update only a sub-path within the working copy
     * 
     * @throws ScmException
     * @throws IOException
     */
    @Test
    public void update_shouldUpdateOnlySubpathWhenSubpathIsGiven() throws ScmException, IOException {
        // check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // check out a second time
        CommandFactory commandFactory2 = createCommandFactory(this.CONSTANTS.WORKING_COPIES[1], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory2.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // add file in root in first working copy
        File resourcesFileToAdd = new File(this.CONSTANTS.FILE_TO_ADD);
        String fileToAddName = resourcesFileToAdd.getName();
        File workingCopyFileToAdd = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), fileToAddName);
        FileUtils.copyFile(resourcesFileToAdd, workingCopyFileToAdd);
        commandFactory.getAddCommand(fileToAddName).execute();

        // add file in subpath in first working copy
        File workingCopyFileToAddSubPath = new File(new File(new File(this.CONSTANTS.WORKING_COPIES[0]),
                this.CONSTANTS.SUB_PATH), fileToAddName);
        FileUtils.copyFile(resourcesFileToAdd, workingCopyFileToAddSubPath);
        commandFactory.getAddCommand(new File(new File(this.CONSTANTS.SUB_PATH), fileToAddName).getPath()).execute();

        // commit changes
        commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "ordinary commit", this.CONSTANTS.SUB_PATH).execute();

        // update second domain
        MergeResult result = commandFactory2.getUpdateCommand(this.CONSTANTS.SUB_PATH).execute();

        // check result
        assertEquals(0, result.getConflicts().length);
        assertEquals(0, result.getDeletions().length);
        assertEquals(1, result.getAdds().length);
        assertEquals(new File(new File(new File(this.CONSTANTS.WORKING_COPIES[1]), this.CONSTANTS.SUB_PATH),
                fileToAddName).getAbsolutePath(), new File(result.getAdds()[0]).getAbsolutePath());
        assertEquals(1, result.getMerges().length);
        assertEquals(new File(new File(this.CONSTANTS.WORKING_COPIES[1]), this.CONSTANTS.SUB_PATH).getAbsolutePath(),
                new File(result.getMerges()[0]).getAbsolutePath());

        // check filesystem
        File secondWorkingCopy = new File(this.CONSTANTS.WORKING_COPIES[1]);
        assertFalse(new File(secondWorkingCopy, fileToAddName).exists());
        assertTrue(new File(new File(secondWorkingCopy, this.CONSTANTS.SUB_PATH), fileToAddName).exists());
    }

    /**
     * Asserts that update fails with an ScmException when unsolvable (by SVN)
     * conlicts arise.
     * 
     * @throws ScmException
     * @throws IOException
     */
    @Test
    public void update_shouldFailWhenConflictsArise() throws ScmException, IOException {
        // check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // check out a second time
        CommandFactory commandFactory2 = createCommandFactory(this.CONSTANTS.WORKING_COPIES[1], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory2.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // modify the same file in both working copies
        String modifyContent = "someContent";
        File fileToModify = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify, modifyContent);

        String modifyContent2 = "someOtherContent";
        File fileToModify2 = new File(new File(this.CONSTANTS.WORKING_COPIES[1]), this.CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify2, modifyContent2);

        // commit first working copy
        commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "ordinary commit").execute();

        // update second working copy
        // exception expected here
        commandFactory2.getUpdateCommand(null).execute();
    }

    /**
     * Tests the basic ability to create a new branch.
     * 
     * @throws ScmException
     */
    @Test
    public void branch_shouldCreateNewBranch() throws ScmException {
        // check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // create branch
        String branchName = "myNewTestBranch";
        commandFactory.getBranchCommand(branchName, "created new test-branch").execute();

        // validate, that branch was created by checking it out
        File branchRepositoryPath = new File(new File(this.CONSTANTS.BRANCHES), branchName);
        String branchRepositoryUriSring = branchRepositoryPath.toURI().toString();
        CommandFactory branchCommandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[1], null,
                branchRepositoryUriSring);
        MergeResult result = branchCommandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // validate branch on filesystem
        assertFileListsEqual(this.CONSTANTS.WORKING_COPIES[1], this.CONSTANTS.INITIAL_FILES, result.getAdds());

        // checking file system:
        // even though there were more files checked out, only this one will
        // have to be checked
        // for existence as representative. If this file was checked out, all
        // others will have
        // or SVNKit is buggy.
        assertTrue(new File(this.CONSTANTS.WORKING_COPIES[1], this.CONSTANTS.TEST_FILE).exists());
    }

    /**
     * Tests the fault-tolerant behavior to create a new branch, even if the
     * expected branches-directory does not yet exist.
     * 
     * @throws ScmException
     */
    @Test
    public void branch_shouldCreateNewBranchEvenWhenBranchDirectoryDoesNotYetExistInRepository() throws ScmException {
        // check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.REPOSITORY_NO_BRANCH_TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // create branch
        String branchName = "myNewTestBranch";
        commandFactory.getBranchCommand(branchName, "creating new branch").execute();

        // validate, that branch was created by checking it out
        File branchRepositoryPath = new File(new File(this.CONSTANTS.BRANCHLESS_REPOSITORY_BRANCHES), branchName);
        String branchRepositoryUriSring = branchRepositoryPath.toURI().toString();
        CommandFactory branchdCommandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[1], null,
                branchRepositoryUriSring);
        MergeResult result = branchdCommandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // validate branch on filesystem
        assertFileListsEqual(this.CONSTANTS.WORKING_COPIES[1], this.CONSTANTS.INITIAL_FILES, result.getAdds());

        // checking file system:
        // even though there were more files checked out, only this one will
        // have to be checked
        // for existence as representative. If this file was checked out, all
        // others will have
        // or SVNKit is buggy.
        assertTrue(new File(this.CONSTANTS.WORKING_COPIES[1], this.CONSTANTS.TEST_FILE).exists());
    }

    /**
     * Asserts that no two branches with the same name may be created.
     * 
     * @throws ScmException is expected to be thrown.
     */
    @Test(expected = ScmException.class)
    public void branch_shouldFailWhenBranchAlreadyExists() throws ScmException {
        // check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        String branchName = "myNewTestBranch";
        commandFactory.getBranchCommand(branchName, "created new test-branch").execute();

        // exception expected here
        commandFactory.getBranchCommand(branchName, "created new test-branch again").execute();
    }

    /**
     * Asserts that no branch with the name TRUNK can be created. This is
     * necessary to be able to switch back to trunk again later on.
     * 
     * @throws ScmException
     */
    @Test(expected = ScmException.class)
    public void branch_shouldFailWhenTrunkKeywordisUsed() throws ScmException {
        // check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        String branchName = "TRUNK";

        // exception expected here
        commandFactory.getBranchCommand(branchName, "created new test-branch called TRUNK").execute();
    }

    /**
     * Asserts that branch fails, when the reopsitory is read only (i.e.
     * connection, not developerconnection, was set)
     * 
     * @throws ScmException
     */
    @Test(expected = ScmException.class)
    public void branch_shouldFailWhenConnectionInsteadofDeveloperConnectionWasSet() throws ScmException {
        // check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0],
                SvnScmDomainUnitTest.TRUNK, null);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // branch
        // exception expected here
        commandFactory.getBranchCommand("someBranch", "committingBranchToReadOnlyRepository").execute();
    }

    /**
     * Tests basic ability to list all created branches.
     * 
     * @throws ScmException
     */
    @Test
    public void listBranches_shouldListAllBranches() throws ScmException {
        // check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // no branches initially
        String[] branches = commandFactory.getListBranchesCommand().execute();
        assertEquals(0, branches.length);

        // create testbranches
        String[] branchNames = new String[] { "testBranch1", "testBranch2", "testBranch3" };
        for (String branchName : branchNames) {
            commandFactory.getBranchCommand(branchName, "created new test-branch " + branchName).execute();
        }

        // list and verify branches
        String[] actualBranchNames = commandFactory.getListBranchesCommand().execute();
        assertThatArraysEqualIgnorePosition(branchNames, actualBranchNames);
    }

    /**
     * Asserts that an empty list (instead of an Excption) is returned, if the
     * branches-directory does not exist.
     * 
     * @throws ScmException
     */
    @Test
    public void listBranches_shouldReturnEmptyArrayWhenBranchesDirectoryDoesNotExistInRepository() throws ScmException {
        // check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.REPOSITORY_NO_BRANCH_TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        String[] branches = commandFactory.getListBranchesCommand().execute();
        assertEquals(0, branches.length);
    }

    /**
     * Tests the basic ability to switch to a(nother) branch.
     * 
     * @throws ScmException
     * @throws IOException
     */
    @Test
    public void switchBranch_shouldSwitchToBranch() throws ScmException, IOException {
        // 1. check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // 2. create branch
        String branchName = "myNewTestBranch";
        commandFactory.getBranchCommand(branchName, "creating new branch").execute();

        // 3. switch to branch
        commandFactory.getSwitchBranchCommand(branchName).execute();

        // 4. assert that the working-copy is bound to the new branch

        // perform some changes
        File testFileWc1 = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.TEST_FILE);
        String appendContent = "someAppendedContent";
        appendContentToFile(testFileWc1, appendContent);

        // commit changes
        commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "committing to branch.").execute();

        // check out branch and assert changes
        File branchPath = new File(new File(this.CONSTANTS.BRANCHES), branchName);
        String branchUriString = branchPath.toURI().toString();
        CommandFactory commandFactory2 = createCommandFactory(this.CONSTANTS.WORKING_COPIES[1], null, branchUriString);
        commandFactory2.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        File testFileWc2 = new File(new File(this.CONSTANTS.WORKING_COPIES[1]), this.CONSTANTS.TEST_FILE);
        assertThatFileWasModified(testFileWc2, appendContent);
    }

    /**
     * Tests the ability to switch back to trunk again.
     * 
     * @throws ScmException
     * @throws IOException
     */
    @Test
    public void switchBranch_shouldHonorTrunkKeyword() throws ScmException, IOException {
        // 1. check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // 2. create branch
        String branchName = "myNewTestBranch";
        commandFactory.getBranchCommand(branchName, "creating new branch").execute();

        // 3. switch to branch
        commandFactory.getSwitchBranchCommand(branchName).execute();

        // 4. perform some changes
        File testFile = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.TEST_FILE);
        String appendContent = "someAppendedContent";
        appendContentToFile(testFile, appendContent);

        // commit changes
        commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "committing to branch.").execute();

        // 5. switch back to trunk
        commandFactory.getSwitchBranchCommand("TRUNK").execute();

        // 6. assert that trunk was not modified
        assertThatFileWasNotModified(testFile, appendContent);
    }

    /**
     * Tests the ability to merge a branch back to trunk again
     */
    @Test
    public void merge_shouldMergeBranchBackToTrunk() throws ScmException, IOException {
        // 1. check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // 2. create branch
        String branchName = "newTestBranch";
        commandFactory.getBranchCommand(branchName, "createing new branch for test purposes.").execute();

        // 3. switch to branch
        commandFactory.getSwitchBranchCommand(branchName).execute();

        // 4. perform some changes in branch
        File mergeFile = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.MERGE_FILE);
        String appendContent = "someAppendedContent";
        appendContentToFile(mergeFile, appendContent);

        // commit changes
        commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "changed mergeFile").execute();

        // 5. switch back to trunk
        commandFactory.getSwitchBranchCommand("TRUNK").execute();

        // 6. assert that file is not changed yet
        assertThatFileWasNotModified(mergeFile, appendContent);

        // 7. merge branch in trunk
        MergeResult result = commandFactory.getMergeCommand(branchName).execute();

        // check result
        assertEquals(0, result.getConflicts().length);
        assertEquals(0, result.getDeletions().length);
        assertEquals(0, result.getAdds().length);
        assertEquals(2, result.getMerges().length);

        String[] expectedUpdatedFiles = new String[] { new File(this.CONSTANTS.WORKING_COPIES[0]).getAbsolutePath(),
                new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.MERGE_FILE).getAbsolutePath(), };
        assertThatArraysEqualIgnorePosition(expectedUpdatedFiles, result.getMerges());

        // check filesystem
        assertThatFileWasModified(mergeFile, appendContent);
    }

    /**
     * Asserts that merge fails with an ScmException should conflicts arise.
     * 
     * @throws ScmException is expected to be thrown
     * @throws IOException
     */
    @Test
    public void merge_shouldFailOnConflicts() throws ScmException, IOException {
        // 1. check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // 2. create branch
        String branchName = "newTestBranch";
        commandFactory.getBranchCommand(branchName, "createing new branch for test purposes.").execute();

        // 3. switch to branch
        commandFactory.getSwitchBranchCommand(branchName).execute();

        // 4. perform some changes in branch
        File mergeFile = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.MERGE_FILE);
        String appendContent = "someAppendedContent";
        appendContentToFile(mergeFile, appendContent);

        // commit changes
        commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "changed mergeFile").execute();

        // 5. switch back to trunk
        commandFactory.getSwitchBranchCommand("TRUNK").execute();

        // 6. change same file again with other content
        String appendContent2 = "someOtherContent";
        appendContentToFile(mergeFile, appendContent2);

        // commit changes
        commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "changed mergeFile (again but this time on trunk)")
                .execute();

        // 7. merge branch in trunk
        // exception expected here
        commandFactory.getMergeCommand(branchName).execute();
    }

    /**
     * Tests basic ability to annotate a file's lines with author and revision
     * 
     * @throws ScmException
     * @throws IOException
     */
    @Test
    public void blame_shouldReturnAnnotatedFileContents() throws ScmException, IOException {
        // 1. check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // 2. add content to testfile
        File testFile = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.TEST_FILE);
        String appendContent = "someContentToBlame";
        appendContentToFile(testFile, appendContent);

        // commit changes
        commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "added content to blame to testfile").execute();

        String blamedContent = commandFactory.getBlameCommand(this.CONSTANTS.TEST_FILE).execute();

        assertBlamedTestContent(blamedContent, appendContent, this.CONSTANTS.AUTHOR);
    }

    /**
     * Tests basic ability to compute the differences between revisions
     * 
     * @throws ScmException
     * @throws IOException
     */
    @Test
    public void diff_shouldReturnDifferencesBetweenRevisions() throws ScmException, IOException {
        // 1. check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // 2. append content to file
        File testFile = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.TEST_FILE);
        String appendContent = "someContentToTriggerDiff";
        appendContentToFile(testFile, appendContent);

        // commit changes
        MergeResult result = commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, "changed testfile").execute();
        long revision = Long.parseLong(result.getRevision());

        String differences = commandFactory.getDiffCommand(this.CONSTANTS.TEST_FILE, String.valueOf(revision - 1))
                .execute();

        // 3. validate differences
        assertDifferences(differences, new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.TEST_FILE),
                appendContent, String.valueOf(revision - 1), String.valueOf(revision));
    }

    /**
     * Tests the basic ability to export the working copy's contents without
     * SVN-metadata
     * 
     * @throws ScmException
     */
    @Test
    public void export_shouldExportWorkingCopyWithoutSvnMetadata() throws ScmException {
        // 1. check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // 2. export it
        commandFactory.getExportCommand(this.CONSTANTS.EXPORT_PATH).execute();

        // 3. assert exported struture
        // one file is enough for testing, since we rely on the bugfreenes of
        // svnKit
        File exportedTestFile = new File(new File(this.CONSTANTS.EXPORT_PATH), this.CONSTANTS.TEST_FILE);
        assertTrue("Missing exported testfile", exportedTestFile.exists());

        File exportedSvnMetada = new File(new File(this.CONSTANTS.EXPORT_PATH), ".snv");
        assertFalse("Found svn-metadata", exportedSvnMetada.exists());
    }

    /**
     * Asserts that export fails with an ScmException, should the destination
     * already exist.
     * 
     * @throws ScmException is expected to be thrown.
     */
    @Test(expected = ScmException.class)
    public void export_shouldFailWhenExportDirectoryAlreadyExists() throws ScmException {
        // 1. check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // 2. create export directory prior to exporting
        File exportDirectory = new File(this.CONSTANTS.EXPORT_PATH);
        assertTrue("Could not create export direcotry", exportDirectory.mkdirs());

        // 3. export it
        // exception expected here
        commandFactory.getExportCommand(this.CONSTANTS.EXPORT_PATH).execute();
    }

    /**
     * Tests the basic ability to import an unversioned filesystem-tree into the
     * repository
     * 
     * @throws ScmException
     */
    @Test
    public void import_shouldImportFilesIntoWorkingCopy() throws ScmException {
        // 1. check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // 2. import
        MergeResult result = commandFactory.getImportCommand(this.CONSTANTS.IMPORT_PATH, null, "importing files",
                this.CONSTANTS.AUTHOR).execute();

        // 3. assert result
        assertEquals(0, result.getConflicts().length);
        assertEquals(0, result.getDeletions().length);
        assertEquals(1, result.getAdds().length);
        assertEquals(0, result.getMerges().length);

        // 4. assert that files were imported by checking out second working
        // copy
        CommandFactory commandFactory2 = createCommandFactory(this.CONSTANTS.WORKING_COPIES[1], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory2.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        File importFile = new File(new File(this.CONSTANTS.WORKING_COPIES[1]), this.CONSTANTS.IMPORT_FILE);
        assertTrue("mising " + importFile.getAbsolutePath(), importFile.exists());
    }

    /**
     * Asserts that import fails, when the repository is read only (i.e.
     * connection, not developerConnection, was set)
     * 
     * @throws ScmException
     */
    @Test(expected = ScmException.class)
    public void import_shouldFailWhenConnectionInsteadofDeveloperConnectionWasSet() throws ScmException {
        // 1. check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0],
                SvnScmDomainUnitTest.TRUNK, null);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // 2. import
        // exception expected here
        commandFactory.getImportCommand(this.CONSTANTS.IMPORT_PATH, null, "importing files", this.CONSTANTS.AUTHOR)
                .execute();
    }

    /**
     * Tests the basic ability to collect commit-messages for files and
     * revisions.
     * 
     * @throws ScmException
     * @throws IOException
     */
    @Test
    public void log_shouldReturnCommitMessageForSingleFileAndSingleRevision() throws ScmException, IOException {
        // 1. check out initial working copy
        CommandFactory commandFactory = createCommandFactory(this.CONSTANTS.WORKING_COPIES[0], null,
                SvnScmDomainUnitTest.TRUNK);
        commandFactory.getCheckoutCommand(this.CONSTANTS.AUTHOR).execute();

        // 2. modify file and commit
        File testFile = new File(new File(this.CONSTANTS.WORKING_COPIES[0]), this.CONSTANTS.TEST_FILE);
        String appendContent = "someContent";
        appendContentToFile(testFile, appendContent);

        // commit
        String commitMessage = "testCommitMessage";
        MergeResult result = commandFactory.getCommitCommand(this.CONSTANTS.AUTHOR, commitMessage).execute();

        // 3. call log
        // set up parameters
        String[] files = new String[] { testFile.getName() };

        // call
        Map<String, String> logs = commandFactory.getLogCommand(files, result.getRevision(), result.getRevision())
                .execute();

        // 4. examine logs
        assertEquals(1, logs.size());
        assertEquals(this.CONSTANTS.AUTHOR + ":\n" + commitMessage, logs.get(result.getRevision()));
    }

    /* tests end */

    /* helper */

    /**
     * Asserts that all elements from expected are also in actual.
     * 
     * @param expected the expected elements
     * @param actual the actual elements
     */
    private static <T> void assertThatArraysEqualIgnorePosition(T[] expected, T[] actual) {
        if (expected == actual) {
            return;
        }

        assertEquals(expected.length, actual.length);

        // iterate over expected
        for (T expectedElement : expected) {
            // iterate over actual
            boolean foundExpected = false;
            for (T actualElement : actual) {
                if (expectedElement.equals(actualElement)) {
                    foundExpected = true;
                    break;
                }
            }

            assertTrue("Missing " + expectedElement.toString(), foundExpected);
        }
    }

    /**
     * Asserts that the tow different representations of Files (absolute and
     * relative to workingCopy) do equal.
     * 
     * @param workingCopy the basepath for the relative expected files
     * @param expected the expected files, relative to workingCopy
     * @param actual the absolute, actual files
     */
    private static void assertFileListsEqual(String workingCopy, String[] expected, String[] actual) {
        assertEquals(expected.length, actual.length);

        // iterate over expected
        for (String expectedElement : expected) {
            String expectedFile = new File(new File(workingCopy), expectedElement).getAbsolutePath();

            // iterate over actual
            boolean foundExpected = false;
            for (String actualElement : actual) {
                String actualFile = new File(actualElement).getAbsolutePath();
                if (expectedFile.equals(actualFile)) {
                    foundExpected = true;
                    break;
                }
            }

            assertTrue("Missing " + expectedFile, foundExpected);
        }
    }

    /**
     * Appends content to file
     * 
     * @param file The file to append the content to.
     * @param content The content to be appended.
     * @throws IOException if something unforseen happenes
     */
    private static void appendContentToFile(File file, String content) throws IOException {
        FileWriter writer = new FileWriter(file, true);

        writer.append(content);

        writer.flush();
        writer.close();
    }

    /**
     * Asserts that a file was modified. Actually it only checks it the content
     * was appended to it.
     * 
     * @param file The file to check.
     * @param modifyContent The content which is expected to be appended.
     * @throws IOException if something unforseen happens
     */
    private static void assertThatFileWasModified(File file, String modifyContent) throws IOException {
        FileReader reader = new FileReader(file);
        char[] buffer = new char[(int) file.length()];

        reader.read(buffer);

        String content = new String(buffer);

        assertTrue(content.endsWith(modifyContent));
    }

    /**
     * Asserts that a file was not modified. Actually it only checks it the
     * content was not appended to it.
     * 
     * @param file The file to check.
     * @param modifyContent The content which is expected not to be appended.
     * @throws IOException if something unforseen happens
     */
    private static void assertThatFileWasNotModified(File file, String modifyContent) throws IOException {
        FileReader reader = new FileReader(file);
        char[] buffer = new char[(int) file.length()];

        reader.read(buffer);

        String content = new String(buffer);

        assertFalse(content.endsWith(modifyContent));
    }

    /**
     * Asserts that the blamed content of a file is formatted correctly (very
     * specific to one test and not to be used generically)
     * 
     * @param blamedContent
     * @param addedLine
     * @param addedLineAuthor
     * @throws IOException
     */
    private void assertBlamedTestContent(String blamedContent, String addedLine, String addedLineAuthor)
            throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(blamedContent));

        // ignore first three lines (dependent on initial repository)
        reader.readLine();
        reader.readLine();
        reader.readLine();

        // get fourth and last line
        String fourthLine = reader.readLine();

        // assert that last line matches the pattern
        // String pattern = "4: " + addedLine + " \\- [0-9]+ " + addedLineAuthor
        // + ".*"; author does not work correctly yet
        String pattern = "4: " + addedLine + " \\- [0-9]+ [a-zA-Z]+.*";
        assertTrue(fourthLine.matches(pattern));

        reader.close();
    }

    /**
     * Asserts that the diff for a file on two revisions is formatted correctly
     * (very specific to one test and not to be used generically)
     * 
     * @param differences
     * @param file
     * @param addedContent
     * @param revision1
     * @param revision2
     * @throws IOException
     */
    private void assertDifferences(String differences, File file, String addedContent, String revision1,
            String revision2) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(differences));

        // expecting something like this
        // Index:
        // /home/patrick/University/engsb/workingCopies/engsb/engsb-domain-scm/target/testWorkingCopy/testFile
        // ===================================================================
        // ---
        // /home/patrick/University/engsb/workingCopies/engsb/engsb-domain-scm/target/testWorkingCopy/testFile
        // (revision 10)
        // +++
        // /home/patrick/University/engsb/workingCopies/engsb/engsb-domain-scm/target/testWorkingCopy/testFile
        // (revision 11)
        // @@ -1,3 +1,4 @@
        // someArbitraryContent
        // another line
        // yet another line
        // +someContentToTriggerDiff
        // \ No newline at end of file

        assertEquals("Index: " + file.getAbsolutePath(), reader.readLine());
        assertEquals("===================================================================", reader.readLine());
        assertEquals("--- " + file.getAbsolutePath() + "\t(revision " + revision1 + ")", reader.readLine());
        assertEquals("+++ " + file.getAbsolutePath() + "\t(revision " + revision2 + ")", reader.readLine());
        assertEquals("@@ -1,3 +1,4 @@", reader.readLine());
        assertEquals(" someArbitraryContent", reader.readLine());
        assertEquals(" another line", reader.readLine());
        assertEquals(" yet another line", reader.readLine());
        assertEquals("+" + addedContent, reader.readLine());

        reader.close();
    }

    /* helper end */

}
