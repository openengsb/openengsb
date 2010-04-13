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

package org.openengsb.edb.jbi.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.InOutImpl;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.contextcommon.ContextHelperImpl;
import org.openengsb.core.OpenEngSBComponent;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.core.api.EDBHandlerFactory;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.util.IO;
import org.openengsb.util.Prelude;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:testBeans.xml" })
public class RepositoryRemoveTest extends SpringTestSupport {
    @Resource
    private EDBHandlerFactory config;

    /* test-parameters */
    private static String TEST_NAMESPACE = "urn:openengsb:edb";
    private static final UUID UUID_1 = UUID.fromString("5ff89772-0e20-44bd-9a97-d022ec2680db");
    private static final UUID UUID_2 = UUID.fromString("5ff89773-0e20-44bd-9a97-d022ec2680db");
    private static final String USER = "andreas";

    private static final String PATH_1 = "d2sdf000";
    private static final String PATH_2 = "any key";

    private static final String ABSTRACT_PATH_COMMIT = "/first/second/";

    private static final String MESSAGE_TYPE_COMMIT_EDB = "acmPersistMessage";
    private static final String MESSAGE_TYPE_RESPONSE = "acmResponseMessage";
    private static final String MESSAGE_TYPE_FULLRESET = "acmResetFullRequestMessage";

    private static final String EDB_SERVICE_NAME = "edb";
    private static GenericContent gc1;
    private static GenericContent gc2;
    private static Document persistMessage;
    private static Document fullResetMessage;

    /* end test-parameters */

    /* test-variables */
    private DefaultServiceMixClient client;
    private static EDBHandler handler;

    /*
     * It is required to override these methods for some reason.
     */

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("testXBean.xml");
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        storeEDBContext();
        
        makeParameters(this.config);
        RepositoryRemoveTest.handler = this.config.loadDefaultRepository();
        this.client = createClient();
        sendMessageAndParseResponse(RepositoryRemoveTest.persistMessage);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        IO.deleteStructure(RepositoryRemoveTest.handler.getRepositoryBase().getParentFile());
        IO.deleteStructure(new File("links"));
        super.tearDown();
    }

    @Test
    public void testFullReset() throws Exception {
        File edbBaseDir = RepositoryRemoveTest.handler.getRepositoryBase().getParentFile();
        final Document doc = sendMessageAndParseResponse(RepositoryRemoveTest.fullResetMessage);

        final Element root = doc.getRootElement();
        assertEquals(MESSAGE_TYPE_RESPONSE, root.getName());
        assertFalse(edbBaseDir.exists());

    }

    private static void makeParameters(final EDBHandlerFactory config) {

        RepositoryRemoveTest.handler = config.loadDefaultRepository();

        /* generic content */
        RepositoryRemoveTest.gc1 = new GenericContent(RepositoryRemoveTest.handler.getRepositoryBase().toString(),
                Prelude.dePathize(RepositoryRemoveTest.ABSTRACT_PATH_COMMIT), new String[] {
                        RepositoryRemoveTest.PATH_1, RepositoryRemoveTest.PATH_2, }, RepositoryRemoveTest.UUID_1);

        RepositoryRemoveTest.gc2 = new GenericContent(RepositoryRemoveTest.handler.getRepositoryBase().toString(),
                Prelude.dePathize(RepositoryRemoveTest.ABSTRACT_PATH_COMMIT), new String[] {
                        RepositoryRemoveTest.PATH_1, RepositoryRemoveTest.PATH_2, }, RepositoryRemoveTest.UUID_2);
        RepositoryRemoveTest.gc2.setProperty("third", "some other value");

        /* Request-messages */
        /* valid commit */
        Element root = DocumentHelper.createElement(MESSAGE_TYPE_COMMIT_EDB);
        Element body = root.addElement("body");
        addGCToMessagePart(RepositoryRemoveTest.gc1, body, RepositoryRemoveTest.USER);
        addGCToMessagePart(RepositoryRemoveTest.gc2, body, RepositoryRemoveTest.USER);
        RepositoryRemoveTest.persistMessage = DocumentHelper.createDocument(root);

        /* valid full reset */
        root = DocumentHelper.createElement(MESSAGE_TYPE_FULLRESET);
        final Element resetBody = root.addElement("body");
        resetBody.addElement("repoId").setText("");
        RepositoryRemoveTest.fullResetMessage = DocumentHelper.createDocument(root);

    }

    private static void addGCToMessagePart(final GenericContent gc, final Element elem, final String user) {
        final Element gcElem = elem.addElement("acmMessageObjects");
        gcElem.addElement("user").setText(user);
        gcElem.addElement("uuid").setText(gc.getUUID());
        gcElem.addElement("path").setText(gc.getPath());
        for (final Entry<Object, Object> entry : gc.getEntireContent()) {
            final Element pairs = gcElem.addElement("acmMessageObject");
            pairs.addElement("key").setText(entry.getKey().toString());
            pairs.addElement("value").setText(entry.getValue().toString());
        }
    }

    private InOut createInOutMessage(final String message) throws MessagingException {
        final InOut inOut = new InOutImpl(UUID.randomUUID().toString());

        final NormalizedMessage inMsg = inOut.createMessage();
        inMsg.setContent(new StringSource(message));
        inMsg.setProperty("contextId", "42");
        inOut.setInMessage(inMsg);
        inOut.setService(new QName(RepositoryRemoveTest.TEST_NAMESPACE, RepositoryRemoveTest.EDB_SERVICE_NAME));
        return inOut;
    }

    private Document sendMessageAndParseResponse(final Document doc) throws Exception {
        final InOut inout = createInOutMessage(doc.asXML());
        this.client.sendSync(inout);
        if (inout.getStatus() == ExchangeStatus.ERROR) {
            fail("received error");
        }
        return parseResponse(inout.getOutMessage());
    }
    
    private void storeEDBContext() throws Exception {
        OpenEngSBComponent comp = new OpenEngSBComponent();
        NotificationMockEndpoint dummy = new NotificationMockEndpoint();
        dummy.setServiceUnit(comp.getServiceUnit());
        dummy.setService(new QName("urn:openengsb:test", "TestService"));
        dummy.setEndpoint("dummyEndpoint");
        comp.addEndpoint(dummy);
        jbi.activateComponent(comp, "dummy");
        
        ContextHelperImpl contextHelper = new ContextHelperImpl(dummy, null);
        contextHelper.setContext("42");
        
        HashMap<String, String> newProperties = new HashMap<String, String>();
        newProperties.put("edb/namespace", "urn:openengsb:edb");
        newProperties.put("edb/servicename", "edb");
        
        contextHelper.store(newProperties);
    }

    private Document parseResponse(final NormalizedMessage response) throws IOException, SAXException,
            TransformerException {
        final SourceTransformer st = new SourceTransformer();
        final SAXSource rSource = st.toSAXSource(response.getContent());
        final SAXReader saxreader = new SAXReader(rSource.getXMLReader());
        try {
            return saxreader.read(rSource.getInputSource());
        } catch (final DocumentException e) {
            final StreamSource rawSource = st.toStreamSource(response.getContent());
            final BufferedReader br = new BufferedReader(rawSource.getReader());
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            this.log.error(e.getMessage());
            this.log.error(sb.toString());
            return null;
        }
    }

    /**
     * Creates a new ServiceMixClieant
     * 
     * @return The new ServiceMixClient
     */
    private DefaultServiceMixClient createClient() throws JBIException {
        return new DefaultServiceMixClient(this.jbi);
    }
}
