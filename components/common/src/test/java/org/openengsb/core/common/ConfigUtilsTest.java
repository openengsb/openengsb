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

package org.openengsb.core.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.openengsb.core.common.util.ConfigUtils;
import org.openengsb.core.common.util.MergeException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class ConfigUtilsTest {

    @Test
    public void testMakeNoChanges_shouldLeaveMapUnchanged() throws Exception {
        Map<String, String> original = ImmutableMap.of("foo", "bar");
        Map<String, String> oldMap = ImmutableMap.copyOf(original);
        Map<String, String> newMap = ImmutableMap.copyOf(oldMap);

        MapDifference<String, String> difference = Maps.difference(oldMap, newMap);
        Map<String, String> result = ConfigUtils.updateMap(original, difference);

        assertThat(result, is(original));
    }

    @Test
    public void testIntroduceNewKey_shouldAddNewEntry() throws Exception {
        Map<String, String> original = ImmutableMap.of("foo", "bar");
        Map<String, String> oldMap = ImmutableMap.copyOf(original);
        Map<String, String> newMap = ImmutableMap.of("foo", "bar", "test", "42");

        MapDifference<String, String> difference = Maps.difference(oldMap, newMap);
        Map<String, String> result = ConfigUtils.updateMap(original, difference);

        assertThat(result, is(newMap));
    }

    @Test
    public void testRemoveKeyFromNewMap_shouldRemoveEntry() throws Exception {
        Map<String, String> original = ImmutableMap.of("foo", "bar", "test", "42");
        Map<String, String> oldMap = ImmutableMap.copyOf(original);
        Map<String, String> newMap = ImmutableMap.of("foo", "bar");

        MapDifference<String, String> difference = Maps.difference(oldMap, newMap);
        Map<String, String> result = ConfigUtils.updateMap(original, difference);

        assertThat(result, is(newMap));
    }

    @Test
    public void testValueChange_shouldChangeValue() throws Exception {
        Map<String, String> original = ImmutableMap.of("foo", "bar", "test", "42");
        Map<String, String> oldMap = ImmutableMap.copyOf(original);
        Map<String, String> newMap = ImmutableMap.of("foo", "bar", "test", "21");

        MapDifference<String, String> difference = Maps.difference(oldMap, newMap);
        Map<String, String> result = ConfigUtils.updateMap(original, difference);

        assertThat(result, is(newMap));
    }

    @Test(expected = MergeException.class)
    public void testChangeValueNotInOriginal_shouldThrowException() throws Exception {
        Map<String, String> original = ImmutableMap.of("foo", "bar");
        Map<String, String> oldMap = ImmutableMap.of("foo", "bar", "test", "42");
        Map<String, String> newMap = ImmutableMap.of("foo", "bar", "test", "21");

        MapDifference<String, String> difference = Maps.difference(oldMap, newMap);
        ConfigUtils.updateMap(original, difference);
    }

    @Test(expected = MergeException.class)
    public void testIntroduceValueAlreadyInOriginal_shouldThrowException() throws Exception {
        Map<String, String> original = ImmutableMap.of("foo", "bar", "test", "21");
        Map<String, String> oldMap = ImmutableMap.of("foo", "bar");
        Map<String, String> newMap = ImmutableMap.of("foo", "bar", "test", "42");

        MapDifference<String, String> difference = Maps.difference(oldMap, newMap);
        ConfigUtils.updateMap(original, difference);
    }

    @Test
    public void testIntroduceValueAlreadySameInOriginal_shouldLeaveUnchanged() throws Exception {
        Map<String, String> original = ImmutableMap.of("foo", "bar", "test", "42");
        Map<String, String> oldMap = ImmutableMap.of("foo", "bar");
        Map<String, String> newMap = ImmutableMap.of("foo", "bar", "test", "42");

        MapDifference<String, String> difference = Maps.difference(oldMap, newMap);
        Map<String, String> result = ConfigUtils.updateMap(original, difference);

        assertThat(result, is(original));
    }

    @Test
    public void testChangeValueAlreadySameInOriginal_shouldLeaveMapUnchanged() throws Exception {
        Map<String, String> original = ImmutableMap.of("foo", "bar", "test", "21");
        Map<String, String> oldMap = ImmutableMap.of("foo", "bar", "test", "42");
        Map<String, String> newMap = ImmutableMap.of("foo", "bar", "test", "21");

        MapDifference<String, String> difference = Maps.difference(oldMap, newMap);
        Map<String, String> result = ConfigUtils.updateMap(original, difference);

        assertThat(result, is(original));
    }

    @Test
    public void testRemoveValueAlreadyRemovedFromOriginal_shouldLeaveOriginalUnchanged() throws Exception {
        Map<String, String> original = ImmutableMap.of("foo", "bar");
        Map<String, String> oldMap = ImmutableMap.of("foo", "bar", "test", "21");
        Map<String, String> newMap = ImmutableMap.of("foo", "bar");

        MapDifference<String, String> difference = Maps.difference(oldMap, newMap);
        Map<String, String> result = ConfigUtils.updateMap(original, difference);

        assertThat(result, is(original));
    }

    @Test
    public void testRemoveValueWithOtherOriginalValue_shouldThrowException() throws Exception {
        Map<String, String> original = ImmutableMap.of("foo", "bar", "test", "42");
        Map<String, String> oldMap = ImmutableMap.of("foo", "bar", "test", "21");
        Map<String, String> newMap = ImmutableMap.of("foo", "bar");

        MapDifference<String, String> difference = Maps.difference(oldMap, newMap);
        ConfigUtils.updateMap(original, difference);
    }

    @Test
    public void testChangeKeyName_shouldChangeMap() throws Exception {
        Map<String, String> original = ImmutableMap.of("foo", "bar", "test", "42");
        Map<String, String> oldMap = ImmutableMap.of("foo", "bar", "test", "42");
        Map<String, String> newMap = ImmutableMap.of("foo", "bar", "test1", "42");

        MapDifference<String, String> difference = Maps.difference(oldMap, newMap);
        Map<String, String> result = ConfigUtils.updateMap(original, difference);

        assertThat(result, is(newMap));
    }

    @Test
    public void testUpdateMulipleEntries_shouldWork() throws Exception {
        Map<String, String> original = ImmutableMap.of("foo", "bar", "test", "42", "other", "bleh");
        Map<String, String> oldMap = ImmutableMap.of("foo", "bar", "test", "42");
        Map<String, String> newMap = ImmutableMap.of("foo", "bar2", "test", "41", "x", "y");

        MapDifference<String, String> difference = Maps.difference(oldMap, newMap);
        Map<String, String> result = ConfigUtils.updateMap(original, difference);

        assertThat(result,
            is((Map<String, String>) ImmutableMap.of("foo", "bar2", "test", "41", "x", "y", "other", "bleh")));
    }
}
