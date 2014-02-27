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
package org.openengsb.core.edbi.jdbc.names;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.edbi.api.Index;

/**
 * SQLIndexNameTranslatorTest
 */
public class SQLIndexNameTranslatorTest {

    SQLIndexNameTranslator translator;

    @Before
    public void setUp() throws Exception {
        translator = new SQLIndexNameTranslator();
    }

    @After
    public void tearDown() throws Exception {
        translator = null;
    }

    @Test
    public void translate_translatesCorrectly() throws Exception {
        Index<?> index = mock(Index.class);

        when(index.getName()).thenReturn("The quick brown fox jumps over the lazy dog");

        assertEquals("2FD4E1C67A", translator.translate(index));
    }

    @Test(expected = IllegalArgumentException.class)
    public void translate_null_throwsException() throws Exception {
        translator.translate(null);
    }
}
