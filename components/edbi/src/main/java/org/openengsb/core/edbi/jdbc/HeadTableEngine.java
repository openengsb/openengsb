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

import java.util.Date;

import javax.sql.DataSource;

import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.api.NameTranslator;
import org.openengsb.core.edbi.jdbc.api.TableFactory;
import org.openengsb.core.edbi.jdbc.api.TypeMap;
import org.openengsb.core.edbi.jdbc.names.PrependingNameTranslator;
import org.openengsb.core.edbi.jdbc.names.SQLIndexFieldNameTranslator;
import org.openengsb.core.edbi.jdbc.names.SQLIndexNameTranslator;
import org.openengsb.core.edbi.jdbc.operation.DeleteOperation;
import org.openengsb.core.edbi.jdbc.operation.InsertOperation;
import org.openengsb.core.edbi.jdbc.operation.UpdateOperation;
import org.openengsb.core.edbi.jdbc.sql.Column;
import org.openengsb.core.edbi.jdbc.sql.PrimaryKeyConstraint;
import org.openengsb.core.edbi.jdbc.sql.Table;
import org.openengsb.core.edbi.jdbc.util.Introspector;

/**
 * HeadTableEngine
 */
public class HeadTableEngine extends AbstractTableEngine {

    public static final String TABLE_PREFIX = "HEAD_";

    private HeadTableFactory tableFactory;

    public HeadTableEngine(DataSource dataSource, TypeMap typeMap) {
        this(dataSource, typeMap, new PrependingNameTranslator<>(new SQLIndexNameTranslator(), TABLE_PREFIX),
            new SQLIndexFieldNameTranslator());
    }

    public HeadTableEngine(DataSource dataSource, TypeMap typeMap, NameTranslator<Index<?>> indexNameTranslator,
            NameTranslator<IndexField<?>> indexFieldNameTranslator) {
        this(dataSource, typeMap, indexNameTranslator, indexFieldNameTranslator, new HeadTableFactory(typeMap,
            indexNameTranslator, indexFieldNameTranslator));
    }

    protected HeadTableEngine(DataSource dataSource, TypeMap typeMap, NameTranslator<Index<?>> indexNameTranslator,
            NameTranslator<IndexField<?>> indexFieldNameTranslator, HeadTableFactory tableFactory) {
        super(dataSource, typeMap, indexNameTranslator, indexFieldNameTranslator);

        this.tableFactory = tableFactory;
    }

    @Override
    public void execute(final InsertOperation operation) {
        execute(operation, new IndexRecordCallback() {
            @Override
            public void call(IndexRecord record) {
                record.addValue("REV_CREATED", operation.getCommit().getTimestamp());
            }
        });
    }

    @Override
    public void execute(final UpdateOperation operation) {
        execute(operation, new IndexRecordCallback() {
            @Override
            public void call(IndexRecord record) {
                record.addValue("REV_MODIFIED", operation.getCommit().getTimestamp());
            }
        });
    }

    @Override
    public void execute(DeleteOperation operation) {
        delete(get(operation.getIndex()), collectRecords(operation, null));
    }

    @Override
    protected TableFactory getTableFactory() {
        return tableFactory;
    }

    public static final class HeadTableFactory extends AbstractTableFactory {

        private HeadTableFactory(TypeMap typeMap, NameTranslator<Index<?>> tableNameTranslator,
                NameTranslator<IndexField<?>> columnNameTranslator) {
            super(typeMap, tableNameTranslator, columnNameTranslator);
        }

        @Override
        protected void onBeforeCreate(Table table, JdbcIndex<?> index) {
            super.onBeforeCreate(table, index);

            table.addElement(new Column("REV_CREATED", getTypeMap().getType(Date.class)));
        }

        @Override
        protected void onAfterCreate(Table table, JdbcIndex<?> index) {
            super.onAfterCreate(table, index);

            index.setHeadTableName(table.getName());

            String idProperty = Introspector.getOpenEngSBModelIdProperty(index.getModelClass());

            for (IndexField<?> field : index.getFields()) {
                if (field.getName().equals(idProperty)) {
                    table.addElement(new PrimaryKeyConstraint(field.getMappedName()));
                    break;
                }
            }
        }

        @Override
        protected void onAfterFieldVisit(Table table, Column column, IndexField<?> field) {
            super.onAfterFieldVisit(table, column, field);

            JdbcIndexField<?> jdbcField = (JdbcIndexField<?>) field;

            jdbcField.setMappedName(column.getName());
            jdbcField.setTypeName(column.getType().getName());
        }
    }
}
