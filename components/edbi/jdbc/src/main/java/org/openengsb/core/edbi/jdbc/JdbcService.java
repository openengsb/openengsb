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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.openengsb.core.edbi.jdbc.sql.PrimaryKeyConstraint;
import org.openengsb.core.edbi.jdbc.sql.Table;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * JdbcService holds a DataSource and provides several helper methods for spring-jdbc.
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

    public DataSource getDataSource() {
        return dataSource;
    }

    public <T> T queryForObject(String sql, Class<T> type) {
        return jdbc().queryForObject(sql, type);
    }

    public <T> T queryForObject(String sql, Class<T> type, Object... args) {
        return jdbc().queryForObject(sql, type, args);
    }

    public Boolean queryForBoolean(String sql, Object... args) {
        return queryForObject(sql, Boolean.class, args);
    }

    public long count(String table) {
        return queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }

    public long count(String table, String where, Object... args) {
        return queryForObject("SELECT COUNT(*) FROM " + table + " WHERE " + where, Long.class, args);
    }

    public int insert(String table, String columns, Object... args) {
        String sql =
            String.format("INSERT INTO `%s` (%s) VALUES (%s)", table, columns,
                StringUtils.repeat("?", ",", args.length));

        return jdbc().update(sql, args);
    }

    public int insert(String table, String[] columns, Object[] args) {
        return insert(table, StringUtils.join(columns, ","), args);
    }

    public int insert(String table, Object... args) {
        String sql = String.format("INSERT INTO `%s` VALUES (%s)", table, StringUtils.repeat("?", ",", args.length));

        return jdbc().update(sql, args);
    }

    public int[] insert(String table, Collection<String> columns, SqlParameterSource[] records) {
        String columnList = StringUtils.join(columns, ",");
        String placeholders = ":" + StringUtils.join(columns, ",:");
        String sql = String.format("INSERT INTO `%s` (%s) VALUES (%s)", table, columnList, placeholders);

        return jdbcn().batchUpdate(sql, records);
    }

    public int[] insert(Table table, List<IndexRecord> records) {
        List<String> columns = table.getColumns().getColumnNames();

        return insert(table.getName(), columns, toParameterSourceArray(records));
    }

    public int[] update(String table, Collection<String> columns, String whereClause, SqlParameterSource[] records) {
        String setClauseList = makeNamedSetClauseList(columns);
        String sql = String.format("UPDATE `%s` SET %s WHERE %s", table, setClauseList, whereClause);

        return jdbcn().batchUpdate(sql, records);
    }

    public int[] update(Table table, List<IndexRecord> records) {
        List<String> columns = table.getColumns().getColumnNames();
        String whereClause = makeWhereClause(table.getPrimaryKey());

        // FIXME: find an elegant solution for a dynamic SET clause within an UPDATE statement for batch updates
        // either column exclusions or find subsets of columns within the list of IndexRecords
        IndexRecord record = records.get(0);
        columns.retainAll(record.getValues().keySet());

        return update(table.getName(), columns, whereClause, toParameterSourceArray(records));
    }

    public int[] delete(String table, String whereClause, SqlParameterSource[] records) {
        String sql = String.format("DELETE FROM `%s` WHERE %s", table, whereClause);

        return jdbcn().batchUpdate(sql, records);
    }

    public int[] delete(Table table, List<IndexRecord> records) {
        String whereClause = makeWhereClause(table.getPrimaryKey());

        return delete(table.getName(), whereClause, toParameterSourceArray(records));
    }

    public int delete(String table, String whereClause, Object... args) {
        return jdbc().update(String.format("DELETE FROM `%s` WHERE %s", table, whereClause), args);
    }

    protected SqlParameterSource[] toParameterSourceArray(List<? extends SqlParameterSource> list) {
        return list.toArray(new SqlParameterSource[list.size()]);
    }

    protected String makeNamedSetClauseList(Collection<String> parameters) {
        return joinNamedParameters(parameters, "=", ",");
    }

    protected String makeWhereClause(PrimaryKeyConstraint key) {
        return joinNamedParameters(key.getColumns(), "=", " AND ");
    }

    protected String joinNamedParameters(Collection<String> parameters, String glue, String delimiter) {
        StringBuilder str = new StringBuilder(parameters.size() * 20);

        Iterator<String> iterator = parameters.iterator();
        while (iterator.hasNext()) {
            String parameter = iterator.next();

            str.append(parameter);
            str.append(glue);
            str.append(" :");
            str.append(parameter);

            if (iterator.hasNext()) {
                str.append(delimiter);
            }
        }

        return str.toString();
    }

}
