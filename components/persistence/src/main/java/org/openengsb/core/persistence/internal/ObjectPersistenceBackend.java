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

package org.openengsb.core.persistence.internal;

import java.io.File;

import org.openengsb.core.api.persistence.PersistenceException;

/**
 * Interface capsulating the read/write operations against the serialisation database.
 */
public interface ObjectPersistenceBackend {
    /**
     * Writes an object into the specified file
     */
    void writeDatabaseObject(Object obj, File file) throws PersistenceException;

    /**
     * Reads an object from an specified file.
     */
    Object readDatabaseObject(File file) throws PersistenceException;
}
