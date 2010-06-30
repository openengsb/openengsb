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
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.contextcommon.ContextHelper;

public class AbstractMavenDomainImpl {

    private Log log = LogFactory.getLog(getClass());

    private ContextHelper contextHelper;

    public AbstractMavenDomainImpl(ContextHelper contextHelper) {
        this.contextHelper = contextHelper;
    }

    protected MavenParameters getMavenParametersForMavenCall(String pathPrefix) {
        MavenParameters params = new MavenParameters();

        Properties executionRequestProperties = getPropertiesFromContext(pathPrefix
                + "/config/executionRequestProperties");
        params.setExecutionRequestProperties(executionRequestProperties);

        String baseDir = contextHelper.getValue(pathPrefix + "/config/baseDirectory");
        params.setBaseDir(new File(baseDir));

        params.setGoals(getValuesFromContext(pathPrefix + "/config/goals"));

        return params;
    }

    protected MavenResult callMaven(MavenParameters params) {
        MavenConnector maven = new MavenConnector(params.getBaseDir(), params.getGoals(), params
                .getExecutionRequestProperties());
        log.info("Executing embedded maven with parameters: " + params);
        return maven.execute();
    }

    protected Properties getPropertiesFromContext(String path) {
        Map<String, String> props = contextHelper.getAllValues(path);

        Properties properties = new Properties();
        for (Entry<String, String> entry : props.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return properties;
    }

    protected String[] getValuesFromContext(String path) {
        String values = contextHelper.getValue(path);
        return values.split(",");
    }

}
