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
package org.openengsb.config.jbi.component;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openengsb.config.jbi.ParseException;
import org.openengsb.config.jbi.util.MapNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ComponentParser {
    /**
     * Parses a jbi.xml and extracts component type, name and description
     * information. Namespaces are not supported, the default namespace has to
     * be "http://java.sun.com/xml/ns/jbi"
     * 
     * @throws ParseException
     */
    public ComponentDescriptor parseJbi(InputSource in) throws ParseException {
        Document doc = parseDocument(in);
        try {
            MapNamespaceContext ns = new MapNamespaceContext();
            ns.addNamespace("jbi", "http://java.sun.com/xml/ns/jbi");
            XPath xpath = newXPath(ns);
            Object componentNode = xpath.evaluate("/jbi:jbi/jbi:component", doc.getDocumentElement(),
                    XPathConstants.NODE);
            if (!(componentNode instanceof Element)) {
                throw new ParseException("component element not found while parsing jbi.xml");
            }
            Element component = (Element) componentNode;
            String typeString = xpath.evaluate("./@type", component);
            String name = xpath.evaluate("./jbi:identification/jbi:name", component);
            String desc = xpath.evaluate("./jbi:identification/jbi:description", component);
            ComponentDescriptor.Type type;
            if (ComponentDescriptor.Type.BINDING_COMPONENT.getTextual().equals(typeString)) {
                type = ComponentDescriptor.Type.BINDING_COMPONENT;
            } else if (ComponentDescriptor.Type.SERVICE_ENGINE.getTextual().equals(typeString)) {
                type = ComponentDescriptor.Type.SERVICE_ENGINE;
            } else {
                throw new ParseException("unsupported component type: " + typeString);
            }
            return new ComponentDescriptor(type, name, desc);
        } catch (XPathExpressionException e) {
            throw new ParseException(e);
        }
    }

    /**
     * Parses a jbi component schema and extracts target namespace and endpoints
     * information.
     * 
     * @throws ParseException
     */
    public ComponentDescriptor parseSchema(InputSource in) throws ParseException {
        Element root = parseDocument(in).getDocumentElement();

        String targetNamespace = root.getAttribute("targetNamespace");
        if (targetNamespace.equals("")) {
            throw new ParseException("targetNamespace attribute not found");
        }

        MapNamespaceContext ns = new MapNamespaceContext();
        ns.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
        ns.addNamespace("tns", targetNamespace);

        XPath xpath = newXPath(ns);

        return new ComponentDescriptor(targetNamespace, parseEndpoints(root, xpath));
    }

    private ArrayList<EndpointDescriptor> parseEndpoints(Element root, XPath xpath) throws ParseException {
        ArrayList<EndpointDescriptor> endpoints = new ArrayList<EndpointDescriptor>();
        try {
            NodeList elementNodes = (NodeList) xpath.evaluate("/xsd:schema/xsd:element", root, XPathConstants.NODESET);
            for (int i = 0; i < elementNodes.getLength(); ++i) {
                Element elem = (Element) elementNodes.item(i);
                if (elem.getAttribute("name").toLowerCase().contains("component")) {
                    continue;
                }
                endpoints.add(new EndpointDescriptor(elem.getAttribute("name"), parseAttributes(elem, xpath)));
            }
        } catch (XPathExpressionException e) {
            throw new ParseException(e);
        }
        return endpoints;
    }

    private ArrayList<AttributeDescriptor> parseAttributes(Element endpoint, XPath xpath)
            throws XPathExpressionException {
        ArrayList<AttributeDescriptor> attributes = new ArrayList<AttributeDescriptor>();
        NodeList attributeNodes = (NodeList) xpath.evaluate("./xsd:complexType/xsd:attribute", endpoint,
                XPathConstants.NODESET);
        for (int i = 0; i < attributeNodes.getLength(); ++i) {
            Element attribute = (Element) attributeNodes.item(i);
            attributes.add(new AttributeDescriptor(attribute.getAttribute("name"), attribute.getAttribute("type")));
        }
        return attributes;
    }

    private Document parseDocument(InputSource in) throws ParseException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            return dbf.newDocumentBuilder().parse(in);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    private XPath newXPath(MapNamespaceContext ns) {
        XPathFactory fac = XPathFactory.newInstance();
        XPath xpath = fac.newXPath();
        xpath.setNamespaceContext(ns);
        return xpath;
    }
}
