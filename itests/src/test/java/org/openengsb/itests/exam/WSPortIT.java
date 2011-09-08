/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.itests.exam;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.apache.karaf.features.FeaturesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.itests.util.AbstractRemoteTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.w3c.dom.Document;

@RunWith(JUnit4TestRunner.class)
public class WSPortIT extends AbstractRemoteTestHelper {

    @Inject
    private FeaturesService featuresService;

    @Test
    public void jmsPort_shouldBeExportedWithCorrectId() throws Exception {
        installPortsWsFeature();

        OutgoingPort serviceWithId =
            OpenEngSBCoreServices.getServiceUtilsService().getServiceWithId(OutgoingPort.class, "ws-json", 60000);
        assertNotNull(serviceWithId);
    }

    @Test
    public void startSimpleWorkflow_ShouldReturn42() throws Exception {
        installPortsWsFeature();

        Dispatch<DOMSource> dispatcher = createMessageDispatcher();
        DOMSource request = convertMessageToDomSource();

        DOMSource response = dispatchMessage(dispatcher, request);
        String message = transformResponseToMessage(response);

        assertThat(message, containsString("The answer to life the universe and everything"));
    }

    @Test
    public void recordAuditInCoreService_ShouldReturnVoid() throws Exception {
        installPortsWsFeature();

        Dispatch<DOMSource> dispatcher = createMessageDispatcher();
        DOMSource request = convertAuditingRequestToDomSource();

        DOMSource response = dispatchMessage(dispatcher, request);
        String message = transformResponseToMessage(response);

        assertThat(message, containsString("\"type\":\"Void\""));
        assertThat(message, not(containsString("Exception")));
    }

    private void installPortsWsFeature() throws Exception {
        if (!featuresService.isInstalled(featuresService.getFeature("openengsb-ports-ws"))) {
            featuresService.installFeature("openengsb-ports-ws");
            URL url;
            InputStream is = null;
            DataInputStream dis;
            String line;
            long counter = 0;
            while (counter < 11) {
                counter++;
                Thread.sleep(1000);
                try {
                    url = new URL("http://localhost:" + WEBUI_PORT + "/ws/receiver/?wsdl");
                    is = url.openStream(); // throws an IOException
                    dis = new DataInputStream(new BufferedInputStream(is));
                    while ((line = dis.readLine()) != null) {
                        return;
                    }
                } catch (Exception mue) {
                    // nevermind...
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException ioe) {
                        // nothing to see here
                    }
                }
            }
            throw new IllegalStateException("Webservices couldnt be installed correctly.");
        }
    }

    private Dispatch<DOMSource> createMessageDispatcher() throws Exception {
        addWorkflow("simpleFlow");
        QName serviceName = new QName("http://ws.ports.openengsb.org/", "PortReceiverService");
        Service service = Service.create(new URL("http://localhost:" + WEBUI_PORT + "/ws/receiver/?wsdl"), serviceName);
        QName portName = new QName("http://ws.ports.openengsb.org/", "PortReceiverPort");
        Dispatch<DOMSource> disp = service.createDispatch(portName, DOMSource.class, Service.Mode.MESSAGE);
        return disp;
    }

    private DOMSource convertAuditingRequestToDomSource() throws Exception {
        String message =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                    "xmlns:ws=\"http://ws.ports.openengsb.org/\" >" +
                    "<soapenv:Header/>" +
                    "<soapenv:Body>" +
                    "<ws:receive>" +
                    "<arg0>" + getAuditingRequest("1235") + "</arg0>" +
                    "</ws:receive>" +
                    "</soapenv:Body>" +
                    "</soapenv:Envelope>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder db = factory.newDocumentBuilder();
        Document requestDoc = db.parse(new ByteArrayInputStream(message.getBytes()));
        DOMSource request = new DOMSource(requestDoc);
        return request;
    }

    private DOMSource convertMessageToDomSource() throws Exception {
        String message =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                    "xmlns:ws=\"http://ws.ports.openengsb.org/\" >" +
                    "<soapenv:Header/>" +
                    "<soapenv:Body>" +
                    "<ws:receive>" +
                    "<arg0>" + getRequest("1235") + "</arg0>" +
                    "</ws:receive>" +
                    "</soapenv:Body>" +
                    "</soapenv:Envelope>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder db = factory.newDocumentBuilder();
        Document requestDoc = db.parse(new ByteArrayInputStream(message.getBytes()));
        DOMSource request = new DOMSource(requestDoc);
        return request;
    }

    private DOMSource dispatchMessage(Dispatch<DOMSource> dispatcher, DOMSource request) {
        DOMSource response = dispatcher.invoke(request);
        return response;
    }

    private String transformResponseToMessage(DOMSource response) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(response, result);
        String xml = result.getWriter().toString();
        return xml;
    }

}
