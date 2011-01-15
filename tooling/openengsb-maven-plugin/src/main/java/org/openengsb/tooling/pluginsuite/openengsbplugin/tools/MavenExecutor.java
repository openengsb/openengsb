/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.tooling.pluginsuite.openengsbplugin.tools;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.maven.Maven;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

public interface MavenExecutor {

    /**
     * Builds and executes a {@link org.apache.maven.execution.MavenExecutionRequest MavenExecutionRequest} with given
     * parameters. The request is copied from the wrapper request so all parameters except goals, activeProfiles,
     * deactivateProfiles and userProperties are inherited and have to be changed explicitly.
     *
     * @param mojo the wrapper mojo
     * @param goals goals to execute
     * @param activatedProfiles active profiles
     * @param deactivatedProfiles inactive profiles
     * @param userproperties properties for the mojo
     * @param project maven project from the wrapper mojo
     * @param session maven session from the wrapper mojo
     * @param maven maven implementation from the wrapper mojo
     * @throws MojoExecutionException
     */
    void execute(AbstractMojo mojo, List<String> goals,
            List<String> activatedProfiles, List<String> deactivatedProfiles,
            Properties userproperties, MavenProject project,
            MavenSession session, Maven maven)
        throws MojoExecutionException;

    /**
     * Changes this inherited parameter explicitely.
     *
     * @param interactiveMode <code>true</code> enables interactive mode for this execution <br/>
     *        <code>false</code> is equivalent to <code>mvn --batch-mode &lt;goal&gt;
     */
    MavenExecutor setInterActiveMode(boolean interactiveMode);

    /**
     * Changes this inherited parameter explicitely.
     *
     * @param recursive <code>true</code> recursive execution of the embedded request
     */
    MavenExecutor setRecursive(boolean recursive);

    MavenExecutor setCustomPomFile(File pomFile);

}
