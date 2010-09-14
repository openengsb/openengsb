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

package org.openengsb.maven.common;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.embedder.Configuration;
import org.apache.maven.embedder.DefaultConfiguration;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderException;
import org.apache.maven.embedder.MavenEmbedderLogger;
import org.apache.maven.execution.BuildFailure;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.ReactorManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.reactor.MavenExecutionException;

public class MavenConnector {

    private Log log = LogFactory.getLog(getClass());

    public enum LogLevel {
        DEBUG(0), INFO(1), WARN(2), ERROR(3), FATAL(4), QUIET(5);

        private final int level;

        private LogLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * Directory in which the pom.xml is expected
     */
    private File baseDirectory;

    /**
     * Goals to execute for the pom.xml in the directory specified by parameter
     * 'file'
     */
    private String[] goals;

    private File userSettings;

    private Properties executionRequestProperties;

    /**
     * logLevel one of the values DEBUG, INFO, WARN, ERROR, FATAL, or QUIET
     */
    private LogLevel logLevel = LogLevel.INFO;

    public MavenConnector(File baseDirectory, String[] goals, Properties executionRequestProperties) {
        this.baseDirectory = baseDirectory;
        this.goals = goals;
        this.executionRequestProperties = executionRequestProperties;
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public String[] getGoals() {
        return goals;
    }

    public void setGoals(String[] goals) {
        this.goals = goals;
    }

    public File getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(File userSettings) {
        this.userSettings = userSettings;
    }

    public Properties getExecutionRequestProperties() {
        return executionRequestProperties;
    }

    public void setExecutionRequestProperties(Properties executionRequestProperties) {
        this.executionRequestProperties = executionRequestProperties;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public MavenResult execute() {
        StringWriter writer = new StringWriter();
        MavenEmbedderStringLogger stringLogger = new MavenEmbedderStringLogger(writer);
        stringLogger.setThreshold(logLevel.getLevel());
        MavenEmbedder embedder = setUpEmbedder(stringLogger);

        MavenExecutionRequest request = new DefaultMavenExecutionRequest().setBaseDirectory(baseDirectory).setGoals(
                Arrays.asList(goals)).setProperties(executionRequestProperties);
        request.setPom(new File(baseDirectory, "/pom.xml"));

        MavenResult mavenResult = new MavenResult();
        MavenExecutionResult executionResult = embedder.execute(request);

        readResult(mavenResult, embedder, executionResult);
        mavenResult.setTimestamp(new Date().getTime());

        if (Arrays.binarySearch(goals, "test") >= 0) {
            appendTestResults(writer, mavenResult);
        }

        mavenResult.setOutput(stringLogger.getContent());

        return mavenResult;
    }

    private void appendTestResults(StringWriter writer, MavenResult mavenResult) {
        File surefireReportDir = new File(baseDirectory, "target/surefire-reports");
        if (!surefireReportDir.exists()) {
            return;
        }
        appendTextualTestReports(writer, surefireReportDir);
        addXmlReports(mavenResult, surefireReportDir);
    }

    private void addXmlReports(MavenResult mavenResult, File surefireReportDir) {
        File[] xmlTestReports = surefireReportDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("xml");
            }
        });
        Map<String, byte[]> reports = new HashMap<String, byte[]>();
        for (File testReport : xmlTestReports) {
            try {
                reports.put(testReport.getName(), FileUtils.readFileToByteArray(testReport));
            } catch (IOException e) {
                log.error("Error reading xml test report", e);
            }
        }
        mavenResult.setTestReports(reports);
    }

    private void appendTextualTestReports(StringWriter writer, File surefireReportDir) {
        File[] textualTestReports = surefireReportDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("txt");
            }
        });
        for (File testReport : textualTestReports) {
            try {
                writer.append("\n\n ------ test report ------\n");
                writer.append(FileUtils.readFileToString(testReport));
            } catch (IOException e) {
                log.error("Error readeing test report", e);
            }
        }
    }

    private MavenEmbedder setUpEmbedder(MavenEmbedderLogger logger) {
        Configuration configuration = new DefaultConfiguration().setClassLoader(Thread.currentThread()
                .getContextClassLoader());

        configuration.setMavenEmbedderLogger(logger);

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

        return embedder;
    }

    private void readResult(MavenResult mavenResult, MavenEmbedder embedder, MavenExecutionResult executionResult) {
        if (executionResult.hasExceptions()) {
            mavenResult.setResult(MavenResult.ERROR);

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

            readReactorResult(mavenResult, embedder, executionResult);
        } else {
            mavenResult.setResult(MavenResult.SUCCESS);
        }
    }

    private void readReactorResult(MavenResult mavenResult, MavenEmbedder embedder, MavenExecutionResult executionResult) {
        ReactorManager reactor = executionResult.getReactorManager();

        if (reactor != null && reactor.hasBuildFailures()) {
            MavenProject mavenProject;
            try {
                mavenProject = embedder.readProject(new File(baseDirectory, "/pom.xml"));

                if (reactor.hasBuildFailures()) {
                    BuildFailure buildFailure = executionResult.getReactorManager().getBuildFailure(mavenProject);
                    mavenResult.setTask(buildFailure.getTask());
                    mavenResult.setResult(MavenResult.FAILURE);
                }

            } catch (ProjectBuildingException e) {
                log.error("Error reading reactor result", e);
            } catch (MavenExecutionException e) {
                log.error("Error reading reactor result", e);
            }
        }
    }
}
