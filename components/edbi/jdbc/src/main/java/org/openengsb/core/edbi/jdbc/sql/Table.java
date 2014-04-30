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
import java.util.Arrays;
import java.util.List;

/**
 * A database table, uniquely identified by a name and consists of {@code TableElement}s.
 */
public class Table {
    private String name;
    private List<TableElement> elements;

    public Table() {
        this(null);
    }

    public Table(String name) {
        this(name, new ArrayList<TableElement>());
    }

    public Table(String name, TableElement... elements) {
        this(name, new ArrayList<>(Arrays.asList(elements)));
    }

    public Table(String name, List<TableElement> elements) {
        this.name = name;
        this.elements = elements;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addElement(TableElement element) {
        this.elements.add(element);
    }

    public List<TableElement> getElements() {
        return elements;
    }

    public void setElements(List<TableElement> elements) {
        this.elements = elements;
    }

    public void accept(TableElementVisitor visitor) {
        for (TableElement element : elements) {
            element.accept(visitor);
        }
    }

    public PrimaryKeyConstraint getPrimaryKey() {
        for (TableElement element : elements) {
            if (element instanceof PrimaryKeyConstraint) {
                return (PrimaryKeyConstraint) element;
            }
        }

        return null;
    }

    public ColumnSet getColumns() {
        final ColumnSet columns = new ColumnSet();

        accept(new ColumnVisitor() {

            @Override
            protected void onVisit(Column column) {
                columns.add(column);
            }
        });

        return columns;
    }

}
