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

package org.openengsb.core.ekb.persistence.persist.edb.internal;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.reflect.FieldUtils;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

/**
 * The ModelDiff class was introduced for the easier calculation and handling of the differences of two models.
 */
public final class ModelDiff {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelDiff.class);
    private OpenEngSBModel before;
    private OpenEngSBModel after;
    private Map<Field, ModelDiffEntry> differences;

    /**
     * Creates an instance of the ModelDiff class based on the both models which are passed to the function. It
     * instantiate the class and calculates the differences of this two models.
     */
    public static ModelDiff createModelDiff(OpenEngSBModel before, OpenEngSBModel after) {
        ModelDiff diff = new ModelDiff(before, after);
        calculateDifferences(diff);
        return diff;
    }

    private ModelDiff(OpenEngSBModel before, OpenEngSBModel after) {
        this.before = before;
        this.after = after;
        differences = new HashMap<Field, ModelDiffEntry>();
    }

    /**
     * Calculates the real differences between the two models of the given ModelDiff and saves them to the differences
     * field of the ModelDiff class.
     */
    private static void calculateDifferences(ModelDiff diff) {
        Class<?> modelClass = diff.getBefore().getClass();
        for (Field field : modelClass.getDeclaredFields()) {
            try {
                Object before = FieldUtils.readField(field, diff.getBefore(), true);
                Object after = FieldUtils.readField(field, diff.getAfter(), true);
                if (!Objects.equal(before, after)) {
                    diff.addDifference(field, before, after);
                }
            } catch (IllegalAccessException e) {
                LOGGER.warn("Skipped field '{}' because of illegal access to it", field.getName(), e);
            }
        }
    }

    /**
     * Adds a difference to the list of differences of this ModelDiff instance.
     */
    public void addDifference(Field field, Object before, Object after) {
        ModelDiffEntry entry = new ModelDiffEntry();
        entry.setBefore(before);
        entry.setAfter(after);
        entry.setField(field);
        differences.put(field, entry);
    }

    /**
     * Returns true if one of the differences of this ModelDiff instance is an OpenEngSBForeignKey. Returns false
     * otherwise.
     */
    public boolean isForeignKeyChanged() {
        return CollectionUtils.exists(differences.values(), new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return ((ModelDiffEntry) object).isForeignKey();
            }
        });
    }

    /**
     * Returns true if one of the differences of this ModelDiff instance is not an OpenEngSBForeignKey. Returns false
     * otherwise.
     */
    public boolean isValueChanged() {
        return CollectionUtils.exists(differences.values(), new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return !((ModelDiffEntry) object).isForeignKey();
            }
        });
    }

    public OpenEngSBModel getBefore() {
        return before;
    }

    public OpenEngSBModel getAfter() {
        return after;
    }

    public Map<Field, ModelDiffEntry> getDifferences() {
        return differences;
    }
}
