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

package org.openengsb.core.deployer.connector.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConnectorFile {
    
    private static final String PROPERTY_CONNECTOR = "connector";
    private static final String PROPERTY_SERVICE_ID = "id";

    private File connectorFile;

    public ConnectorFile(File connectorFile) {
        this.connectorFile = connectorFile;
    }
    
    public String getConnectorName() throws IOException, FileNotFoundException {
        return readProperty(PROPERTY_CONNECTOR);
    }

    public String getServiceId() throws IOException, FileNotFoundException {
        return readProperty(PROPERTY_SERVICE_ID);
    }

    private String readProperty(String propertyId) throws IOException, FileNotFoundException {
        Properties props = loadProperties();
        return props.getProperty(propertyId);
    }

    private Properties loadProperties() throws IOException, FileNotFoundException {
        Properties props = new Properties();
        FileInputStream inputStream = new FileInputStream(connectorFile.getAbsoluteFile());
        props.load(inputStream);
        inputStream.close();
        return props;
    }

    public Map<String, String> getAttributes() throws IOException, FileNotFoundException {
        Properties props = loadProperties();
        Map<String, String> instanceAttributes = new HashMap<String, String>();
        for (String propertyName : props.stringPropertyNames()) {
            instanceAttributes.put(propertyName, props.getProperty(propertyName));
        }
        instanceAttributes.remove(PROPERTY_CONNECTOR);
        return instanceAttributes;
    }

}
