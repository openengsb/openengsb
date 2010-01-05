package org.openengsb.config.jbi.test.unit;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.config.jbi.ServiceUnitInfo;
import org.w3c.dom.Document;

public class ServiceUnitInfoTest {
    private ServiceUnitInfo sui;
    private Document doc;
    private XPath x;

    @Before
    public void setup() throws ParserConfigurationException {
        sui = Fixtures.createSUI();
        doc = sui.createXBeanXml();
        x = Fixtures.newXPath();
    }

    @Test
    public void toXBeanXml_shouldAddNamespaceAtEndpointNode() throws Exception {
        assertThat(x.evaluate("/beans/ns:a", doc.getDocumentElement()), notNullValue());
    }

	@Test
    public void toXBeanXml_shouldAddEndpointAttributes() throws Exception {
        String s = x.evaluate("/beans/ns:a/@service", doc.getDocumentElement());
        assertThat(s, is("servicename"));
        s = x.evaluate("/beans/ns:a/@endpoint", doc.getDocumentElement());
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
