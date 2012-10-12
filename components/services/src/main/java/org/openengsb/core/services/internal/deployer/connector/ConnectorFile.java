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
import org.apache.commons.lang.math.NumberUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class ConnectorFile {

    private static final String PROPERTY = "property";
    private static final String ATTRIBUTE = "attribute";
    private static final String LIST_DELIMITER = ",";

    private String name;
    private String domainType;
    private String connectorType;
    private ImmutableMap<String, String> attributes;
    private ImmutableMap<String, Object> properties;

    public static class ChangeSet {
        private String domainType;
        private String connectorType;
        private MapDifference<String, String> changedAttributes;
        private MapDifference<String, Object> changedProperties;

        public ChangeSet(String domainType, String connectorType, MapDifference<String, String> changedAttributes,
                MapDifference<String, Object> changedProperties) {
            this.domainType = domainType;
            this.connectorType = connectorType;
            this.changedAttributes = changedAttributes;
            this.changedProperties = changedProperties;
        }

        public MapDifference<String, String> getChangedAttributes() {
            return changedAttributes;
        }

        public MapDifference<String, Object> getChangedProperties() {
            return changedProperties;
        }

        public String getDomainType() {
            return domainType;
        }

        public String getConnectorType() {
            return connectorType;
        }

    }

    public ConnectorFile(File connectorFile) {
        name = FilenameUtils.removeExtension(connectorFile.getName());
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
                    if (NumberUtils.isNumber(input)) {
                        return NumberUtils.createNumber(input);
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

        return new ChangeSet(newPropertyMap.get("domainType"), newPropertyMap.get("connectorType"),
            changedAttributes, changedProperties);
    }

    public void update(File file) {
        ImmutableMap<String, String> newPropertyMap = readProperties(file);
        domainType = newPropertyMap.get("domainType");
        connectorType = newPropertyMap.get("connectorType");
        attributes = getAttributesFromMap(newPropertyMap);
        properties = getPropertiesFromMap(newPropertyMap);
    }

    private ImmutableMap<String, String> readProperties(File file) {
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            Properties props = new Properties();
            props.load(reader);
            Map<String, String> map = Maps.fromProperties(props);
            Map<String, String> transformedMap = Maps.transformValues(map, new TrimFunction<String, String>());
            return ImmutableMap.copyOf(transformedMap);

        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
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
        result.put("domainType", domainType);
        result.put("connectorType", connectorType);
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

    public String getName() {
        return name;
    }

    public String getDomainType() {
        return domainType;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public ImmutableMap<String, String> getAttributes() {
        return attributes;
    }

    public ImmutableMap<String, Object> getProperties() {
        return properties;
    }

    private class TrimFunction<F, T> implements Function<F, T> {
        @SuppressWarnings("unchecked")
        @Override
        public Object apply(Object input) {
            return (input == null) ? null : ((String) input).trim();
        }
    }
}
