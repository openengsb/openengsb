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
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexCommit;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.api.IndexFieldNameTranslator;
import org.openengsb.core.edbi.api.IndexNameTranslator;
import org.openengsb.core.edbi.jdbc.api.TableEngine;
import org.openengsb.core.edbi.jdbc.api.TypeMap;
import org.openengsb.core.edbi.jdbc.operation.DeleteOperation;
import org.openengsb.core.edbi.jdbc.operation.InsertOperation;
import org.openengsb.core.edbi.jdbc.operation.UpdateOperation;
import org.openengsb.core.edbi.models.TestModel;

public class HistoryTableEngineTest extends AbstractTableEngineTest {

    @Before
    public void setUp() throws Exception {
        List<IndexField<?>> fields = testIndex.getFields();

        ((JdbcIndexField<?>) fields.get(0)).setMappedName("TESTID");
        ((JdbcIndexField<?>) fields.get(1)).setMappedName("TESTINTEGER");
        ((JdbcIndexField<?>) fields.get(2)).setMappedName("COMPOSITEMODEL");
    }

    @Override
    protected TableEngine createEngine(DataSource dataSource, TypeMap typeMap) {

        // Translators (mockito 1.8.5 has a bug with overloading generics)
        IndexNameTranslator indexNameTranslatorStub = new IndexNameTranslator() {
            @Override
            public String translate(Index<?> index) {
                return (index == testIndex) ? "HISTORY_TABLE" : null;
            }
        };

        IndexFieldNameTranslator fieldNameTranslatorStub = new IndexFieldNameTranslator() {
            @Override
            public String translate(IndexField<?> field) {
                return field.getName().toUpperCase();
            }
        };

        return new HistoryTableEngine(dataSource, typeMap, indexNameTranslatorStub, fieldNameTranslatorStub);
    }

    @Test
    public void create_works() throws Exception {
        engine.create(testIndex);

        try (ResultSet rs = getDataSource().getConnection().createStatement().executeQuery("SELECT * FROM HISTORY_TABLE")) {
            ResultSetMetaData metaData = rs.getMetaData();

            assertEquals(7, metaData.getColumnCount());

            assertEquals("REV_ID", metaData.getColumnName(1));
            assertEquals("REV_CREATED", metaData.getColumnName(2));
            assertEquals("REV_MODIFIED", metaData.getColumnName(3));
            assertEquals("REV_OPERATION", metaData.getColumnName(4));
            assertEquals("TESTID", metaData.getColumnName(5));
            assertEquals("TESTINTEGER", metaData.getColumnName(6));
            assertEquals("COMPOSITEMODEL", metaData.getColumnName(7));
        }

        assertEquals("HISTORY_TABLE", testIndex.getHistoryTableName());
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

        try (ResultSet rs = getDataSource().getConnection().createStatement().executeQuery("SELECT * FROM HISTORY_TABLE")) {
            assertTrue(rs.next());
            assertEquals("A", rs.getString("TESTID"));
            assertEquals(42, rs.getInt("TESTINTEGER"));
            assertEquals(1L, rs.getLong("REV_ID"));
            assertEquals("INSERT", rs.getString("REV_OPERATION"));
            assertEquals(new Date(42), rs.getTimestamp("REV_CREATED"));

            assertTrue(rs.next());
            assertEquals("B", rs.getString("TESTID"));
            assertEquals(-42, rs.getInt("TESTINTEGER"));
            assertEquals(2L, rs.getLong("REV_ID"));
            assertEquals("INSERT", rs.getString("REV_OPERATION"));
            assertEquals(new Date(42), rs.getTimestamp("REV_CREATED"));

            assertFalse(rs.next());
        }
    }

    @Test
    public void execute_update_updatesRecordsCorrectly() throws Exception {
        engine.create(testIndex);

        IndexCommit commit = mock(IndexCommit.class);
        when(commit.getTimestamp()).thenReturn(new Date(42));

        TestModel testModelA = new TestModel("A", 42);
        TestModel testModelB = new TestModel("B", -42);

        engine.execute(new InsertOperation(commit, testIndex, new ArrayList<OpenEngSBModel>(Arrays.asList(testModelA,
            testModelB))));

        testModelB.setTestInteger(43);

        IndexCommit updateCommit = mock(IndexCommit.class);
        when(updateCommit.getTimestamp()).thenReturn(new Date(84));
        engine.execute(new UpdateOperation(updateCommit, testIndex, new ArrayList<OpenEngSBModel>(Arrays.asList(testModelB))));

        try (ResultSet rs = getDataSource().getConnection().createStatement().executeQuery("SELECT * FROM HISTORY_TABLE")) {
            assertTrue(rs.next());
            assertEquals("A", rs.getString("TESTID"));
            assertEquals(42, rs.getInt("TESTINTEGER"));
            assertEquals(1L, rs.getLong("REV_ID"));
            assertEquals("INSERT", rs.getString("REV_OPERATION"));
            assertEquals(new Date(42), rs.getTimestamp("REV_CREATED"));

            assertTrue(rs.next());
            assertEquals("B", rs.getString("TESTID"));
            assertEquals(-42, rs.getInt("TESTINTEGER"));
            assertEquals(2L, rs.getLong("REV_ID"));
            assertEquals("INSERT", rs.getString("REV_OPERATION"));
            assertEquals(new Date(42), rs.getTimestamp("REV_CREATED"));

            assertTrue(rs.next());
            assertEquals("B", rs.getString("TESTID"));
            assertEquals(43, rs.getInt("TESTINTEGER"));
            assertEquals(3L, rs.getLong("REV_ID"));
            assertEquals("UPDATE", rs.getString("REV_OPERATION"));
            assertEquals(new Date(84), rs.getTimestamp("REV_MODIFIED"));

            assertFalse(rs.next());
        }
    }

    @Test
    public void execute_delete_deletesRecordsCorrectly() throws Exception {
        engine.create(testIndex);

        IndexCommit commit = mock(IndexCommit.class);
        when(commit.getTimestamp()).thenReturn(new Date(42));

        TestModel testModelA = new TestModel("A", 42);
        TestModel testModelB = new TestModel("B", -42);

        engine.execute(new InsertOperation(commit, testIndex, new ArrayList<OpenEngSBModel>(Arrays.asList(testModelA,
            testModelB))));

        IndexCommit deleteCommit = mock(IndexCommit.class);
        when(deleteCommit.getTimestamp()).thenReturn(new Date(84));

        engine.execute(new DeleteOperation(deleteCommit, testIndex, new ArrayList<OpenEngSBModel>(Arrays
            .asList(testModelB))));

        try (ResultSet rs = getDataSource().getConnection().createStatement().executeQuery("SELECT * FROM HISTORY_TABLE")) {
            assertTrue(rs.next());
            assertEquals("A", rs.getString("TESTID"));
            assertEquals(42, rs.getInt("TESTINTEGER"));
            assertEquals(1L, rs.getLong("REV_ID"));
            assertEquals("INSERT", rs.getString("REV_OPERATION"));
            assertEquals(new Date(42), rs.getTimestamp("REV_CREATED"));

            assertTrue(rs.next());
            assertEquals("B", rs.getString("TESTID"));
            assertEquals(-42, rs.getInt("TESTINTEGER"));
            assertEquals(2L, rs.getLong("REV_ID"));
            assertEquals("INSERT", rs.getString("REV_OPERATION"));
            assertEquals(new Date(42), rs.getTimestamp("REV_CREATED"));

            assertTrue(rs.next());
            assertEquals("B", rs.getString("TESTID"));
            assertEquals(-42, rs.getInt("TESTINTEGER"));
            assertEquals(3L, rs.getLong("REV_ID"));
            assertEquals("DELETE", rs.getString("REV_OPERATION"));
            assertEquals(new Date(84), rs.getTimestamp("REV_MODIFIED"));

            assertFalse(rs.next());
        }
    }

}
