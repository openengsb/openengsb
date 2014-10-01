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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexCommit;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.api.IndexFieldNameTranslator;
import org.openengsb.core.edbi.api.IndexNameTranslator;
import org.openengsb.core.edbi.jdbc.api.NoSuchTableException;
import org.openengsb.core.edbi.jdbc.api.TableEngine;
import org.openengsb.core.edbi.jdbc.api.TypeMap;
import org.openengsb.core.edbi.jdbc.operation.DeleteOperation;
import org.openengsb.core.edbi.jdbc.operation.InsertOperation;
import org.openengsb.core.edbi.jdbc.operation.UpdateOperation;
import org.openengsb.core.edbi.models.TestModel;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class HeadTableEngineTest extends AbstractTableEngineTest {

    private Connection connection;

    @Before
    public void setUp() throws Exception {
        connection = getDataSource().getConnection();
    }

    @Override
    protected TableEngine createEngine(DataSource dataSource, TypeMap typeMap) {
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

        return new HeadTableEngine(dataSource, typeMap, indexNameTranslatorStub, fieldNameTranslatorStub);
    }

    @Test
    public void create_works() throws Exception {
        engine.create(testIndex);

        String sql = "SELECT * FROM HEAD_TABLE";
        try (ResultSet rs = connection.createStatement().executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData();

            assertEquals(5, metaData.getColumnCount());

            assertEquals("REV_CREATED", metaData.getColumnName(1));
            assertEquals("REV_MODIFIED", metaData.getColumnName(2));
            assertEquals("TESTID", metaData.getColumnName(3));
            assertEquals("TESTINTEGER", metaData.getColumnName(4));
            assertEquals("SUBMODEL", metaData.getColumnName(5));
        }

        assertEquals("HEAD_TABLE", testIndex.getHeadTableName());

        List<IndexField<?>> fields = testIndex.getFields();
        assertEquals("TESTID", fields.get(0).getMappedName());
        assertEquals("TESTINTEGER", fields.get(1).getMappedName());
        assertEquals("SUBMODEL", fields.get(2).getMappedName());
    }

    @Test
    public void drop_works() throws Exception {
        long cnt;
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";

        engine.create(testIndex);
        cnt = jdbc().queryForLong(sql, "HEAD_TABLE");
        assertEquals(1, cnt);

        engine.drop(testIndex);
        cnt = jdbc().queryForLong(sql, "HEAD_TABLE");
        assertEquals(0, cnt);

        try {
            engine.get(testIndex);
            fail("Did not throw NoSuchTableException");
        } catch (NoSuchTableException e) {
        }
    }

    @Test(expected = NoSuchTableException.class)
    public void drop_nonExistingIndex_throwsException() throws Exception {
        engine.drop(testIndex);
    }

    @Test
    public void execute_insert_createsRecordsCorrectly() throws Exception {
        engine.create(testIndex);

        IndexCommit commit = mock(IndexCommit.class);
        when(commit.getTimestamp()).thenReturn(new Date(42));
        // TODO: mock entire commit

        List models = new ArrayList<>();

        models.add(new TestModel("A", 42));
        models.add(new TestModel("B", -42));

        InsertOperation operation = new InsertOperation(commit, testIndex, models);

        engine.execute(operation);

        String sql = "SELECT * FROM HEAD_TABLE";
        try (ResultSet rs = connection.createStatement().executeQuery(sql)) {
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

    @Test
    public void execute_update_updatesRecordsCorrectly() throws Exception {
        engine.create(testIndex);

        IndexCommit commit = mock(IndexCommit.class);
        when(commit.getTimestamp()).thenReturn(new Date(42));

        TestModel testModelA = new TestModel("A", 42);
        TestModel testModelB = new TestModel("B", -42);

        engine.execute(new InsertOperation(commit, testIndex, new ArrayList(Arrays.asList(testModelA, testModelB))));

        testModelB.setTestInteger(43);

        IndexCommit updateCommit = mock(IndexCommit.class);
        when(updateCommit.getTimestamp()).thenReturn(new Date(84));
        engine.execute(new UpdateOperation(updateCommit, testIndex,
            new ArrayList(Arrays.asList(testModelA, testModelB))));

        try (ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM HEAD_TABLE")) {
            assertTrue(rs.next());
            assertEquals("A", rs.getString("TESTID"));
            assertEquals(42, rs.getInt("TESTINTEGER"));
            assertEquals(new Date(42), rs.getTimestamp("REV_CREATED"));

            assertTrue(rs.next());
            assertEquals("B", rs.getString("TESTID"));
            assertEquals(43, rs.getInt("TESTINTEGER"));
            assertEquals(new Date(42), rs.getTimestamp("REV_CREATED"));

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

        engine.execute(new InsertOperation(commit, testIndex, new ArrayList(Arrays.asList(testModelA, testModelB))));

        IndexCommit deleteCommit = mock(IndexCommit.class);
        when(deleteCommit.getTimestamp()).thenReturn(new Date(84));

        engine.execute(new DeleteOperation(deleteCommit, testIndex, new ArrayList(Arrays.asList(testModelB))));

        try (ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM HEAD_TABLE")) {
            assertTrue(rs.next());
            assertEquals("A", rs.getString("TESTID"));
            assertEquals(42, rs.getInt("TESTINTEGER"));
            assertEquals(new Date(42), rs.getTimestamp("REV_CREATED"));

            assertFalse(rs.next());
        }
    }
}
