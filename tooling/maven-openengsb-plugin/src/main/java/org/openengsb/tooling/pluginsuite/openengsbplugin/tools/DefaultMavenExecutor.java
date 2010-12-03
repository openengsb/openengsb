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

import java.util.List;
import java.util.Properties;

import org.apache.maven.Maven;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

public class DefaultMavenExecutor implements MavenExecutor {
	
	private boolean changeInterActiveMode = false;
	private boolean interActiveMode = true;

    @Override
    public void execute(AbstractMojo mojo, List<String> goals, List<String> activatedProfiles,
            List<String> deactivatedProfiles, Properties userproperties, MavenProject project, MavenSession session,
            Maven maven, boolean showErrors) throws MojoExecutionException {

        MavenExecutionRequest wrapperRequest = session.getRequest();

        MavenExecutionRequest embeddedRequest = DefaultMavenExecutionRequest.copy(wrapperRequest);
        
		if (changeInterActiveMode) {
			embeddedRequest.setInteractiveMode(interActiveMode);
		}

        embeddedRequest.getGoals().clear();
        embeddedRequest.getGoals().addAll(goals);
        embeddedRequest.setShowErrors(showErrors);

        if (userproperties != null) {
            embeddedRequest.getUserProperties().clear();
            embeddedRequest.getUserProperties().putAll(userproperties);
        }
        if (activatedProfiles != null) {
            embeddedRequest.getActiveProfiles().clear();
            embeddedRequest.getActiveProfiles().addAll(activatedProfiles);
        }
        if (deactivatedProfiles != null) {
            embeddedRequest.getInactiveProfiles().clear();
            embeddedRequest.getInactiveProfiles().addAll(deactivatedProfiles);
        }

        mojo.getLog().info("////////////////////////////////////////////////");
        mojo.getLog().info(String.format("EMBEDDED EXECUTION REQUESTS - BEGIN"));

        if (mojo.getLog().isDebugEnabled()) {
            String goalsDebug = "goals:";
            // only iterate when necessary
            for (String goal : embeddedRequest.getGoals()) {
                goalsDebug += String.format(" %s", goal);
            }
            mojo.getLog().debug(goalsDebug);
        }

        MavenExecutionResult result = maven.execute(embeddedRequest);
		mojo.getLog().info(String.format("EMBEDDED EXECUTION REQUESTS - END"));
		mojo.getLog().info("////////////////////////////////////////////////");

		if (result.hasExceptions()) {
			mojo.getLog().warn("###################");
			mojo.getLog()
					.warn(String
							.format("The following exceptions occured during executions of mojo %s:",
									mojo.getClass().getName()));
			for (Throwable t : result.getExceptions()) {
				mojo.getLog().warn("--------");
				mojo.getLog().warn(t);
			}
			mojo.getLog().warn("###################");
			Throwable ex = result.getExceptions().get(0);
			Throwable cause = ex.getCause();
			String errmsg = (cause != null ? cause.getMessage() : ex
					.getMessage());
			throw new MojoExecutionException(
					String.format(
							"%s\nFAIL - see log statements above for additional info",
							errmsg));
		}

	}

	@Override
	public void setInterActiveMode(boolean interactiveMode) {
		this.changeInterActiveMode = true;
		this.interActiveMode = interactiveMode;
	}

}
