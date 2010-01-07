/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.connector.svn.test.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Resource;
import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.RobustInOnly;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.connector.svn.test.integration.constants.SvnScmComponentIntegrationTestConstants;
import org.openengsb.scm.common.exceptions.ScmException;
import org.openengsb.scm.common.pojos.MergeResult;
import org.openengsb.scm.common.util.MergeResultSerializer;
import org.openengsb.scm.common.util.StringArraySerializer;
import org.openengsb.scm.common.util.StringMapSerializer;
import org.openengsb.scm.common.util.StringSerializer;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

/**
 * Collection of integration tests, that test the Component and Endpoints of the
 * SVN deploy domain by simulating a call from a fictive component. These tests
 * are more or less the same as in the UnitTest. They set up the calls
 * differently, but essentially perform the same checks and assertions.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Ignore("Spring setup does not work for windows users")
@ContextConfiguration(locations = { "/integrationtestSpring.xml" })
public class SvnScmComponentIntegrationTest extends SpringTestSupport {
    private static SVNClientManager clientManager;

    /**
     * The constants needed for the tests
     */
    @Resource
    private SvnScmComponentIntegrationTestConstants CONSTANTS;

    private boolean environmentIsSetUp = false;

    /* creators */

    /**
     * Creates a new ServiceMixClieant
     *
     * @return The new ServiceMixClient
     */
    private DefaultServiceMixClient createClient() throws JBIException {
        return new DefaultServiceMixClient(jbi);
    }

    /**
     * Creates and configures a new Message-Object for the In-Out-MEP
     *
     * @param client The client used to create the empty Message-Object
     * @param service The configured entpoint's name as noted in the xbean.xml
     * @param message The actual message as xml-String
     * @return The new and configured InOut-Message-Object
     * @throws MessagingException should something go wrong
     */
    private InOut createInOutMessage(DefaultServiceMixClient client, String service, String message)
            throws MessagingException {
        InOut inOut = client.createInOutExchange();
        inOut.setService(new QName(CONSTANTS.TEST_NAMESPACE, service));
        inOut.getInMessage().setContent(new StringSource(message));

        return inOut;
    }

    /**
     * Creates and configures a new Message-Object for the Out-Only-MEP
     *
     * @param client The client used to create the empty Message-Object
     * @param service The configured entpoint's name as noted in the xbean.xml
     * @param message The actual message as xml-String
     * @return The new and configured In-Only-Message-Object
     * @throws MessagingException should something go wrong
     */
    private RobustInOnly createRobustInOnlyMessage(DefaultServiceMixClient client, String service, String message)
            throws MessagingException {
        RobustInOnly robustInOnly = client.createRobustInOnlyExchange();
        robustInOnly.setService(new QName(CONSTANTS.TEST_NAMESPACE, service));
        robustInOnly.getInMessage().setContent(new StringSource(message));

        return robustInOnly;
    }

    /* end creators */

    /* setup */

    @BeforeClass
    public static void setUpClientManager() {
        SvnScmComponentIntegrationTest.clientManager = SVNClientManager.newInstance();
    }

    /**
     * Called before each test. Performs basic JUnit setup Don't get confused by
     * the name, this is actually JUnit 4 ;)
     */
    @Before
    @Override
    public void setUp() throws Exception {
        setEnvironmentVariables();
        setUpRepository();
        deleteWorkingCopies();
        super.setUp();
    }

    /**
     * Called after each test. Don't get confused by the name, this is actually
     * JUnit 4
     */
    @After
    @Override
    public void tearDown() throws Exception {
        deleteRepository();
        deleteWorkingCopies();
        deleteExportDirectory();
        super.tearDown();
    }

    /* end setup */

    /* tests */

    /**
     * Tests basic checkout behavior
     *
     * @throws ScmException
     */
    @Test
    public void checkout_shouldCheckoutRepositoryWithValidRepository() throws Exception {
        MergeResult result = doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // examine result
        // checking checkedOutFiles
        assertFileListsEqual(CONSTANTS.WORKING_COPIES[0], CONSTANTS.INITIAL_FILES, result.getAdds());

        // checking file system:
        // even though there were more files checked out, only this one will
        // have to be checked
        // for existence as representative. If this file was checked out, all
        // others will have
        // or SVNKit is buggy.
        File workingCopyPath = new File(CONSTANTS.WORKING_COPIES[0]);
        assertTrue(new File(workingCopyPath, CONSTANTS.TEST_FILE).exists());
    }

    /**
     * Tests whether the developerConnection is prefered over the connection
     *
     * @throws Exception
     */
    @Test
    public void checkout_shouldPreferDeveloperConnectionIfBothConnectionsAreSet() throws Exception {
        doCheckoutRepository(CONSTANTS.CHECKOUT_BOTH_CONNECTIONS_SET);

        // assert that the branch-folder does exist (because developerConnection
        // was set the repository with the branches-folder, while connection was
        // set the repository without)
        File branchesFolder = new File(new File(CONSTANTS.WORKING_COPIES[0]), "branches");
        assertTrue("Branches-folder " + branchesFolder + " does not exist", branchesFolder.exists());
    }

    /**
     * Tests whether the connection is actually used, when the
     * developerConnection is missing
     *
     * @throws Exception
     */
    @Test
    public void checkout_shouldUseConnectionIfDeveloperConnectionIsNotSet() throws Exception {
        doCheckoutRepository(CONSTANTS.CHECKOUT_ONLY_CONNECTION_SET);

        // assert that the branch-folder does exist
        File branchesFolder = new File(new File(CONSTANTS.WORKING_COPIES[0]), "branches");
        assertTrue("Branches-folder " + branchesFolder + " does not exist", branchesFolder.exists());
    }

    /**
     * Asserts that checkout checks out the repository to the default directory
     *
     * @throws Exception
     */
    @Test
    public void checkout_shouldCheckoutToDefaultDirectoryIfWorkigCopyWasNotSet() throws Exception {
        doCheckoutRepository(CONSTANTS.CHECKOUT_NO_WORKING_COPY_SET_SERVICE_NAME);

        // assert that workingcopy was put in ./workingCopies/trunk
        assertTrue("Could not find " + CONSTANTS.DEFAULT_WORKING_COPY, new File(
                CONSTANTS.DEFAULT_WORKING_COPY).exists());
    }

    /**
     * Tests basic ability to add files to the working copy.
     *
     * @throws Exception
     */
    @Test
    public void add_shouldAddFileToWorkingCopy() throws Exception {
        // set up and perform checkout
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // copy the file to add
        File resourcesFileToAdd = new File(CONSTANTS.FILE_TO_ADD);
        File addedFile = doAddFile(resourcesFileToAdd, CONSTANTS.WORKING_COPIES[0], null,
                CONSTANTS.ADD_SERVICE_NAME);

        // check if file was actually added
        SVNStatus status = SvnScmComponentIntegrationTest.clientManager.getStatusClient().doStatus(addedFile, false);
        assertEquals(SVNStatusType.STATUS_ADDED.getID(), status.getContentsStatus().getID());
    }

    /**
     * Tries to add a not existing file, which should result in an ScmException
     *
     * @throws Exception is expected to be thrown
     */
    @Test(expected = ScmException.class)
    public void add_shouldThrowExceptionOnNotExistingFile() throws Exception {
        // set up and perform checkout
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // do NOT copy the file to add

        // set up and perform add
        DefaultServiceMixClient client = createClient();
        RobustInOnly robustinOnly = createRobustInOnlyMessage(client, CONSTANTS.ADD_SERVICE_NAME,
                "<add fileToAdd=\"" + CONSTANTS.NOT_EXISTING_FILE + "\"/>");

        client.sendSync(robustinOnly);
        validateReturnMessageSuccess(robustinOnly); // exception expected here
    }

    /**
     * Tests basic ability to mark files for deletion
     *
     * @throws Exception
     */
    @Test
    public void delete_shouldMarkfileForDeletion() throws Exception {
        // set up and perform checkout
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // set up and perform delete
        doDeleteFile(CONSTANTS.DELETE_FILE, CONSTANTS.DELETE_SERVICE_NAME);

        // check if file was actually deleted
        File workingCopyDeleteFile = new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.DELETE_FILE);
        SVNStatus status = SvnScmComponentIntegrationTest.clientManager.getStatusClient().doStatus(
                workingCopyDeleteFile, false);
        assertEquals(SVNStatusType.STATUS_DELETED.getID(), status.getContentsStatus().getID());
    }

    /**
     * Tries to delete a not existing file, which should result in an
     * ScmException
     *
     * @throws Exception is expected to be thrown
     */
    @Test(expected = ScmException.class)
    public void delete_shouldThrowExceptionOnNotexistingFile() throws Exception {
        // set up and perform checkout
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // set up and perform delete
        // exception expected here
        doDeleteFile(CONSTANTS.NOT_EXISTING_FILE, CONSTANTS.DELETE_SERVICE_NAME);
    }

    /**
     * Tests basic ability to commit changes to the repository. Also extends the
     * tests for add and delete since those changes are expected to go into a
     * repository sooner or later
     *
     * @throws Exception
     */
    @Test
    public void commit_shouldTransferChangesToRepository() throws Exception {
        // set up and perform checkout
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // do some changes to the files:
        // + add file
        File addedFile = doAddFile(new File(CONSTANTS.FILE_TO_ADD), CONSTANTS.WORKING_COPIES[0], null,
                CONSTANTS.ADD_SERVICE_NAME);

        // + delete file
        doDeleteFile(CONSTANTS.DELETE_FILE, CONSTANTS.DELETE_SERVICE_NAME);

        // + modify file
        String modifyContent = "someContent";
        File fileToModify = new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify, modifyContent);

        // set up and perform commit
        MergeResult result = doCommit("someArbitraryMessage", null, CONSTANTS.COMMIT_SERVICE_NAMES[0]);

        // check result
        assertEquals(0, result.getConflicts().length);
        assertEquals(1, result.getDeletions().length);
        assertEquals(
                new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.DELETE_FILE).getAbsolutePath(),
                new File(result.getDeletions()[0]).getAbsolutePath());
        assertEquals(1, result.getAdds().length);
        assertEquals(addedFile.getAbsolutePath(), new File(result.getAdds()[0]).getAbsolutePath());
        assertEquals(1, result.getMerges().length);
        assertEquals(fileToModify.getAbsolutePath(), new File(result.getMerges()[0]).getAbsolutePath());

        // check if files reached the repository by checking it out a second
        // time
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC2_TRUNK_SERVICE_NAME);
        File newWorkingCopy = new File(CONSTANTS.WORKING_COPIES[1]);

        // assert changes
        assertTrue(new File(newWorkingCopy, addedFile.getName()).exists());
        assertFalse(new File(newWorkingCopy, CONSTANTS.DELETE_FILE).exists());

        assertThatFileWasModified(new File(newWorkingCopy, CONSTANTS.UPDATE_FILE), modifyContent);
    }

    /**
     * Test the ability to commit changes only from a sub-path within the
     * working copy.
     *
     * @throws Exception
     */
    @Test
    public void commit_shouldTransferPartialChangesToRepositoryWhenSubpathIsGiven() throws Exception {
        // set up and perform checkout
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // add file in root
        File resourcesFileToAdd = new File(CONSTANTS.FILE_TO_ADD);
        File addedFile = doAddFile(resourcesFileToAdd, CONSTANTS.WORKING_COPIES[0], null,
                CONSTANTS.ADD_SERVICE_NAME);

        // add file in subpath
        File addedSubPathFile = doAddFile(resourcesFileToAdd, CONSTANTS.WORKING_COPIES[0],
                CONSTANTS.SUB_PATH, CONSTANTS.ADD_SERVICE_NAME);

        // set up and perform commit
        MergeResult result = doCommit("someArbitraryMessage", CONSTANTS.SUB_PATH,
                CONSTANTS.COMMIT_SERVICE_NAMES[0]);

        // check result
        assertEquals(0, result.getConflicts().length);
        assertEquals(0, result.getDeletions().length);
        assertEquals(1, result.getAdds().length);
        assertEquals(addedSubPathFile.getAbsolutePath(), new File(result.getAdds()[0]).getAbsolutePath());
        assertEquals(0, result.getMerges().length);

        // check if files reached the repository by checking it out a second
        // time
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC2_TRUNK_SERVICE_NAME);

        // check filesystem
        File secondWorkingCopy = new File(CONSTANTS.WORKING_COPIES[1]);
        assertFalse(new File(secondWorkingCopy, addedFile.getName()).exists());
        assertTrue(new File(new File(secondWorkingCopy, CONSTANTS.SUB_PATH), addedFile.getName()).exists());
    }

    /**
     * Tests that commit fails, when unsolvable (by SVN) conflicts between the
     * working-copy and the repository arise
     *
     * @throws Exception is expected to be thrown
     */
    @Test(expected = ScmException.class)
    public void commit_shouldFailWhenConflictsArise() throws Exception {
        // check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // check out a second time
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC2_TRUNK_SERVICE_NAME);

        // modify the same file in both working copies
        String modifyContent = "someContent";
        File fileToModify = new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify, modifyContent);

        String modifyContent2 = "someOtherContent";
        File fileToModify2 = new File(new File(CONSTANTS.WORKING_COPIES[1]), CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify2, modifyContent2);

        // commit first working copy
        doCommit("someArbitraryMessage", null, CONSTANTS.COMMIT_SERVICE_NAMES[0]);

        // commit second working copy
        // exception expected here
        doCommit("someArbitraryMessage", null, CONSTANTS.COMMIT_SERVICE_NAMES[1]);
    }

    /**
     * Asserts that commit fails, if we may not write to the repository (i.e.
     * the connection, not the developerConnection was used)
     *
     * @throws Exception
     */
    @Test(expected = ScmException.class)
    public void commit_shouldFailWhenConnectionInsteadOfDeveloperConnectionWasSet() throws Exception {
        // check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_ONLY_CONNECTION_SET);

        // modify file
        String modifyContent = "someContent";
        File fileToModify = new File(new File(new File(CONSTANTS.WORKING_COPIES[0]), "trunk"),
                CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify, modifyContent);

        // commit
        // exception expected here
        doCommit("ordinary commit", null, CONSTANTS.COMMIT_ONLY_CONNECTION_SET_SERVICE_NAME);
    }

    /**
     * Tests basic ability to update the working copy with changes from the
     * repository.
     *
     * @throws Exception
     */
    @Test
    public void update_shouldUpdateFilesFromRepository() throws Exception {
        // check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // check out a second time
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC2_TRUNK_SERVICE_NAME);

        // do some changes to the files:
        // + add file
        File resourcesFileToAdd = new File(CONSTANTS.FILE_TO_ADD);
        File addedFile = doAddFile(resourcesFileToAdd, CONSTANTS.WORKING_COPIES[0], null,
                CONSTANTS.ADD_SERVICE_NAME);

        // + delete file
        doDeleteFile(CONSTANTS.DELETE_FILE, CONSTANTS.DELETE_SERVICE_NAME);

        // + modify file
        String modifyContent = "someContent";
        File fileToModify = new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify, modifyContent);

        // commit first working copy
        doCommit("someArbitraryMessage", null, CONSTANTS.COMMIT_SERVICE_NAMES[0]);

        // actual call to test: update second working copy
        MergeResult result = doUpdate(CONSTANTS.UPDATE_SERVICE_NAME, null);

        // check result
        assertEquals(0, result.getConflicts().length);
        assertEquals(1, result.getDeletions().length);
        assertEquals(
                new File(new File(CONSTANTS.WORKING_COPIES[1]), CONSTANTS.DELETE_FILE).getAbsolutePath(),
                new File(result.getDeletions()[0]).getAbsolutePath());
        assertEquals(1, result.getAdds().length);
        assertEquals(new File(new File(CONSTANTS.WORKING_COPIES[1]), addedFile.getName()).getAbsolutePath(),
                new File(result.getAdds()[0]).getAbsolutePath());
        assertEquals(2, result.getMerges().length);
        String[] expectedMerges = new String[] { new File(CONSTANTS.WORKING_COPIES[1]).getAbsolutePath(),
                new File(new File(CONSTANTS.WORKING_COPIES[1]), CONSTANTS.UPDATE_FILE).getAbsolutePath() };
        assertThatArraysEqualIgnorePosition(expectedMerges, result.getMerges());

        // assert changes
        File secondWorkingCopy = new File(CONSTANTS.WORKING_COPIES[1]);
        assertTrue(new File(secondWorkingCopy, addedFile.getName()).exists());
        assertFalse(new File(secondWorkingCopy, CONSTANTS.DELETE_FILE).exists());

        assertThatFileWasModified(new File(secondWorkingCopy, CONSTANTS.UPDATE_FILE), modifyContent);
    }

    /**
     * Tests the ability to update only a sub-path within the working copy
     *
     * @throws Exception
     */
    @Test
    public void update_shouldUpdateOnlySubpathWhenSubpathIsGiven() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. check out a second time
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC2_TRUNK_SERVICE_NAME);

        // 3. add file in root in first working copy
        File resourcesFileToAdd = new File(CONSTANTS.FILE_TO_ADD);
        doAddFile(resourcesFileToAdd, CONSTANTS.WORKING_COPIES[0], null, CONSTANTS.ADD_SERVICE_NAME);

        // 4. add file in subpath in first working copy
        File addedSubPathFile = doAddFile(resourcesFileToAdd, CONSTANTS.WORKING_COPIES[0],
                CONSTANTS.SUB_PATH, CONSTANTS.ADD_SERVICE_NAME);

        // 5. commit changes
        doCommit("added files", null, CONSTANTS.COMMIT_SERVICE_NAMES[0]);

        // 6. update second working copy
        MergeResult result = doUpdate(CONSTANTS.UPDATE_SERVICE_NAME, CONSTANTS.SUB_PATH);

        // 7. check result
        String fileToAddName = addedSubPathFile.getName();
        assertEquals(0, result.getConflicts().length);
        assertEquals(0, result.getDeletions().length);
        assertEquals(1, result.getAdds().length);
        assertEquals(new File(new File(new File(CONSTANTS.WORKING_COPIES[1]), CONSTANTS.SUB_PATH),
                fileToAddName).getAbsolutePath(), new File(result.getAdds()[0]).getAbsolutePath());
        assertEquals(1, result.getMerges().length);
        assertEquals(new File(new File(CONSTANTS.WORKING_COPIES[1]), CONSTANTS.SUB_PATH).getAbsolutePath(),
                new File(result.getMerges()[0]).getAbsolutePath());

        // 8. check filesystem
        File secondWorkingCopy = new File(CONSTANTS.WORKING_COPIES[1]);
        assertFalse(new File(secondWorkingCopy, fileToAddName).exists());
        assertTrue(new File(new File(secondWorkingCopy, CONSTANTS.SUB_PATH), fileToAddName).exists());
    }

    /**
     * Asserts that update fails with an ScmException when unsolvable (by SVN)
     * conlicts arise.
     *
     * @throws Exception
     */
    @Test
    public void update_shouldFailWhenConflictsArise() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. check out a second time
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC2_TRUNK_SERVICE_NAME);

        // 3. modify the same file in both working copies
        String modifyContent = "someContent";
        File fileToModify = new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify, modifyContent);

        String modifyContent2 = "someOtherContent";
        File fileToModify2 = new File(new File(CONSTANTS.WORKING_COPIES[1]), CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify2, modifyContent2);

        // 4. commit changes from first working copy
        doCommit("modified file", null, CONSTANTS.COMMIT_SERVICE_NAMES[0]);

        // 5. update second working copy
        // exception expected here
        doUpdate(CONSTANTS.UPDATE_SERVICE_NAME, CONSTANTS.SUB_PATH);
    }

    /**
     * Tests basic checkout behavior
     *
     * @throws ScmException
     */
    @Test
    public void checkoutOrUpdate_shouldCheckoutRepositoryWheWorkingcopyDoesNotYetExist() throws Exception {
        MergeResult result = doCheckoutOrUpdate(CONSTANTS.CHECKOUT_OR_UPDATE_SERVICE_NAME);

        // examine result
        // checking checkedOutFiles
        assertFileListsEqual(CONSTANTS.WORKING_COPIES[0], CONSTANTS.INITIAL_FILES, result.getAdds());

        // checking file system:
        File workingCopyPath = new File(CONSTANTS.WORKING_COPIES[0]);
        assertTrue(new File(workingCopyPath, CONSTANTS.TEST_FILE).exists());
    }

    /**
     * Tests basic ability to update the working copy with changes from the
     * repository.
     *
     * @throws Exception
     */
    @Test
    public void checkoutOrUpdate_shouldUpdateFilesFromRepository() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. check out a second time
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC2_TRUNK_SERVICE_NAME);

        // 3. modify a file in second working copy
        String modifyContent = "someContent";
        File fileToModify = new File(new File(CONSTANTS.WORKING_COPIES[1]), CONSTANTS.UPDATE_FILE);
        appendContentToFile(fileToModify, modifyContent);

        // 4. commit second working copy
        doCommit("someArbitraryMessage", null, CONSTANTS.COMMIT_SERVICE_NAMES[1]);

        // actual call to test: update second working copy
        MergeResult result = doCheckoutOrUpdate(CONSTANTS.CHECKOUT_OR_UPDATE_SERVICE_NAME);

        // check result
        assertEquals(0, result.getConflicts().length);
        assertEquals(2, result.getMerges().length);

        // assert changes
        File secondWorkingCopy = new File(CONSTANTS.WORKING_COPIES[0]);
        assertThatFileWasModified(new File(secondWorkingCopy, CONSTANTS.UPDATE_FILE), modifyContent);
    }

    /**
     * Tests the basic ability to create a new branch.
     *
     * @throws Exception
     */
    @Test
    public void branch_shouldCreateNewBranch() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. create branch
        String branchName = "myNewTestBranch";
        doBranch(branchName, "created new test-branch", CONSTANTS.BRANCH_SERVICE_NAME);

        // 3. validate, that branch was created by checking it out
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC2_BRANCHES_SERVICE_NAME);

        // checking file system:
        // even though there were more files checked out, only this one will
        // have to be checked
        // for existence as representative. If this file was checked out, all
        // others will have
        // or SVNKit is buggy.
        assertTrue(new File(new File(CONSTANTS.WORKING_COPIES[1], branchName), CONSTANTS.TEST_FILE).exists());
    }

    /**
     * Tests the fault-tolerant behavior to create a new branch, even if the
     * expected branches-directory does not yet exist.
     *
     * @throws Exception
     */
    @Test
    public void branch_shouldCreateNewBranchEvenWhenBranchDirectoryDoesNotYetExistInRepository() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_NO_BRANCHES_TRUNK_SERVICE_NAME);

        // 2. create branch
        String branchName = "myNewTestBranch";
        doBranch(branchName, "created new test-branch", CONSTANTS.BRANCH_SERVICE_NAME);

        // 3. validate, that branch was created by checking it out
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC2_NO_BRANCHES_BRANCHES_SERVICE_NAME);

        // checking file system:
        // even though there were more files checked out, only this one will
        // have to be checked
        // for existence as representative. If this file was checked out, all
        // others will have
        // or SVNKit is buggy.
        assertTrue(new File(new File(CONSTANTS.WORKING_COPIES[1], branchName), CONSTANTS.TEST_FILE).exists());
    }

    /**
     * Asserts that no two branches with the same name may be created.
     *
     * @throws Exception is expected to be thrown.
     */
    @Test(expected = ScmException.class)
    public void branch_shouldFailWhenBranchAlreadyExists() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. create branch
        String branchName = "myNewTestBranch";
        doBranch(branchName, "created new test-branch", CONSTANTS.BRANCH_SERVICE_NAME);

        // 3. create branch again
        // exception expected here
        doBranch(branchName, "created new test-branch", CONSTANTS.BRANCH_SERVICE_NAME);
    }

    /**
     * Asserts that no branch with the name TRUNK can be created. This is
     * necessary to be able to switch back to trunk again later on.
     *
     * @throws Exception
     */
    @Test(expected = ScmException.class)
    public void branch_shouldFailWhenTrunkKeywordisUsed() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. create branch
        String branchName = "TRUNK";
        // exception expected here
        doBranch(branchName, "created new test-branch", CONSTANTS.BRANCH_SERVICE_NAME);
    }

    /**
     * Asserts that branch fails, when the repository is read only (i.e.
     * connection, not developerConnection, was set)
     *
     * @throws Exception
     */
    @Test(expected = ScmException.class)
    public void branch_shouldFailWhenConnectionInsteadofDeveloperConnectionWasSet() throws Exception {
        // check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_ONLY_CONNECTION_SET);

        // branch
        // exception expected here
        doBranch("someBranch", "committingBranchToReadOnlyRepository",
                CONSTANTS.BRANCH_ONLY_CONNECTION_SET_SERVICE_NAME);
    }

    /**
     * Tests basic ability to list all created branches.
     *
     * @throws Exception
     */
    @Test
    public void listBranches_shouldListAllBranches() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // no branches initially
        String[] branches = doListBranches(CONSTANTS.LIST_BRANCHES_SERVICE_NAME);
        assertEquals(0, branches.length);

        // 2. create test-branches
        String[] branchNames = new String[] { "testBranch1", "testBranch2", "testBranch3" };
        for (String branchName : branchNames) {
            doBranch(branchName, "created new test-branch " + branchName, CONSTANTS.BRANCH_SERVICE_NAME);
        }

        // list and verify branches
        branches = doListBranches(CONSTANTS.LIST_BRANCHES_SERVICE_NAME);
        assertThatArraysEqualIgnorePosition(branchNames, branches);
    }

    /**
     * Asserts that an empty list (instead of an Excption) is returned, if the
     * branches-directory does not exist.
     *
     * @throws Exception
     */
    @Test
    public void listBranches_shouldReturnEmptyArrayWhenBranchesDirectoryDoesNotExistInRepository() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_NO_BRANCHES_TRUNK_SERVICE_NAME);

        // 2. list branches
        String[] branches = doListBranches(CONSTANTS.LIST_BRANCHES_SERVICE_NAME);
        assertEquals(0, branches.length);
    }

    /**
     * Tests the basic ability to switch to a(nother) branch.
     *
     * @throws Exception
     */
    @Test
    public void switchBranch_shouldSwitchToBranch() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. create branch
        String branchName = "myNewTestBranch";
        doBranch(branchName, "created new test-branch", CONSTANTS.BRANCH_SERVICE_NAME);

        // 3. switch to branch
        doSwitchBranch(branchName, CONSTANTS.SWITCH_BRANCH_SERVICE_NAME);

        // 4. assert that the working-copy is bound to the new branch
        // perform some changes
        File testFileWc1 = new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.TEST_FILE);
        String appendContent = "someAppendedContent";
        appendContentToFile(testFileWc1, appendContent);

        // commit changes
        doCommit("committing to branch.", null, CONSTANTS.COMMIT_SERVICE_NAMES[0]);

        // check out branch and assert changes
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC2_BRANCHES_SERVICE_NAME);

        File testFileWc2 = new File(new File(new File(CONSTANTS.WORKING_COPIES[1]), branchName),
                CONSTANTS.TEST_FILE);
        assertThatFileWasModified(testFileWc2, appendContent);
    }

    /**
     * Tests the ability to switch back to trunk again.
     *
     * @throws Exception
     */
    @Test
    public void switchBranch_shouldHonorTrunkKeyword() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. create branch
        String branchName = "myNewTestBranch";
        doBranch(branchName, "created new test-branch", CONSTANTS.BRANCH_SERVICE_NAME);

        // 3. switch to branch
        doSwitchBranch(branchName, CONSTANTS.SWITCH_BRANCH_SERVICE_NAME);

        // 4. perform some changes
        File testFile = new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.TEST_FILE);
        String appendContent = "someAppendedContent";
        appendContentToFile(testFile, appendContent);

        // commit changes
        doCommit("committing to branch.", null, CONSTANTS.COMMIT_SERVICE_NAMES[0]);

        // 5. switch to trunk
        doSwitchBranch("TRUNK", CONSTANTS.SWITCH_BRANCH_SERVICE_NAME);

        // 6. assert that trunk was not modified
        assertThatFileWasNotModified(testFile, appendContent);
    }

    /**
     * Tests the ability to merge a branch back to trunk again
     */
    @Test
    public void merge_shouldMergeBranchBackToTrunk() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. create branch
        String branchName = "myNewTestBranch";
        doBranch(branchName, "created new test-branch", CONSTANTS.BRANCH_SERVICE_NAME);

        // 3. switch to branch
        doSwitchBranch(branchName, CONSTANTS.SWITCH_BRANCH_SERVICE_NAME);

        // 4. perform some changes in branch
        File mergeFile = new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.MERGE_FILE);
        String appendContent = "someAppendedContent";
        appendContentToFile(mergeFile, appendContent);

        // commit changes
        doCommit("committing to branch.", null, CONSTANTS.COMMIT_SERVICE_NAMES[0]);

        // 5. switch back to trunk
        doSwitchBranch("TRUNK", CONSTANTS.SWITCH_BRANCH_SERVICE_NAME);

        // 6. assert that file is not changed yet
        assertThatFileWasNotModified(mergeFile, appendContent);

        // 7. merge branch in trunk
        MergeResult result = doMerge(branchName, CONSTANTS.MERGE_SERVICE_NAME);

        // check result
        assertEquals(0, result.getConflicts().length);
        assertEquals(0, result.getDeletions().length);
        assertEquals(0, result.getAdds().length);
        assertEquals(2, result.getMerges().length);

        String[] expectedUpdatedFiles = new String[] { new File(CONSTANTS.WORKING_COPIES[0]).getAbsolutePath(),
                new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.MERGE_FILE).getAbsolutePath(), };
        assertThatArraysEqualIgnorePosition(expectedUpdatedFiles, result.getMerges());

        // check filesystem
        assertThatFileWasModified(mergeFile, appendContent);
    }

    /**
     * Tests basic ability to annotate a file's lines with author and revision
     *
     * @throws Exception
     */
    @Test
    public void blame_shouldReturnAnnotatedFileContents() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. add content to testfile
        File testFile = new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.TEST_FILE);
        String appendContent = "someContentToBlame";
        appendContentToFile(testFile, appendContent);

        // commit changes
        doCommit("added content to blame to testfile.", null, CONSTANTS.COMMIT_SERVICE_NAMES[0]);

        // 3. blame (MUAHAHA ^^)
        String blamedContent = doBlame(CONSTANTS.TEST_FILE, CONSTANTS.BLAME_SERVICE_NAME);

        // 4. verfy content
        assertBlamedTestContent(blamedContent, appendContent, CONSTANTS.AUTHOR);
    }

    /**
     * Tests basic ability to compute the differences between revisions
     *
     * @throws Exception
     */
    @Test
    public void diff_shouldReturnDifferencesBetweenRevisions() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. append content to file
        File testFile = new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.TEST_FILE);
        String appendContent = "someContentToTriggerDiff";
        appendContentToFile(testFile, appendContent);

        // commit changes
        MergeResult result = doCommit("changed testfile", null, CONSTANTS.COMMIT_SERVICE_NAMES[0]);
        long revision = Long.parseLong(result.getRevision());

        // 3. execute dif-call
        String differences = doDiff(CONSTANTS.TEST_FILE, String.valueOf(revision - 1),
                CONSTANTS.DIFF_SERVICE_NAME);

        // 4. validate differences
        assertDifferences(differences, new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.TEST_FILE),
                appendContent, String.valueOf(revision - 1), String.valueOf(revision));
    }

    /**
     * Tests the basic ability to export the working copy's contents without
     * SVN-metadata
     *
     * @throws Exception
     */
    @Test
    public void export_shouldExportWorkingCopyWithoutSvnMetadata() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. export it
        doExport(CONSTANTS.EXPORT_PATH, CONSTANTS.EXPORT_SERVICE_NAME);

        // 3. assert exported struture
        // one file is enough for testing, since we rely on the bugfreenes of
        // svnKit
        File exportedTestFile = new File(new File(CONSTANTS.EXPORT_PATH), CONSTANTS.TEST_FILE);
        assertTrue("Missing exported testfile", exportedTestFile.exists());

        File exportedSvnMetada = new File(new File(CONSTANTS.EXPORT_PATH), ".snv");
        assertFalse("Found svn-metadata", exportedSvnMetada.exists());
    }

    /**
     * Asserts that export fails with an ScmException, should the destination
     * already exist.
     *
     * @throws Exception is expected to be thrown.
     */
    @Test(expected = ScmException.class)
    public void export_shouldFailWhenExportDirectoryAlreadyExists() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. create export directory prior to exporting
        File exportDirectory = new File(CONSTANTS.EXPORT_PATH);
        assertTrue("Could not create export direcotry", exportDirectory.mkdirs());

        // 3. export it
        // exception expected here
        doExport(CONSTANTS.EXPORT_PATH, CONSTANTS.EXPORT_SERVICE_NAME);
    }

    /**
     * Tests the basic ability to import an unversioned filesystem-tree into the
     * repository
     *
     * @throws Exception
     */
    @Test
    public void importTree_shouldImportFilesIntoWorkingCopy() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. import
        MergeResult result = doImport(CONSTANTS.IMPORT_PATH, null, "importing files", CONSTANTS.AUTHOR,
                CONSTANTS.IMPORT_SERVICE_NAME);

        // 3. assert result
        assertEquals(0, result.getConflicts().length);
        assertEquals(0, result.getDeletions().length);
        assertEquals(1, result.getAdds().length);
        assertEquals(0, result.getMerges().length);

        // 4. assert that files were imported by checking out second working
        // copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC2_TRUNK_SERVICE_NAME);

        File importFile = new File(new File(CONSTANTS.WORKING_COPIES[1]), CONSTANTS.IMPORT_FILE);
        assertTrue("mising " + importFile.getAbsolutePath(), importFile.exists());
    }

    /**
     * Asserts that import fails, when the repository is read only (i.e.
     * connection, not developerConnection, was set)
     *
     * @throws Exception
     */
    @Test(expected = ScmException.class)
    public void import_shouldFailWhenConnectionInsteadofDeveloperConnectionWasSet() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_ONLY_CONNECTION_SET);

        // 2. import
        // exception expected here
        doImport(CONSTANTS.IMPORT_PATH, null, "importing files", CONSTANTS.AUTHOR,
                CONSTANTS.IMPORT_NO_CONNECTION_SET_SERVICE_NAME);
    }

    /**
     * Tests the basic ability to collect commit-messages for files and
     * revisions.
     *
     * @throws Exception
     */
    @Test
    public void log_shouldReturnCommitMessageForSingleFileAndSingleRevision() throws Exception {
        // 1. check out initial working copy
        doCheckoutRepository(CONSTANTS.CHECKOUT_WC1_TRUNK_SERVICE_NAME);

        // 2. modify file and commit
        File testFile = new File(new File(CONSTANTS.WORKING_COPIES[0]), CONSTANTS.TEST_FILE);
        String appendContent = "someContent";
        appendContentToFile(testFile, appendContent);

        // commit
        String commitMessage = "testCommitMessage";
        MergeResult result = doCommit(commitMessage, null, CONSTANTS.COMMIT_SERVICE_NAMES[0]);

        // 3. call log
        // set up parameters
        String[] files = new String[] { testFile.getName() };

        // call
        Map<String, String> logs = doLog(files, result.getRevision(), result.getRevision(),
                CONSTANTS.LOG_SERVICE_NAME);

        // 4. examine logs
        assertEquals(1, logs.size());
        assertEquals(CONSTANTS.AUTHOR + ":\n" + commitMessage, logs.get(result.getRevision()));
    }

    /* end tests */

    /* implementation of abstract class */

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext(CONSTANTS.XBEAN_XML_NAME);
    }

    /* end implementation of abstract class */

    /* helpers */

    /**
     * Checks the message for errors and either fails or throws an Exception
     *
     * @param message
     * @throws Exception
     */
    private void validateReturnMessageSuccess(MessageExchange message) throws Exception {
        if (message.getStatus() == ExchangeStatus.ERROR) {
            if (message.getError() != null) {
                throw message.getError();
            } else {
                fail("Received ERROR status");
            }
        } else if (message.getFault() != null) {
            fail("Received fault: " + new SourceTransformer().toString(message.getFault().getContent()));
        }
    }

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

    /**
     * Triggers a jbi-call (sends normalized message) to check out a repository
     *
     * @param serviceName the service's name
     * @param repository The repository to check out from
     * @return The deserialized MergeReusult
     * @throws Exception
     */
    private MergeResult doCheckoutRepository(String serviceName) throws Exception {
        // set up and perform call
        DefaultServiceMixClient client = createClient();
        InOut inOut = createInOutMessage(client, serviceName, "<checkout " + " author=\"" + CONSTANTS.AUTHOR
                + "\"/>");

        client.sendSync(inOut);
        validateReturnMessageSuccess(inOut);

        // get result
        MergeResult result = MergeResultSerializer.deserialize(inOut.getOutMessage());

        // // finish MEP
        // client.done (inOut);

        return result;
    }

    /**
     * Triggers a jbi-call (sends normalized message) to check out or update a
     * repository
     *
     * @param serviceName the service's name
     * @param repository The repository to check out from
     * @return The deserialized MergeReusult
     * @throws Exception
     */
    private MergeResult doCheckoutOrUpdate(String serviceName) throws Exception {
        // set up and perform call
        DefaultServiceMixClient client = createClient();
        InOut inOut = createInOutMessage(client, serviceName, "<checkoutOrUpdate " + " author=\""
                + CONSTANTS.AUTHOR + "\"/>");

        client.sendSync(inOut);
        validateReturnMessageSuccess(inOut);

        // get result
        MergeResult result = MergeResultSerializer.deserialize(inOut.getOutMessage());

        // // finish MEP
        // client.done (inOut);

        return result;
    }

    /**
     * Copies a File to the Working copy and the triggers a jbi-call (sends
     * normalized message) to add that file
     *
     * @param fileToAdd The file to add (not in the working copy
     * @param workingCopy The path to the working copy. This is only needed to
     *        compute the fileToAdd correctly and should be the same as
     *        configured in xbean.xml
     * @param destination The destination (within then working copy) where the
     *        fileToAdd shall be copied.
     * @param serviceName The service's name
     * @return The file that was added on the working copy
     * @throws Exception
     */
    private File doAddFile(File fileToAdd, String workingCopy, String destination, String serviceName) throws Exception {
        // set up parameters
        String fileToAddName = fileToAdd.getName();
        String filePathRelativeToWorkingCopy = null;

        if (destination != null) {
            filePathRelativeToWorkingCopy = new File(new File(destination), fileToAddName).getPath();
        } else {
            filePathRelativeToWorkingCopy = fileToAddName;
        }

        File workingCopyFileToAdd = new File(new File(workingCopy), filePathRelativeToWorkingCopy);

        // copy file
        FileUtils.copyFile(fileToAdd, workingCopyFileToAdd);

        // set up message and send it
        DefaultServiceMixClient addClient = createClient();
        RobustInOnly addMessage = createRobustInOnlyMessage(addClient, serviceName, "<add fileToAdd=\""
                + filePathRelativeToWorkingCopy + "\"/>");
        addClient.sendSync(addMessage);

        validateReturnMessageSuccess(addMessage);

        return workingCopyFileToAdd;
    }

    /**
     * Triggers a jbi-call (sends normalized message) to delete a file.
     *
     * @param deletePath The file to delete.
     * @param serviceName The service's name.
     * @throws Exception
     */
    private void doDeleteFile(String deletePath, String serviceName) throws Exception {
        DefaultServiceMixClient client = createClient();
        RobustInOnly inOnly = createRobustInOnlyMessage(client, serviceName, "<delete fileToDelete=\"" + deletePath
                + "\"/>");

        client.sendSync(inOnly);
        validateReturnMessageSuccess(inOnly);
    }

    /**
     * Triggers a jbi-call (sends normalized message) to commit a working copy
     * to the repository
     *
     * @param message The commit message.
     * @param subPath Optional subpath that shall be committed instead of the
     *        whole working copy
     * @param serviceName The service's name.
     * @return The MergeResult.
     * @throws Exception
     */
    private MergeResult doCommit(String message, String subPath, String serviceName) throws Exception {
        DefaultServiceMixClient commitClient = createClient();

        InOut commitMessage = null;
        if (subPath == null) {
            commitMessage = createInOutMessage(commitClient, serviceName, "<commit message=\"" + message
                    + "\" author=\"" + CONSTANTS.AUTHOR + "\"/>");
        } else {
            commitMessage = createInOutMessage(commitClient, serviceName, "<commit message=\"" + message
                    + "\" author=\"" + CONSTANTS.AUTHOR + "\" subPath=\"" + subPath + "\"/>");
        }

        commitClient.sendSync(commitMessage);
        validateReturnMessageSuccess(commitMessage);

        // get result
        MergeResult result = MergeResultSerializer.deserialize(commitMessage.getOutMessage());

        // finish MEP
        commitClient.done(commitMessage);

        return result;
    }

    /**
     * Triggers a jbi-call (sends normalized message) to update the working copy
     * from the repository.
     *
     * @param serviceName The service's name
     * @param subPath Optional subpath to be updated instead of the whole
     *        working copy
     * @return The Merge-result
     * @throws Exception
     */
    private MergeResult doUpdate(String serviceName, String subPath) throws Exception {
        DefaultServiceMixClient updateClient = createClient();

        InOut updateMessage = null;
        if (subPath == null) {
            updateMessage = createInOutMessage(updateClient, serviceName, "<update />");
        } else {
            updateMessage = createInOutMessage(updateClient, serviceName, "<update updatePath=\"" + subPath + "\"/>");
        }

        updateClient.sendSync(updateMessage);
        validateReturnMessageSuccess(updateMessage);

        // get result
        MergeResult result = MergeResultSerializer.deserialize(updateMessage.getOutMessage());

        // finish MEP
        updateClient.done(updateMessage);

        return result;
    }

    /**
     * Triggers a jbi-call (sends normalized message) to create a new branch.
     *
     * @param branchName The branch's name that shall be created.
     * @param commitMessage The commit-message.
     * @param serviceName The service's name.
     * @throws Exception
     */
    private void doBranch(String branchName, String commitMessage, String serviceName) throws Exception {
        DefaultServiceMixClient client = createClient();
        RobustInOnly inOnly = createRobustInOnlyMessage(client, serviceName, "<branch name=\"" + branchName
                + "\" message=\"" + commitMessage + "\"/>");

        client.sendSync(inOnly);
        validateReturnMessageSuccess(inOnly);
    }

    /**
     * Triggers a jbi-call (sends normalized message) to list all branches.
     *
     * @param serviceName the service's name.
     * @return A list of branches.
     * @throws Exception
     */
    private String[] doListBranches(String serviceName) throws Exception {
        DefaultServiceMixClient client = createClient();
        InOut inOut = createInOutMessage(client, serviceName, "<listBranches/>");

        client.sendSync(inOut);
        validateReturnMessageSuccess(inOut);

        String[] result = StringArraySerializer.deserialize(inOut.getOutMessage());

        client.done(inOut);

        return result;
    }

    /**
     * Triggers a jbi-call (sends normalized message) to switch to a branch.
     *
     * @param branchName The branch's name to switch to.
     * @param serviceName the service's name.
     * @throws Exception
     */
    private void doSwitchBranch(String branchName, String serviceName) throws Exception {
        DefaultServiceMixClient client = createClient();
        RobustInOnly inOnly = createRobustInOnlyMessage(client, serviceName, "<switchBranch name=\"" + branchName
                + "\"/>");

        client.sendSync(inOnly);
        validateReturnMessageSuccess(inOnly);
    }

    /**
     * Triggers a jbi-call (sends normalized message) to merge a branch into the
     * working copy.
     *
     * @param branchName The branch's name to merge.
     * @param serviceName The service's name.
     * @return the merge-result
     * @throws Exception
     */
    private MergeResult doMerge(String branchName, String serviceName) throws Exception {
        DefaultServiceMixClient client = createClient();
        InOut inOut = createInOutMessage(client, serviceName, "<merge name=\"" + branchName + "\"/>");

        // send message
        client.sendSync(inOut);
        validateReturnMessageSuccess(inOut);

        // get result
        MergeResult result = MergeResultSerializer.deserialize(inOut.getOutMessage());

        // finish MEP
        client.done(inOut);

        return result;
    }

    /**
     * Triggers a jbi-call (sends normalized message) to blame/annotate a file.
     *
     * @param file the file's name relative to the working-copy
     * @param serviceName The service's name
     * @return The annotaten content of the file.
     * @throws Exception
     */
    private String doBlame(String file, String serviceName) throws Exception {
        DefaultServiceMixClient client = createClient();
        InOut inOut = createInOutMessage(client, serviceName, "<blame file=\"" + file + "\"/>");

        // send message
        client.sendSync(inOut);
        validateReturnMessageSuccess(inOut);

        // get result
        String result = StringSerializer.deserialize(inOut.getOutMessage());

        // finish MEP
        client.done(inOut);

        return result;
    }

    /**
     * Triggers a jbi-call (sends normalized message) to perform a diff on a
     * file between revion and HEAD.
     *
     * @param file The file to compute the diff for.
     * @param revision The revision to start with.
     * @param serviceName The service's name.
     * @return The differences between the two revisions.
     * @throws Exception
     */
    private String doDiff(String file, String revision, String serviceName) throws Exception {
        DefaultServiceMixClient client = createClient();
        InOut inOut = createInOutMessage(client, serviceName, "<diff file=\"" + file + "\" revision=\"" + revision
                + "\"/>");

        // send message
        client.sendSync(inOut);
        validateReturnMessageSuccess(inOut);

        // get result
        String result = StringSerializer.deserialize(inOut.getOutMessage());

        // finish MEP
        client.done(inOut);

        return result;
    }

    /**
     * Triggers a jbi-call (sends normalized message) to export a working copy
     * without SVN-metadata.
     *
     * @param exportPath The path to eport to
     * @param serviceName The service's name
     * @throws Exception
     */
    private void doExport(String exportPath, String serviceName) throws Exception {
        DefaultServiceMixClient client = createClient();
        RobustInOnly robustInOnly = createRobustInOnlyMessage(client, serviceName, "<export path=\"" + exportPath
                + "\"/>");

        client.sendSync(robustInOnly);
        validateReturnMessageSuccess(robustInOnly);
    }

    /**
     * Triggers a jbi-call (sends normalized message) to import an unversionen
     * file-tree into the repository
     *
     * @param importSourcePath the path to the location to be imported
     * @param importDestinationPath The destination within the repository to
     *        import to
     * @param commitMessage The commit-messag
     * @param author The author
     * @param serviceName The service's name
     * @return The merge-result
     * @throws Exception
     */
    private MergeResult doImport(String importSourcePath, String importDestinationPath, String commitMessage,
            String author, String serviceName) throws Exception {
        DefaultServiceMixClient client = createClient();

        InOut inOut = null;
        if (importDestinationPath != null) {
            inOut = createInOutMessage(client, serviceName, "<import source=\"" + importSourcePath
                    + "\" destination=\"" + importDestinationPath + "\"" + " message=\"" + commitMessage
                    + "\" author=\"" + author + "\"/>");
        } else {
            inOut = createInOutMessage(client, serviceName, "<import source=\"" + importSourcePath + "\""
                    + " message=\"" + commitMessage + "\" author=\"" + author + "\"/>");
        }

        // send message
        client.sendSync(inOut);
        validateReturnMessageSuccess(inOut);

        // get result
        MergeResult result = MergeResultSerializer.deserialize(inOut.getOutMessage());

        // finish MEP
        client.done(inOut);

        return result;
    }

    /**
     * Triggers a jbi-call (sends normalized message) to collect commit-messages
     * over files and revisions
     *
     * @param files The files to collect the commit-messages for
     * @param startRevision The revision to start to collect
     * @param endRevision The revision to end to collect
     * @param serviceName The service's name
     * @return A Map, where the key is the revision and the value is the commit
     *         message.
     * @throws Exception
     */
    private Map<String, String> doLog(String[] files, String startRevision, String endRevision, String serviceName)
            throws Exception {
        DefaultServiceMixClient client = createClient();

        // 1. create message
        // xml-ify files-array
        Source filesSource = StringArraySerializer.serialize(files, "files");
        String filesString = new SourceTransformer().toString(filesSource);

        // get rid of xml header
        // rather crude, but ok for testing
        filesString = filesString.replaceAll("<\\?xml.*\\?>", "");

        // assemble message
        String message = "<log startRevision=\"" + startRevision + "\" endRevision=\"" + endRevision + "\">"
                + filesString + "</log>";

        // 2. create and send message
        InOut inOut = createInOutMessage(client, serviceName, message);

        // send message
        client.sendSync(inOut);
        validateReturnMessageSuccess(inOut);

        // 3. parse result
        Map<String, String> result = StringMapSerializer.deserialize(inOut.getOutMessage());

        // 4. finish MEP
        client.done(inOut);

        return result;
    }

    /**
     * Rather hacky attempt to set an environment variable for test-purposes.
     * Copied and modified from {@link http
     * ://stackoverflow.com/questions/318239/
     * how-do-i-set-environment-variables-from-java}
     *
     * @param name The environment variable's name
     * @param value The environment varibale's value
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static void setEnvironmentVariable(String name, String value) throws Exception {
        Class<?>[] classes = Collections.class.getDeclaredClasses();
        Map<String, String> env = System.getenv();
        for (Class<?> clazz : classes) {
            if ("java.util.Collections$UnmodifiableMap".equals(clazz.getName())) {
                Field field = clazz.getDeclaredField("m");
                field.setAccessible(true);
                Object obj = field.get(env);
                Map<String, String> map = (Map<String, String>) obj;
                map.put(name, value);

                break;
            }
        }
    }

    private void setEnvironmentVariables() throws Exception {
        if (environmentIsSetUp) {
            return;
        }

        String repository = new File(CONSTANTS.REPOSITORY).toURI().toString();
        String repositoryNoBranches = new File(CONSTANTS.REPOSITORY_NO_BRANCH).toURI().toString();
        String trunk = new File(CONSTANTS.TRUNK).toURI().toString();
        String branches = new File(CONSTANTS.BRANCHES).toURI().toString();
        String noBranchTrunk = new File(CONSTANTS.BRANCHLESS_REPOSITORY_TRUNK).toURI().toString();
        String noBranchBranches = new File(CONSTANTS.BRANCHLESS_REPOSITORY_BRANCHES).toURI().toString();
        String workingCopy1 = new File(CONSTANTS.WORKING_COPIES[0]).getAbsolutePath().toString();
        String workingCopy2 = new File(CONSTANTS.WORKING_COPIES[1]).getAbsolutePath().toString();

        setEnvironmentVariable(CONSTANTS.REPOSITORY_ENV_NAME, repository);
        setEnvironmentVariable(CONSTANTS.REPOSITORY_NO_BRANCH_ENV_NAME, repositoryNoBranches);
        setEnvironmentVariable(CONSTANTS.TRUNK_ENV_NAME, trunk);
        setEnvironmentVariable(CONSTANTS.BRANCHES_ENV_NAME, branches);
        setEnvironmentVariable(CONSTANTS.NO_BRANCH_TRUNK_ENV_NAME, noBranchTrunk);
        setEnvironmentVariable(CONSTANTS.NO_BRANCH_BRANCHES_ENV_NAME, noBranchBranches);
        setEnvironmentVariable(CONSTANTS.WORKING_COPY1_ENV_NAME, workingCopy1);
        setEnvironmentVariable(CONSTANTS.WORKING_COPY2_ENV_NAME, workingCopy2);

        environmentIsSetUp = true;
    }

    private void setUpRepository() throws IOException, SVNException {
        deleteRepository();
        FileUtils.copyDirectoryStructure(new File(CONSTANTS.REFERENCE_REPOSITORY), new File(
                CONSTANTS.REPOSITORY));
        FileUtils.copyDirectoryStructure(new File(CONSTANTS.REFERENCE_REPOSITORY_NO_BRANCH), new File(
                CONSTANTS.REPOSITORY_NO_BRANCH));
    }

    private void deleteRepository() throws IOException {
        File repositoryPath = new File(CONSTANTS.REPOSITORY);
        File repositoryPathNoBranch = new File(CONSTANTS.REPOSITORY_NO_BRANCH);

        if (repositoryPath.exists()) {
            FileUtils.deleteDirectory(repositoryPath);
        }

        if (repositoryPathNoBranch.exists()) {
            FileUtils.deleteDirectory(repositoryPathNoBranch);
        }
    }

    private void deleteWorkingCopies() throws IOException {
        for (String workingCopy : CONSTANTS.WORKING_COPIES) {
            File workingCopyPath = new File(workingCopy);

            if (workingCopyPath.exists()) {
                FileUtils.deleteDirectory(workingCopyPath);
            }
        }

        File defaultWorkingCopy = new File("./data"); // well, actually the path
        // to the working copy is
        // longer, but we want to
        // delete the whole
        // data-directory

        if (defaultWorkingCopy.exists()) {
            FileUtils.deleteDirectory(defaultWorkingCopy);
        }
    }

    private void deleteExportDirectory() throws IOException {
        File exportDirectory = new File(CONSTANTS.EXPORT_PATH);

        if (exportDirectory.exists()) {
            FileUtils.deleteDirectory(exportDirectory);
        }
    }

    /* end helpers */
}
