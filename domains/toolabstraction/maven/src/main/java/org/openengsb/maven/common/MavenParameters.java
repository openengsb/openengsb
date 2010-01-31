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
package org.openengsb.maven.common;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

public class MavenParameters {

    private String[] goals;

    private File baseDir;

    private Properties executionRequestProperties;

    public File getBaseDir() {
        return baseDir;
    }

    public Properties getExecutionRequestProperties() {
        return executionRequestProperties;
    }

    public String[] getGoals() {
        return goals;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setExecutionRequestProperties(Properties executionRequestProperties) {
        this.executionRequestProperties = executionRequestProperties;
    }

    public void setGoals(String[] goals) {
        this.goals = goals;
    }

    @Override
    public String toString() {
        return "MavenParameters [baseDir=" + baseDir + ", executionRequestProperties=" + executionRequestProperties
                + ", goals=" + Arrays.toString(goals) + "]";
    }

}
