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

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.api.NameTranslator;
import org.openengsb.core.edbi.jdbc.api.TableEngine;
import org.openengsb.core.edbi.jdbc.api.TableExistsException;
import org.openengsb.core.edbi.jdbc.api.TableFactory;
import org.openengsb.core.edbi.jdbc.api.TypeMap;
import org.openengsb.core.edbi.jdbc.sql.Table;
import org.openengsb.core.edbi.jdbc.sql.TableElementCompiler;

/**
 * AbstractTableEngine
 */
public abstract class AbstractTableEngine extends JdbcService implements TableEngine {

    private TypeMap typeMap;
    private NameTranslator<Index<?>> tableNameTranslator;
    private NameTranslator<IndexField<?>> columnNameTranslator;

    private Map<JdbcIndex<?>, Table> registry;

    protected AbstractTableEngine(DataSource dataSource, TypeMap typeMap, NameTranslator<Index<?>> tableNameTranslator,
            NameTranslator<IndexField<?>> columnNameTranslator) {
        super(dataSource);

        this.typeMap = typeMap;
        this.tableNameTranslator = tableNameTranslator;
        this.columnNameTranslator = columnNameTranslator;

        this.registry = new HashMap<>();
    }

    @Override
    public boolean exists(JdbcIndex<?> index) {
        if (registry.containsKey(index)) {
            return true;
        }
        String tableName = getTableNameTranslator().translate(index);

        // TODO: sql independence
        return count("`INFORMATION_SCHEMA`.`TABLES`", "TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = ?", tableName) > 0;
    }

    @Override
    public Table create(JdbcIndex<?> index) {
        Table table = getTableFactory().create(index);

        if (exists(index)) {
            throw new TableExistsException("Table for index " + index.getName() + " exists");
        }

        // TODO: sql independence
        String sql =
            String.format("CREATE TABLE `%s` ( %s );", table.getName(), new TableElementCompiler(table).toSql());

        jdbc().execute(sql);

        registry.put(index, table);

        return table;
    }

    @Override
    public void drop(JdbcIndex<?> index) {
        // TODO
    }

    protected abstract TableFactory getTableFactory();

    protected Map<JdbcIndex<?>, Table> getRegistry() {
        return registry;
    }

    public TypeMap getTypeMap() {
        return typeMap;
    }

    public void setTypeMap(TypeMap typeMap) {
        this.typeMap = typeMap;
    }

    public NameTranslator<Index<?>> getTableNameTranslator() {
        return tableNameTranslator;
    }

    public void setTableNameTranslator(NameTranslator<Index<?>> tableNameTranslator) {
        this.tableNameTranslator = tableNameTranslator;
    }

    public NameTranslator<IndexField<?>> getColumnNameTranslator() {
        return columnNameTranslator;
    }

    public void setColumnNameTranslator(NameTranslator<IndexField<?>> columnNameTranslator) {
        this.columnNameTranslator = columnNameTranslator;
    }
}