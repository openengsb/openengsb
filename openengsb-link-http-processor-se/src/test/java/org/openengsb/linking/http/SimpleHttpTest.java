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
package org.openengsb.linking.http;

import java.util.ArrayList;
import java.util.List;

import javax.jbi.JBIException;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for the http-binding of the link-service
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testSpring.xml" })
public class SimpleHttpTest extends SpringTestSupport {
    /* creators */

    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String WHOAMI_REQUEST = "whoami";
    private static final String HTTP_REQUEST_URL = "http://localhost:8192/Link/";
    private static HttpClient httpClient;
    private static GetMethod get;

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
        super.setUp();
        createClient();
        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
        get = new GetMethod(HTTP_REQUEST_URL);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /* end implementation of abstract class */

    /**
     * sends a whoami-request to the endpoint to determine the IP-address.
     */
    @Test
    public void testHttpClientSuccess() throws Exception {

        get.setQueryString(WHOAMI_REQUEST);

        int resultCode = httpClient.executeMethod(get);
        Assert.assertEquals("HTTP-request did not return with status OK", 200, resultCode);

        final String responseBody = get.getResponseBodyAsString();
        Assert.assertTrue("the service did not return the correct html-page, but \"" + responseBody + "\"",
                responseBody.contains(LOCALHOST_IP));

    }

    /*
     * ignored because jms does not work in embedded smx.
     * javax.jms.JMSException: Could not connect to broker URL:
     * tcp://localhost:61616. Reason: java.net.ConnectException: Connection
     * refused
     */
    /**
     * sends a real request to the endpoint and verify the result in a
     * jms-listener
     */
    @Test(timeout = 5000)
    @Ignore
    public void testJmsResponse() throws Exception {
        get.setQueryString("12345");

        // do not check the return-code here, that's what other tests are for.
        List<Object> messages = new ArrayList<Object>();
        JmsListener listener = new JmsListener(LOCALHOST_IP);
        listener.start();
        httpClient.executeMethod(get);
        synchronized (messages) {
            messages.wait();
        }

    }
}
