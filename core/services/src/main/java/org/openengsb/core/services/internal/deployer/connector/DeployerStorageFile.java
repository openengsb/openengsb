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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class DeployerStorageFile implements DeployerStorage {

    private static final String STORAGE_FILE_NAME = "connector";
    private static final String KEY_CONNECTOR = STORAGE_FILE_NAME;
    private static final String KEY_SERVICE_ID = "serviceId";
    File storageDir;

    public DeployerStorageFile(File storageDir) {
        this.storageDir = storageDir;
    }

    @Override
    public void put(File file, ConnectorConfiguration config) throws IOException {
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        getStorageFile().createNewFile();
        Properties props = loadProperties();
        props.setProperty(getServiceKeyOf(file), config.getServiceId());
        props.setProperty(getConnectorKeyOf(file), config.getConnectorType());
        storeProperties(props);
    }

    @Override
    public String getServiceId(File file) throws IOException {
        return readProperty(getServiceKeyOf(file));
    }

    @Override
    public String getConnectorType(File file) throws IOException {
        return readProperty(getConnectorKeyOf(file));
    }

    @Override
    public void remove(File file) throws IOException {
        if (!getStorageFile().exists()) {
            return;
        }

        Properties props = loadProperties();
        props.remove(getServiceKeyOf(file));
        props.remove(getConnectorKeyOf(file));
        storeProperties(props);
    }

    private String readProperty(String propertyKey) throws IOException {
        if (!getStorageFile().exists()) {
            return null;
        }
        Properties props = loadProperties();
        return props.getProperty(propertyKey);
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        FileInputStream fis;
        fis = new FileInputStream(getStorageFile());
        props.load(fis);
        fis.close();
        return props;
    }

    private File getStorageFile() {
        return new File(storageDir, STORAGE_FILE_NAME);
    }

    private void storeProperties(Properties props) throws IOException {
        FileOutputStream fos = new FileOutputStream(getStorageFile());
        props.store(fos, "");
        fos.close();
    }

    private String getServiceKeyOf(File file) {
        return String.format("%s.%s", file.getName(), KEY_SERVICE_ID);
    }

    private String getConnectorKeyOf(File file) {
        return String.format("%s.%s", file.getName(), KEY_CONNECTOR);
    }

}
