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

import java.lang.reflect.Field;

import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.api.IndexFieldVisitor;
import org.openengsb.core.edbi.api.NameTranslator;
import org.openengsb.core.edbi.jdbc.api.TableFactory;
import org.openengsb.core.edbi.jdbc.api.TypeMap;
import org.openengsb.core.edbi.jdbc.sql.Column;
import org.openengsb.core.edbi.jdbc.sql.DataType;
import org.openengsb.core.edbi.jdbc.sql.Table;
import org.openengsb.core.edbi.jdbc.util.Introspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractTableFactory
 */
public abstract class AbstractTableFactory implements TableFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTableFactory.class);

    private TypeMap typeMap;
    private NameTranslator<Index<?>> indexNameTranslator;
    private NameTranslator<IndexField<?>> columnNameTranslator;

    protected AbstractTableFactory() {

    }

    public AbstractTableFactory(TypeMap typeMap, NameTranslator<Index<?>> tableNameTranslator,
            NameTranslator<IndexField<?>> columnNameTranslator) {
        this.typeMap = typeMap;
        this.indexNameTranslator = tableNameTranslator;
        this.columnNameTranslator = columnNameTranslator;
    }

    @Override
    public Table create(JdbcIndex<?> index) {
        final Table table = new Table();

        onBeforeCreate(table, index);

        if (table.getName() == null) {
            table.setName(getIndexNameTranslator().translate(index));
        }

        index.accept(new IndexFieldVisitor() {
            @Override
            public void visit(IndexField<?> field) {
                onBeforeFieldVisit(table, field);

                DataType type = getTypeMap().getType(field.getType());
                if (type == null) {
                    onMissingTypeVisit(table, field);
                    return;
                }
                ((JdbcIndexField) field).setMappedType(type);

                Column column = new Column(getColumnNameTranslator().translate(field), type);

                table.addElement(column);
                onAfterFieldVisit(table, column, field);
            }
        });

        onAfterCreate(table, index);

        return table;
    }

    /**
     * Called when type map returns null.
     * 
     * @param table the table to be created
     * @param field the field being visited and has no type information
     */
    protected void onMissingTypeVisit(Table table, IndexField<?> field) {
        if (!Introspector.isModelClass(field.getType())) {
            return;
        }

        Field idField = Introspector.getOpenEngSBModelIdField(field.getType());

        if (idField == null) {
            LOG.warn("@Model class {} does not have an @OpenEngSBModelId", field.getType());
            return;
        }

        DataType type = getTypeMap().getType(idField.getType());

        if (type == null) {
            LOG.warn("@OpenEngSBModelId field {} has an unmapped type {}", field.getName(), field.getType());
            return;
        }

        ((JdbcIndexField) field).setMappedType(type);

        Column column = new Column(getColumnNameTranslator().translate(field), type);
        table.addElement(column); // will hold the models OID

        onAfterFieldVisit(table, column, field);
    }

    /**
     * Called directly after the (empty) Table object was instantiated.
     * 
     * @param table the table being created
     * @param index the source index
     */
    protected void onBeforeCreate(Table table, JdbcIndex<?> index) {
        // hook
    }

    /**
     * Called directly before the Table is returned.
     * 
     * @param table the table being created
     * @param index the source index
     */
    protected void onAfterCreate(Table table, JdbcIndex<?> index) {
        // hook
    }

    /**
     * Called directly after the visit method of the IndexFieldVisitor is called.
     * 
     * @param table the table being created
     * @param field the field being visited
     */
    protected void onBeforeFieldVisit(Table table, IndexField<?> field) {
        // hook
    }

    /**
     * Called directly before the visit method of the IndexFieldVisitor is exited.
     * 
     * @param table the table being created
     * @param column the column that was created based on the field name
     * @param field the field being visited
     */
    protected void onAfterFieldVisit(Table table, Column column, IndexField<?> field) {
        // hook
    }

    public TypeMap getTypeMap() {
        return typeMap;
    }

    public void setTypeMap(TypeMap typeMap) {
        this.typeMap = typeMap;
    }

    public NameTranslator<Index<?>> getIndexNameTranslator() {
        return indexNameTranslator;
    }

    public void setIndexNameTranslator(NameTranslator<Index<?>> indexNameTranslator) {
        this.indexNameTranslator = indexNameTranslator;
    }

    public NameTranslator<IndexField<?>> getColumnNameTranslator() {
        return columnNameTranslator;
    }

    public void setColumnNameTranslator(NameTranslator<IndexField<?>> columnNameTranslator) {
        this.columnNameTranslator = columnNameTranslator;
    }
}
