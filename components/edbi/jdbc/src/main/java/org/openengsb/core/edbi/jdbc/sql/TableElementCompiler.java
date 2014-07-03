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
package org.openengsb.core.edbi.jdbc.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * {@link TableElementVisitor} implementation that builds the table-element definition for an SQL <code>CREATE</code>
 * statement.
 */
public class TableElementCompiler implements TableElementVisitor {

    private Table table;

    private List<String> elements;

    public TableElementCompiler(Table table) {
        this.table = table;
    }

    /**
     * Executes the compilation process.
     * 
     * @return a list of Strings, each containing a single SQL table-element definition for an SQL <code>CREATE</code>
     *         statement.
     */
    public synchronized List<String> compile() {
        elements = new ArrayList<>();
        table.accept(this);
        return elements;
    }

    /**
     * Returns the entire table-element definition as SQL string, usable in an SQL <code>CREATE</code> statement.
     * 
     * @return SQL code
     */
    public String toSql() {
        return StringUtils.join(compile(), ",");
    }

    @Override
    public void visit(Column element) {
        elements.add(compile(element));
    }

    @Override
    public void visit(UniqueConstraint element) {
        elements.add(compile(element));
    }

    @Override
    public void visit(PrimaryKeyConstraint element) {
        elements.add(compile(element));
    }

    @Override
    public void visit(ReferentialConstraint element) {
        elements.add(compile(element));
    }

    protected String compile(Column element) {
        StringBuilder str = new StringBuilder();

        str.append(element.getName());
        str.append(" ");

        DataType type = element.getType();
        str.append(type.getName());

        if (type.getScale() > 0) {
            str.append("(");
            str.append(Integer.toString(type.getScale()));
            str.append(")");
        }

        if (element.hasOption(Column.Option.NOT_NULL)) {
            str.append(" NOT NULL");
        }
        if (element.hasOption(Column.Option.AUTO_INCREMENT)) {
            str.append(" AUTO_INCREMENT");
        }

        return str.toString();
    }

    protected String compile(UniqueConstraint element) {
        return "UNIQUE (" + getJoinedColumns(element) + ")";
    }

    protected String compile(PrimaryKeyConstraint element) {
        return "PRIMARY KEY (" + getJoinedColumns(element) + ")";
    }

    protected String compile(ReferentialConstraint element) {
        String columns = getJoinedColumns(element);
        String table = element.getReferenceTable();
        String refs = getJoinedColumns(element.getReferenceColumns());

        return String.format("(%s) REFERENCES %s (%s)", columns, table, refs);
    }

    protected String getJoinedColumns(Constraint constraint) {
        return getJoinedColumns(constraint.getColumns());
    }

    protected String getJoinedColumns(Collection<String> strings) {
        return StringUtils.join(strings, ",");
    }

}
