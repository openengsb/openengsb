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

package org.openengsb.maven.common.pojos;

import java.io.File;
import java.util.Properties;

/**
 * The Options for the build tool can be set. Expansions are wished.
 * 
 */
public class Options {

    private File buildFile;
    private File settings;
    private File baseDirectory;
    private Properties properties = new Properties();

    /**
     * sets the file for the build process
     * 
     * @param buildFile - File with path
     */
    public void setBuildfile(File buildFile) {
        this.buildFile = buildFile;
    }

    /**
     * supplies the build file
     * 
     * @return the build file
     */
    public File getBuildfile() {
        return this.buildFile;
    }

    /**
     * sets the file for the settings
     * 
     * @param settings - File with path
     */
    public void setSettings(File settings) {
        this.settings = settings;
    }

    /**
     * supplies the settings file
     * 
     * @return the settings file
     */
    public File getSettings() {
        return this.settings;
    }

    /**
     * sets the base directory
     * 
     * @param directory as a File Object
     */
    public void setBaseDirectory(File directory) {
        this.baseDirectory = directory;
    }

    /**
     * supplies the directory
     * 
     * @return the directory
     */
    public File getBaseDirectory() {
        return this.baseDirectory;
    }

    /**
     * activate a profile
     * 
     * @param profile - name of the profile
     */
    public void activateProfile(String profile) {

    }

    /**
     * supplies the activated profile or null, if no profile was set
     * 
     * @return the activated profile or null
     */
    public String getActivatedProfile() {
        return null;
    }

    /**
     * define a property
     * 
     * @param property - name of the property
     * @param value - value of the property
     */
    public void defineProperty(String property, String value) {
        this.properties.setProperty(property, value);
    }

    /**
     * supplies the properties
     * 
     * @return a Properties Object
     */
    public Properties getProperties() {
        return this.properties;
    }

}
