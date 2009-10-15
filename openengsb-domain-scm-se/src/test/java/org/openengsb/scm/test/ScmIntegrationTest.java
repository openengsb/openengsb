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
package org.openengsb.scm.test;

import java.io.File;
import java.io.IOException;
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

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.scm.common.pojos.MergeResult;
import org.openengsb.scm.common.util.MergeResultSerializer;
import org.openengsb.scm.exceptions.UnknownIdException;
import org.openengsb.scm.test.constants.ScmIntegrationTestConstants;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;


/**
 * Integration-tests for the SCM-Domain. These Tests verify, that the forwarding
 * of messages and errors to and from the actual Connector is handled correctly.
 * For more thorough tests (i.e. the whole functionality of the domain), have a
 * look at the specific connectors (SVN, Git, etc...)
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/integrationtestSpring.xml" })
public class ScmIntegrationTest extends SpringTestSupport {
    private static SVNClientManager clientManager;

    /**
     * The constants needed for the tests
     */
    @Resource
    private ScmIntegrationTestConstants CONSTANTS;

    private boolean environmentIsSetUp = false;

    /* creators */

    /**
     * Creates a new ServiceMixClieant
     * 
     * @return The new ServiceMixClient
     */
    private DefaultServiceMixClient createClient() throws JBIException {
        return new DefaultServiceMixClient(this.jbi);
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
        inOut.setService(new QName(this.CONSTANTS.TEST_NAMESPACE, service));
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
        robustInOnly.setService(new QName(this.CONSTANTS.TEST_NAMESPACE, service));
        robustInOnly.getInMessage().setContent(new StringSource(message));

        return robustInOnly;
    }

    /* end creators */

    /* setup */

    @BeforeClass
    public static void setUpClientManager() {
        ScmIntegrationTest.clientManager = SVNClientManager.newInstance();
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
        super.tearDown();
    }

    /* end setup */

    /**
     * Verifies, that an InOut-MEP is forwarded correctly (to the correct
     * connector), by issuing a call for checkout.
     * 
     * @throws Exception
     */
    @Test
    public void inOut_shouldCheckoutRepositoryWhenCheckoutWasPassed() throws Exception {
        // 1. checkout repository 1
        MergeResult result = doCheckoutRepository(this.CONSTANTS.SERVICE_ID1);

        // 2. validate result
        assertEquals(1, result.getAdds().length);

        // 3. validate filesystem
        File checkedOutFile = new File(this.CONSTANTS.WORKING_COPY1, this.CONSTANTS.TEST_FILE1);
        assertEquals(checkedOutFile.getAbsolutePath(), result.getAdds()[0]);
    }

    /**
     * Verifies, that two distinct calls to two distict connectors are handled
     * properly (InOut-MEP)
     * 
     * @throws Exception
     */
    @Test
    public void inOut_shouldPassCallToCorrectService() throws Exception {
        // 1. checkout repository 1 and 2
        MergeResult result1 = doCheckoutRepository(this.CONSTANTS.SERVICE_ID1);
        MergeResult result2 = doCheckoutRepository(this.CONSTANTS.SERVICE_ID2);

        // 2. validate result
        assertEquals(1, result1.getAdds().length);
        assertEquals(1, result2.getAdds().length);

        // 3. validate filesystem
        File checkedOutFile1 = new File(this.CONSTANTS.WORKING_COPY1, this.CONSTANTS.TEST_FILE1);
        File checkedOutFile2 = new File(this.CONSTANTS.WORKING_COPY2, this.CONSTANTS.TEST_FILE2);
        assertEquals(checkedOutFile1.getAbsolutePath(), result1.getAdds()[0]);
        assertEquals(checkedOutFile2.getAbsolutePath(), result2.getAdds()[0]);
    }

    /**
     * Asserts that a call with an unknown ID fails (InOut).
     * 
     * @throws Exception
     */
    @Test(expected = UnknownIdException.class)
    public void inOut_shouldFailWhenIdIsInknown() throws Exception {
        // exception expected here
        doCheckoutRepository(this.CONSTANTS.SERVICE_ID_UNKNOWN);
    }

    /**
     * Verifies, that an RobustInOnly-MEP is forwarded correctly (to the correct
     * connector), by issuing a deletion of a file.
     * 
     * @throws Exception
     */
    @Test
    public void inOnly_shouldDeleteFileWhenDeleteWasPassed() throws Exception {
        // 1. checkout repository 1
        doCheckoutRepository(this.CONSTANTS.SERVICE_ID1);

        // 2. delete file
        doDeleteFile(this.CONSTANTS.TEST_FILE1, this.CONSTANTS.SERVICE_ID1);

        // 3. validate file's status
        File workingCopyDeleteFile = new File(new File(this.CONSTANTS.WORKING_COPY1), this.CONSTANTS.TEST_FILE1);
        SVNStatus status = ScmIntegrationTest.clientManager.getStatusClient().doStatus(workingCopyDeleteFile, false);
        assertEquals(SVNStatusType.STATUS_DELETED.getID(), status.getContentsStatus().getID());
    }

    /**
     * Verifies, that two distinct calls to two distinct connectors are handled
     * properly (RobustInOnly-MEP)
     * 
     * @throws Exception
     */
    @Test
    public void inOnly_shouldPassCallToCorrectService() throws Exception {
        // 1. checkout repositories
        doCheckoutRepository(this.CONSTANTS.SERVICE_ID1);
        doCheckoutRepository(this.CONSTANTS.SERVICE_ID2);

        // 2. delete files
        doDeleteFile(this.CONSTANTS.TEST_FILE1, this.CONSTANTS.SERVICE_ID1);
        doDeleteFile(this.CONSTANTS.TEST_FILE2, this.CONSTANTS.SERVICE_ID2);

        // 3. validate file's status
        File workingCopyDeleteFile1 = new File(new File(this.CONSTANTS.WORKING_COPY1), this.CONSTANTS.TEST_FILE1);
        File workingCopyDeleteFile2 = new File(new File(this.CONSTANTS.WORKING_COPY2), this.CONSTANTS.TEST_FILE2);

        SVNStatus status1 = ScmIntegrationTest.clientManager.getStatusClient().doStatus(workingCopyDeleteFile1, false);
        SVNStatus status2 = ScmIntegrationTest.clientManager.getStatusClient().doStatus(workingCopyDeleteFile2, false);

        assertEquals(SVNStatusType.STATUS_DELETED.getID(), status1.getContentsStatus().getID());
        assertEquals(SVNStatusType.STATUS_DELETED.getID(), status2.getContentsStatus().getID());
    }

    /**
     * Asserts that a call with an unknown ID fails (RobustInOnly).
     * 
     * @throws Exception
     */
    @Test(expected = UnknownIdException.class)
    public void inOnly_shouldFailWhenIdIsUnknown() throws Exception {
        // exception expected here
        doDeleteFile(this.CONSTANTS.TEST_FILE1, this.CONSTANTS.SERVICE_ID_UNKNOWN);
    }

    /**
     * Asserts that a call to an operation, that requires an InOut-MEP, fails,
     * when InOnly is used. This is accomplished by calling checkout with
     * InOnly.
     * 
     * @throws Exception
     */
    @Test(expected = UnsupportedOperationException.class)
    public void inOut_shouldThrowExceptionWhenActuallyCallingInOnlyOperation() throws Exception {
        DefaultServiceMixClient client = createClient();
        RobustInOnly inOnly = createRobustInOnlyMessage(client, this.CONSTANTS.TEST_SERVICE_NAME, "<execute id=\""
                + this.CONSTANTS.SERVICE_ID1 + "\">" + "<checkout " + " author=\"" + this.CONSTANTS.AUTHOR + "\"/>"
                + "</execute>");

        client.sendSync(inOnly);

        // exception expected here
        validateReturnMessageSuccess(inOnly);
    }

    /**
     * Asserts that a call to an operation, that requires an InOnly-MEP, fails,
     * when InOut is used. This is accomplished by calling delete with InOut.
     * 
     * @throws Exception
     */
    @Test(expected = UnsupportedOperationException.class)
    public void inOnly_shouldFailWhenActuallyCallingInOutOperation() throws Exception {
        DefaultServiceMixClient client = createClient();
        InOut inOut = createInOutMessage(client, this.CONSTANTS.TEST_SERVICE_NAME, "<execute id=\""
                + this.CONSTANTS.SERVICE_ID1 + "\">" + "<delete fileToDelete=\"" + this.CONSTANTS.TEST_FILE1 + "\"/>"
                + "</execute>");

        client.sendSync(inOut);

        // exception expected here
        validateReturnMessageSuccess(inOut);
    }

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext(this.CONSTANTS.XBEAN_XML_NAME);
    }

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
     * Triggers a jbi-call (sends normalized message) to check out a repository.
     * Used to test InOut calls.
     * 
     * @param id The id of the real service to lookup and call.
     * @return The deserialized MergeReusult.
     * @throws Exception
     */
    private MergeResult doCheckoutRepository(String id) throws Exception {
        // set up and perform call
        DefaultServiceMixClient client = createClient();
        InOut inOut = createInOutMessage(client, this.CONSTANTS.TEST_SERVICE_NAME, "<execute id=\"" + id + "\">"
                + "<checkout " + " author=\"" + this.CONSTANTS.AUTHOR + "\"/>" + "</execute>");

        client.sendSync(inOut);
        validateReturnMessageSuccess(inOut);

        // get result
        MergeResult result = MergeResultSerializer.deserialize(inOut.getOutMessage());

        return result;
    }

    /**
     * Triggers a jbi-call (sends normalized message) to delete a file. Used to
     * test InOnly calls.
     * 
     * @param deletePath The file to delete.
     * @param id The id of the real service to lookup and call.
     * @throws Exception
     */
    private void doDeleteFile(String deletePath, String id) throws Exception {
        DefaultServiceMixClient client = createClient();
        RobustInOnly inOnly = createRobustInOnlyMessage(client, this.CONSTANTS.TEST_SERVICE_NAME, "<execute id=\"" + id
                + "\">" + "<delete fileToDelete=\"" + deletePath + "\"/>" + "</execute>");

        client.sendSync(inOnly);
        validateReturnMessageSuccess(inOnly);
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
        if (this.environmentIsSetUp) {
            return;
        }

        String repository1 = new File(this.CONSTANTS.TRUNK1).toURI().toString();
        String repository2 = new File(this.CONSTANTS.TRUNK2).toURI().toString();

        setEnvironmentVariable(this.CONSTANTS.REPOSITORY1_ENV_NAME, repository1);
        setEnvironmentVariable(this.CONSTANTS.REPOSITORY2_ENV_NAME, repository2);

        this.environmentIsSetUp = true;
    }

    private void setUpRepository() throws IOException {
        deleteRepository();
        FileUtils.copyDirectoryStructure(new File(this.CONSTANTS.REFERENCE_REPOSITORY1), new File(
                this.CONSTANTS.REPOSITORY1));
        FileUtils.copyDirectoryStructure(new File(this.CONSTANTS.REFERENCE_REPOSITORY2), new File(
                this.CONSTANTS.REPOSITORY2));
    }

    private void deleteRepository() throws IOException {
        File repositoryPath1 = new File(this.CONSTANTS.REPOSITORY1);
        File repositoryPath2 = new File(this.CONSTANTS.REPOSITORY2);

        if (repositoryPath1.exists()) {
            FileUtils.deleteDirectory(repositoryPath1);
        }

        if (repositoryPath2.exists()) {
            FileUtils.deleteDirectory(repositoryPath2);
        }
    }

    private void deleteWorkingCopies() throws IOException {
        String[] workingCopies = new String[] { this.CONSTANTS.WORKING_COPY1, this.CONSTANTS.WORKING_COPY2 };

        for (String workingCopy : workingCopies) {
            File workingCopyPath = new File(workingCopy);

            if (workingCopyPath.exists()) {
                FileUtils.deleteDirectory(workingCopyPath);
            }
        }
    }
}
