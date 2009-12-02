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
package org.openengsb.edb.http;

import javax.jbi.JBIException;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Collection of integration tests, that test the Component and Endpoints of the
 * SVN deploy domain by simulating a call from a fictive component. These tests
 * are more or less the same as in the UnitTest. They set up the calls
 * differently, but essentially perform the same checks and assertions.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/integrationtestSpring.xml" })
public class SimpleHttpTest extends SpringTestSupport {
    /* creators */

    /**
     * Creates a new ServiceMixClieant
     *
     * @return The new ServiceMixClient
     */
    private DefaultServiceMixClient createClient() throws JBIException {
        return new DefaultServiceMixClient(jbi);
    }

    /* implementation of abstract class */

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("testXbean.xml");
    }

    @Before
    @Override
    public void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }

    /* end implementation of abstract class */

    @Test
    public void dummyTest() throws Exception {
        ServiceMixClient client = createClient();
        System.out.println(client.getContext());
    }
}
