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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.openengsb.config.jbi.ServiceAssemblyInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ServiceAssemblyInfoTest {
    @Test
    public void toZip_shouldContainJbiXmlAndSUZips() throws Exception {
        ServiceAssemblyInfo sa = Fixtures.createSAI();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        sa.toZip(out);
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
        ZipEntry e1 = zip.getNextEntry();
        ZipEntry e2 = zip.getNextEntry();
        assertThat(zip.getNextEntry(), nullValue());
        Matcher<String> fileMatchers = anyOf(is("META-INF/jbi.xml"), is(sa.getServiceUnits().get(0).getIdentifier()
                + ".zip"));
        assertThat(e1.getName(), fileMatchers);
        assertThat(e2.getName(), fileMatchers);
    }

    @Test
    public void createJbiXml_shouldContainServiceUnitEntries() throws Exception {
        XPath x = Fixtures.newXPath();
        ServiceAssemblyInfo sa = Fixtures.createSAI();
        Document doc = sa.createJbiXml();
        Node node = (Node) x.evaluate("/jbi/service-assembly/service-unit", doc.getDocumentElement(),
                XPathConstants.NODE);
        assertThat(node, notNullValue());
        assertThat(x.evaluate("target/artifacts-zip", node), is(sa.getServiceUnits().get(0).getIdentifier() + ".zip"));
    }
}
