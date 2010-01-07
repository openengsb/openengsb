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

import java.util.Date;

import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.pojos.LogLevelMaven;
import org.openengsb.maven.common.pojos.Options;
import org.openengsb.maven.common.pojos.ProjectConfiguration;
import org.openengsb.maven.common.pojos.result.MavenResult;


/**
 * The interface describes how to use the build domain and the possibilities to
 * configure.
 * 
 */
public interface BuildDomain {

    /**
     * Sets the LogLevel, which is set for the build process
     * 
     * @param loglevel - possible options are info, warn, debug, error or
     *        verbose
     */
    void setLogLevel(LogLevelMaven loglevel);

    /**
     * supplies the actual LogLevel
     * 
     * @return LogLevel
     */
    LogLevelMaven getLogLevel();

    /**
     * Sets the parameters, which were defined For example, system properties
     * could be set
     * 
     * @param options
     */
    void setOptions(Options options);

    /**
     * supplies the used options of the build process
     * 
     * @return the options set by the definition of the build process
     */
    Options getOptions();

    /**
     * supplies the required values of the build process
     * 
     * @return the projectConfiguration
     */
    ProjectConfiguration getProjectConfiguration();

    /**
     * Sets the project configuration of the build tool
     * 
     * @param projectConfiguration
     */
    void setProjectConfiguration(ProjectConfiguration projectConfiguration);

    /**
     * Executes the tool which is selected
     * 
     * @throws BuildException
     */
    MavenResult executeBuild() throws MavenException;

    /**
     * supplies the start time of the time
     * 
     * @return start time as Date
     */
    Date buildStartTime();

    /**
     * supplies the end time of the time
     * 
     * @return end time as Date
     */
    Date buildEndTime();

    /**
     * checks, if the build was started
     * 
     * @return true if the build was started, else false
     */
    boolean buildStarted();

    /**
     * checks, if the settings are defined
     * 
     * @return true if the settings are defined, else false
     */
    boolean settingsDefined();

    /**
     * checks, if the build includes tests
     * 
     * @return true if the build includes tests, else false
     */
    boolean testsIncluded();
}
