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

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * JdbcService holds a DataSource and provides several protected helper methods for spring-jdbc.
 */
public class JdbcService {
    private DataSource dataSource;

    private JdbcTemplate jdbc;
    private NamedParameterJdbcTemplate jdbcn;

    public JdbcService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public JdbcTemplate jdbc() {
        if (jdbc == null) {
            jdbc = new JdbcTemplate(dataSource);
        }

        return jdbc;
    }

    public NamedParameterJdbcTemplate jdbcn() {
        if (jdbcn == null) {
            jdbcn = new NamedParameterJdbcTemplate(dataSource);
        }

        return jdbcn;
    }

    protected <T> T queryForObject(String sql, Class<T> type) {
        return jdbc().queryForObject(sql, type);
    }

    protected <T> T queryForObject(String sql, Class<T> type, Object... args) {
        return jdbc().queryForObject(sql, type, args);
    }

    protected Boolean queryForBoolean(String sql, Object... args) {
        return queryForObject(sql, Boolean.class, args);
    }

    protected long count(String table) {
        return queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }

    protected long count(String table, String where, Object... args) {
        return queryForObject("SELECT COUNT(*) FROM " + table + " WHERE " + where, Long.class, args);
    }

    protected int insert(String table, String columns, Object... args) {
        String sql =
            String.format("INSERT INTO `%s` (%s) VALUES (%s)", table, columns,
                StringUtils.repeat("?", ",", args.length));

        return jdbc().update(sql, args);
    }

    protected int insert(String table, String[] columns, Object[] args) {
        return insert(table, StringUtils.join(columns, ","), args);
    }

    protected int insert(String table, Object... args) {
        String sql = String.format("INSERT INTO `%s` VALUES (%s)", table, StringUtils.repeat("?", ",", args.length));

        return jdbc().update(sql, args);
    }

}