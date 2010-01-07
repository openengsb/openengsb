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

package org.openengsb.maven.se.endpoints;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.openengsb.maven.common.domains.BuildDomain;
import org.openengsb.maven.common.domains.TestDomain;
import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.common.serializer.MavenResultSerializer;
import org.openengsb.maven.common.util.dom.DOMCreator;
import org.openengsb.maven.se.AbstractMavenEndpoint;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @org.apache.xbean.XBean element="mavenTester"
 */
public class MavenTestEndpoint extends AbstractMavenEndpoint implements TestDomain {

    private DOMCreator dom = new DOMCreator();

    private File baseDirectory;
    private File testClassesDir;
    private File testSrcDir;
    private File testReportDir;

    private String[] includes;
    private String[] excludes;

    private Boolean skipTests;
    private Boolean testReport;
    private Boolean stopAtFailuresOrErrors;

    private boolean configured = false;

    @Override
    public void validate() throws DeploymentException {
        super.validate();
    }

    /**
     * The overridden InOut MEP depending on the parameter
     * 
     * @see BuildDomain
     * @see AbstractMavenEndpoint
     */
    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        configureSystem();
        if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
            MavenResult testresult = null;

            try {
                testresult = executeTests();
            } catch (MavenException e) {
            }

            if (testresult != null) {
                out.setContent(MavenResultSerializer.serializeAsSource(out.getContent(), testresult));
            }
            getChannel().send(exchange);
        }
    }

    public void configureSystem() throws Exception {
        if (!this.configured) {
            this.configured = true;

            if (new File(this.baseDirectory, "pom.xml") == null) {
                throw new MavenException("No pom.xml found!");
            }

            try {
                File pom = new File(this.baseDirectory, "pom.xml");
                this.dom.readDOM(pom);

                this.dom.findProjectNode(this.dom.getDocument(), this.dom.getDocument());
                this.dom.findBuildNode(this.dom.getDocument(), this.dom.getDocument());
                this.dom.findPluginsNode(this.dom.getDocument(), this.dom.getDocument());
                this.dom.findSurefirePlugin(this.dom.getDocument(), this.dom.getDocument());
                this.dom.generateSurefirePlugin();

            } catch (Exception e) {
                throw new MavenException(e);
            }

            if (this.baseDirectory != null) {
                addParameterToPlugin("baseDir", this.baseDirectory.getAbsolutePath());
            }
            if (this.testReport != null) {
                addParameterToPlugin("disableXmlReport", this.testReport);
            }
            if (this.stopAtFailuresOrErrors != null) {
                addParameterToPlugin("testFailureIgnore", this.stopAtFailuresOrErrors);
            }
            if (this.testReportDir != null) {
                addParameterToPlugin("reportsDirectory", this.testReportDir.getAbsolutePath());
            }
            if (this.testSrcDir != null) {
                addParameterToPlugin("testSourceDirectory", this.testSrcDir.getAbsolutePath());
            }
            if (this.testClassesDir != null) {
                addParameterToPlugin("testClassesDirectory", this.testClassesDir.getAbsolutePath());
            }
            if (this.skipTests != null) {
                addParameterToPlugin("skipTests", this.skipTests);
            }
            if (this.excludes != null && this.excludes.length != 0) {
                addParameterListToPlugin("excludes", this.excludes);
            }
            if (this.includes != null && this.includes.length != 0) {
                addParameterListToPlugin("includes", this.includes);
            }
        }
    }

    /**
     * @see TestDomain
     */
    @Override
    public boolean isTestReport() {
        return this.testReport;
    }

    /**
     * @see TestDomain
     */
    @Override
    public void setTestReport(boolean testReport) {
        this.testReport = testReport;
    }

    /**
     * @see TestDomain
     */
    @Override
    public MavenResult executeTests() throws MavenException {

        // delete old pom.xml
        File pom_delete = new File(this.baseDirectory, "pom.xml");
        pom_delete.getParentFile().delete();

        try {
            // write modified pom.xml
            this.dom.writeInXMLFile(this.dom.getDocument(), new File(this.baseDirectory, "pom.xml"));
        } catch (Exception e) {
            throw new MavenException(e);
        }

        if (!this.dom.validateSurefirePlugin()) {
            throw new MavenException("Illegal Parameter for the Surefire Plugin.");
        }

        this.projectConfiguration.setBaseDirectory(this.baseDirectory);
        addParameterToPlugin("baseDir", this.baseDirectory.getAbsolutePath());

        MavenResult testResult = execute(this.baseDirectory.getAbsolutePath(), Arrays.asList(new String[] { "test" }));

        return testResult;

    }

    /**
     * @see TestDomain
     */
    @Override
    public List<File> getClasspath() {
        return null;
    }

    /**
     * @see TestDomain
     */
    @Override
    public String[] getExcludes() {
        return this.excludes;
    }

    /**
     * @see TestDomain
     */
    @Override
    public String[] getIncludes() {
        return this.includes;
    }

    /**
     * @see TestDomain
     */
    @Override
    public File getTestClassesDir() {
        return this.testClassesDir;
    }

    /**
     * @see TestDomain
     */
    @Override
    public File getTestReportDir() {
        return this.testReportDir;
    }

    /**
     * @see TestDomain
     */
    @Override
    public boolean isStopAtFailuresOrErrors() {
        return this.stopAtFailuresOrErrors;
    }

    /**
     * @see TestDomain
     */
    @Override
    public void setExcludes(String... excludes) {
        this.excludes = excludes;
    }

    /**
     * @see TestDomain
     */
    @Override
    public void setIncludes(String... includes) {
        this.includes = includes;
    }

    /**
     * @see TestDomain
     */
    @Override
    public void setStopAtFailuresOrErrors(boolean stopAtFailuresOrErrors) {
        this.stopAtFailuresOrErrors = stopAtFailuresOrErrors;
    }

    /**
     * @see TestDomain
     */
    @Override
    public void setTestClassesDir(File testClassesDir) {
        this.testClassesDir = testClassesDir;
    }

    /**
     * @see TestDomain
     */
    @Override
    public void setTestReportDir(File testReportDir) {
        this.testReportDir = testReportDir;
    }

    /**
     * @see TestDomain
     */
    @Override
    public File getBaseDirectory() {
        return this.baseDirectory;
    }

    /**
     * @see TestDomain
     */
    @Override
    public void setBaseDirectory(File baseDirectory) throws MavenException {
        this.baseDirectory = baseDirectory;
    }

    /**
     * @see TestDomain
     */
    @Override
    public boolean isSkipTests() {
        return this.skipTests;
    }

    /**
     * @see TestDomain
     */
    @Override
    public void setSkipTests(boolean skipTests) {
        this.skipTests = skipTests;
    }

    /**
     * @see TestDomain
     */
    @Override
    public File getTestSrcDirs() {
        return this.testSrcDir;
    }

    /**
     * @see TestDomain
     */
    @Override
    public void setTestSrcDirs(File testSrcDir) {
        this.testSrcDir = testSrcDir;
    }

    /**
     * Adds a paramter to the surefire configuration
     * 
     * @param paramter - tag for the configuration
     * @param value - value of the node
     */
    private void addParameterToPlugin(String paramter, Object value) {
        Node configuration = this.dom.getSurefireConfigurationNode();
        Node node = this.dom.getDocument().createElement(paramter);

        Node parameterNode = DOMCreator.checkIfNodeHasElement(configuration, node);

        if (parameterNode != null) {
            Node parent = parameterNode.getParentNode();
            parent.removeChild(parameterNode);
        }
        node.setTextContent(String.valueOf(value));
        node.setNodeValue(String.valueOf(value));

        configuration.appendChild(node);
    }

    /**
     * Adds a list of parameter
     * 
     * @param parameter - tag of the surefire configuration
     * @param values - values of the single entries
     */
    private void addParameterListToPlugin(String parameter, String... values) {
        Node configuration = this.dom.getSurefireConfigurationNode();
        Node nodeList = this.dom.getDocument().createElement(parameter);

        Node nodeChceck = DOMCreator.checkIfNodeHasElement(configuration, nodeList);

        if (nodeChceck != null) {

            // Remove Childs
            NodeList childnodes = nodeChceck.getChildNodes();
            for (int i = 0; i < childnodes.getLength(); i++) {
                Node nd = childnodes.item(i);
                nodeChceck.removeChild(nd);
            }

            Node parent = nodeChceck.getParentNode();
            parent.removeChild(nodeChceck);
        }

        configuration.appendChild(nodeList);

        for (String value : values) {
            Node node = this.dom.getDocument().createElement(parameter.substring(0, parameter.length() - 1));
            node.appendChild(this.dom.getDocument().createTextNode(value));

            node.setNodeValue(value);
            nodeList.appendChild(node);
        }
    }

    /**
     * Returns the document of the DOM
     * 
     * @return dom
     */
    public DOMCreator getDom() {
        return this.dom;
    }
}
