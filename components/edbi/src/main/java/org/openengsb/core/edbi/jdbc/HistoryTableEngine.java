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
import org.openengsb.core.edbi.jdbc.operation.InsertOperation;
import org.openengsb.core.edbi.jdbc.sql.Column;
import org.openengsb.core.edbi.jdbc.sql.PrimaryKeyConstraint;
import org.openengsb.core.edbi.jdbc.sql.Table;

/**
 * HistoryTableEngine
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
    public void execute(InsertOperation operation) {

    }

    @Override
    protected TableFactory getTableFactory() {
        return tableFactory;
    }

    public static class HistoryTableFactory extends AbstractTableFactory {

        public HistoryTableFactory(TypeMap typeMap, NameTranslator<Index<?>> tableNameTranslator,
                NameTranslator<IndexField<?>> columnNameTranslator) {
            super(typeMap, tableNameTranslator, columnNameTranslator);
        }

        @Override
        protected void onBeforeCreate(Table table, JdbcIndex<?> index) {
            super.onBeforeCreate(table, index);

            table.addElement(new Column("REV_ID", getTypeMap().getType(Long.class), AUTO_INCREMENT));
            table.addElement(new Column("REV_CREATED", getTypeMap().getType(Date.class)));
            // TODO: more columns ...
        }

        @Override
        protected void onAfterCreate(Table table, JdbcIndex<?> index) {
            super.onAfterCreate(table, index);

            table.addElement(new PrimaryKeyConstraint("REV_ID"));
        }
    }
}
