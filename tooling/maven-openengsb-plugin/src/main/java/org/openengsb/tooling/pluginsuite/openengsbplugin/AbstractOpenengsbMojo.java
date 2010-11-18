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

import org.apache.maven.Maven;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.openengsb.tooling.pluginsuite.openengsbplugin.tools.MavenExecutor;

public abstract class AbstractOpenengsbMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @parameter expression="${session}"
     */
    private MavenSession session;

    /**
     * @component role="org.apache.maven.Maven"
     */
    private Maven maven;

    /**
     * @component role=
     *            "org.openengsb.tooling.pluginsuite.openengsbplugin.tools.MavenExecutor"
     */
    private MavenExecutor mavenExecutor;

    public MavenProject getProject() {
        return project;
    }

    public MavenSession getSession() {
        return session;
    }

    public Maven getMaven() {
        return maven;
    }

    public MavenExecutor getMavenExecutor() {
        return mavenExecutor;
    }

}
