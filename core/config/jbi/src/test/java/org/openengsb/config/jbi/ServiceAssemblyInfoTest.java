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
package org.openengsb.config.jbi;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThan;
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
    @SuppressWarnings("unchecked")
    public void toZip_shouldContainJbiXmlAndSUZips() throws Exception {
        ServiceAssemblyInfo sa = Fixtures.createSAI();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        sa.toZip(out);
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
        ZipEntry e1 = zip.getNextEntry();
        ZipEntry e2 = zip.getNextEntry();
        assertThat(zip.getNextEntry(), nullValue());
        Matcher<String> fileMatchers = anyOf(is("META-INF/jbi.xml"), endsWith(".zip"));
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
        assertThat(x.evaluate("identification", node, XPathConstants.NODE), notNullValue());
        assertThat(x.evaluate("target/artifacts-zip", node).length(), greaterThan(0));
    }
}
