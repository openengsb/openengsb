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

package org.openengsb.tooling.pluginsuite.openengsbplugin.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ToolsTest {

    @Test
    public void testExpectedInput() {
        assertEquals("Hello world", Tools.capitalizeFirst("hello world"));
        assertEquals("H", Tools.capitalizeFirst("h"));
    }

    @Test
    public void testUnexpectedInput() {
        assertNull(Tools.capitalizeFirst(null));
        assertEquals("", Tools.capitalizeFirst(""));
        assertEquals(" ", Tools.capitalizeFirst(" "));
        assertEquals("  ", Tools.capitalizeFirst("  "));
        assertEquals("?", Tools.capitalizeFirst("?"));
        assertEquals("/&%(#", Tools.capitalizeFirst("/&%(#"));
    }

}
