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

package org.openengsb.maven.common;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import org.apache.maven.embedder.Configuration;
import org.apache.maven.embedder.DefaultConfiguration;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderConsoleLogger;
import org.apache.maven.embedder.MavenEmbedderException;
import org.apache.maven.execution.BuildFailure;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.ReactorManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.reactor.MavenExecutionException;
import org.openengsb.maven.common.pojos.LogLevelMaven;
import org.openengsb.maven.common.pojos.MavenResult;

import edu.emory.mathcs.backport.java.util.Arrays;

public class MavenConnector {

    /**
     * Executes the given Maven goals in the given directory.
     * 
     * @param baseDirectory Directory in which the pom.xml is expected
     * @param goals Goals to execute for the pom.xml in the directory specified
     *        by parameter 'file'
     * @param userSettings
     * @param properties the properties for the maven execution request
     * @param logLevel one of the values specified in {@link LogLevelMaven}
     * @return Result of the Maven goal execution.
     * @throws MavenException
     */
    protected MavenResult execute(File baseDirectory, String[] goals, File userSettings, Properties properties,
            int logLevel) {
        MavenResult mavenResult = new MavenResult();

        MavenEmbedder embedder = setUpEmbedder(userSettings, logLevel);

        // further set up call
        MavenExecutionRequest request = new DefaultMavenExecutionRequest().setBaseDirectory(baseDirectory).setGoals(
                Arrays.asList(goals)).setProperties(properties);

        request.setPom(new File(baseDirectory, "/pom.xml"));

        MavenExecutionResult executionResult = embedder.execute(request);

        readResult(mavenResult, baseDirectory, embedder, executionResult);

        mavenResult.setTimestamp(new Date().getTime());

        return mavenResult;
    }

    private MavenEmbedder setUpEmbedder(File userSettings, int logLevel) {
        Configuration configuration = new DefaultConfiguration().setClassLoader(Thread.currentThread()
                .getContextClassLoader());

        if (userSettings != null) {
            configuration.setUserSettingsFile(userSettings);
        } else {
            configuration.setUserSettingsFile(MavenEmbedder.DEFAULT_USER_SETTINGS_FILE);
        }

        MavenEmbedder embedder = null;

        try {
            embedder = new MavenEmbedder(configuration);
        } catch (MavenEmbedderException exception) {
            throw new MavenException(exception);
        }

        // default logging is info level
        MavenEmbedderConsoleLogger consoleLogger = new MavenEmbedderConsoleLogger();
        consoleLogger.setThreshold(logLevel);

        embedder.setLogger(consoleLogger);
        return embedder;
    }

    @SuppressWarnings("unchecked")
    private void readResult(MavenResult mavenResult, File baseDirectory, MavenEmbedder embedder,
            MavenExecutionResult executionResult) {
        if (executionResult.hasExceptions()) {
            mavenResult.setExceptions(executionResult.getExceptions());
            mavenResult.setMavenOutput(MavenResult.ERROR);

            // construct errormessage
            StringBuilder stringBuilder = new StringBuilder();

            for (Object exceptionObject : executionResult.getExceptions()) {
                String exceptionMessage;
                if (exceptionObject instanceof Exception) {
                    exceptionMessage = ((Exception) exceptionObject).getMessage();
                } else {
                    exceptionMessage = exceptionObject.toString();
                }

                stringBuilder.append(exceptionMessage);
                stringBuilder.append("\n");
            }

            mavenResult.setErrorMessage(stringBuilder.toString());

            readReactorResult(mavenResult, baseDirectory, embedder, executionResult);
        } else {
            mavenResult.setMavenOutput(MavenResult.SUCCESS);
        }
    }

    private void readReactorResult(MavenResult mavenResult, File baseDirectory, MavenEmbedder embedder,
            MavenExecutionResult executionResult) {
        ReactorManager reactor = executionResult.getReactorManager();

        if (reactor != null && reactor.hasBuildFailures()) {
            MavenProject mavenProject;
            try {
                mavenProject = embedder.readProject(new File(baseDirectory, "/pom.xml"));

                if (reactor.hasBuildFailures()) {
                    BuildFailure buildFailure = executionResult.getReactorManager().getBuildFailure(mavenProject);
                    mavenResult.setTask(buildFailure.getTask());
                    mavenResult.setMavenOutput(MavenResult.FAILURE);
                }

            } catch (ProjectBuildingException e) {
                e.printStackTrace();
            } catch (MavenExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
