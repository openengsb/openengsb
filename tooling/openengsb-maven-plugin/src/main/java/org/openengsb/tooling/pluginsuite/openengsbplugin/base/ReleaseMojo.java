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

package org.openengsb.tooling.pluginsuite.openengsbplugin.base;

import org.apache.maven.plugin.MojoExecutionException;

public abstract class ReleaseMojo extends ConfiguredMojo {

    /**
     * The SCM URL to checkout from. If omitted, the one from the
     * release.properties file is used, followed by the URL from the current
     * POM.
     * 
     * @parameter expression="${connectionUrl}"
     */
    protected String connectionUrl;

    public ReleaseMojo() {
        configPath = "releaseMojo/releaseCommonConfig.xml";
        configProfileXpath = "/rcc:releaseCommonConfig/rcc:profile";
    }

    protected abstract void configure();

    @Override
    protected void preExecute() throws MojoExecutionException {
        throwErrorIfProjectIsNotExecutedInRootDirectory();
        configure();
        goals.add("release:prepare");
        goals.add("release:perform");
        userProperties.put("maven.test.skip", "true");
        userProperties.put("connectionUrl", connectionUrl);
    }

}
