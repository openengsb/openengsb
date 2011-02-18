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

package org.openengsb.connector.maven.internal;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.AliveState;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.domain.build.BuildDomainEvents;
import org.openengsb.domain.build.BuildFailEvent;
import org.openengsb.domain.build.BuildStartEvent;
import org.openengsb.domain.build.BuildSuccessEvent;
import org.openengsb.domain.deploy.DeployDomainEvents;
import org.openengsb.domain.deploy.DeployFailEvent;
import org.openengsb.domain.deploy.DeployStartEvent;
import org.openengsb.domain.deploy.DeploySuccessEvent;
import org.openengsb.domain.test.TestDomainEvents;
import org.openengsb.domain.test.TestFailEvent;
import org.openengsb.domain.test.TestStartEvent;
import org.openengsb.domain.test.TestSuccessEvent;

public class MavenServiceImpl extends AbstractOpenEngSBService implements MavenDomain {

    private static final String MVN_COMMAND = "mvn" + addSystemEnding();
    private static final int MAX_LOG_FILES = 5;
    private Log log = LogFactory.getLog(this.getClass());
    private String projectPath;

    private BuildDomainEvents buildEvents;
    private TestDomainEvents testEvents;
    private DeployDomainEvents deployEvents;

    private Executor executor = Executors.newSingleThreadExecutor();
    private ExecutorService outputReaderPool = Executors.newCachedThreadPool();

    private boolean synchronous = false;

    private boolean useLogFile = true;

    private ContextCurrentService contextService;

    private String command;
    private File logDir;

    public MavenServiceImpl(String id) {
        super(id);
        String karafData = System.getProperty("karaf.data");
        logDir = new File(karafData, "log");
        if (!logDir.exists()) {
            logDir.mkdir();
        } else if (!logDir.isDirectory()) {
            throw new IllegalStateException("cannot access log-directory");
        }
    }

    private static String addSystemEnding() {
        if (System.getProperty("os.name").contains("Windows")) {
            return ".bat";
        }
        return "";
    }

    void setProjectPath(String projectPath) {
        this.projectPath = projectPath.replaceAll("%20", " ");
    }

    @Override
    public AliveState getAliveState() {
        if (validate()) {
            return AliveState.ONLINE;
        } else {
            return AliveState.OFFLINE;
        }
    }

    @Override
    public String runTests() {
        final String id = createId();
        final String contextId = contextService.getThreadLocalContext();
        Runnable runTests = new Runnable() {

            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                testEvents.raiseEvent(new TestStartEvent(id));
                if (result.isSuccess()) {
                    testEvents.raiseEvent(new TestSuccessEvent(id, result.getOutput()));
                } else {
                    testEvents.raiseEvent(new TestFailEvent(id, result.getOutput()));
                }
            }
        };
        execute(runTests);
        return id;
    }

    @Override
    public void runTests(final long processId) {
        final String contextId = contextService.getThreadLocalContext();
        Runnable runTests = new Runnable() {
            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                testEvents.raiseEvent(new TestStartEvent(processId));
                if (result.isSuccess()) {
                    testEvents.raiseEvent(new TestSuccessEvent(processId, result.getOutput()));
                } else {
                    testEvents.raiseEvent(new TestFailEvent(processId, result.getOutput()));
                }
            }
        };
        execute(runTests);
    }

    @Override
    public String build() {
        final String id = createId();
        final String contextId = contextService.getThreadLocalContext();
        Runnable doBuild = new Runnable() {
            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                buildEvents.raiseEvent(new BuildStartEvent(id));
                if (result.isSuccess()) {
                    buildEvents.raiseEvent(new BuildSuccessEvent(id, result.getOutput()));
                } else {
                    buildEvents.raiseEvent(new BuildFailEvent(id, result.getOutput()));
                }
            }
        };
        execute(doBuild);
        return id;
    }

    @Override
    public void build(final long processId) {
        final String contextId = contextService.getThreadLocalContext();
        Runnable doBuild = new Runnable() {
            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                BuildStartEvent buildStartEvent = new BuildStartEvent();
                buildStartEvent.setProcessId(processId);
                buildEvents.raiseEvent(buildStartEvent);
                if (result.isSuccess()) {
                    buildEvents.raiseEvent(new BuildSuccessEvent(processId, result.getOutput()));
                } else {
                    buildEvents.raiseEvent(new BuildFailEvent(processId, result.getOutput()));
                }
            }
        };
        execute(doBuild);

    }

    private void execute(Runnable runnable) {
        if (synchronous) {
            runnable.run();
        } else {
            executor.execute(runnable);
        }
    }

    @Override
    public String deploy() {
        final String id = createId();
        final String contextId = contextService.getThreadLocalContext();
        Runnable doDeploy = new Runnable() {

            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                deployEvents.raiseEvent(new DeployStartEvent(id));
                if (result.isSuccess()) {
                    deployEvents.raiseEvent(new DeploySuccessEvent(id, result.getOutput()));
                } else {
                    deployEvents.raiseEvent(new DeployFailEvent(id, result.getOutput()));
                }
            }
        };
        execute(doDeploy);
        return id;
    }

    @Override
    public void deploy(final long processId) {
        final String contextId = contextService.getThreadLocalContext();
        Runnable doDeploy = new Runnable() {
            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                deployEvents.raiseEvent(new DeployStartEvent(processId));
                if (result.isSuccess()) {
                    deployEvents.raiseEvent(new DeploySuccessEvent(processId, result.getOutput()));
                } else {
                    deployEvents.raiseEvent(new DeployFailEvent(processId, result.getOutput()));
                }

            }
        };
        execute(doDeploy);
    }

    private String createId() {
        return UUID.randomUUID().toString();
    }

    public Boolean validate() {
        return excuteCommand("validate").isSuccess();
    }

    private synchronized MavenResult excuteCommand(String goal) {
        File dir = new File(projectPath);

        List<String> command = new ArrayList<String>();
        command.add(MVN_COMMAND);
        command.addAll(Arrays.asList(goal.trim().split(" ")));

        try {
            return runMaven(dir, command);
        } catch (IOException e) {
            log.error(e);
            return new MavenResult(false, e.getMessage());
        } catch (InterruptedException e) {
            log.error(e);
            return new MavenResult(false, e.getMessage());
        }
    }

    private MavenResult runMaven(File dir, List<String> command) throws IOException, InterruptedException {
        log.info("running '" + command + "' in directory '" + dir.getPath() + "'");
        Process process = configureProcess(dir, command);
        Future<String> outputFuture = configureProcessOutputReader(process);
        Future<String> errorFuture = configureProcessErrorReader(process);
        boolean processResultCode = process.waitFor() == 0;
        String outputResult = readResultFromFuture(outputFuture);
        String errorResult = readResultFromFuture(errorFuture);
        if (!errorResult.isEmpty()) {
            log.warn("Maven connector error stream output: " + errorResult);
        }
        log.info("maven exited with status " + processResultCode);
        return new MavenResult(processResultCode, outputResult);
    }

    private Process configureProcess(File dir, List<String> command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.directory(dir).start();
        return process;
    }

    private Future<String> configureProcessErrorReader(Process process) {
        ProcessOutputReader error = new ProcessOutputReader(process.getErrorStream());
        return outputReaderPool.submit(error);
    }

    private Future<String> configureProcessOutputReader(Process process) throws IOException {
        ProcessOutputReader output;
        if (useLogFile) {
            File logFile = getNewLogFile();
            output = new ProcessOutputReader(process.getInputStream(), logFile);
        } else {
            output = new ProcessOutputReader(process.getInputStream());
        }
        return outputReaderPool.submit(output);
    }

    private String readResultFromFuture(Future<String> future) throws InterruptedException {
        String result;
        try {
            result = future.get();
        } catch (ExecutionException e) {
            log.error(e.getCause());
            result = ExceptionUtils.getFullStackTrace(e);
        }
        return result;
    }

    private File getNewLogFile() throws IOException {
        if (logDir.list().length + 1 > MAX_LOG_FILES) {
            assertLogLimit();
        }
        String dateString = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
        String fileName = String.format("maven.%s.log", dateString);
        File logFile = new File(logDir, fileName);
        logFile.createNewFile();
        return logFile;
    }

    private boolean assertLogLimit() {
        File[] logFiles = logDir.listFiles();
        Arrays.sort(logFiles, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });
        return logFiles[0].delete();
    }

    public void setBuildEvents(BuildDomainEvents buildEvents) {
        this.buildEvents = buildEvents;
    }

    public void setTestEvents(TestDomainEvents testEvents) {
        this.testEvents = testEvents;
    }

    public void setDeployEvents(DeployDomainEvents deployEvents) {
        this.deployEvents = deployEvents;
    }

    public void setContextService(ContextCurrentService contextService) {
        this.contextService = contextService;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setUseLogFile(boolean useLogFile) {
        this.useLogFile = useLogFile;
    }

    public int getLogLimit() {
        return MAX_LOG_FILES;
    }

    private class MavenResult {
        private String output;

        private boolean success;

        public MavenResult(boolean success, String output) {
            this.success = success;
            this.output = output;
        }

        public String getOutput() {
            return output;
        }

        public boolean isSuccess() {
            return success;
        }
    }

}
