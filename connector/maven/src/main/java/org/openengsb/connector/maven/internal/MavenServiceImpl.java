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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.AliveState;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.domain.build.BuildDomain;
import org.openengsb.domain.build.BuildDomainEvents;
import org.openengsb.domain.build.BuildEndEvent;
import org.openengsb.domain.build.BuildStartEvent;
import org.openengsb.domain.deploy.DeployDomain;
import org.openengsb.domain.deploy.DeployDomainEvents;
import org.openengsb.domain.deploy.DeployEndEvent;
import org.openengsb.domain.deploy.DeployStartEvent;
import org.openengsb.domain.test.TestDomain;
import org.openengsb.domain.test.TestDomainEvents;
import org.openengsb.domain.test.TestEndEvent;
import org.openengsb.domain.test.TestStartEvent;

public class MavenServiceImpl implements TestDomain, BuildDomain, DeployDomain {

    private static final String MVN_COMMAND = "mvn" + addSystemEnding();
    private Log log = LogFactory.getLog(this.getClass());
    private String projectPath;

    private BuildDomainEvents buildEvents;
    private TestDomainEvents testEvents;
    private DeployDomainEvents deployEvents;

    private Executor executor;

    private boolean synchronous = false;

    private ContextCurrentService contextService;

    private String command;

    public MavenServiceImpl() {
        executor = Executors.newSingleThreadExecutor();
    }

    private static String addSystemEnding() {
        if (System.getProperty("os.name").contains("Windows")) {
            return ".bat";
        }
        return "";
    }

    void setProjectPath(String projectPath) {
        this.projectPath = projectPath.replaceAll("%20"," ");
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
        testEvents.raiseEvent(new TestStartEvent(id));
        Runnable runTests = new Runnable() {

            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                testEvents.raiseEvent(new TestEndEvent(id, result.isSuccess(), result.getOutput()));
            }
        };
        execute(runTests);
        return id;
    }

    @Override
    public String build() {
        final String id = createId();
        final String contextId = contextService.getThreadLocalContext();
        buildEvents.raiseEvent(new BuildStartEvent(id));
        Runnable doBuild = new Runnable() {

            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                buildEvents.raiseEvent(new BuildEndEvent(id, result.isSuccess(), result.getOutput()));
            }
        };
        execute(doBuild);
        return id;
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
        deployEvents.raiseEvent(new DeployStartEvent(id));
        Runnable doDeploy = new Runnable() {

            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                deployEvents.raiseEvent(new DeployEndEvent(id, result.isSuccess(), result.getOutput()));
            }
        };
        execute(doDeploy);
        return id;
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
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.directory(dir).start();

        StreamReader output = new StreamReader(process.getInputStream());
        StreamReader error = new StreamReader(process.getErrorStream());
        output.start();
        error.start();

        boolean result = process.waitFor() == 0;
        output.join();
        error.join();

        String errorOutput = error.getString();
        if (!errorOutput.isEmpty()) {
            log.warn("Maven connector error stream output: " + errorOutput);
        }
        return new MavenResult(result, output.getString());
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
