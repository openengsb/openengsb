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
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.common.util.DictionaryAsMap;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class ConnectorFile {

    private static final String PROPERTY = "property";
    private static final String ATTRIBUTE = "attribute";
    private static final String LIST_DELIMITER = ",";

    private ConnectorConfiguration config;

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
        config = parse(connectorFile);
    }

    private ConnectorConfiguration parse(File file) {
        String name = FilenameUtils.removeExtension(file.getName());
        ConnectorId id = ConnectorId.fromFullId(name);
        ImmutableMap<String, String> propertyMap = readProperties(file);
        Map<String, String> serviceProperties = getFilteredEntries(propertyMap, PROPERTY);
        Map<String, Object> transformedProperties =
            Maps.transformValues(serviceProperties, new Function<String, Object>() {
                @Override
                public Object apply(String input) {
                    if (input.contains(LIST_DELIMITER)) {
                        return input.split(LIST_DELIMITER);
                    }
                    return input;
                }
            });
        Map<String, String> attributes = getFilteredEntries(propertyMap, ATTRIBUTE);
        ConnectorDescription connectorDescription =
            new ConnectorDescription(attributes, new Hashtable<String, Object>(transformedProperties));
        return new ConnectorConfiguration(id, connectorDescription);
    }

    public ChangeSet update(File file) {
        ConnectorConfiguration newConfig = parse(file);

        ConnectorDescription oldDescription = config.getContent();
        Map<String, String> oldAttributes = oldDescription.getAttributes();
        MapDifference<String, String> changedAttributes =
            Maps.difference(oldAttributes, newConfig.getContent().getAttributes());

        Map<String, Object> oldProperties = DictionaryAsMap.wrap(oldDescription.getProperties());
        MapDifference<String, Object> changedProperties =
            Maps.difference(oldProperties, DictionaryAsMap.wrap(newConfig.getContent().getProperties()));
        return new ChangeSet(changedAttributes, changedProperties);
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

    private Map<String, String> getFilteredEntries(Map<String, String> propertyMap, final String prefix) {
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
        return new HashMap<String, String>(transformedMap);
    }

    public static Boolean isRootService(File connectorFile) {
        return isRootServiceDirectory(connectorFile.getParentFile());
    }

    private static Boolean isRootServiceDirectory(File directory) {
        return directory.isDirectory() && directory.getName().equals("etc");
    }

    public ConnectorConfiguration getConfig() {
        return config;
    }

}
