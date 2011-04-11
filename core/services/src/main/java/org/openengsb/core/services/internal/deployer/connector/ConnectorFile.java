/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.services.internal.deployer.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.Constants;

public class ConnectorFile {

    private static final String DEFAULT_ROOT_SERVICE_RANKING = "-1";
    private static final String PROPERTY_CONNECTOR = "connector";
    private static final String PROPERTY_SERVICE_ID = "id";

    private File connectorFile;

    public ConnectorFile(File connectorFile) {
        this.connectorFile = connectorFile;
    }

    public String getConnectorName() throws IOException {
        return readProperty(PROPERTY_CONNECTOR);
    }

    public String getServiceId() throws IOException {
        return readProperty(PROPERTY_SERVICE_ID);
    }

    private String readProperty(String propertyId) throws IOException {
        Properties props = loadProperties();
        return props.getProperty(propertyId);
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        FileInputStream inputStream = new FileInputStream(connectorFile.getAbsoluteFile());
        props.load(inputStream);
        inputStream.close();
        return props;
    }

    public Map<String, String> getAttributes() throws IOException {
        Properties props = loadProperties();
        Map<String, String> instanceAttributes = createMapFrom(props);
        instanceAttributes.remove(PROPERTY_CONNECTOR);
        if (!instanceAttributes.containsKey(Constants.SERVICE_RANKING) && isRootService(connectorFile)) {
            instanceAttributes.put(Constants.SERVICE_RANKING, DEFAULT_ROOT_SERVICE_RANKING);
        }
        return instanceAttributes;
    }

    private static Map<String, String> createMapFrom(Properties props) {
        Map<String, String> instanceAttributes = new HashMap<String, String>();
        for (String propertyName : props.stringPropertyNames()) {
            instanceAttributes.put(propertyName, props.getProperty(propertyName));
        }
        return instanceAttributes;
    }

    public static Boolean isRootService(File connectorFile) {
        return isRootServiceDirectory(connectorFile.getParentFile());
    }

    private static Boolean isRootServiceDirectory(File directory) {
        return directory.isDirectory() && directory.getName().equals("etc");
    }

}
