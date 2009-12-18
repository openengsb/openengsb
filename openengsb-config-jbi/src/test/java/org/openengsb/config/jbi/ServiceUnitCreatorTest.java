package org.openengsb.config.jbi;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.openengsb.config.jbi.types.ComponentType;
import org.w3c.dom.Document;

public class ServiceUnitCreatorTest {
    @Test
    public void writeJbiXmlFile_shouldWriteTheComponentType() throws Exception {
        MapNamespaceContext ns = new MapNamespaceContext();
        ns.addNamespace("jbi", "http://java.sun.com/xml/ns/jbi");
        XPath xpath = newXPath(ns);

        ComponentType component = new ComponentType();
        component.setBindingComponent(true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ServiceUnitCreator.writeJbiXmlFile(out, component);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Document doc = parseDocument(in);
        Object attr = xpath.evaluate("/jbi:jbi/jbi:services/@binding-component", doc.getDocumentElement(), XPathConstants.STRING);
        assertEquals("true", attr);
    }

    private Document parseDocument(InputStream in) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            return dbf.newDocumentBuilder().parse(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private XPath newXPath(MapNamespaceContext ns) {
        XPathFactory fac = XPathFactory.newInstance();
        XPath xpath = fac.newXPath();
        xpath.setNamespaceContext(ns);
        return xpath;
    }
}
