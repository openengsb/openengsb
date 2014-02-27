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
 * A table constraint.
 */
public abstract class Constraint implements TableElement {

    /**
     * The columns this key consists of.
     */
    private List<String> columns;

    public Constraint() {
        this(new ArrayList<String>());
    }

    public Constraint(String column) {
        this(new ArrayList<String>(1));
        addColumn(column);
    }

    public Constraint(String... columns) {
        this(Arrays.asList(columns));
    }

    public Constraint(List<String> columns) {
        this.columns = columns;
    }

    /**
     * Adds a column to the compound the key consists of.
     * 
     * @param column the Column to add
     */
    public void addColumn(String column) {
        columns.add(column);
    }

    public List<String> getColumns() {
        return columns;
    }

}
