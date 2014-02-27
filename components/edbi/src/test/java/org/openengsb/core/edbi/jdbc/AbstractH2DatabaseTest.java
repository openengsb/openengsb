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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;

/**
 * AbstractH2DatabaseTest
 */
public abstract class AbstractH2DatabaseTest {

    protected DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    @Before
    public void setUpDataSource() throws Exception {
        this.dataSource = createDataSource();
        doExecuteInitScript();

    }

    @After
    public void tearDownDataSource() throws Exception {
        shutdownDataSource(dataSource);
        this.dataSource = null;
    }

    private void doExecuteInitScript() throws Exception {
        for (String resource : getInitScriptResourceNames()) {
            String string = readResourceContent(resource);

            try (Connection c = dataSource.getConnection()) {
                c.createStatement().execute(string);
            }
        }
    }

    protected String readResourceContent(String resource) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalArgumentException("Stream for resource " + resource + " is null");
            }

            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer);
            return writer.toString();
        }
    }

    protected static DataSource createDataSource() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();

        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("");
        dataSource.setPassword("");

        return dataSource;
    }

    protected static void shutdownDataSource(DataSource dataSource) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            c.createStatement().execute("SHUTDOWN");
        }
    }

    protected String[] getInitScriptResourceNames() {
        return new String[0];
    }

}
