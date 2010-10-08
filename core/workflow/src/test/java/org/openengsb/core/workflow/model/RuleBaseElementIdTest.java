/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.workflow.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RuleBaseElementIdTest {

    @Test
    public void testToStringWithDefaultConstructor() throws Exception {
        assertEquals(RuleBaseElementId.DEFAULT_RULE_PACKAGE, new RuleBaseElementId().toString());
    }

    @Test
    public void testToStringWithNameOnly() throws Exception {
        assertEquals(RuleBaseElementId.DEFAULT_RULE_PACKAGE + ".name", new RuleBaseElementId(RuleBaseElementType.Rule,
                "name").toString());
    }

    @Test
    public void testToStringWithNameAndPackage() throws Exception {
        assertEquals("my.package.name",
                new RuleBaseElementId(RuleBaseElementType.Rule, "my.package", "name").toString());
    }

}
