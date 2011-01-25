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

package org.openengsb.tooling.pluginsuite.openengsbplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.openengsb.tooling.pluginsuite.openengsbplugin.base.AbstractOpenengsbMojo;

/**
 * update development version
 * 
 * @goal pushVersion
 * 
 * @inheritedByDefault false
 * 
 * @requiresProject true
 * 
 * @aggregator true
 * 
 */
public class PushVersion extends AbstractOpenengsbMojo {

    /**
     * the new version
     * 
     * @parameter expression="${developmentVersion}"
     * 
     * @required
     */
    private String developmentVersion;

    private List<String> goals = new ArrayList<String>();
    private Properties userproperties = new Properties();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateIfExecutionIsAllowed();
        configure();
        executeMaven();
    }

    private void configure() {
        goals.add("release:update-versions");
        userproperties.put("autoVersionSubmodules", "true");
        userproperties.put("developmentVersion", developmentVersion);
    }

    private void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfWrapperRequestIsRecursive();
        throwErrorIfProjectIsNotExecutedInRootDirectory();
    }

    private void executeMaven() throws MojoExecutionException {
        getNewMavenExecutor().setRecursive(true).setInterActiveMode(false)
                .execute(this, goals, null, null, userproperties, getProject(), getSession(), getMaven());
    }

}
