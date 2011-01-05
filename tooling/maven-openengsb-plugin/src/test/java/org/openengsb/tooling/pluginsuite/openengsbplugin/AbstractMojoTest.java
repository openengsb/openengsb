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

package org.openengsb.tooling.pluginsuite.openengsbplugin;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.xpath.XPathConstants;

import org.openengsb.tooling.pluginsuite.openengsbplugin.tools.Tools;
import org.openengsb.tooling.pluginsuite.openengsbplugin.xml.OpenEngSBMavenPluginNSContext;
import org.w3c.dom.Document;

public abstract class AbstractMojoTest {

    private static boolean installed = false;

    protected static String groupId;
    protected static String artifactId;
    protected static String version;

    protected static final OpenEngSBMavenPluginNSContext nsContext = new OpenEngSBMavenPluginNSContext();

    protected static String invocation;

    public static void prepare(String goal) throws Exception {
        File f = new File("pom.xml");
        Document doc = Tools.readXML(new FileInputStream(f));
        groupId =
            Tools.evaluateXPath("/pom:project/pom:groupId/text()", doc, nsContext,
                XPathConstants.STRING, String.class).trim();
        artifactId =
            Tools.evaluateXPath("/pom:project/pom:artifactId/text()", doc, nsContext,
                XPathConstants.STRING, String.class).trim();
        version =
            Tools.evaluateXPath("/pom:project/pom:version/text()", doc, nsContext,
                XPathConstants.STRING, String.class).trim();

        invocation = String.format("%s:%s:%s:%s", groupId, artifactId, version, goal);

        if (!installed) {
            Tools.executeProcess(new String[]{ "mvn", "install", "-Dmaven.test.skip=true" }, ".", false);
            installed = true;
        }
    }

}
