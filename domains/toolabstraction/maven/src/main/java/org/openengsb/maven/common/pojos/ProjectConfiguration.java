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

package org.openengsb.maven.common.pojos;

import java.io.File;
import java.util.List;

/**
 * The ProjectConfiguraion are required values to start the build tool.
 * 
 */
public class ProjectConfiguration {
    private List<String> goals;
    private File baseDirectory;

    /**
     * Returns a List of goals, that should be realized
     * 
     * @return
     */
    public List<String> getGoals() {
        return this.goals;
    }

    /**
     * sets the goals, that should be realized
     * 
     * @param goals
     */
    public void setGoals(List<String> goals) {
        this.goals = goals;
    }

    /**
     * sets the base directory
     * 
     * @param directory - the root directory for the project
     */
    public void setBaseDirectory(File directory) {
        this.baseDirectory = directory;
    }

    /**
     * supplies the directory
     * 
     * @return baseDirectory - the root directory for the project
     */
    public File getBaseDirectory() {
        return this.baseDirectory;
    }

}
