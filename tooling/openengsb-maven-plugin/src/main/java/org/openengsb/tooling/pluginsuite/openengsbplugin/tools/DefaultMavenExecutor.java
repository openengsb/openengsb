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

import org.apache.log4j.Logger;
import org.apache.maven.Maven;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class DefaultMavenExecutor implements MavenExecutor {

    private static final Logger LOG = Logger.getLogger(DefaultMavenExecutor.class);

    private MavenExecutionRequest embeddedRequest;

    private Boolean recursive = null;
    private Boolean interActiveMode = null;

    private File customPomFile = null;

    private File baseDir = null;

    @Override
    public void execute(AbstractMojo mojo, List<String> goals, List<String> activatedProfiles,
            List<String> deactivatedProfiles, Properties userproperties, MavenProject project, MavenSession session,
            Maven maven) throws MojoExecutionException {

        baseDir = session.getRequest().getPom().getParentFile();

        LOG.trace(String.format("basedir: %s", baseDir.toURI().toString()));

        generateRequestFromWrapperRequest(session);
        initExecParametersFromArguments(goals, activatedProfiles, deactivatedProfiles, userproperties);
        changeExecutionParametersIfNecessary();

        printExecutionStartInfoLog(mojo.getLog());

        LOG.trace(String.format("basedir of embedded request: %s", embeddedRequest.getBaseDirectory()));
        LOG.trace(String.format("pomfile of embedded request: %s", embeddedRequest.getPom().toURI().toString()));

        LOG.trace("executing execution request with maven - start");
        MavenExecutionResult result = maven.execute(embeddedRequest);
        LOG.trace("executing execution request with maven - end");

        LOG.trace(String.format("basedir of embedded request: %s", embeddedRequest.getBaseDirectory()));

        printExecutionEndInfoLog(mojo.getLog());
        logAndPassOnExceptionIfAny(result, mojo.getLog());

    }

    private void generateRequestFromWrapperRequest(MavenSession session) {
        MavenExecutionRequest wrapperRequest = session.getRequest();
        embeddedRequest = DefaultMavenExecutionRequest.copy(wrapperRequest);
    }

    private void initExecParametersFromArguments(List<String> goals, List<String> activatedProfiles,
            List<String> deactivatedProfiles, Properties userproperties) {
        embeddedRequest.getGoals().clear();
        embeddedRequest.getGoals().addAll(goals);

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
    }

    private void changeExecutionParametersIfNecessary() {
        if (recursive != null) {
            embeddedRequest.setRecursive(recursive);
        }
        if (interActiveMode != null) {
            embeddedRequest.setInteractiveMode(interActiveMode);
        }
        if (customPomFile != null) {
            LOG.trace(String.format("setting custom pom: %s", customPomFile.toURI().toString()));
            embeddedRequest.setPom(customPomFile);
            LOG.trace(String.format("setting basedir: %s", baseDir.toURI().toString()));
            embeddedRequest.setBaseDirectory(baseDir);
        }
    }

    private void printExecutionStartInfoLog(Log log) {
        log.info("////////////////////////////////////////////////");
        log.info(String.format("EMBEDDED EXECUTION REQUESTS - BEGIN"));

        String goalsInfo = "goals:";
        // only iterate when necessary
        for (String goal : embeddedRequest.getGoals()) {
            goalsInfo += String.format(" %s", goal);
        }
        log.info(goalsInfo);

        if (embeddedRequest.getActiveProfiles() != null && embeddedRequest.getActiveProfiles().size() > 0) {
            String profilesInfo = "active profiles:";
            for (String profile : embeddedRequest.getActiveProfiles()) {
                profilesInfo += String.format(" %s", profile);
            }
            log.info(profilesInfo);
        }
    }

    private void printExecutionEndInfoLog(Log log) {
        log.info(String.format("EMBEDDED EXECUTION REQUESTS - END"));
        log.info("////////////////////////////////////////////////");
    }

    @Override
    public MavenExecutor setInterActiveMode(boolean interactiveMode) {
        interActiveMode = interactiveMode;
        return this;
    }

    @Override
    public MavenExecutor setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    @Override
    public MavenExecutor setCustomPomFile(File pomFile) {
        customPomFile = pomFile;
        return this;
    }

    private void logAndPassOnExceptionIfAny(MavenExecutionResult result, Log log) throws MojoExecutionException {
        if (result.hasExceptions()) {
            log.warn("###################");
            log
                    .warn("The following exceptions occured during execution:");
            for (Throwable t : result.getExceptions()) {
                log.warn("--------");
                log.warn(t);
            }
            log.warn("###################");
            Throwable ex = result.getExceptions().get(0);
            Throwable cause = ex.getCause();
            String errmsg = cause != null ? cause.getMessage() : ex
                    .getMessage();
            throw new MojoExecutionException(
                    String.format(
                            "%s\nFAIL - see log statements above for additional info",
                            errmsg));
        }
    }

}
