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

package org.openengsb.scm.common;

import java.io.File;
import java.net.URI;

import javax.jbi.management.DeploymentException;

import org.openengsb.scm.common.exceptions.ScmException;

public abstract class ScmConnector {
    private static final String DEFAULT_WORKING_COPY_NAME = "workingCopy";
    private static final String DEFAULT_ENGSB_WORKING_COPY = "./data/openengsb";

    protected String workingCopy;
    protected File workingCopyFile;

    protected String connection; // expected to be read only
    protected URI connectionUri;

    protected String developerConnection; // expected to have full access
    protected URI developerConnectionUri;

    protected String username;
    protected String password;

    /**
     * Since the factory is configured by the xbean.xml and instantiated by
     * ServiceMix it needs a chance to tell us, that is was misconfigured. Well,
     * this is it.
     * 
     * Used to validate the Commandfactory's parameters. Override this method to
     * perform additional validations, but be sure to call super ;)
     * 
     * @throws DeploymentException
     */
    public void validate() throws DeploymentException {
        try {
            URI connection = getConnectionUri();
            URI developerConnection = getDeveloperConnectionUri();

            if ((connection == null) && (developerConnection == null)) {
                throw new DeploymentException("Neither connection nor developerConnection was set!");
            }
        } catch (IllegalArgumentException exception) {
            throw new DeploymentException(exception);
        }
    }

    protected File getWorkingCopyFile() {
        if (this.workingCopyFile == null) {
            this.workingCopyFile = calculateWorkingCopyFile();
        }

        return this.workingCopyFile;
    }

    protected File calculateWorkingCopyFile() {
        File file = null;

        if (this.workingCopy != null) {
            file = new File(this.workingCopy);
            if (!file.isAbsolute()) {
                // to the default engsb-direcotry
                file = new File(DEFAULT_ENGSB_WORKING_COPY, this.workingCopy);
            }
        } else // working-copy was not set -> fall back to default
        {
            file = new File(DEFAULT_ENGSB_WORKING_COPY, DEFAULT_WORKING_COPY_NAME);
        }

        return file;
    }

    protected URI getConnectionUri() {
        if (connectionUri == null && connection != null) {
            connectionUri = URI.create(connection);
        }

        return this.connectionUri;
    }

    protected URI getDeveloperConnectionUri() {
        if (developerConnectionUri == null && developerConnection != null) {
            developerConnectionUri = URI.create(developerConnection);
        }

        return developerConnectionUri;
    }

    /**
     * Convenience-method that either returns connection or developerConnection,
     * depending on which one is set. If both are set developerConnection is
     * preferred. If none was set, null is returned
     * 
     * @return The connection or developerConnection depending on which
     *         connection was set.
     * @throws ScmException when no connection was set at all
     */
    protected URI getRepositoryUri() throws ScmException {
        if (developerConnectionUri != null) {
            return developerConnectionUri;
        } else if (connectionUri != null) {
            return connectionUri;
        } else {
            throw new ScmException("No connection was set.");
        }
    }

    protected boolean canWriteToRepository() {
        // we can write, if we have a developerConnection
        return developerConnection != null;
    }

}
