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
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URL;

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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.itests.util.AbstractRemoteTestHelper;
import org.openengsb.labs.paxexam.karaf.options.configs.FeaturesCfg;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.w3c.dom.Document;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class WSPortIT extends AbstractRemoteTestHelper {

    @Configuration
    public Option[] additionalConfiguration() throws Exception {
        return combine(baseConfiguration(), editConfigurationFileExtend(FeaturesCfg.BOOT, ",openengsb-ports-ws"));
    }

    @Test
    public void jmsPort_shouldBeExportedWithCorrectId() throws Exception {
        OutgoingPort serviceWithId =
            OpenEngSBCoreServices.getServiceUtilsService().getServiceWithId(OutgoingPort.class, "ws-json", 60000);
        assertNotNull(serviceWithId);
    }

    @Test
    public void startSimpleWorkflow_ShouldReturn42() throws Exception {
        Dispatch<DOMSource> dispatcher = createMessageDispatcher();
        DOMSource request = convertMessageToDomSource();

        DOMSource response = dispatchMessage(dispatcher, request);
        String message = transformResponseToMessage(response);

        assertThat(message, containsString("The answer to life the universe and everything"));
    }

    @Test
    public void recordAuditInCoreService_ShouldReturnVoid() throws Exception {
        Dispatch<DOMSource> dispatcher = createMessageDispatcher();
        DOMSource request = convertAuditingRequestToDomSource();

        DOMSource response = dispatchMessage(dispatcher, request);
        String message = transformResponseToMessage(response);

        assertThat(message, containsString("\"type\":\"Void\""));
        assertThat(message, not(containsString("Exception")));
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
