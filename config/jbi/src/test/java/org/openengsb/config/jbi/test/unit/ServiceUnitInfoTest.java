package org.openengsb.config.jbi.test.unit;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.config.jbi.EndpointInfo;
import org.openengsb.config.jbi.ServiceUnitInfo;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointNameType;
import org.openengsb.config.jbi.types.EndpointType;
import org.openengsb.config.jbi.types.ServiceType;
import org.w3c.dom.Document;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ServiceUnitInfoTest {
    private final String NS = "ns";
    private ServiceUnitInfo sui;
    private Document doc;
    private XPath x;

    private ServiceUnitInfo createTestSUI() {
        ComponentType c = new ComponentType("a", "b", "http://a.b.c", true);
        EndpointType e = new EndpointType("a");
        e.addAttribute(new ServiceType("theservice", false, 0, ""));
        e.addAttribute(new EndpointNameType("theendpoint", false, 0, ""));
        HashMap<String, String> values = Maps.<String, String> newHashMap();
        values.put("theservice", "servicename");
        values.put("theendpoint", "endpointname");
        EndpointInfo ei = new EndpointInfo(e, values);
        return new ServiceUnitInfo(c, Lists.newArrayList(ei));
    }

    private XPath newXPath() {
        MapNamespaceContext ctx = new MapNamespaceContext();
        ctx.addNamespace(NS, "http://a.b.c");
        return newXPath(ctx);
    }

    private XPath newXPath(NamespaceContext ns) {
        XPathFactory fac = XPathFactory.newInstance();
        XPath xpath = fac.newXPath();
        xpath.setNamespaceContext(ns);
        return xpath;
    }

    @Before
    public void setup() throws ParserConfigurationException {
        sui = createTestSUI();
        doc = sui.createXBeanXml();
        x = newXPath();
    }

    @Test
    public void toXBeanXml_shouldAddNamespaceAtEndpointNode() throws Exception {
        assertThat(x.evaluate("/beans/ns:a", doc.getDocumentElement()), notNullValue());
    }

	@Test
    public void toXBeanXml_shouldAddEndpointAttributes() throws Exception {
        String s = x.evaluate("/beans/ns:a/@theservice", doc.getDocumentElement());
        assertThat(s, is("servicename"));
        s = x.evaluate("/beans/ns:a/@theendpoint", doc.getDocumentElement());
        assertThat(s, is("endpointname"));
	}

    @Test
    public void toJbiXml_shouldAddBindingComponentAttribute() throws Exception {
        Document doc = sui.createJbiXml();
        String s = x.evaluate("/jbi/services/@binding-component", doc.getDocumentElement());
        assertThat(s, is("true"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void toZip_holdsXBeanAndJbiFile() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        sui.toZip(out);
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
        ZipEntry e1 = zip.getNextEntry();
        ZipEntry e2 = zip.getNextEntry();
        assertThat(zip.getNextEntry(), nullValue());
        assertThat(e1.getName(), anyOf(is("xbean.xml"), is("META-INF/jbi.xml")));
        assertThat(e2.getName(), anyOf(is("xbean.xml"), is("META-INF/jbi.xml")));
    }
}
