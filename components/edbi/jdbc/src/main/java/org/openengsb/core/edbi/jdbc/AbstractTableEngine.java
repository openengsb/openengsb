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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.api.NameTranslator;
import org.openengsb.core.edbi.jdbc.api.NoSuchTableException;
import org.openengsb.core.edbi.jdbc.api.TableEngine;
import org.openengsb.core.edbi.jdbc.api.TableExistsException;
import org.openengsb.core.edbi.jdbc.api.TableFactory;
import org.openengsb.core.edbi.jdbc.api.TypeMap;
import org.openengsb.core.edbi.jdbc.operation.DeleteOperation;
import org.openengsb.core.edbi.jdbc.operation.IndexOperation;
import org.openengsb.core.edbi.jdbc.operation.InsertOperation;
import org.openengsb.core.edbi.jdbc.operation.Operation;
import org.openengsb.core.edbi.jdbc.operation.OperationExecutor;
import org.openengsb.core.edbi.jdbc.operation.UpdateOperation;
import org.openengsb.core.edbi.jdbc.sql.Table;
import org.openengsb.core.edbi.jdbc.sql.TableElementCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractTableEngine
 */
public abstract class AbstractTableEngine extends JdbcService implements TableEngine, OperationExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTableEngine.class);

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
    public Table get(JdbcIndex<?> index) {
        if (!exists(index)) {
            throw new NoSuchTableException("Table for index " + index.getName() + " does not exist");
        }

        Table table = registry.get(index);

        if (table == null) {
            table = getTableFactory().create(index); // TODO: proper load function
            registry.put(index, table);
        }

        return table;
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

        LOG.info("Creating table for Index {}. SQL is: {}", index.getName(), sql);

        jdbc().execute(sql);

        registry.put(index, table);

        return table;
    }

    @Override
    public void drop(JdbcIndex<?> index) {
        if (!exists(index)) {
            throw new NoSuchTableException("Table for index " + index.getName() + " does not exist");
        }

        String tableName = getTableNameTranslator().translate(index);

        if (tableName == null || tableName.isEmpty()) {
            throw new NoSuchTableException("Table name for index " + index + " could not be resolved. Can not drop.");
        }

        jdbc().update("DROP TABLE " + tableName);
        registry.remove(index);
    }

    @Override
    public void execute(Operation operation) {
        operation.executeWith(this);
    }

    @Override
    public void execute(InsertOperation operation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(UpdateOperation operation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(DeleteOperation operation) {
        throw new UnsupportedOperationException();
    }

    protected void execute(InsertOperation operation, IndexRecordCallback callback) {
        Table table = get(operation.getIndex());

        insert(table, collectRecords(operation, callback));
    }

    protected void execute(UpdateOperation operation, IndexRecordCallback callback) {
        Table table = get(operation.getIndex());

        update(table, collectRecords(operation, callback));
    }

    protected List<IndexRecord> collectRecords(IndexOperation operation, IndexRecordCallback callback) {
        JdbcIndex<?> index = operation.getIndex();
        List<OpenEngSBModel> models = operation.getModels();
        List<IndexRecord> records = new ArrayList<>(models.size());

        for (OpenEngSBModel model : models) {
            IndexRecord record = new IndexRecord(index, model);

            if (callback != null) {
                callback.call(record);
            }
            records.add(record);
        }

        return records;
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
