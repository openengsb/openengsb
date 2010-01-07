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

package org.openengsb.maven.test.unit.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.maven.common.pojos.ProjectConfiguration;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.common.serializer.MavenResultSerializer;
import org.openengsb.maven.common.serializer.ProjectConfigurationSerializer;
import org.openengsb.maven.se.MavenComponent;
import org.openengsb.maven.se.endpoints.MavenBuildEndpoint;
import org.openengsb.maven.test.unit.build.constants.BuildMvnTestConstantsIntegration;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.emory.mathcs.backport.java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testbeans.xml" })
public class MavenBuildIntegrationTest extends SpringTestSupport {

    @Resource(name = "integration_constants_build")
    private BuildMvnTestConstantsIntegration CONSTANTS;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    protected void assertExchangeWorked(InOut inOut) throws Exception {
        if (inOut.getStatus() == ExchangeStatus.ERROR) {
            if (inOut.getError() != null) {
                throw inOut.getError();
            } else {
                fail("Received ERROR status");
            }
        } else if (inOut.getFault() != null) {
            fail("Received fault: " + new SourceTransformer().toString(inOut.getFault().getContent()));
        }
    }

    private InOut createInOutMessage(DefaultServiceMixClient client, String namespace, String service, String message)
            throws MessagingException {
        InOut inOut = client.createInOutExchange();
        inOut.setService(new QName(namespace, service));
        inOut.getInMessage().setContent(new StringSource(message));

        return inOut;
    }

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext(this.CONSTANTS.getXbean());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Ignore
    public void testBuildShouldBeSuccessful() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(this.jbi);

        InOut inOut = createInOutMessage(client, "urn:buildtest", "build-test", "<mavenBuilder/>");

        ClassPathResource res = new ClassPathResource(this.CONSTANTS.getTest_project());
        File baseDirectory = res.getFile();

        ProjectConfiguration projectConfig = (ProjectConfiguration) getBean("projectConfigBean");
        projectConfig.setBaseDirectory(baseDirectory);
        projectConfig.setGoals(new ArrayList<String>(Arrays.asList(new String[] { "clean", "package" })));

        Source source = ProjectConfigurationSerializer.serialize(inOut.getInMessage().getContent(), projectConfig);
        inOut.getInMessage().setContent(source);
        client.sendSync(inOut);

        assertExchangeWorked(inOut);

        MavenResult result = MavenResultSerializer.deserialize(inOut.getOutMessage());
        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Ignore
    public void testBuildShouldFail() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(this.jbi);

        InOut inOut = createInOutMessage(client, "urn:buildtest", "build-test-fail", "<mavenBuilder/>");

        ClassPathResource res = new ClassPathResource(this.CONSTANTS.getTest_project_fail());
        File basedirecotry = res.getFile();

        ProjectConfiguration projectConfig = (ProjectConfiguration) getBean("projectConfigBeanFail");
        projectConfig.setBaseDirectory(basedirecotry);
        projectConfig.setGoals(new ArrayList<String>(Arrays.asList(new String[] { "clean", "package" })));

        Source source = ProjectConfigurationSerializer.serialize(inOut.getInMessage().getContent(), projectConfig);
        inOut.getInMessage().setContent(source);
        client.sendSync(inOut);

        assertExchangeWorked(inOut);
        MavenResult result = MavenResultSerializer.deserialize(inOut.getOutMessage());
        assertEquals(MavenResult.FAILURE, result.getMavenOutput());
    }

    @Test
    @Ignore
    public void testBuildShouldBeSuccessfulWithDownloadDep() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(this.jbi);

        InOut inOut = createInOutMessage(client, "urn:buildtest", "build-test-dep", "<mavenBuilder/>");

        ProjectConfiguration projectConfig = (ProjectConfiguration) getBean("projectConfigBeanDep");

        Source source = ProjectConfigurationSerializer.serialize(inOut.getInMessage().getContent(), projectConfig);
        inOut.getInMessage().setContent(source);
        client.sendSync(inOut);

        assertExchangeWorked(inOut);
        MavenResult result = MavenResultSerializer.deserialize(inOut.getOutMessage());
        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
    }

    @Test
    @Ignore
    public void testBuildShouldFailOfInvalidPom() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(this.jbi);

        InOut inOut = createInOutMessage(client, "urn:buildtest", "build-test-invalid-pom", "<mavenBuilder/>");

        ProjectConfiguration projectConfig = (ProjectConfiguration) getBean("projectConfigBeanInvalidPom");

        Source source = ProjectConfigurationSerializer.serialize(inOut.getInMessage().getContent(), projectConfig);
        inOut.getInMessage().setContent(source);
        client.sendSync(inOut);

        assertExchangeWorked(inOut);
        MavenResult result = MavenResultSerializer.deserialize(inOut.getOutMessage());
        assertEquals(MavenResult.ERROR, result.getMavenOutput());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Ignore
    public void testProviderEndpoint() throws IOException, JBIException {

        JBIContainer jbi = new JBIContainer();
        jbi.setEmbedded(true);
        jbi.init();

        MavenComponent build = new MavenComponent();
        MavenBuildEndpoint endpoint = new MavenBuildEndpoint();
        endpoint.setService(new QName("build-maven"));
        endpoint.setEndpoint("endpoint");

        ClassPathResource res = new ClassPathResource(this.CONSTANTS.getTest_project_fail());
        File testProjectPOM = res.getFile();
        ProjectConfiguration projectConfig = (ProjectConfiguration) getBean("projectConfigBeanFail");
        projectConfig.setBaseDirectory(testProjectPOM.getParentFile());
        projectConfig.setGoals(new ArrayList<String>(Arrays.asList(new String[] { "clean", "package" })));

        endpoint.setProjectConfiguration(projectConfig);
        build.setEndpoints(new MavenBuildEndpoint[] { endpoint });
        jbi.activateComponent(build, "servicemix-build");

        jbi.start();

        build.stop();

        build.start();

        jbi.shutDown();
    }

}
