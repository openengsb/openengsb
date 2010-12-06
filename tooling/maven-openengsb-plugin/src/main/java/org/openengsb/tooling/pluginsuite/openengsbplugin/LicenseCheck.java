/**
// * Copyright 2010 OpenEngSB Division, Vienna University of Technology
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

import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * equivalent to <code>mvn clean validate -Plicense-check</code>
 * 
 * @goal licenseCheck
 * 
 * @inheritedByDefault false
 * 
 * @requiresProject true
 * 
 */
public class LicenseCheck extends AbstractOpenengsbMojo {

    private List<String> goals;
    private List<String> activatedProfiles;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!getProject().isExecutionRoot()) {
            return;
        }
        validateIfExecutionIsAllowed();
        initializeMavenExecutionProperties();
        executeMaven();
    }

    private void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfWrapperRequestIsRecursive();
        throwErrorIfProjectIsNotExecutedInRootDirectory();
    }

    private void initializeMavenExecutionProperties() {
        goals = Arrays
            .asList(new String[]{ "clean", "validate" });
        activatedProfiles = Arrays
            .asList(new String[]{ "license-check" });
    }

    private void executeMaven() throws MojoExecutionException {
        getNewMavenExecutor().setRecursive(true).execute(this, goals, activatedProfiles, null, null,
            getProject(), getSession(), getMaven());
    }

}
