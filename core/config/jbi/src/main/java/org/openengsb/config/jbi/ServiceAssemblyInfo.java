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
package org.openengsb.config.jbi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.collect.Lists;

public class ServiceAssemblyInfo {
    private final String name;
    private final List<EndpointInfo> endpoints;
    private final List<BeanInfo> beans;

    public ServiceAssemblyInfo(String name) {
        this(name, Lists.<EndpointInfo> newArrayList(), Lists.<BeanInfo> newArrayList());
    }

    public ServiceAssemblyInfo(String name, List<EndpointInfo> endpoints, List<BeanInfo> beans) {
        this.name = name;
        this.endpoints = endpoints;
        this.beans = beans;
    }

    public String getName() {
        return name;
    }

    public List<EndpointInfo> getEndpoints() {
        return endpoints;
    }

    public void addEndpoint(EndpointInfo ei) {
        endpoints.add(ei);
    }

    public List<BeanInfo> getBeans() {
        return beans;
    }

    public BeanInfo getBean(String name) {
        for (BeanInfo b : beans) {
            if (b.getMap().get("id").equals(name)) {
                return b;
            }
        }
        return null;
    }

    public void addBean(BeanInfo bean) {
        beans.add(bean);
    }

    public Document createJbiXml() throws ParserConfigurationException {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        DocumentBuilder docBuilder = fac.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element jbi = doc.createElement("jbi");
        doc.appendChild(jbi);

        jbi.setAttribute("xmlns", "http://java.sun.com/xml/ns/jbi");
        jbi.setAttribute("version", "1.0");

        Element sa = doc.createElement("service-assembly");
        jbi.appendChild(sa);
        sa.appendChild(createIdentification(doc, getName(), ""));

        for (EndpointInfo su : getEndpoints()) {
            sa.appendChild(createServiceUnit(doc, su));
        }

        return doc;
    }

    private Node createIdentification(Document doc, String name, String desc) {
        Element id = doc.createElement("identification");
        id.appendChild(doc.createElement("name")).appendChild(doc.createTextNode(name));
        id.appendChild(doc.createElement("description")).appendChild(doc.createTextNode(desc));
        return id;
    }

    private Node createServiceUnit(Document doc, EndpointInfo info) {
        Node su = doc.createElement("service-unit");
        String suId = createIdentifier(info);
        su.appendChild(createIdentification(doc, suId, ""));
        Node target = doc.createElement("target");
        su.appendChild(target);
        target.appendChild(doc.createElement("artifacts-zip")).appendChild(doc.createTextNode(suId + ".zip"));
        target.appendChild(doc.createElement("component-name")).appendChild(
                doc.createTextNode(info.getEndpointType().getParent().getName()));
        return su;
    }

    private String createIdentifier(EndpointInfo e) {
        return e.getEndpointType().getParent().getName() + "-" + e.getEndpointType().getName() + "-"
                + e.getMap().get("service") + "-" + e.getMap().get("endpoint");
    }

    public void toZip(OutputStream out) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(out);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            outputXml(createJbiXml(), bout);
        } catch (Exception e) {
            throw new IOException(e);
        }
        zip.putNextEntry(new ZipEntry("META-INF/jbi.xml"));
        zip.write(bout.toByteArray());
        zip.closeEntry();

        for (EndpointInfo ei : getEndpoints()) {
            bout = new ByteArrayOutputStream();
            ServiceUnitInfo su = new ServiceUnitInfo(ei.getEndpointType().getParent(), Lists.newArrayList(ei));
            su.toZip(this, bout);
            zip.putNextEntry(new ZipEntry(createIdentifier(ei) + ".zip"));
            zip.write(bout.toByteArray());
            zip.closeEntry();
        }

        zip.close();
    }

    private void outputXml(Node xml, OutputStream out) throws TransformerFactoryConfigurationError,
            TransformerException {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.transform(new DOMSource(xml), new StreamResult(out));
    }
}
