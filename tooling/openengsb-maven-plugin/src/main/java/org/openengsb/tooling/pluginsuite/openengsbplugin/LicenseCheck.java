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
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.xml.xpath.XPathConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.openengsb.tooling.pluginsuite.openengsbplugin.tools.Tools;
import org.openengsb.tooling.pluginsuite.openengsbplugin.xml.OpenEngSBMavenPluginNSContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Validates license headers.
 * 
 * @goal licenseCheck
 * 
 * @inheritedByDefault false
 * 
 * @requiresProject true
 * 
 * @aggregator true
 * 
 */
public class LicenseCheck extends AbstractOpenengsbMojo {

    private static final Logger log = Logger.getLogger(LicenseCheck.class);

    private List<String> goals;
    private List<String> activatedProfiles;
    private Properties userProperties;

    private File licenseHeaderFile;
    private File tmpPom;

    private static final OpenEngSBMavenPluginNSContext nsContext = new OpenEngSBMavenPluginNSContext();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            licenseHeaderFile = readHeaderStringAndwriteHeaderIntoTmpFile();
            String profileName = UUID.randomUUID().toString();
            tmpPom = configureTmpPom(profileName);
            initializeMavenExecutionProperties(profileName);
            executeMavenWithCustomPom(tmpPom);
        } finally {
            cleanUp();
        }
    }

    private void initializeMavenExecutionProperties(String profileName) {
        goals = Arrays.asList(new String[] { "validate" });
        activatedProfiles = Arrays.asList(new String[] { profileName });
        userProperties = new Properties();
        userProperties.put("license.header", licenseHeaderFile.toURI().toString());
        userProperties.put("license.failIfMissing", "true");
        userProperties.put("license.aggregate", "true");
        userProperties.put("license.strictCheck", "true");
    }

    private void executeMavenWithCustomPom(File pom) throws MojoExecutionException {
        getNewMavenExecutor().setRecursive(true).setCustomPomFile(pom)
                .execute(this, goals, activatedProfiles, null, userProperties, getProject(), getSession(), getMaven());
    }

    private File readHeaderStringAndwriteHeaderIntoTmpFile() throws MojoExecutionException {
        try {

            String headerString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(
                    "licenseCheck/header.txt"));
            File generatedFile = Tools.generateTmpFile(headerString, ".txt");
            return generatedFile;
        } catch (Exception e) {
            throw new MojoExecutionException("Couldn't create license header temp file!", e);
        }
    }

    private File configureTmpPom(String profileName) throws MojoExecutionException {
        try {
            Document originalPomDocument = Tools.parseXMLFromString(FileUtils.readFileToString(getSession()
                    .getRequest().getPom()));
            // read plugin default configuration
            Document configDocument = Tools.parseXMLFromString(IOUtils.toString(getClass().getClassLoader()
                    .getResourceAsStream("licenseCheck/licenseCheckConfig.xml")));

            // .. and insert the profile node into the pom dom tree ..
            Node licenseCheckMojoProfileNode = Tools.evaluateXPath("/lc:licenseCheckMojo/lc:profile", configDocument,
                    nsContext, XPathConstants.NODE, Node.class);

            Node idNode = configDocument.createElement("id");
            idNode.setTextContent(profileName);
            licenseCheckMojoProfileNode.insertBefore(idNode, licenseCheckMojoProfileNode.getFirstChild());

            Node importedLicenseCheckProfileNode = originalPomDocument.importNode(licenseCheckMojoProfileNode, true);

            Tools.insertDomNode(originalPomDocument, importedLicenseCheckProfileNode, "/pom:project/pom:profiles",
                    nsContext);

            // .. the finally serialize that modified pom into a temporary file
            String serializedXml = Tools.serializeXML(originalPomDocument);

            String baseDirURI = getSession().getRequest().getPom().getParentFile().toURI().toString();
            File temporaryPom = new File(new URI(baseDirURI + "/" + "tmpPom.xml"));

            FileUtils.writeStringToFile(temporaryPom, serializedXml);

            return temporaryPom;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw new MojoExecutionException("Couldn't configure temporary pom for this execution!", e);
        }
    }

    private void cleanUp() {
        FileUtils.deleteQuietly(licenseHeaderFile);
        FileUtils.deleteQuietly(tmpPom);
    }

}
