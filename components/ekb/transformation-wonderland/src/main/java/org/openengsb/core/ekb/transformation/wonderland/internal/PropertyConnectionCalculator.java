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

package org.openengsb.core.ekb.transformation.wonderland.internal;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.core.ekb.api.transformation.TransformationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PropertyConnectionCalculator class contains the logic to calculate the property connections based on
 * transformation descriptions.
 */
public class PropertyConnectionCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyConnectionCalculator.class);
    private ModelRegistry registry;

    public PropertyConnectionCalculator(ModelRegistry registry) {
        this.registry = registry;
    }

    /**
     * Calculates the connections of properties of the source and target model of a transformation description. Returns
     * a map where the keys are the properties of the source model and the values are a set of property names which are
     * influenced if the key property is changed.
     */
    public Map<String, Set<String>> getPropertyConnections(TransformationDescription description) {
        Map<String, Set<String>> propertyMap = getSourceProperties(description.getSourceModel());
        fillPropertyMap(propertyMap, description);
        resolveTemporaryProperties(propertyMap);
        deleteTemporaryProperties(propertyMap);
        return propertyMap;
    }

    /**
     * Returns a map where the keys are the properties of the model described by the given model description. The values
     * are empty sets.
     */
    private Map<String, Set<String>> getSourceProperties(ModelDescription description) {
        Map<String, Set<String>> result = new LinkedHashMap<String, Set<String>>();
        try {
            Class<?> sourceModel = registry.loadModel(description);
            while (sourceModel != null && !sourceModel.equals(Object.class)) {
                for (Field field : sourceModel.getDeclaredFields()) {
                    result.put(field.getName(), new HashSet<String>());
                }
                sourceModel = sourceModel.getSuperclass();
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error("Unable to load model {}", description);
            return result;
        }
        return result;
    }

    /**
     * Fills the given map based on the transformation steps given in the transformation description. It analyzes the
     * transformation step and adds the target of the transformation step to the set of properties which are influenced
     * by the source property.
     */
    private void fillPropertyMap(Map<String, Set<String>> map, TransformationDescription description) {
        for (TransformationStep step : description.getTransformingSteps()) {
            if (step.getSourceFields() == null) {
                LOGGER.debug("Step {} is ignored since no source properties are defined");
                continue;
            }
            String targetField = step.getTargetField();
            if (!map.containsKey(targetField) && isTemporaryProperty(targetField)) {
                LOGGER.debug("Add new property entry for field {}", targetField);
                map.put(targetField, new HashSet<String>());
            }
            for (String sourceField : step.getSourceFields()) {
                Set<String> targets = map.get(sourceField);
                if (targets != null) {
                    targets.add(targetField);
                } else {
                    LOGGER.error("Accessed property with the name {} which isn't existing", sourceField);
                }
            }
        }
    }

    /**
     * Resolves the temporary properties of the given property map. It replaces the temporary properties in the values
     * of the given map with the values of the temporary property it replaces. This procedure is done until there are no
     * more temporary fields present in the values of the map.
     */
    private void resolveTemporaryProperties(Map<String, Set<String>> map) {
        boolean temporaryPresent = false;
        do {
            temporaryPresent = false;
            for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
                Set<String> newProperties = new HashSet<String>();
                Iterator<String> properties = entry.getValue().iterator();
                while (properties.hasNext()) {
                    String property = properties.next();
                    if (isTemporaryProperty(property)) {
                        LOGGER.debug("Resolve temporary field {} for property {}", entry.getKey(), property);
                        temporaryPresent = true;
                        newProperties.addAll(map.get(property));
                        properties.remove();
                    }
                }
                entry.getValue().addAll(newProperties);
            }
        } while (temporaryPresent);
    }

    /**
     * Iterates over the map entries and removes all temporary properties so that a clean map can be returned to the
     * user which is interested in the property connections.
     */
    private void deleteTemporaryProperties(Map<String, Set<String>> map) {
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (isTemporaryProperty(key)) {
                LOGGER.debug("Delete temporary field {} from the connection result", key);
                iterator.remove();
            }
        }
    }

    /**
     * Returns true if the given property name is a temporary property, returns false if not.
     */
    private boolean isTemporaryProperty(String propertyName) {
        return propertyName.startsWith("temp.");
    }
}
