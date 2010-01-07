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

package org.openengsb.maven.common.domains;

import java.util.List;

import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.pojos.result.MavenResult;


/**
 * The Interface that defines the deploy-domain.
 * 
 * @author patrick
 * 
 */
public interface DeployDomain {
    /**
     * Deploys all files listed in the SU's configuration.
     * 
     * @return A List of DeployResults, containing an entry for every deployed
     *         file, describing the success for this deploy-attempt.
     * @throws MavenException for a generic indication of failure while
     *         deploying.
     */
    List<MavenResult> executeDeploy() throws MavenException;

    /**
     * Deploys all files listed in the SU's configuration asynchronously.
     * 
     * @throws MavenException for a generic indication of failure while
     *         deploying.
     */
    void executeDeployAsynchronous() throws MavenException;

    /**
     * Deploys a single file. The file must exist and be configured in the SU.
     * 
     * @param file The file to be deployed. A list of files, that may be used
     *        for this call can be obtained from listFileToDeploy().
     * @return The MavenResult describing the success for this deploy-attempt.
     * @throws MavenException for a generic indication of failure while
     *         deploying.
     */
    MavenResult deployFile(String file) throws MavenException;

    /**
     * Deploys a single file. The file must exist and be configured in the SU.
     * 
     * @param file The file to be deployed. A list of files, that may be used
     *        for this call can be obtained from listFileToDeploy().
     * @throws MavenException for a generic indication of failure while
     *         deploying.
     */
    void deployFileAsynchronous(String file) throws MavenException;

    /**
     * Returns a list of files, configured in the SU.
     * 
     * @return A list of configured files.
     */
    String[] listFilesToDeploy();
}
