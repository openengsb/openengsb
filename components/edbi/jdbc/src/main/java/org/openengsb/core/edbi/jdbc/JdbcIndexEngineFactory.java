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

import javax.sql.DataSource;

import org.openengsb.core.edbi.jdbc.api.SchemaMapper;
import org.openengsb.core.edbi.jdbc.api.TableEngine;
import org.openengsb.core.edbi.jdbc.api.TypeMap;
import org.openengsb.core.edbi.jdbc.driver.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory fro creating basic JdbcIndexEngine instances from a Driver.
 */
public class JdbcIndexEngineFactory {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcIndexEngineFactory.class);

    private Driver driver;

    public JdbcIndexEngineFactory(Driver driver) {
        this.driver = driver;
    }

    public JdbcIndexEngine create() {
        LOG.info("Creating new JdbcIndexEngine with driver {}", driver.getClass());

        TypeMap typeMap = driver.getTypeMap();
        DataSource dataSource = driver.getDataSource();

        TableEngine headTableEngine = new HeadTableEngine(dataSource, typeMap);
        TableEngine historyTableEngine = new HistoryTableEngine(dataSource, typeMap);

        SchemaMapper schemaMapper = new DefaultSchemaMapper(headTableEngine, historyTableEngine);

        return new JdbcIndexEngine(dataSource, schemaMapper);
    }
}
