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

package org.openengsb.scm.common.commands;

import java.io.File;
import java.net.URI;

import org.openengsb.scm.common.exceptions.ScmException;


public abstract class AbstractScmCommand<ReturnType> implements Command<ReturnType> {
    private File workingCopy;
    private URI connection;
    private URI developerConnection;

    public File getWorkingCopy() {
        return this.workingCopy;
    }

    public void setWorkingCopy(File workingCopy) {
        this.workingCopy = workingCopy;
    }

    public URI getConnection() {
        return this.connection;
    }

    public void setConnection(URI connection) {
        this.connection = connection;
    }

    public URI getDeveloperConnection() {
        return this.developerConnection;
    }

    public void setDeveloperConnection(URI developerConnection) {
        this.developerConnection = developerConnection;
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
    public URI getRepositoryUri() throws ScmException {
        if (this.developerConnection != null) {
            return this.developerConnection;
        } else if (this.connection != null) {
            return this.connection;
        } else {
            throw new ScmException("No Connection was set.");
        }
    }

    public boolean canWriteToRepository() {
        // we can write, if we have a developerConnection
        return (this.developerConnection != null);
    }
}
