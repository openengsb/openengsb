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

package org.openengsb.maven.test.unit.deploy;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.common.serializer.MavenResultSerializer;
import org.openengsb.maven.test.unit.deploy.constants.DeployMvnTestConstantsIntegration;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Collection of integration tests, that test the Component and Endpoints of the
 * Maven deploy domain by simulating a call from a fictive component.
 * 
 * @author patrick
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testbeans.xml" })
public class MavenDeployIntegrationTest extends SpringTestSupport {
    /**
     * The constants needed for the tests
     */
    @Resource(name = "integration_constants_deploy")
    private DeployMvnTestConstantsIntegration CONSTANTS;

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

    /* end creators */

    /**
     * Called before each test. Sets up a new, empty Maven-repository Don't get
     * confused by the name, this is actually JUnit 4 ;)
     */
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        if (new File(this.CONSTANTS.REPOSITORY).exists()) {
            deleteRepository();
        }

        createRepository();
    }

    /**
     * Called after each test. Deletes the newly created Maven-repository again.
     * Don't get confused by the name, this is actually JUnit 4
     */
    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        if (new File(this.CONSTANTS.REPOSITORY).exists()) {
            deleteRepository();
        }
    }

    /* tests */

    /**
     * Tests basic functionality by deploying one valid artifact
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void deployFile_shouldReturnSuccessfulResultWithValidArtifact() throws Exception {
        // set up and perform call
        DefaultServiceMixClient client = createClient();
        InOut inOut = createInOutMessage(client, this.CONSTANTS.VALID_ARTIFACTS_SERVICE_NAME,
                "<mavenDeployer fileToDeploy=\"" + this.CONSTANTS.VALID_ARTIFACT + "\"/>");

        client.sendSync(inOut);

        validateReturnMessageSuccess(inOut);

        // get resultList
        List<MavenResult> resultList = MavenResultSerializer.deserializeList(inOut.getOutMessage());
        client.done(inOut);

        assertEquals(1, resultList.size());
        MavenResult result = resultList.get(0);

        // examine resultList
        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());

        // that should be enough testing for an integration test
        // more extensive testing is done in unitTest
    }

    /**
     * Tests basic failure behavior by attempting to deploy an artifact without
     * pom-file
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void deployFile_houldReturnUnsuccessfulResultWithMissingPomArtifact() throws Exception {
        // set up and perform call
        DefaultServiceMixClient client = createClient();
        InOut inOut = createInOutMessage(client, this.CONSTANTS.INVALID_ARTIFACTS_SERVICE_NAME,
                "<mavenDeployer fileToDeploy=\"" + this.CONSTANTS.MISSING_POM_ARTIFACT + "\"/>");
        client.sendSync(inOut);

        validateReturnMessageSuccess(inOut);

        // get resultList
        List<MavenResult> resultList = MavenResultSerializer.deserializeList(inOut.getOutMessage());
        ;
        client.done(inOut);

        assertEquals(1, resultList.size());
        MavenResult result = resultList.get(0);

        // examine resultList
        assertEquals(MavenResult.ERROR, result.getMavenOutput());

        // that should be enough testing for an integration test
        // more extensive testing is done in unitTest
    }

    /**
     * Tests basic failure behavior by attempting to deploy a non-existing
     * artifact
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void deployFile_shouldReturnUnsuccessfulResultWithNotExistingArtifact() throws Exception {
        // set up and perform call
        DefaultServiceMixClient client = createClient();
        InOut inOut = createInOutMessage(client, this.CONSTANTS.VALID_ARTIFACTS_SERVICE_NAME,
                "<mavenDeployer fileToDeploy=\"" + this.CONSTANTS.MISSING_POM_ARTIFACT + "\"/>");
        client.sendSync(inOut);

        validateReturnMessageSuccess(inOut);

        // get resultList
        List<MavenResult> resultList = MavenResultSerializer.deserializeList(inOut.getOutMessage());
        client.done(inOut);

        assertEquals(1, resultList.size());
        MavenResult result = resultList.get(0);

        // examine resultList
        assertEquals(MavenResult.ERROR, result.getMavenOutput());

        // that should be enough testing for an integration test
        // more extensive testing is done in unitTest
    }

    /**
     * Tests if configuring the component with an xbean.xml works correctly by
     * deploying all valid artifacts listed in it. Also tests functionality of
     * the execute-call.
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void execute_WithValidArtifactsShouldReturnResultListWithSuccessfulItems() throws Exception {
        // set up and perform call
        DefaultServiceMixClient client = createClient();
        InOut inOut = createInOutMessage(client, this.CONSTANTS.VALID_ARTIFACTS_SERVICE_NAME, "<mavenDeployer />");
        client.sendSync(inOut);

        validateReturnMessageSuccess(inOut);

        // get resultList
        List<MavenResult> resultList = MavenResultSerializer.deserializeList(inOut.getOutMessage());
        client.done(inOut);

        // examine resultList
        assertEquals(2, resultList.size());
        for (MavenResult result : resultList) {
            assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
        }

        // that should be enough testing for an integration test
        // more extensive testing is done in unitTest
    }

    /**
     * Tests if configuring the component with an xbean.xml works correctly by
     * deploying all invalid artifacts listed in it. Also tests functionality of
     * the execute-call.
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void execute_WithInvalidArtifactsShouldReturnResultListWithUnsuccessfulItems() throws Exception {
        // set up and perform call
        DefaultServiceMixClient client = createClient();
        InOut inOut = createInOutMessage(client, this.CONSTANTS.INVALID_ARTIFACTS_SERVICE_NAME, "<mavenDeveloper />");
        client.sendSync(inOut);

        validateReturnMessageSuccess(inOut);

        // get resultList
        List<MavenResult> resultList = MavenResultSerializer.deserializeList(inOut.getOutMessage());
        client.done(inOut);

        // examine resultList
        assertEquals(2, resultList.size());
        for (MavenResult result : resultList) {
            assertEquals(MavenResult.ERROR, result.getMavenOutput());
        }

        // that should be enough testing for an integration test
        // more extensive testing is done in unitTest
    }

    /* end tests */

    /* implementation of abstrat class */

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext(this.CONSTANTS.XBEAN_XML_NAME);
    }

    /* end implementation of abstrat class */

    /* helpers */

    /**
     * Checks the message for errors and either fails or throws an Exception
     * 
     * @param message
     */
    private void validateReturnMessageSuccess(InOut message) throws Exception {
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
     * creates a new repository on the filesystem
     */
    private void createRepository() {
        File repositoryFile = new File(this.CONSTANTS.REPOSITORY);
        assertTrue("Could not create test-repository", repositoryFile.mkdirs());
    }

    /**
     * Deletes the repository from the filesystem
     * 
     * @throws IOException
     */
    private void deleteRepository() throws IOException {
        File repositoryFile = new File(this.CONSTANTS.REPOSITORY);

        FileUtils.deleteDirectory(repositoryFile);
    }

    /* end helpers */

}
