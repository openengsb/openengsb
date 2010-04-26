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

public abstract class ScmConnector {
    private static final String DEFAULT_WORKING_COPY_NAME = "workingCopy";
    private static final String DEFAULT_ENGSB_WORKING_COPY = "./data/openengsb";

    private File workingCopyFile;
    private URI developerConnectionUri;
    private String username;
    private String password;

    protected boolean canWriteToRepository() {
        return developerConnectionUri != null;
    }

    protected URI getDeveloperConnectionUri() {
        return developerConnectionUri;
    }

    public void setDeveloperConnection(String developerConnection) {
        developerConnectionUri = URI.create(developerConnection);
    }

    protected String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    protected String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    protected File getWorkingCopyFile() {
        return workingCopyFile;
    }

    public void setWorkingCopy(String workingCopy) {
        if (workingCopy != null) {
            workingCopyFile = new File(workingCopy);
            if (!workingCopyFile.isAbsolute()) {
                workingCopyFile = new File(DEFAULT_ENGSB_WORKING_COPY, workingCopy);
            }
        } else {
            workingCopyFile = new File(DEFAULT_ENGSB_WORKING_COPY, DEFAULT_WORKING_COPY_NAME);
        }
    }
}
