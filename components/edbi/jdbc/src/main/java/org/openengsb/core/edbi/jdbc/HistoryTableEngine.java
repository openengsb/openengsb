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

import static org.openengsb.core.edbi.jdbc.sql.Column.Option.AUTO_INCREMENT;

import java.sql.Types;
import java.util.Date;
import java.util.UUID;

import javax.sql.DataSource;

import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexCommit;
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

/**
 * Implementation of a TableEngine, that manages the 'history' table of models. It contains a factory for creating those
 * tables.
 */
public class HistoryTableEngine extends AbstractTableEngine {

    public static final String TABLE_PREFIX = "HISTORY_";

    private HistoryTableFactory tableFactory;

    public HistoryTableEngine(DataSource dataSource, TypeMap typeMap) {
        this(dataSource, typeMap, new PrependingNameTranslator<>(new SQLIndexNameTranslator(), TABLE_PREFIX));
    }

    public HistoryTableEngine(DataSource dataSource, TypeMap typeMap, NameTranslator<Index<?>> indexNameTranslator) {
        this(dataSource, typeMap, indexNameTranslator, new SQLIndexFieldNameTranslator());
    }

    public HistoryTableEngine(DataSource dataSource, TypeMap typeMap, NameTranslator<Index<?>> indexNameTranslator,
            NameTranslator<IndexField<?>> indexFieldNameTranslator) {
        this(dataSource, typeMap, indexNameTranslator, indexFieldNameTranslator, new HistoryTableFactory(typeMap,
            indexNameTranslator, indexFieldNameTranslator));
    }

    protected HistoryTableEngine(DataSource dataSource, TypeMap typeMap, NameTranslator<Index<?>> indexNameTranslator,
            NameTranslator<IndexField<?>> indexFieldNameTranslator, HistoryTableFactory tableFactory) {
        super(dataSource, typeMap, indexNameTranslator, indexFieldNameTranslator);

        this.tableFactory = tableFactory;
    }

    @Override
    public void execute(final InsertOperation operation) {
        execute(operation, new CommitIndexRecordCallback(operation.getCommit(), "INSERT"));
    }

    @Override
    public void execute(UpdateOperation operation) {
        final InsertOperation insert = new InsertOperation(operation);

        execute(insert, new CommitIndexRecordCallback(insert.getCommit(), "UPDATE"));
    }

    @Override
    public void execute(DeleteOperation operation) {
        final InsertOperation insert = new InsertOperation(operation);

        execute(insert, new CommitIndexRecordCallback(insert.getCommit(), "DELETE"));
    }

    @Override
    protected TableFactory getTableFactory() {
        return tableFactory;
    }

    protected static class CommitIndexRecordCallback implements IndexRecordCallback {
        private final String operation;
        private final IndexCommit commit;

        public CommitIndexRecordCallback(IndexCommit commit, String operation) {
            this.commit = commit;
            this.operation = operation;
        }

        @Override
        public void call(IndexRecord record) {
            record.addValue("REV_OPERATION", operation, Types.VARCHAR);

            record.addValue("REV_COMMIT", commit.getCommitId(), Types.VARCHAR);
            record.addValue("REV_TIMESTAMP", commit.getTimestamp(), Types.TIMESTAMP);
            record.addValue("REV_USER", commit.getUser(), Types.VARCHAR);
            record.addValue("REV_CONTEXTID", commit.getContextId(), Types.VARCHAR);
            record.addValue("REV_DOMAINID", commit.getDomainId(), Types.VARCHAR);
            record.addValue("REV_CONNECTORID", commit.getConnectorId(), Types.VARCHAR);
            record.addValue("REV_INSTANCEID", commit.getInstanceId(), Types.VARCHAR);
        }
    }

    /**
     * A TableFactory that creates 'history' tables for models.
     */
    public static class HistoryTableFactory extends AbstractTableFactory {

        public HistoryTableFactory(TypeMap typeMap, NameTranslator<Index<?>> tableNameTranslator,
                NameTranslator<IndexField<?>> columnNameTranslator) {
            super(typeMap, tableNameTranslator, columnNameTranslator);
        }

        @Override
        protected void onBeforeCreate(Table table, JdbcIndex<?> index) {
            super.onBeforeCreate(table, index);

            table.addElement(new Column("REV_ID", getTypeMap().getType(Long.class), AUTO_INCREMENT));
            table.addElement(new Column("REV_COMMIT", getTypeMap().getType(UUID.class)));
            table.addElement(new Column("REV_TIMESTAMP", getTypeMap().getType(Date.class)));
            table.addElement(new Column("REV_OPERATION", getTypeMap().getType(String.class)));
            table.addElement(new Column("REV_USER", getTypeMap().getType(String.class)));
            table.addElement(new Column("REV_CONTEXTID", getTypeMap().getType(String.class)));
            table.addElement(new Column("REV_DOMAINID", getTypeMap().getType(String.class)));
            table.addElement(new Column("REV_CONNECTORID", getTypeMap().getType(String.class)));
            table.addElement(new Column("REV_INSTANCEID", getTypeMap().getType(String.class)));
        }

        @Override
        protected void onAfterCreate(Table table, JdbcIndex<?> index) {
            super.onAfterCreate(table, index);

            table.addElement(new PrimaryKeyConstraint("REV_ID"));

            index.setHistoryTableName(table.getName());
        }
    }
}
