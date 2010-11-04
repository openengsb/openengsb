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
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.util.AliveState;
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

    private static String addSystemEnding() {
        if (System.getProperty("os.name").contains("Windows")) {
            return ".bat";
        }
        return "";
    }

    void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
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
    public Boolean runTests() {
        testEvents.raiseEvent(new TestStartEvent());
        MavenResult result = excuteGoal("test");
        testEvents.raiseEvent(new TestEndEvent(result.isSuccess(), result.getOutput()));
        return result.isSuccess();
    }

    @Override
    public Boolean build() {
        buildEvents.raiseEvent(new BuildStartEvent());
        MavenResult result = excuteGoal("build");
        buildEvents.raiseEvent(new BuildEndEvent(result.isSuccess(), result.getOutput()));
        return result.isSuccess();
    }

    @Override
    public Boolean deploy() {
        deployEvents.raiseEvent(new DeployStartEvent());
        MavenResult result = excuteGoal("deploy");
        deployEvents.raiseEvent(new DeployEndEvent(result.isSuccess(), result.getOutput()));
        return result.isSuccess();
    }

    public Boolean validate() {
        return excuteGoal("validate").isSuccess();
    }

    private synchronized MavenResult excuteGoal(String goal) {
        File dir = new File(projectPath);

        List<String> command = new ArrayList<String>();
        command.add(MVN_COMMAND);
        command.add(goal);

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
        String output = IOUtils.toString(process.getInputStream());
        log.trace(output);
        boolean result = process.waitFor() == 0;
        return new MavenResult(result, output);
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
