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

/**
 * Represents a database table unique constraint. It must consist of at least one Column. (i.e. may be a compound key).
 */
public class UniqueConstraint extends Constraint {

    public UniqueConstraint() {
        super();
    }

    public UniqueConstraint(String column) {
        super(column);
    }

    /**
     * Creates a new UniqueConstraint instance composed of the given columns.
     * 
     * @param columns the columns the key consists of
     */
    public UniqueConstraint(String... columns) {
        super(columns);
    }

    @Override
    public void accept(TableElementVisitor visitor) {
        visitor.visit(this);
    }

}
