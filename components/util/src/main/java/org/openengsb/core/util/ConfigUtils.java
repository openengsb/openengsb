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

package org.openengsb.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;

public final class ConfigUtils {

    /**
     * apply the differences to the given map (3-way-merge)
     *
     * @throws MergeException if the changes cannot be applied because of merge-conflicts
     */
    public static <K, V> Map<K, V> updateMap(final Map<K, V> original, MapDifference<K, V> diff) throws MergeException {
        Map<K, V> result = new HashMap<K, V>(original);
        if (diff.areEqual()) {
            return result;
        }
        for (Entry<K, V> entry : diff.entriesOnlyOnLeft().entrySet()) {
            V originalValue = original.get(entry.getKey());
            if (ObjectUtils.equals(originalValue, entry.getValue())) {
                result.remove(entry.getKey());
            }
        }
        for (Entry<K, V> entry : diff.entriesOnlyOnRight().entrySet()) {
            K key = entry.getKey();
            if (original.containsKey(key)) {
                if (ObjectUtils.notEqual(original.get(key), entry.getValue())) {
                    throw new MergeException(String.format(
                        "tried to introduce a new value, but it was already there: %s (%s,%s)",
                        key, original.get(key), entry.getValue()));
                }
            }
            result.put(entry.getKey(), entry.getValue());
        }
        for (Entry<K, ValueDifference<V>> entry : diff.entriesDiffering().entrySet()) {
            K key = entry.getKey();
            V originalValue = original.get(entry.getKey());
            if (ObjectUtils.equals(originalValue, entry.getValue().leftValue())) {
                // Map changed in diff only
                result.put(key, entry.getValue().rightValue());
            } else if (ObjectUtils.equals(originalValue, entry.getValue().rightValue())) {
                // Diff would change value to value already in original Map
                result.put(key, originalValue);
            } else {
                // Merge conflict, got 3 different Values
                String errorMessage =
                    String
                        .format(
                            "Changes could not be applied, because original value differes from left-side of the"
                                    + "MapDifference: %s (%s,%s)", entry.getKey(), original.get(entry.getKey()),
                            entry.getValue());
                throw new MergeException(errorMessage);
            }
        }
        return result;
    }

    private ConfigUtils() {
    }
}
