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
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Transformer;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class ConnectorFile {

    private static final String PROPERTY_CONNECTOR = "connector";
    private static final String PROPERTY_DOMAIN = "domain";
    private static final String PROPERTY_SERVICE_ID = "id";

    private ImmutableMap<String, String> propertiesMap;
    private long cacheTimestamp = 0;
    private File connectorFile;

    public ConnectorFile(File connectorFile) {
        this.connectorFile = connectorFile;
    }

    public String getConnectorName() throws IOException {
        return readProperty(PROPERTY_CONNECTOR);
    }

    public String getDomainName() throws IOException {
        return readProperty(PROPERTY_DOMAIN);
    }

    public String getServiceId() throws IOException {
        return readProperty(PROPERTY_SERVICE_ID);
    }

    private String readProperty(String propertyId) throws IOException {
        updateProperties();
        return propertiesMap.get(propertyId);
    }

    private synchronized void updateProperties() {
        if (connectorFile.lastModified() == cacheTimestamp) {
            return;
        }
        Properties props = new Properties();
        try {
            FileReader reader = new FileReader(connectorFile);
            props.load(reader);
            reader.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        this.propertiesMap = Maps.fromProperties(props);
        cacheTimestamp = connectorFile.lastModified();
    }

    public Map<String, String> getAttributes() throws IOException {
        return getFilteredEntries("attribute");
    }

    private Map<String, String> getFilteredEntries(final String key) throws IOException {
        updateProperties();
        @SuppressWarnings("unchecked")
        Map<String, String> transformedMap = MapUtils.transformedMap(new HashMap<String, String>(), new Transformer() {
            @Override
            public Object transform(Object input) {
                return ((String) input).replaceFirst(key + ".", "");
            }
        }, null);
        Map<String, String> filterEntries =
            Maps.filterEntries(propertiesMap, new Predicate<Map.Entry<String, String>>() {
                @Override
                public boolean apply(Entry<String, String> input) {
                    return input.getKey().startsWith(key + ".");
                }
            });
        transformedMap.putAll(filterEntries);
        return Collections.unmodifiableMap(transformedMap);
    }

    public static Boolean isRootService(File connectorFile) {
        return isRootServiceDirectory(connectorFile.getParentFile());
    }

    private static Boolean isRootServiceDirectory(File directory) {
        return directory.isDirectory() && directory.getName().equals("etc");
    }

    public ConnectorConfiguration load() throws IOException {
        ConnectorId connectorId =
            new ConnectorId(getDomainName(), getConnectorName(), getServiceId());
        ConnectorDescription description = new ConnectorDescription(getAttributes());
        return new ConnectorConfiguration(connectorId, description);
    }

}
