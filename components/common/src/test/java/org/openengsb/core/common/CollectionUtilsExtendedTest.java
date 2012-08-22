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

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.openengsb.core.common.util.CollectionUtilsExtended;

public class CollectionUtilsExtendedTest {

    @Test
    public void testFilterByClass_shouldFilterArrayByInteger() throws Exception {
        List<Object> example = Arrays.asList(new Object(), 1, 2, 3, "sadf");
        Collection<Integer> filtered =
            CollectionUtilsExtended.filterCollectionByClass(example, Integer.class);
        assertThat(filtered, hasItems(1, 2, 3));
    }
}
