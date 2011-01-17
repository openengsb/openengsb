/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.tooling.pluginsuite.openengsbplugin.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.xml.xpath.XPathConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openengsb.tooling.pluginsuite.openengsbplugin.xml.OpenEngSBMavenPluginNSContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ToolsTest {

    @Test
    public void testExpectedInput() {
        assertEquals("Hello world", Tools.capitalizeFirst("hello world"));
        assertEquals("H", Tools.capitalizeFirst("h"));
    }

    @Test
    public void testUnexpectedInput() {
        assertNull(Tools.capitalizeFirst(null));
        assertEquals("", Tools.capitalizeFirst(""));
        assertEquals(" ", Tools.capitalizeFirst(" "));
        assertEquals("  ", Tools.capitalizeFirst("  "));
        assertEquals("?", Tools.capitalizeFirst("?"));
        assertEquals("/&%(#", Tools.capitalizeFirst("/&%(#"));
    }

    @Test
    public void testXPath() throws Exception {
        Document doc =
            Tools
                .parseXMLFromString(IOUtils.toString(ClassLoader
                    .getSystemResourceAsStream("licenseCheck/licenseCheckConfig.xml")));
        Node n =
            Tools.evaluateXPath("//lc:licenseCheckMojo", doc, new OpenEngSBMavenPluginNSContext(), XPathConstants.NODE,
                Node.class);
        assertEquals("licenseCheckMojo", n.getLocalName());
    }

    @Test
    public void testTmpFile() throws IOException {
        String fileContent = "BLA\n";
        File generatedFile = Tools.generateTmpFile(fileContent, ".txt");
        File f = new File(generatedFile.getAbsolutePath());
        String readContent = FileUtils.readFileToString(f);
        assertEquals(fileContent, readContent);
        assertTrue(generatedFile.delete());
    }

    @Test
    public void testInsertDomNode() throws Exception {
        Document thePom =
            Tools.parseXMLFromString(IOUtils.toString(ClassLoader
                .getSystemResourceAsStream("licenseCheck/pass/pom.xml")));
        Document config =
            Tools
                .parseXMLFromString(IOUtils.toString(ClassLoader
                    .getSystemResourceAsStream("licenseCheck/licenseCheckConfig.xml")));

        Node licenseCheckMojoProfileNode =
            Tools.evaluateXPath("/lc:licenseCheckMojo/lc:profile", config, new OpenEngSBMavenPluginNSContext(),
                XPathConstants.NODE,
                Node.class);

        String profileName = UUID.randomUUID().toString();

        Node idNode = config.createElement("id");
        idNode.setTextContent(profileName);
        licenseCheckMojoProfileNode.insertBefore(idNode, licenseCheckMojoProfileNode.getFirstChild());
        Node importedNode = thePom.importNode(licenseCheckMojoProfileNode, true);

        Tools.insertDomNode(thePom, importedNode, "/pom:project/pom:profiles",
            new OpenEngSBMavenPluginNSContext());

        String serializedXml = Tools.serializeXML(thePom);
        File generatedFile = Tools.generateTmpFile(serializedXml, ".xml");

        Document generatedPom = Tools.parseXMLFromString(FileUtils.readFileToString(generatedFile));

        Node foundNode =
            Tools.evaluateXPath(
                String.format("/pom:project/pom:profiles/pom:profile/pom:id[text()='%s']", profileName),
                generatedPom, new OpenEngSBMavenPluginNSContext(),
                XPathConstants.NODE,
                Node.class);

        assertNotNull(foundNode);
        assertTrue(generatedFile.delete());
    }

}
