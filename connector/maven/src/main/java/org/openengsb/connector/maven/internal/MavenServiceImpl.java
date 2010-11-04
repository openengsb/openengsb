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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.domain.build.BuildDomain;
import org.openengsb.domain.deploy.DeployDomain;
import org.openengsb.domain.test.TestDomain;

public class MavenServiceImpl implements TestDomain, BuildDomain, DeployDomain {

    private static final String MVN_COMMAND = "mvn";
    private Log log = LogFactory.getLog(this.getClass());
    private String projectPath;

    public MavenServiceImpl() {
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
        return excuteGoal("test");
    }

    @Override
    public Boolean build() {
        return excuteGoal("build");
    }

    @Override
    public Boolean deploy() {
        return excuteGoal("deploy");
    }

    public Boolean validate() {
        return excuteGoal("validate");
    }

    private synchronized Boolean excuteGoal(String goal) {
        File dir = new File(projectPath);

        StringBuilder command = new StringBuilder(MVN_COMMAND);
        command.append(' ');
        command.append(goal);
        try {
            Process process = Runtime.getRuntime().exec(command.toString(), new String[]{}, dir);
            return process.waitFor() == 0;
        } catch (IOException e) {
            log.error(e);
            return false;
        } catch (InterruptedException e) {
            log.error(e);
            return false;
        }

    }

}
