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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexCommit;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.api.IndexFieldNameTranslator;
import org.openengsb.core.edbi.api.IndexNameTranslator;
import org.openengsb.core.edbi.jdbc.api.NoSuchTableException;
import org.openengsb.core.edbi.jdbc.api.TableExistsException;
import org.openengsb.core.edbi.jdbc.api.TypeMap;
import org.openengsb.core.edbi.jdbc.operation.InsertOperation;
import org.openengsb.core.edbi.jdbc.sql.DataType;
import org.openengsb.core.edbi.jdbc.sql.Table;
import org.openengsb.core.edbi.models.CompositeTestModel;
import org.openengsb.core.edbi.models.TestModel;

public class HeadTableEngineTest extends AbstractH2DatabaseTest {

    HeadTableEngine engine;

    JdbcIndex<TestModel> testIndex;

    @Before
    public void setUp() throws Exception {
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

        JdbcIndexField<CompositeTestModel> compositeModel = new JdbcIndexField<>(testIndex);
        compositeModel.setName("compositeModel");
        compositeModel.setType(CompositeTestModel.class);

        testIndex.setFields(Arrays.asList(testId, testInteger, compositeModel));

        // Type Map
        TypeMap typeMap = mock(TypeMap.class);
        when(typeMap.getType(String.class)).thenReturn(new DataType(Types.LONGNVARCHAR, "LONGVARCHAR"));
        when(typeMap.getType(Integer.class)).thenReturn(new DataType(Types.INTEGER, "INTEGER"));
        when(typeMap.getType(Long.class)).thenReturn(new DataType(Types.BIGINT, "LONG"));
        when(typeMap.getType(Date.class)).thenReturn(new DataType(Types.TIMESTAMP, "TIMESTAMP"));

        // Translators (mockito 1.8.5 has a bug with overloading generics)
        IndexNameTranslator indexNameTranslatorStub = new IndexNameTranslator() {
            @Override
            public String translate(Index<?> index) {
                return (index == testIndex) ? "HEAD_TABLE" : null;
            }
        };

        IndexFieldNameTranslator fieldNameTranslatorStub = new IndexFieldNameTranslator() {
            @Override
            public String translate(IndexField<?> field) {
                return field.getName().toUpperCase();
            }
        };

        // Engine
        engine = new HeadTableEngine(getDataSource(), typeMap, indexNameTranslatorStub, fieldNameTranslatorStub);
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

    @Test
    public void create_works() throws Exception {
        engine.create(testIndex);

        try (ResultSet rs = getDataSource().getConnection().createStatement().executeQuery("SELECT * FROM HEAD_TABLE")) {
            ResultSetMetaData metaData = rs.getMetaData();

            assertEquals(4, metaData.getColumnCount());

            assertEquals("REV_CREATED", metaData.getColumnName(1));
            assertEquals("TESTID", metaData.getColumnName(2));
            assertEquals("TESTINTEGER", metaData.getColumnName(3));
            assertEquals("COMPOSITEMODEL", metaData.getColumnName(4));
        }

        assertEquals("HEAD_TABLE", testIndex.getHeadTableName());

        List<IndexField<?>> fields = testIndex.getFields();
        assertEquals("TESTID", fields.get(0).getMappedName());
        assertEquals("TESTINTEGER", fields.get(1).getMappedName());
        assertEquals("COMPOSITEMODEL", fields.get(2).getMappedName());
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

    @Test
    public void execute_insert_createsRecordsCorrectly() throws Exception {
        engine.create(testIndex);

        IndexCommit commit = mock(IndexCommit.class);
        when(commit.getTimestamp()).thenReturn(new Date(42));
        // TODO: mock entire commit

        List<OpenEngSBModel> models = new ArrayList<>();

        models.add(new TestModel("A", 42));
        models.add(new TestModel("B", -42));

        InsertOperation operation = new InsertOperation(commit, testIndex, models);

        engine.execute(operation);

        try (ResultSet rs = getDataSource().getConnection().createStatement().executeQuery("SELECT * FROM HEAD_TABLE")) {
            assertTrue(rs.next());
            assertEquals("A", rs.getString("TESTID"));
            assertEquals(42, rs.getInt("TESTINTEGER"));
            assertEquals(new Date(42), rs.getTimestamp("REV_CREATED"));

            assertTrue(rs.next());
            assertEquals("B", rs.getString("TESTID"));
            assertEquals(-42, rs.getInt("TESTINTEGER"));
            assertEquals(new Date(42), rs.getTimestamp("REV_CREATED"));

            assertFalse(rs.next());
        }
    }

    @Test(expected = NoSuchTableException.class)
    public void execute_insert_onNonExistingIndex_throwsException() throws Exception {
        IndexCommit commit = mock(IndexCommit.class);
        when(commit.getTimestamp()).thenReturn(new Date(42));

        engine.execute(new InsertOperation(commit, testIndex, new ArrayList<OpenEngSBModel>()));
    }

}
