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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openengsb.core.api.model.ConnectorId;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class ConnectorFile {

    private static final String PROPERTY = "property";
    private static final String ATTRIBUTE = "attribute";
    private static final String LIST_DELIMITER = ",";

    private ConnectorId connectorId;
    private ImmutableMap<String, String> attributes;
    private ImmutableMap<String, Object> properties;

    public static class ChangeSet {
        private MapDifference<String, String> changedAttributes;
        private MapDifference<String, Object> changedProperties;

        public ChangeSet(MapDifference<String, String> changedAttributes,
                MapDifference<String, Object> changedProperties) {
            super();
            this.changedAttributes = changedAttributes;
            this.changedProperties = changedProperties;
        }

        public MapDifference<String, String> getChangedAttributes() {
            return changedAttributes;
        }

        public MapDifference<String, Object> getChangedProperties() {
            return changedProperties;
        }
    }

    public ConnectorFile(File connectorFile) {
        connectorId = ConnectorId.fromFullId(FilenameUtils.removeExtension(connectorFile.getName()));
        update(connectorFile);
    }

    private ImmutableMap<String, String> getAttributesFromMap(ImmutableMap<String, String> propertyMap) {
        return getFilteredEntries(propertyMap, ATTRIBUTE);
    }

    private ImmutableMap<String, Object> getPropertiesFromMap(Map<String, String> propertyMap) {
        ImmutableMap<String, String> serviceProperties = getFilteredEntries(propertyMap, PROPERTY);
        Map<String, Object> transformedProperties =
            Maps.transformValues(serviceProperties, new Function<String, Object>() {
                @Override
                public Object apply(String input) {
                    if (input.contains(LIST_DELIMITER)) {
                        return splitBySeparatorAndTrimItems(input);
                    }
                    return input;
                }

                private Object splitBySeparatorAndTrimItems(String input) {
                    String[] split = input.split(LIST_DELIMITER);
                    for (int i = 0; i < split.length; i++) {
                        split[i] = StringUtils.trim(split[i]);
                    }
                    return split;
                }
            });
        return ImmutableMap.copyOf(transformedProperties);
    }

    public ChangeSet getChanges(File file) {
        ImmutableMap<String, String> newPropertyMap = readProperties(file);

        MapDifference<String, String> changedAttributes =
            Maps.difference(attributes, getAttributesFromMap(newPropertyMap));
        MapDifference<String, Object> changedProperties =
            Maps.difference(properties, getPropertiesFromMap(newPropertyMap));

        return new ChangeSet(changedAttributes, changedProperties);
    }

    public void update(File file) {
        ImmutableMap<String, String> newPropertyMap = readProperties(file);
        attributes = getAttributesFromMap(newPropertyMap);
        properties = getPropertiesFromMap(newPropertyMap);
    }

    private ImmutableMap<String, String> readProperties(File file) {
        Properties props = new Properties();
        FileReader reader;
        try {
            reader = new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
        try {
            props.load(reader);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return Maps.fromProperties(props);
    }

    private ImmutableMap<String, String> getFilteredEntries(Map<String, String> propertyMap, final String prefix) {
        @SuppressWarnings("unchecked")
        Map<String, String> transformedMap = MapUtils.transformedMap(new HashMap<String, String>(), new Transformer() {
            @Override
            public Object transform(Object input) {
                return ((String) input).replaceFirst(prefix + ".", "");
            }
        }, null);
        Map<String, String> filterEntries =
            Maps.filterEntries(propertyMap, new Predicate<Map.Entry<String, String>>() {
                @Override
                public boolean apply(Entry<String, String> input) {
                    return input.getKey().startsWith(prefix + ".");
                }
            });
        transformedMap.putAll(filterEntries);
        return ImmutableMap.copyOf(transformedMap);
    }

    public Properties toProperties() {
        Properties result = new Properties();
        for (Entry<String, String> entry : attributes.entrySet()) {
            result.put(ATTRIBUTE + "." + entry.getKey(), entry.getValue());
        }
        for (Entry<String, Object> entry : properties.entrySet()) {
            String key = PROPERTY + "." + entry.getKey();
            Object value = entry.getValue();
            if (value.getClass().isArray()) {
                result.put(key, StringUtils.join((Object[]) value));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    public static Boolean isRootService(File connectorFile) {
        return isRootServiceDirectory(connectorFile.getParentFile());
    }

    private static Boolean isRootServiceDirectory(File directory) {
        return directory.isDirectory() && directory.getName().equals("etc");
    }

    public ConnectorId getConnectorId() {
        return connectorId;
    }

    public ImmutableMap<String, String> getAttributes() {
        return attributes;
    }

    public ImmutableMap<String, Object> getProperties() {
        return properties;
    }

}
