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

	@Override
	public void execute() throws MojoExecutionException {

		if (!getProject().isExecutionRoot()) {
			return;
		}

		if (getProject().hasParent()) {
			throw new MojoExecutionException(
					"Please invoke this mojo only in the OpenEngSB root!");
		}

		assert (getMavenExecutor() != null);

		List<String> goals = Arrays.asList(new String[] { "install" });
		List<String> activatedProfiles = Arrays.asList(new String[] {
				"release", "nightly" });

		Properties userproperties = new Properties();
		userproperties.put("maven.test.skip", "true");

		getMavenExecutor().execute(this, goals, activatedProfiles, null,
				userproperties, getProject(), getSession(), getMaven(), true);

	}
}
