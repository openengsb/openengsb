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
import java.util.ArrayList;
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

import org.openengsb.config.jbi.types.AbstractType;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.RefType;
import org.openengsb.config.jbi.types.ServiceEndpointTargetType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.collect.Lists;

/**
 * Service unit information needed to write a zip file.
 */
public class ServiceUnitInfo {
    private final ComponentType component;
    private final ArrayList<EndpointInfo> endpoints;

    public ServiceUnitInfo(ComponentType component) {
        this(component, Lists.<EndpointInfo> newArrayList());
    }

    public ServiceUnitInfo(ComponentType component, ArrayList<EndpointInfo> endpoints) {
        this.component = component;
        this.endpoints = endpoints;
    }

    public ComponentType getComponent() {
        return component;
    }

    public ArrayList<EndpointInfo> getEndpoints() {
        return endpoints;
    }

    public void addEndpoint(EndpointInfo e) {
        endpoints.add(e);
    }

    // TODO identifier creation shouldn't happen here
    // this code assumes that there is exactly one bound endpoint
    public String getIdentifier() {
        EndpointInfo e = endpoints.get(0);
        return component.getName() + "-" + e.getEndpointType().getName() + "-" + e.getMap().get("service") + "-"
                + e.getMap().get("endpoint");
    }

    /**
     * Creates the XBean xml document, containing all endpoints.
     */
    public Document createXBeanXml(ServiceAssemblyInfo saInfo) throws ParserConfigurationException {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        DocumentBuilder docBuilder = fac.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element beans = doc.createElement("beans");
        beans.setAttribute("xmlns:" + component.getNsname(), component.getNamespace());
        doc.appendChild(beans);

        List<String> beanRefs = Lists.newArrayList();

        for (EndpointInfo e : endpoints) {
            Element node = doc.createElementNS(component.getNamespace(), e.getEndpointType().getName());
            beans.appendChild(node);
            for (AbstractType t : e.getEndpointType().getAttributes()) {
                t.toAttributeOnElement(e.getMap(), node);
                if (t.getClass().equals(RefType.class)) {
                    beanRefs.add(e.getMap().get(t.getName()));
                }
            }
        }

        for (String ref : beanRefs) {
            BeanInfo bean = saInfo.getBean(ref);
            Element node = doc.createElement("bean");
            beans.appendChild(node);
            node.setAttribute("id", bean.getMap().get("id"));
            node.setAttribute("class", bean.getBeanType().getClazz());
            for (AbstractType t : bean.getBeanType().getProperties()) {
                if (t.getName().equals("id")) {
                    continue;
                }
                t.toPropertyOnElement(bean.getMap(), node);
            }
        }

        return doc;
    }

    /**
     * Creates the jbi xml document.
     */
    public Document createJbiXml() throws ParserConfigurationException {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        DocumentBuilder docBuilder = fac.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element jbi = doc.createElement("jbi");
        doc.appendChild(jbi);

        jbi.setAttribute("xmlns", "http://java.sun.com/xml/ns/jbi");
        jbi.setAttribute("version", "1.0");

        Element services = doc.createElement("services");
        jbi.appendChild(services);

        for (EndpointInfo e : endpoints) {
            Element node = doc.createElement("provides");
            services.appendChild(node);
            node.setAttribute("service-name", e.getMap().get("service"));
            node.setAttribute("endpoint-name", e.getMap().get("endpoint"));
            for (AbstractType t : e.getEndpointType().getAttributes()) {
                if (!t.getClass().equals(ServiceEndpointTargetType.class)) {
                    continue;
                }
                ServiceEndpointTargetType tt = (ServiceEndpointTargetType) t;
                Element consumes = doc.createElement("consumes");
                services.appendChild(consumes);
                String target = e.getMap().get(tt.getName());
                consumes.setAttribute("service-name", target.substring(0, target.indexOf('.')));
                consumes.setAttribute("endpoint-name", target.substring(target.indexOf('.') + 1));
            }
        }

        services.setAttribute("binding-component", Boolean.toString(component.isBindingComponent()));

        return doc;
    }

    /**
     * Creates the service unit zip, containing the jbi.xml and xbean.xml file.
     */
    public void toZip(ServiceAssemblyInfo saInfo, OutputStream os) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(os);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        try {
            outputXml(createXBeanXml(saInfo), bout);
        } catch (Exception e) {
            throw new IOException(e);
        }
        zip.putNextEntry(new ZipEntry("xbean.xml"));
        zip.write(bout.toByteArray());
        zip.closeEntry();

        bout = new ByteArrayOutputStream();
        try {
            outputXml(createJbiXml(), bout);
        } catch (Exception e) {
            throw new IOException(e);
        }
        zip.putNextEntry(new ZipEntry("META-INF/jbi.xml"));
        zip.write(bout.toByteArray());
        zip.closeEntry();
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
