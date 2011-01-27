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
import java.util.Arrays;

import javax.xml.xpath.XPathConstants;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.openengsb.tooling.pluginsuite.openengsbplugin.tools.Tools;
import org.openengsb.tooling.pluginsuite.openengsbplugin.xml.OpenEngSBMavenPluginNSContext;
import org.w3c.dom.Document;

public abstract class MojoPreparation {

    private static boolean prepared = false;

    private static final Logger LOG = Logger.getLogger(MojoPreparation.class);

    private static File userDir = new File(System.getProperty("user.dir"));

    protected static String mvnCommand = "mvn";

    private static String groupId;
    private static String artifactId;
    private static String version;

    protected static OpenEngSBMavenPluginNSContext nsContext = new OpenEngSBMavenPluginNSContext();

    protected String invocation;

    @BeforeClass
    public static void preparePlugin() throws Exception {
        if (!prepared) {
            LOG.debug("preparing openengsb-maven-plugin..");
            
            File f = new File("pom.xml");
            Document doc = Tools.parseXMLFromString(FileUtils.readFileToString(f));
            groupId = Tools.evaluateXPath("/pom:project/pom:groupId/text()", doc, nsContext, XPathConstants.STRING,
                    String.class).trim();
            artifactId = Tools.evaluateXPath("/pom:project/pom:artifactId/text()", doc, nsContext,
                    XPathConstants.STRING, String.class).trim();
            version = Tools.evaluateXPath("/pom:project/pom:version/text()", doc, nsContext, XPathConstants.STRING,
                    String.class).trim();

            if (System.getProperty("os.name").startsWith("Windows")) {
                mvnCommand = "mvn.bat";
            }

            Tools.executeProcess(Arrays.asList(new String[] { mvnCommand, "install", "-Dmaven.test.skip=true" }),
                    userDir);
            prepared = true;
        }
    }

    protected void prepareGoal(String goal) {
        invocation = String.format("%s:%s:%s:%s", groupId, artifactId, version, goal);
    }

}
