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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.edbi.jdbc.sql.Column;
import org.openengsb.core.edbi.jdbc.sql.DataType;
import org.openengsb.core.edbi.jdbc.sql.PrimaryKeyConstraint;
import org.openengsb.core.edbi.jdbc.sql.Table;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class JdbcServiceTest extends AbstractH2DatabaseTest {

    JdbcService service;

    JdbcTemplate jdbc;
    NamedParameterJdbcTemplate jdbcn;

    Table table;

    @Before
    public void setUp() throws Exception {
        service = new JdbcService(getDataSource());

        jdbc = new JdbcTemplate(getDataSource());
        jdbcn = new NamedParameterJdbcTemplate(getDataSource());

        table = new Table("TEST",
            new Column("ID", new DataType("IDENTITY")),
            new Column("NAME", new DataType("VARCHAR")),
            new Column("AGE", new DataType("INT")));

        table.addElement(new PrimaryKeyConstraint("ID"));

        jdbc.execute("CREATE TABLE `TEST` (ID IDENTITY PRIMARY KEY, NAME VARCHAR, AGE INT)");
    }

    @After
    public void tearDown() throws Exception {
        service = null;
        jdbc = null;
        jdbcn = null;
        table = null;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void insert_indexRecordsIntoTable() throws Exception {
        List<IndexRecord> records = getRecords();

        service.insert(table, records);

        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM `TEST`");
        Map<String, Object> row;

        row = rows.get(0);
        assertEquals(1L, row.get("ID"));
        assertEquals("Arthur", row.get("NAME"));
        assertEquals(42, row.get("AGE"));

        row = rows.get(1);
        assertEquals(2L, row.get("ID"));
        assertEquals("Ford", row.get("NAME"));
        assertNull(row.get("AGE"));
    }

    @Test
    public void update_updatesRecordsCorrectly() throws Exception {
        List<IndexRecord> records = getRecords();
        service.insert(table, records);

        records.get(0).addValue("ID", 1L, Types.BIGINT);
        records.get(0).addValue("NAME", "Zaphod");

        records.get(1).addValue("ID", 2L, Types.BIGINT);
        records.get(1).addValue("AGE", 44, Types.INTEGER);

        service.update(table, records);

        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM `TEST`");
        Map<String, Object> row;

        row = rows.get(0);
        assertEquals(1L, row.get("ID"));
        assertEquals("Zaphod", row.get("NAME"));
        assertEquals(42, row.get("AGE"));

        row = rows.get(1);
        assertEquals(2L, row.get("ID"));
        assertEquals("Ford", row.get("NAME"));
        assertEquals(44, row.get("AGE"));
    }

    private List<IndexRecord> getRecords() {
        JdbcIndex<?> index = mock(JdbcIndex.class);

        List<IndexRecord> records = new ArrayList<>();

        IndexRecord record1 = new IndexRecord(index);
        record1.addValue("NAME", "Arthur", Types.VARCHAR);
        record1.addValue("AGE", 42, Types.INTEGER);
        record1.addValue("FOO", "BAR", Types.VARCHAR);

        IndexRecord record2 = new IndexRecord(index);
        record2.addValue("NAME", "Ford", Types.VARCHAR);

        records.add(record1);
        records.add(record2);

        return records;
    }

}
