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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.edbi.api.IndexCommit;
import org.openengsb.core.edbi.jdbc.api.NoSuchTableException;
import org.openengsb.core.edbi.jdbc.api.TableEngine;
import org.openengsb.core.edbi.jdbc.api.TableExistsException;
import org.openengsb.core.edbi.jdbc.api.TypeMap;
import org.openengsb.core.edbi.jdbc.operation.InsertOperation;
import org.openengsb.core.edbi.jdbc.sql.DataType;
import org.openengsb.core.edbi.jdbc.sql.Table;
import org.openengsb.core.edbi.models.SubTestModel;
import org.openengsb.core.edbi.models.TestModel;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AbstractTableEngineTest
 */
public abstract class AbstractTableEngineTest extends AbstractH2DatabaseTest {

    TableEngine engine;

    JdbcIndex<TestModel> testIndex;

    protected abstract TableEngine createEngine(DataSource dataSource, TypeMap typeMap);

    @Before
    public void setUpEngine() throws Exception {
        // Index
        testIndex = new JdbcIndex<>();
        testIndex.setName(TestModel.class.getCanonicalName());
        testIndex.setModelClass(TestModel.class);

        JdbcIndexField<String> testId = new JdbcIndexField<>(testIndex);
        testId.setName("testId");
        testId.setType(String.class);

        JdbcIndexField<Integer> testInteger = new JdbcIndexField<>(testIndex);
        testInteger.setName("testInteger");
        testInteger.setType(Integer.class);

        JdbcIndexField<SubTestModel> subModel = new JdbcIndexField<>(testIndex);
        subModel.setName("subModel");
        subModel.setType(SubTestModel.class);

        testIndex.setFields(Arrays.asList(testId, testInteger, subModel));

        // Type Map
        TypeMap typeMap = mock(TypeMap.class);
        when(typeMap.getType(String.class)).thenReturn(new DataType(Types.LONGNVARCHAR, "LONGVARCHAR"));
        when(typeMap.getType(UUID.class)).thenReturn(new DataType(Types.VARCHAR, "VARCHAR"));
        when(typeMap.getType(Integer.class)).thenReturn(new DataType(Types.INTEGER, "INTEGER"));
        when(typeMap.getType(Long.class)).thenReturn(new DataType(Types.BIGINT, "LONG"));
        when(typeMap.getType(Date.class)).thenReturn(new DataType(Types.TIMESTAMP, "TIMESTAMP"));

        engine = createEngine(getDataSource(), typeMap);
    }

    @After
    public void tearDown() throws Exception {
        engine = null;
    }

    @Test
    public void exists_onNonExistingIndex_returnsFalse() throws Exception {
        assertFalse(engine.exists(testIndex));
    }

    @Test
    public void exists_afterCreate_returnsTrue() throws Exception {
        engine.create(testIndex);

        assertTrue(engine.exists(testIndex));
    }

    @Test(expected = NoSuchTableException.class)
    public void get_onNonExistingIndex_throwsException() throws Exception {
        engine.get(testIndex);
    }

    @Test
    public void get_onExistingIndex_returnsCorrectTable() throws Exception {
        Table t = engine.create(testIndex);

        assertEquals(t, engine.get(testIndex));
    }

    @Test(expected = TableExistsException.class)
    public void create_afterCreate_throwsException() throws Exception {
        try {
            engine.create(testIndex);
        } catch (TableExistsException e) {
            fail("TableExistsException thrown on the wrong call");
        }

        engine.create(testIndex);
    }

    @Test(expected = NoSuchTableException.class)
    public void execute_insert_onNonExistingIndex_throwsException() throws Exception {
        IndexCommit commit = mock(IndexCommit.class);
        when(commit.getTimestamp()).thenReturn(new Date(42));

        engine.execute(new InsertOperation(commit, testIndex, new ArrayList<OpenEngSBModel>()));
    }
}
