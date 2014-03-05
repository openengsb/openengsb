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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Types;
import java.util.Date;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexExistsException;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.api.IndexFieldNameTranslator;
import org.openengsb.core.edbi.api.IndexNameTranslator;
import org.openengsb.core.edbi.api.IndexNotFoundException;
import org.openengsb.core.edbi.jdbc.api.SchemaMapper;
import org.openengsb.core.edbi.jdbc.api.TypeMap;
import org.openengsb.core.edbi.jdbc.sql.DataType;
import org.openengsb.core.edbi.models.TestModel;

public class JdbcIndexEngineTest extends AbstractH2DatabaseTest {

    @Override
    protected String[] getInitScriptResourceNames() {
        return new String[]{
            "index-schema.h2.sql"
        };
    }

    JdbcIndexEngine indexEngine;

    SchemaMapper schemaMapper;
    HeadTableEngine headTableEngine;
    HistoryTableEngine historyTableEngine;

    @Before
    public void setUp() throws Exception {
        // Type Map
        TypeMap typeMap = mock(TypeMap.class);
        when(typeMap.getType(String.class)).thenReturn(new DataType(Types.LONGVARCHAR, "LONGVARCHAR"));
        when(typeMap.getType(Integer.class)).thenReturn(new DataType(Types.INTEGER, "INTEGER"));
        when(typeMap.getType(Long.class)).thenReturn(new DataType(Types.BIGINT, "LONG"));
        when(typeMap.getType(Date.class)).thenReturn(new DataType(Types.TIMESTAMP, "TIMESTAMP"));

        // Translators
        IndexNameTranslator headIndexNameTranslator = new IndexNameTranslator() {
            @Override
            public String translate(Index<?> index) {
                return "INDEX_HEAD";
            }
        };

        IndexNameTranslator historyIndexNameTranslator = new IndexNameTranslator() {
            @Override
            public String translate(Index<?> index) {
                return "INDEX_HISTORY";
            }
        };

        IndexFieldNameTranslator fieldNameTranslatorStub = new IndexFieldNameTranslator() {
            @Override
            public String translate(IndexField<?> field) {
                return field.getName().toUpperCase();
            }
        };

        // Engine
        headTableEngine =
            new HeadTableEngine(getDataSource(), typeMap, headIndexNameTranslator, fieldNameTranslatorStub);
        historyTableEngine =
            new HistoryTableEngine(getDataSource(), typeMap, historyIndexNameTranslator, fieldNameTranslatorStub);

        schemaMapper = new DefaultSchemaMapper(headTableEngine, historyTableEngine);

        indexEngine = new JdbcIndexEngine(getDataSource(), schemaMapper);
    }

    @After
    public void tearDown() throws Exception {
        indexEngine = null;

        headTableEngine = null;
        historyTableEngine = null;
    }

    @Test
    public void createIndex_works() throws Exception {
        Index<TestModel> index = indexEngine.createIndex(TestModel.class);

        assertEquals("org.openengsb.core.edbi.models.TestModel", index.getName());
        assertEquals(TestModel.class, index.getModelClass());
        assertEquals("INDEX_HEAD", index.getHeadTableName());
        assertEquals("INDEX_HISTORY", index.getHistoryTableName());

        assertEquals(3, index.getFields().size());
    }

    @Test(expected = IndexExistsException.class)
    public void createIndex_twice_throwsException() throws Exception {
        try {
            indexEngine.createIndex(TestModel.class);
        } catch (IndexExistsException e) {
            fail("IndexExistsException caught on wrong call");
        }

        indexEngine.createIndex(TestModel.class);
    }

    @Test
    public void indexExists_onNonExistingClass_returnsFalse() throws Exception {
        assertFalse(indexEngine.indexExists(TestModel.class));
    }

    @Test
    public void indexExists_onNonExistingName_returnsFalse() throws Exception {
        assertFalse(indexEngine.indexExists("org.openengsb.core.edbi.models.TestModel"));
    }

    @Test(expected = IndexNotFoundException.class)
    public void getIndex_onNonExistingClass_throwsException() throws Exception {
        indexEngine.getIndex(TestModel.class);
    }

    @Test(expected = IndexNotFoundException.class)
    public void getIndex_onNonExistingNames_throwsException() throws Exception {
        indexEngine.getIndex("org.openengsb.core.edbi.models.TestModel");
    }

    @Test
    public void indexExists_afterCreateIndex_returnsTrue() throws Exception {
        indexEngine.createIndex(TestModel.class);

        assertTrue(indexEngine.indexExists(TestModel.class));
    }

    @Test
    public void indexExists_byName_afterCreateIndex_returnsTrue() throws Exception {
        indexEngine.createIndex(TestModel.class);

        assertTrue(indexEngine.indexExists("org.openengsb.core.edbi.models.TestModel"));
    }

    @Test
    public void getIndex_returnsCorrectIndex() throws Exception {
        indexEngine.createIndex(TestModel.class);

        Index<?> index = indexEngine.getIndex(TestModel.class);

        assertEquals("org.openengsb.core.edbi.models.TestModel", index.getName());
        assertEquals(TestModel.class, index.getModelClass());
        assertEquals("INDEX_HEAD", index.getHeadTableName());
        assertEquals("INDEX_HISTORY", index.getHistoryTableName());
    }

    @Test
    public void getIndex_returnsCorrectFields() throws Exception {
        indexEngine.createIndex(TestModel.class);

        Index<?> index = indexEngine.getIndex(TestModel.class);

        assertEquals(3, index.getFields().size());

        Iterator<IndexField<?>> iterator = index.getFields().iterator();
        JdbcIndexField<?> field;

        field = (JdbcIndexField<?>) iterator.next();
        assertEquals("compositeModel", field.getName());
        assertEquals("COMPOSITEMODEL", field.getMappedName());
        assertEquals("LONGVARCHAR", field.getMappedType().getName());
        assertEquals(Types.LONGVARCHAR, field.getMappedType().getType());
        assertEquals(0, field.getMappedType().getScale());

        field = (JdbcIndexField<?>) iterator.next();
        assertEquals("testInteger", field.getName());
        assertEquals("TESTINTEGER", field.getMappedName());
        assertEquals("INTEGER", field.getMappedType().getName());
        assertEquals(Types.INTEGER, field.getMappedType().getType());
        assertEquals(0, field.getMappedType().getScale());

        field = (JdbcIndexField<?>) iterator.next();
        assertEquals("testId", field.getName());
        assertEquals("TESTID", field.getMappedName());
        assertEquals("LONGVARCHAR", field.getMappedType().getName());
        assertEquals(Types.LONGVARCHAR, field.getMappedType().getType());
        assertEquals(0, field.getMappedType().getScale());
    }

    @Test
    public void getIndex_byName_returnsCorrectIndex() throws Exception {
        indexEngine.createIndex(TestModel.class);

        Index<?> index = indexEngine.getIndex("org.openengsb.core.edbi.models.TestModel");

        assertEquals("org.openengsb.core.edbi.models.TestModel", index.getName());
        assertEquals(TestModel.class, index.getModelClass());
        assertEquals("INDEX_HEAD", index.getHeadTableName());
        assertEquals("INDEX_HISTORY", index.getHistoryTableName());
    }

    @Test
    public void load_works() throws Exception {
        Method loadIndex = JdbcIndexEngine.class.getDeclaredMethod("load", String.class);
        boolean accessibleFlag = loadIndex.isAccessible();
        loadIndex.setAccessible(true);

        indexEngine.createIndex(TestModel.class);

        Index index = (Index) loadIndex.invoke(indexEngine, "org.openengsb.core.edbi.models.TestModel");

        assertEquals("org.openengsb.core.edbi.models.TestModel", index.getName());
        assertEquals(TestModel.class, index.getModelClass());
        assertEquals("INDEX_HEAD", index.getHeadTableName());
        assertEquals("INDEX_HISTORY", index.getHistoryTableName());

        loadIndex.setAccessible(accessibleFlag);
    }

    @Test(expected = IndexNotFoundException.class)
    public void load_throwsException() throws Throwable {
        Method loadIndex = JdbcIndexEngine.class.getDeclaredMethod("load", String.class);
        loadIndex.setAccessible(true);

        try {
            loadIndex.invoke(indexEngine, "org.openengsb.core.edbi.models.TestModel");
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IndexNotFoundException) {
                throw e.getCause();
            }
        }
    }

}
