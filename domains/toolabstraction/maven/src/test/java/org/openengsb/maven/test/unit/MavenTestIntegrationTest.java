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

package org.openengsb.maven.test.unit;

import javax.annotation.Resource;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.common.serializer.MavenResultSerializer;
import org.openengsb.maven.test.unit.constants.TestMvnTestConstantsIntegration;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testbeans.xml" })
public class MavenTestIntegrationTest extends SpringTestSupport {

    @Resource(name = "integration_constants_test")
    private TestMvnTestConstantsIntegration CONSTANTS;

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

    @Test
    @Ignore
    public void test_shouldRunTestsWithNoSurefirePlugin() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(this.jbi);
        InOut inOut = createInOutMessage(client, "urn:test-surefire", "test-no-surefire", "<mavenTester/>");
        client.sendSync(inOut);
        assertExchangeWorked(inOut);

        MavenResult testresult = MavenResultSerializer.deserialize(inOut.getOutMessage());
        client.done(inOut);

        assertEquals(MavenResult.SUCCESS, testresult.getMavenOutput());
    }

    @Test
    @Ignore
    public void test_shouldRunTestsAndFail_TestFailure() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(this.jbi);
        InOut inOut = createInOutMessage(client, "urn:test-surefire", "test-unit-fail", "<mavenTester/>");
        client.sendSync(inOut);
        assertExchangeWorked(inOut);

        MavenResult testresult = MavenResultSerializer.deserialize(inOut.getOutMessage());
        client.done(inOut);

        assertEquals(MavenResult.FAILURE, testresult.getMavenOutput());
    }

    @Test
    @Ignore
    public void test_shouldRunTestsWithConfiguredSurefire() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(this.jbi);
        InOut inOut = createInOutMessage(client, "urn:test-surefire", "test-valid-surefire", "<mavenTester/>");
        client.sendSync(inOut);
        assertExchangeWorked(inOut);

        MavenResult testresult = MavenResultSerializer.deserialize(inOut.getOutMessage());
        client.done(inOut);

        assertEquals(MavenResult.SUCCESS, testresult.getMavenOutput());
    }

    @Test
    @Ignore
    public void test_shouldRunTestsAndFail_InvalidSurefireParameter() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(this.jbi);
        InOut inOut = createInOutMessage(client, "urn:test-surefire", "test-invalid-surefire", "<mavenTester/>");
        client.sendSync(inOut);
        assertExchangeWorked(inOut);

        MavenResult testresult = MavenResultSerializer.deserialize(inOut.getOutMessage());
        client.done(inOut);

        assertNull(testresult);
    }
}
