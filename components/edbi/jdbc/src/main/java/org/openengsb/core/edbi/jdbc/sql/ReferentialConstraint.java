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

import java.util.Arrays;
import java.util.List;

/**
 * ReferentialConstraint
 */
public class ReferentialConstraint extends Constraint {

    private String referenceTable;
    private List<String> referenceColumns;

    public ReferentialConstraint(String referencingColumn, String referenceTable, String referenceColumn) {
        super(referencingColumn);
        this.referenceTable = referenceTable;
        this.referenceColumns = Arrays.asList(referenceColumn);
    }

    public ReferentialConstraint(String[] referencingColumns, String referenceTable, String[] referenceColumns) {
        super(referencingColumns);
        this.referenceTable = referenceTable;
        this.referenceColumns = Arrays.asList(referenceColumns);
    }

    public ReferentialConstraint(List<String> referencingColumns, String referenceTable,
            List<String> referenceColumns) {
        super(referencingColumns);
        this.referenceTable = referenceTable;
        this.referenceColumns = referenceColumns;
    }

    @Override
    public void accept(TableElementVisitor visitor) {
        visitor.visit(this);
    }

    public String getReferenceTable() {
        return referenceTable;
    }

    public void setReferenceTable(String referenceTable) {
        this.referenceTable = referenceTable;
    }

    public List<String> getReferenceColumns() {
        return referenceColumns;
    }

    /**
     * Adds a table column to the reference columns.
     * 
     * @param column a table column
     */
    public void addReferenceColumn(String column) {
        this.referenceColumns.add(column);
    }
}
