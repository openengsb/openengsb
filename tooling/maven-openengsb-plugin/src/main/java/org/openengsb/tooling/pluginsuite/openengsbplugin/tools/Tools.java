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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Scanner;
import java.util.UUID;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public abstract class Tools {

    private static final Logger log = Logger.getLogger(Tools.class);

    public static String capitalizeFirst(String st) {
        if (st == null) {
            return null;
        } else if (st.matches("[\\s]*")) {
            return st;
        } else if (st.length() == 1) {
            return st.toUpperCase();
        } else {
            return st.substring(0, 1).toUpperCase() + st.substring(1, st.length());
        }
    }

    public static void replaceInFile(File f, String pattern, String replacement) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(f));

        String str = "";
        String line = "";

        while ((line = br.readLine()) != null) {
            str += line + "\n";
        }

        br.close();

        str = str.replaceAll(pattern, replacement);

        FileWriter fw = new FileWriter(f);
        fw.write(str);
        fw.close();

    }

    public static void renameSubmoduleInPom(String oldStr, String newStr) throws MojoExecutionException {
        try {
            File pomFile = new File("pom.xml");
            if (pomFile.exists()) {
                Tools.replaceInFile(pomFile, String.format("<module>%s</module>", oldStr),
                    String.format("<module>%s</module>", newStr));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Couldn't modifiy module entry in pom file!");
        }
    }

    public static String readValue(Scanner sc, String name, String defaultvalue) {
        System.out.print(String.format("%s [%s]: ", name, defaultvalue));
        String line = sc.nextLine();
        if (line == null || line.matches("[\\s]*")) {
            return defaultvalue;
        }
        return line;
    }

    public static void renameArtifactFolderAndUpdateParentPom(String oldStr, String newStr)
        throws MojoExecutionException {
        File from = new File(oldStr);
        System.out.println(String.format("\"%s\" exists: %s", oldStr, from.exists()));
        if (from.exists()) {
            System.out.println(String.format("Trying to rename to: \"%s\"", newStr));
            File to = new File(newStr);
            if (!to.exists()) {
                from.renameTo(to);
                System.out.println("renamed successfully");
                Tools.renameSubmoduleInPom(oldStr, newStr);
            } else {
                throw new MojoExecutionException("Couldn't rename: name clash!");
            }
        } else {
            throw new MojoExecutionException("Artifact wasn't created as expected!");
        }
    }

    public static Document readXML(InputStream is) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        // TODO validate against scheme
        return db.parse(is);
    }

    public static <T> T evaluateXPath(String xpathStr, Document doc, NamespaceContext nsContext, QName returnTypeQName,
            Class<T> returnType) throws XPathExpressionException {
        log.debug(String.format("evaluating xpath: %s", xpathStr));
        XPath xpath = XPathFactory.newInstance().newXPath();
        if (nsContext != null) {
            xpath.setNamespaceContext(nsContext);
        }
        return returnType.cast(xpath.evaluate(xpathStr, doc, returnTypeQName));
    }

    public static File generateTmpFile(String content, String suffix) throws IOException {
        File f = File.createTempFile(UUID.randomUUID().toString(), suffix);
        Tools.writeIntoFile(content, f);
        log.debug(String.format("generated file: %s", f.toURI().toString()));
        return f;
    }

    public static void writeIntoFile(String content, File f) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(content);
        bw.flush();
        bw.close();
    }

    public static String getTxtFileContent(InputStream is) throws IOException {
        StringBuilder contents = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            contents.append(line);
            contents.append("\n");
        }
        return contents.toString();
    }

    public static String serializeXML(Document doc) throws IOException {
        StringWriter stringWriter = new StringWriter();
        XMLSerializer xmlSerializer = new XMLSerializer();
        xmlSerializer.setOutputCharStream(stringWriter);
        xmlSerializer.serialize(doc);
        return stringWriter.toString();
    }

    public static void insertDomNode(Document parentDoc, Node nodeToInsert, String xpath, NamespaceContext nsContext)
        throws XPathExpressionException {
        log.trace("insertDomNode() - start");
        String[] tokens = xpath.split("/");
        String currPath = "";
        Node parent = null;
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].matches("[\\s]*")) {
                continue;
            }
            log.trace(String.format("parent = %s", parent == null ? "null" : parent.getLocalName()));
            currPath += "/" + tokens[i];
            Node result = Tools.evaluateXPath(currPath, parentDoc, nsContext, XPathConstants.NODE, Node.class);
            log.trace(String.format("result empty: %s", result == null));
            if (result == null) {
                String elemName = null;
                // String elemText = null;
                // HashMap<String, String> elemAttrs = new HashMap<String, String>();
                // attribute filter
                elemName = tokens[i].replaceAll("\\[.*\\]", "");
                // namespace prefix
                elemName = elemName.replaceAll(".*:", "");
                log.trace(String.format("elementName: %s", elemName));
                // TODO respect text and attributes
                Element element = parentDoc.createElement(elemName);
                parent.appendChild(element);
                result = element;
            }
            parent = result;
        }
        log.trace("finally inserting the node..");
        log.trace(String.format("parent node: %s", parent == null ? "null" : parent.getLocalName()));
        log.trace(String.format("node to insert = %s", nodeToInsert == null ? "null" : nodeToInsert.getLocalName()));
        parent.appendChild(nodeToInsert);
        log.trace("insertDomNode() - end");
    }

}
