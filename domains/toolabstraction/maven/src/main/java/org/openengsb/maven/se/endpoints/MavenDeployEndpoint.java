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

package org.openengsb.maven.se.endpoints;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.dom.DOMSource;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.xpath.CachedXPathAPI;
import org.openengsb.maven.common.domains.DeployDomain;
import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.common.serializer.MavenResultSerializer;
import org.openengsb.maven.se.AbstractMavenEndpoint;
import org.w3c.dom.Node;


/**
 * Actual Endpoint, that handles the execute-functionality as well as the deploy file-functionality of the Deploy Domain.
 * Both are handled in one Endpoint because they have/need exactly the same configuration. The only difference is the parameter fileToDeploy.
 */
/**
 * @org.apache.xbean.XBean element="mavenDeployer"
 */
public class MavenDeployEndpoint extends AbstractMavenEndpoint implements DeployDomain {
    private static final String FILE_TO_DEPLOY_XPATH = "/mavenDeployer/@fileToDeploy";

    private static final CachedXPathAPI XPATH = new CachedXPathAPI();

    private String[] files = new String[0]; // default value

    public void setFiles(String[] files) {
        this.files = files;
    }

    public String[] getFiles() {
        return this.files;
    }

    @Override
    public void validate() throws DeploymentException {
        validateFilesForExistence();
    }

    /**
     * The overridden InOut MEP. Depending on the parameter fileToDeploy either
     * execute() fileToDeploy() is called. These are implemented methods of
     * DeployDomain.
     * 
     * @see DeployDomain
     * @see AbstractMavenEndpoint
     */
    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
            List<MavenResult> resultList;

            // get the file to deploy (if any)
            SourceTransformer sourceTransformer = new SourceTransformer();
            DOMSource messageXml = (DOMSource) sourceTransformer.toDOMSource(in);
            Node fileToDeployNode = MavenDeployEndpoint.XPATH.selectSingleNode(messageXml.getNode(),
                    MavenDeployEndpoint.FILE_TO_DEPLOY_XPATH);

            String fileToDeploy = null;
            if (fileToDeployNode != null) {
                fileToDeploy = fileToDeployNode.getNodeValue();
            }

            // check if we have to deploy one file, or all
            if (fileToDeploy == null) {
                // deploy all files
                resultList = executeDeploy();
            } else {
                // deploy only one file
                resultList = new ArrayList<MavenResult>(1);
                resultList.add(deployFile(fileToDeploy));
            }

            // set result and send back
            out.setContent(MavenResultSerializer.serialize(out.getContent(), resultList));
            // out.setProperty (DeployDomain.RETURN_DEPLOY_RESULT_LIST,
            // resultList);
            getChannel().send(exchange);
        }
        // else do nothing
    }

    /**
     * @see DeployDomain
     */
    @Override
    public MavenResult deployFile(String file) throws MavenException {
        this.projectConfiguration.setBaseDirectory(new File(file));
        MavenResult deployResult = execute(file, Arrays.asList(new String[] { "deploy" }));

        deployResult.setFile(file);
        deployResult.setDeployedFiles(new String[0]); // set dummy value should
        // something go wrong

        return deployResult;
    }

    /**
     * @see DeployDomain
     */
    @Override
    public void deployFileAsynchronous(String file) throws MavenException {
        // TODO needed?
    }

    /**
     * @see DeployDomain
     */
    @Override
    public List<MavenResult> executeDeploy() throws MavenException {
        List<MavenResult> resultList = new ArrayList<MavenResult>(listFilesToDeploy().length);
        for (String file : listFilesToDeploy()) {
            resultList.add(deployFile(file));
        }

        return resultList;
    }

    /**
     * @see DeployDomain
     */
    @Override
    public void executeDeployAsynchronous() throws MavenException {
        // TODO needed?
    }

    /**
     * @see DeployDomain
     */
    @Override
    public String[] listFilesToDeploy() {
        return getFiles();
    }

    /**
     * Checks if a given string (denoting a file) is configured, i.e. is listed
     * in the Endpoint's configuration.
     * 
     * @param file
     * @return true if the file was found in the list, else false.
     */
    @SuppressWarnings("unused")
    private boolean fileIsConfigured(String file) {
        for (String configuredFile : this.files) {
            if (configuredFile.equals(file)) {
                return true;
            }
        }

        // no file matched
        return false;
    }

    /**
     * Validates that all configured files do exist.
     * 
     * @throws DeploymentException
     */
    private void validateFilesForExistence() throws DeploymentException {
        String nonExistentFiles = "";

        for (String fileString : getFiles()) {
            File file = new File(fileString);
            if (!file.exists()) {
                nonExistentFiles += (file.getAbsolutePath() + ", ");
            }
        }

        if (!nonExistentFiles.isEmpty()) {
            throw new DeploymentException("File(s) " + nonExistentFiles + "do(es) not exist.");
        }
    }
}
