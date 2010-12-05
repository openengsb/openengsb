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

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * equivalent to <code>mvn install -Prelease,nightly -Dmaven.test.skip=true</code>
 * 
 * @goal assemble
 * 
 * @inheritedByDefault false
 * 
 * @requiresProject true
 */
public class Assemble extends AbstractOpenengsbMojo {

    private List<String> goals;
    private List<String> activatedProfiles;
    Properties userProperties = new Properties();

    @Override
    public void execute() throws MojoExecutionException {
        validateIfExecutionIsAllowed();
        initializeMavenExecutionProperties();
        executeMaven();
    }

    private void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfProjectIsNotSetAsExecutionRoot();
        throwErrorIfProjectIsNotExecutedInRootDirectory();
        throwErrorIfMavenExecutorIsNull();
    }

    private void initializeMavenExecutionProperties() {
        goals = Arrays.asList(new String[]{ "install" });
        activatedProfiles = Arrays.asList(new String[]{ "release", "nightly" });
        userProperties.put("maven.test.skip", "true");
    }

    private void executeMaven() throws MojoExecutionException {
        getMavenExecutor().execute(this, goals, activatedProfiles, null,
            userProperties, getProject(), getSession(), getMaven(), true);
    }

}
