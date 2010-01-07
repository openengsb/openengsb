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
package org.openengsb.embedded;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.servicemix.jbi.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JbiTypeChecker {
    public static enum JbiType {
        SERVICE_ASSEMBLY,
        SHARED_LIBRARY,
        COMPONENT
    }
    
    public static JbiType checkJbiInstallerType(File installerFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            ZipFile zip = new ZipFile(installerFile);
            ZipEntry jbiEntry = zip.getEntry("META-INF/jbi.xml");
            Document doc = docBuilder.parse(zip.getInputStream(jbiEntry));
            Element jbi = doc.getDocumentElement();
            Element child = DOMUtil.getFirstChildElement(jbi);
            if ("component".equals(child.getLocalName())) {
                return JbiType.COMPONENT;
            } else if ("shared-library".equals(child.getLocalName())) {
                return JbiType.SHARED_LIBRARY;
            } else if ("service-assembly".equals(child.getLocalName())) {
                return JbiType.SERVICE_ASSEMBLY;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
