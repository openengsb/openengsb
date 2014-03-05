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
import org.openengsb.core.edbi.jdbc.operation.DeleteOperation;
import org.openengsb.core.edbi.jdbc.operation.InsertOperation;
import org.openengsb.core.edbi.jdbc.operation.UpdateOperation;

/**
 * Facade used by the IndexEngine towards the EDBI data schema.
 */
public interface SchemaMapper {
    /**
     * Checks whether the schema for the given index exists.
     * 
     * @param index the index to check
     * @return true of the schema exists
     */
    boolean exists(JdbcIndex<?> index);

    /**
     * Creates the data schema for the given index.
     * 
     * @param index the index to create the schema for
     */
    void create(JdbcIndex<?> index);

    /**
     * Drops the data schema for the given index.
     * 
     * @param index the index to drop the schema for
     */
    void drop(JdbcIndex<?> index);

    /**
     * Executes an InsertOperation.
     * 
     * @param operation the operation to execute.
     */
    void execute(InsertOperation operation);

    /**
     * Executes an UpdateOperation.
     * 
     * @param operation the operation to execute.
     */
    void execute(UpdateOperation operation);

    /**
     * Executes an DeleteOperation.
     * 
     * @param operation the operation to execute.
     */
    void execute(DeleteOperation operation);
}
