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
package org.openengsb.core.edbi.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.edbi.jdbc.api.TableEngine;
import org.openengsb.core.edbi.jdbc.sql.Table;

/**
 * DefaultSchemaMapperTest
 */
public class DefaultSchemaMapperTest {

    private TableEngine headTableEngine;
    private TableEngine historyTableEngine;

    private DefaultSchemaMapper mapper;

    @Before
    public void setUp() throws Exception {
        headTableEngine = mock(HeadTableEngine.class);
        historyTableEngine = mock(HistoryTableEngine.class);

        mapper = new DefaultSchemaMapper(headTableEngine, historyTableEngine);
    }

    @Test
    public void create_setsTableNamesCorreclty() throws Exception {
        JdbcIndex<?> index = new JdbcIndex<>();

        when(headTableEngine.create(index)).thenReturn(new Table("headTableName"));
        when(historyTableEngine.create(index)).thenReturn(new Table("historyTableName"));

        mapper.create(index);

        assertEquals("headTableName", index.getHeadTableName());
        assertEquals("historyTableName", index.getHistoryTableName());
    }

    @Test
    public void create_existingIndex_doesNothing() throws Exception {
        JdbcIndex<?> index = new JdbcIndex<>();

        when(headTableEngine.exists(index)).thenReturn(true);
        when(historyTableEngine.exists(index)).thenReturn(true);

        mapper.create(index);

        verify(headTableEngine, never()).create(index);
        verify(historyTableEngine, never()).create(index);
    }

    @Test
    public void exists_onNonExistingIndex_returnsFalse() throws Exception {
        JdbcIndex<?> index = new JdbcIndex<>();

        when(headTableEngine.exists(index)).thenReturn(false);
        when(historyTableEngine.exists(index)).thenReturn(false);

        assertFalse(mapper.exists(index));
    }

    @Test
    public void exists_onExistingIndex_returnsTrue() throws Exception {
        JdbcIndex<?> index = new JdbcIndex<>();

        when(headTableEngine.exists(index)).thenReturn(true);
        when(historyTableEngine.exists(index)).thenReturn(true);

        assertTrue(mapper.exists(index));
    }

    @Test(expected = IllegalStateException.class)
    public void exists_onInconsistentIndex_throwsException() throws Exception {
        JdbcIndex<?> index = new JdbcIndex<>();

        when(headTableEngine.exists(index)).thenReturn(true);
        when(historyTableEngine.exists(index)).thenReturn(false);

        mapper.exists(index);
    }

}
