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

package org.openengsb.tooling.pluginsuite.openengsbplugin.base;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.xml.xpath.XPathConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.openengsb.tooling.pluginsuite.openengsbplugin.AbstractOpenengsbMojo;
import org.openengsb.tooling.pluginsuite.openengsbplugin.tools.Tools;
import org.openengsb.tooling.pluginsuite.openengsbplugin.xml.OpenEngSBMavenPluginNSContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class ConfiguredMojo extends AbstractOpenengsbMojo {

    private static final Logger LOG = Logger.getLogger(ConfiguredMojo.class);

    // #################################
    // set these in subclass
    // #################################
    
    protected String configProfileXpath;
    protected String configPath;
    
    // #################################

    protected List<String> goals = new ArrayList<String>();
    protected List<String> activatedProfiles = new ArrayList<String>();
    protected List<String> deactivatedProfiles = new ArrayList<String>();
    protected Properties userProperties = new Properties();

    private File tmpPom;

    private static final OpenEngSBMavenPluginNSContext NS_CONTEXT = new OpenEngSBMavenPluginNSContext();
    private static final String POM_PROFILE_XPATH = "/pom:project/pom:profiles";

    protected static final List<File> FILES_TO_REMOVE_FINALLY = new ArrayList<File>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            preExecute();
            String profileName = UUID.randomUUID().toString();
            tmpPom = configureTmpPom(profileName);
            FILES_TO_REMOVE_FINALLY.add(tmpPom);
            configureMojo(profileName);
            executeMavenWithCustomPom(tmpPom);
        } finally {
            cleanUp();
        }
    }

    /**
     * overwrite in subclass
     */
    protected abstract void preExecute() throws MojoExecutionException;

    private void configureMojo(String profileName) {
        activatedProfiles.add(profileName);
    }

    private void executeMavenWithCustomPom(File pom) throws MojoExecutionException {
        // TODO set all parameters
        getNewMavenExecutor()
                .setRecursive(true)
                .setCustomPomFile(pom)
                .execute(this, goals, activatedProfiles, deactivatedProfiles, userProperties, getProject(),
                        getSession(), getMaven());
    }

    private File configureTmpPom(String profileName) throws MojoExecutionException {
        try {
            Document originalPomDocument = Tools.parseXMLFromString(FileUtils.readFileToString(getSession()
                    .getRequest().getPom()));
            // read plugin default configuration
            Document configDocument = Tools.parseXMLFromString(IOUtils.toString(getClass().getClassLoader()
                    .getResourceAsStream(configPath)));

            // .. and insert the profile node into the pom dom tree ..
            Node licenseCheckMojoProfileNode = Tools.evaluateXPath(configProfileXpath, configDocument, NS_CONTEXT,
                    XPathConstants.NODE, Node.class);

            Node idNode = configDocument.createElement("id");
            idNode.setTextContent(profileName);
            licenseCheckMojoProfileNode.insertBefore(idNode, licenseCheckMojoProfileNode.getFirstChild());

            Node importedLicenseCheckProfileNode = originalPomDocument.importNode(licenseCheckMojoProfileNode, true);

            Tools.insertDomNode(originalPomDocument, importedLicenseCheckProfileNode, POM_PROFILE_XPATH, NS_CONTEXT);

            // .. the finally serialize that modified pom into a temporary file
            String serializedXml = Tools.serializeXML(originalPomDocument);

            String baseDirURI = getSession().getRequest().getPom().getParentFile().toURI().toString();
            File temporaryPom = new File(new URI(baseDirURI + "/" + "tmpPom.xml"));

            FileUtils.writeStringToFile(temporaryPom, serializedXml);

            return temporaryPom;
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            throw new MojoExecutionException("Couldn't configure temporary pom for this execution!", e);
        }
    }

    private void cleanUp() {
        for (File f : FILES_TO_REMOVE_FINALLY) {
            FileUtils.deleteQuietly(f);
        }
    }

}
