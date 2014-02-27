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
package org.openengsb.core.edbi.jdbc.api;

import org.openengsb.core.edbi.jdbc.JdbcIndex;
import org.openengsb.core.edbi.jdbc.sql.Table;

/**
 * Maps an Index and respective data towards a specific table.
 */
public interface TableEngine {

    /**
     * Checks whether the table this engine manages exists for the given index.
     * 
     * @param index the index to check for
     * @return true if the table exists, false otherwise
     */
    boolean exists(JdbcIndex<?> index);

    /**
     * Creates a new table for the given index.
     * 
     * @param index the index to create the table for
     * @return the created table
     * @throws TableExistsException if the table already exists
     */
    Table create(JdbcIndex<?> index) throws TableExistsException;

    /**
     * Drops the table mapped to the given index.
     *
     * @param index the index to drop the table for
     */
    void drop(JdbcIndex<?> index);

    // void insert();
    // void update();
    // void delete();

}
