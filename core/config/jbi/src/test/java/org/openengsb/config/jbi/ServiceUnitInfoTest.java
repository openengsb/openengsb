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
import org.openengsb.config.jbi.ServiceAssemblyInfo;
import org.openengsb.config.jbi.ServiceUnitInfo;
import org.w3c.dom.Document;

import com.google.common.collect.Lists;

public class ServiceUnitInfoTest {
    private ServiceUnitInfo sui;
    private Document doc;
    private XPath x;

    @Before
    public void setup() throws ParserConfigurationException {
        ServiceAssemblyInfo sai = Fixtures.createSAI();
        sui = new ServiceUnitInfo(sai.getEndpoints().get(0).getEndpointType().getParent(), Lists.newArrayList(sai
                .getEndpoints()));
        doc = sui.createXBeanXml(sai);
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
    public void toXBeanXml_shouldAddBeans() throws Exception {
        String s = x.evaluate("/beans/bean/@id", doc.getDocumentElement());
        assertThat(s, is("thebean"));
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
        sui.toZip(Fixtures.createSAI(), out);
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
        ZipEntry e1 = zip.getNextEntry();
        ZipEntry e2 = zip.getNextEntry();
        assertThat(zip.getNextEntry(), nullValue());
        assertThat(e1.getName(), anyOf(is("xbean.xml"), is("META-INF/jbi.xml")));
        assertThat(e2.getName(), anyOf(is("xbean.xml"), is("META-INF/jbi.xml")));
    }
}
