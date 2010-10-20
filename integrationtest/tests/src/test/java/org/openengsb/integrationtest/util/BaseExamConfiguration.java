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

package org.openengsb.integrationtest.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.def.PaxRunnerOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class BaseExamConfiguration {

    private BaseExamConfiguration() {
        // should not be instanciable, but should be allowed to contain private
        // methods
    }

    public static List<String> getImportantBundleSymbolicNames() {
        List<String> importantBundles = new ArrayList<String>();
        importantBundles.add("org.openengsb.core.persistence");
        importantBundles.add("org.openengsb.core.workflow");
        return importantBundles;
    }

    public static void addEntireOpenEngSBPlatform(List<Option> baseConfiguration) {
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_WRAPPED_WICKET));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_WRAPPED_JAXB));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_WRAPPED_GUAVA));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_WRAPPED_XMLRPC));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_WRAPPED_JSCH));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_WRAPPED_NEODATIS));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_CONFIG_JETTY));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_CONFIG_WEBEXTENDER));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_CORE_COMMON));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_CORE_WORKFLOW));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_CORE_EVENTS));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_CORE_PERSISTENCE));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_DOMAINS_EXAMPLE_IMPLEMENTATION));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_DOMAINS_EXAMPLE_CONNECTOR));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_DOMAINS_NOTIFICATION_IMPLEMENTATION));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_DOMAINS_NOTIFICATION_EMAIL));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_DOMAINS_REPORT_IMPLEMENTATION));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_DOMAINS_REPORT_PLAINTEXT));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_DOMAINS_ISSUE_IMPLEMENTATION));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_DOMAINS_ISSUE_TRAC));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_DOMAINS_SCM_IMPLEMENTATION));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_DOMAINS_SCM_GIT));
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_UI_WEB));
    }

    public static void addHtmlUnitTestDriver(List<Option> baseConfiguration) {
        baseConfiguration.add(CoreOptions.provision(OpenEngSBBundles.OPENENGSB_INTEGRATIONTEST_WRAPPED_HTMLUNIT));
    }

    public static List<Option> getBaseExamOptions(String pathToRoot) {
        Map<String, String> properties = extractAllPropertiesFromPom(pathToRoot + "poms/pom.xml");
        String pomfile = readFileAsString(pathToRoot + "provision/pom.xml");
        for (Entry<String, String> entry : properties.entrySet()) {
            pomfile = pomfile.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue());
        }
        if (!new File(pathToRoot + "target/").exists()) {
            new File(pathToRoot + "target").mkdirs();
        }
        writeFileFromString(pathToRoot + "target/finalPom.xml", pomfile);
        return new ArrayList<Option>(Arrays.asList(new Option[]{
                PaxRunnerOptions.rawPaxRunnerOption("--platform", "felix"),
                PaxRunnerOptions.rawPaxRunnerOption("--console", "false"),
                PaxRunnerOptions.rawPaxRunnerOption("--ee", "J2SE-1.6"),
                PaxRunnerOptions.rawPaxRunnerOption("--definitionURL", "file:" + pathToRoot
                        + "assembly/target/classes/felix.xml"),
                PaxRunnerOptions.scanComposite("file:" + pathToRoot + "assembly/target/classes/karaf.composite"),
                PaxRunnerOptions.scanComposite("file:" + pathToRoot
                        + "assembly/target/classes/settings.debug.composite"),
                PaxRunnerOptions.scanPom("file:" + pathToRoot + "target/finalPom.xml"),
                CoreOptions.frameworks(CoreOptions.felix()) }));
    }

    private static void writeFileFromString(String filepath, String input) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filepath));
            out.write(input);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException("booom", e);
        }
    }

    public static Option[] convertOptionListToArray(List<Option> baseConfiguration) {
        Option[] options = new Option[baseConfiguration.size()];
        for (int i = 0; i < baseConfiguration.size(); i++) {
            options[i] = baseConfiguration.get(i);
        }
        return options;
    }

    private static String readFileAsString(String filePath) {
        try {
            StringBuffer fileData = new StringBuffer(1000);
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
            return fileData.toString();
        } catch (Exception e) {
            throw new RuntimeException("bummmmm", e);
        }
    }

    private static Map<String, String> extractAllPropertiesFromPom(String fileUrl) {
        try {
            Map<String, String> versions = new HashMap<String, String>();
            File file = new File(fileUrl);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getDocumentElement().getChildNodes();
            for (int s = 0; s < nodeLst.getLength(); s++) {
                Node fstNode = nodeLst.item(s);
                if (fstNode.getNodeType() == Node.ELEMENT_NODE
                        && ((Element) fstNode).getNodeName().equals("properties")) {
                    NodeList results = ((Element) fstNode).getChildNodes();
                    for (int i = 0; i < results.getLength(); i++) {
                        Node resultNode = results.item(i);
                        if (resultNode.getNodeType() == Node.ELEMENT_NODE) {
                            versions.put(resultNode.getNodeName(), resultNode.getTextContent());
                        }
                    }
                }
            }
            return versions;
        } catch (Exception e) {
            throw new RuntimeException("Internal Error", e);
        }
    }

    public static void debug(List<Option> baseConfiguration) {
        baseConfiguration.add(PaxRunnerOptions
                .vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"));
    }

}
