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

package org.openengsb.maven.test.unit.installfile;

import java.util.List;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.apache.camel.converter.jaxp.StringSource;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.maven.common.pojos.InstallFileDescriptor;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.common.serializer.InstallFileDescriptorSerializer;
import org.openengsb.maven.common.serializer.MavenResultSerializer;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testbeans.xml" })
public class MavenInstallFileIntegrationTest extends SpringTestSupport {

    private static final String validFilePath = "target/test-classes/installfile-valid/installfile-sample.jar";
    private static final String invalidFilePath = "target/test-classes/installfile-invalid/installfile-sample.jar";
    private static final String groupId = "com.installfilevalidgroup";
    private static final String artifactId = "installfilevalidartifact";
    private static final String version = "installfilevalidversion-1.0";
    private static final String packaging = "jar";
    private static final String xbean = "spring-test-xbean-installfile.xml";
    private static final String testNamespace = "urn:test";
    private static final String serviceName = "fileInstallerService";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext(xbean);
    }

    private InOut createInOutMessage(DefaultServiceMixClient client, String service, Source source)
            throws MessagingException {
        InOut inOut = client.createInOutExchange();
        inOut.setService(new QName(testNamespace, service));
        inOut.getInMessage().setContent(source);

        return inOut;
    }

    @Test
    @Ignore
    public void successfulRunShouldReturnPositiveResult() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(this.jbi);
        InOut messageExchange = createInOutMessage(client, serviceName, InstallFileDescriptorSerializer
                .serialize(new InstallFileDescriptor(validFilePath, groupId, artifactId, version, packaging)));
        client.sendSync(messageExchange);

        validateReturnMessage(messageExchange);

        List<MavenResult> resultList = MavenResultSerializer.deserializeList(messageExchange.getOutMessage());
        client.done(messageExchange);

        assertEquals(1, resultList.size());
        MavenResult result = resultList.get(0);
        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
    }

    @Test
    @Ignore
    public void runWithNonExistingFilePathShouldReturnError() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(this.jbi);
        InOut messageExchange = createInOutMessage(client, serviceName, InstallFileDescriptorSerializer
                .serialize(new InstallFileDescriptor(invalidFilePath, groupId, artifactId, version, packaging)));
        client.sendSync(messageExchange);

        validateReturnMessage(messageExchange);

        List<MavenResult> resultList = MavenResultSerializer.deserializeList(messageExchange.getOutMessage());
        client.done(messageExchange);

        assertEquals(1, resultList.size());
        MavenResult result = resultList.get(0);
        assertEquals(MavenResult.ERROR, result.getMavenOutput());
    }

    @Test
    @Ignore
    public void runWithInvalidMessageFormatShouldReturnError() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(this.jbi);
        InOut messageExchange = createInOutMessage(client, serviceName, new StringSource(
                "<invalidmessage invalidattribute=\"invalid\" />"));
        client.sendSync(messageExchange);

        validateReturnMessage(messageExchange);

        List<MavenResult> resultList = MavenResultSerializer.deserializeList(messageExchange.getOutMessage());
        client.done(messageExchange);

        assertEquals(1, resultList.size());
        MavenResult result = resultList.get(0);
        assertEquals(MavenResult.ERROR, result.getMavenOutput());
        assertEquals("An error occurred deserializing the incoming message.", result.getErrorMessage());
        assertEquals(1, result.getExceptions().size());
        assertEquals("Not all expected attributes could be found.", result.getExceptions().get(0).getMessage());
    }

    @Test
    @Ignore
    public void runWithInvalidParametersShouldReturnError() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(this.jbi);

        InOut messageExchange = createInOutMessage(client, serviceName, new StringSource(
                "<mavenFileInstaller fileToInstall=\"\" groupId=\"\" artifactId=\"\" version=\"\" packaging=\"\"/>"));
        client.sendSync(messageExchange);

        validateReturnMessage(messageExchange);

        List<MavenResult> resultList = MavenResultSerializer.deserializeList(messageExchange.getOutMessage());
        client.done(messageExchange);

        assertEquals(1, resultList.size());
        MavenResult result = resultList.get(0);
        assertEquals(MavenResult.ERROR, result.getMavenOutput());
        assertEquals("Cannot install file. The given file descriptor is invalid.", result.getErrorMessage());
    }

    // TODO Refactor this, since this method is also used by other maven tests
    // ... pull it into separate maven test base class
    private void validateReturnMessage(InOut message) throws Exception {
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

}