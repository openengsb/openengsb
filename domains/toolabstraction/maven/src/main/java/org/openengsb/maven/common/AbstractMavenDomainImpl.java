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
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AbstractMavenDomainImpl {

    private Log log = LogFactory.getLog(getClass());

    private String baseDir;

    private List<String> goals;

    private Properties executionRequestProperties;

    protected MavenParameters getMavenParametersForMavenCall() {
        MavenParameters params = new MavenParameters();

        params.setExecutionRequestProperties(executionRequestProperties);
        params.setBaseDir(new File(baseDir));
        params.setGoals(goals.toArray(new String[goals.size()]));

        return params;
    }

    protected MavenResult callMaven(MavenParameters params) {
        MavenConnector maven = new MavenConnector(params.getBaseDir(), params.getGoals(), params
                .getExecutionRequestProperties());
        log.info("Executing embedded maven with parameters: " + params);
        return maven.execute();
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public void setExecutionRequestProperties(Properties executionRequestProperties) {
        this.executionRequestProperties = executionRequestProperties;
    }

    public void setGoals(List<String> goals) {
        this.goals = goals;
    }

}
