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

import static junit.framework.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.jdbc.driver.h2.Driver;
import org.openengsb.core.edbi.models.TestModel;

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

}
