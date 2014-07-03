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
package org.openengsb.core.edbi.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexCommit;
import org.openengsb.core.edbi.api.IndexCommitBuilder;
import org.openengsb.core.edbi.jdbc.AbstractH2DatabaseTest;
import org.openengsb.core.edbi.jdbc.JdbcIndex;
import org.openengsb.core.edbi.jdbc.JdbcIndexEngine;
import org.openengsb.core.edbi.jdbc.JdbcIndexEngineFactory;
import org.openengsb.core.edbi.jdbc.driver.h2.Driver;
import org.openengsb.core.edbi.models.PrimitivePropertyModel;
import org.openengsb.core.edbi.models.SubTestModel;
import org.openengsb.core.edbi.models.TestModel;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class H2IndexEngineIntegrationTest extends AbstractH2DatabaseTest {

    @Override
    protected String[] getInitScriptResourceNames() {
        return new String[]{
            "index-schema.h2.sql"
        };
    }

    Driver driver;
    JdbcIndexEngine engine;

    @Before
    public void setUp() throws Exception {
        driver = new Driver(getDataSource());
        JdbcIndexEngineFactory factory = new JdbcIndexEngineFactory(driver);
        engine = factory.create();
    }

    @Test
    public void create_shouldProperlyCreateDatabaseTables() throws Exception {
        Index<TestModel> index = engine.createIndex(TestModel.class);

        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";

        assertEquals(1, jdbc().queryForInt(sql, index.getHeadTableName()));
        assertEquals(1, jdbc().queryForInt(sql, index.getHistoryTableName()));
    }

    @Test
    public void commit_createsIndexInherently() throws Exception {
        assertFalse(engine.indexExists(TestModel.class));
        assertEquals(0, jdbc().queryForInt("SELECT COUNT(*) FROM INDEX_INFORMATION"));

        IndexCommit commit = newTestCommit()
            .insert(new TestModel("foo", 1))
            .get();

        engine.commit(commit);

        assertEquals(1, jdbc().queryForInt("SELECT COUNT(*) FROM INDEX_INFORMATION"));
        assertTrue(engine.indexExists(TestModel.class));
    }

    @Test
    public void commit_insertWithSubmodel_shouldProperlyInsertRecords() throws Exception {
        SubTestModel submodel = new SubTestModel(11);

        IndexCommit commit = newTestCommit()
            .insert(submodel)
            .insert(new TestModel("foo", 1, submodel))
            .insert(new TestModel("bar", 2))
            .get();

        engine.commit(commit);

        assertEquals(2,
            jdbc().queryForInt("SELECT COUNT(*) FROM " + engine.getIndex(TestModel.class).getHeadTableName()));
        assertEquals(2,
            jdbc().queryForInt("SELECT COUNT(*) FROM " + engine.getIndex(TestModel.class).getHistoryTableName()));

        Map<String, Object> record;

        // testmodel
        String sql = "SELECT * FROM " + engine.getIndex(TestModel.class).getHeadTableName() + " WHERE TESTID = 'foo'";
        record = jdbc().queryForMap(sql);
        assertEquals(11, record.get("SUBMODEL")); // foreign key should be the id of the model

        // extensive asserts for sub model tables
        JdbcIndex<SubTestModel> index = engine.getIndex(SubTestModel.class);

        assertEquals(1, jdbc().queryForInt("SELECT COUNT(*) FROM " + index.getHeadTableName()));
        assertEquals(1, jdbc().queryForInt("SELECT COUNT(*) FROM " + index.getHistoryTableName()));

        // submodel
        String column = index.getFields().get(0).getMappedName(); // SubTestModel only has one field

        record = jdbc().queryForMap("SELECT * FROM " + index.getHeadTableName());
        assertEquals(commit.getTimestamp(), record.get("REV_CREATED"));
        assertEquals(null, record.get("REV_MODIFIED"));
        assertEquals(11, record.get(column));

        record = jdbc().queryForMap("SELECT * FROM " + index.getHistoryTableName());
        assertEquals(11, record.get(column));
        // assert meta data
        assertEquals(commit.getTimestamp(), record.get("REV_TIMESTAMP"));
        assertEquals("INSERT", record.get("REV_OPERATION"));
        assertEquals("testUser", record.get("REV_USER"));
        assertEquals("testContext", record.get("REV_CONTEXTID"));
        assertEquals("testDomain", record.get("REV_DOMAINID"));
        assertEquals("testConnector", record.get("REV_CONNECTORID"));
        assertEquals("testInstance", record.get("REV_INSTANCEID"));
    }

    @Test
    public void commit_update_updatesTablesCorrectly() throws Exception {
        TestModel entity = new TestModel("foo", 1);

        // initial insert
        IndexCommit insertCommit = newTestCommit().insert(entity).get();
        engine.commit(insertCommit);

        // update
        entity.setTestInteger(42);
        IndexCommit updateCommit = newTestCommit().update(entity).get();
        engine.commit(updateCommit);

        // assert

        Index<TestModel> index = engine.getIndex(TestModel.class);

        assertEquals(1, jdbc().queryForInt("SELECT COUNT(*) FROM " + index.getHeadTableName()));
        assertEquals(2, jdbc().queryForInt("SELECT COUNT(*) FROM " + index.getHistoryTableName()));

        // check head table
        Map<String, Object> record = jdbc().queryForMap("SELECT * FROM " + index.getHeadTableName());
        assertEquals(insertCommit.getTimestamp(), record.get("REV_CREATED"));
        assertEquals("foo", record.get("TESTID"));
        assertEquals(42, record.get("TESTINTEGER"));

        // check revisions
        SqlRowSet rowset =
            jdbc().queryForRowSet("SELECT * FROM " + index.getHistoryTableName() + " ORDER BY REV_TIMESTAMP");

        assertTrue(rowset.next());
        assertEquals("foo", rowset.getString("TESTID"));
        assertEquals(1, rowset.getInt("TESTINTEGER"));
        assertEquals("INSERT", rowset.getString("REV_OPERATION"));

        assertTrue(rowset.next());
        assertEquals("foo", rowset.getString("TESTID"));
        assertEquals(42, rowset.getInt("TESTINTEGER"));
        assertEquals("UPDATE", rowset.getString("REV_OPERATION"));

        assertFalse(rowset.next());
    }

    @Test
    public void commit_delete_updatesTablesCorrectly() throws Exception {
        TestModel entity = new TestModel("foo", 1);

        // initial insert
        IndexCommit insertCommit = newTestCommit().insert(entity).get();
        engine.commit(insertCommit);

        // deletion
        IndexCommit deleteCommit = newTestCommit().delete(entity).get();
        engine.commit(deleteCommit);

        // assert

        Index<TestModel> index = engine.getIndex(TestModel.class);

        assertEquals(0, jdbc().queryForInt("SELECT COUNT(*) FROM " + index.getHeadTableName()));
        assertEquals(2, jdbc().queryForInt("SELECT COUNT(*) FROM " + index.getHistoryTableName()));

        // check revisions
        String sql = "SELECT * FROM " + index.getHistoryTableName() + " ORDER BY REV_TIMESTAMP";
        SqlRowSet rowset = jdbc().queryForRowSet(sql);

        assertTrue(rowset.next());
        assertEquals("foo", rowset.getString("TESTID"));
        assertEquals(1, rowset.getInt("TESTINTEGER"));
        assertEquals("INSERT", rowset.getString("REV_OPERATION"));

        assertTrue(rowset.next());
        assertEquals("foo", rowset.getString("TESTID"));
        assertEquals(1, rowset.getInt("TESTINTEGER"));
        assertEquals("DELETE", rowset.getString("REV_OPERATION"));

        assertFalse(rowset.next());
    }

    @Test
    public void commit_insert_primitivePropertyModel_createsRecordsCorrectly() throws Exception {
        PrimitivePropertyModel model = new PrimitivePropertyModel();

        model.setId("ppm/1");
        model.setBooleanByGet(true);
        model.setBooleanByIs(true);
        model.setPrimitiveDouble(Double.MAX_VALUE);
        model.setPrimitiveFloat(Float.MAX_VALUE);
        model.setPrimitiveInt(Integer.MAX_VALUE);
        model.setPrimitiveLong(Long.MAX_VALUE);
        model.setPrimitiveShort(Short.MAX_VALUE);

        engine.commit(newTestCommit().insert(model).get());

        JdbcIndex<?> index = engine.getIndex(PrimitivePropertyModel.class);

        Map<String, Object> record;
        try {
            record = jdbc().queryForMap("SELECT * FROM " + index.getHeadTableName());
        } catch (EmptyResultDataAccessException e) {
            fail("There was no record inserted to " + index.getHeadTableName() + ": " + e.getMessage());
            return;
        }

        assertEquals(true, record.get("BOOLEANBYGET"));
        assertEquals(true, record.get("BOOLEANBYIS"));
        assertEquals(Double.MAX_VALUE, record.get("PRIMITIVEDOUBLE"));
        assertEquals(Float.MAX_VALUE, record.get("PRIMITIVEFLOAT"));
        assertEquals(Integer.MAX_VALUE, record.get("PRIMITIVEINT"));
        assertEquals(Long.MAX_VALUE, record.get("PRIMITIVELONG"));
        assertEquals(Short.MAX_VALUE, record.get("PRIMITIVESHORT"));
    }

    private IndexCommitBuilder newTestCommit() {
        return IndexCommitBuilder.create()
            .context("testContext")
            .user("testUser")
            .domain("testDomain")
            .connector("testConnector")
            .instance("testInstance");
    }

}
