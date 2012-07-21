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

package org.openengsb.infrastructure.ldap.internal;

import org.apache.directory.shared.ldap.model.entry.Entry;

/**
 * Since Ldap does not allow duplicate Dns, create or update methods can throw
 * this exception to communicate a negative server response related to the
 * prevention of such inconsistencies.
 * */
public class EntryAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = -3355338506299992562L;

    private final Entry entry;

    /**
     * @param entry the {@link Entry} which was rejected by the server
     * */
    public EntryAlreadyExistsException(Entry entry) {
        super();
        this.entry = entry;
    }

    /**
     * @param entry the {@link Entry} which was rejected by the server
     * */
    public EntryAlreadyExistsException(String message, Throwable cause, Entry entry) {
        super(message, cause);
        this.entry = entry;
    }

    /**
     * @param entry the {@link Entry} which was rejected by the server
     * */
    public EntryAlreadyExistsException(String message, Entry entry) {
        super(message);
        this.entry = entry;
    }

    /**
     * @param entry the {@link Entry} which was rejected by the server
     * */
    public EntryAlreadyExistsException(Throwable cause, Entry entry) {
        super(cause);
        this.entry = entry;
    }

    /**
     * Returns the {@link Entry} which was rejected by the server
     * */
    public Entry getEntry() {
        return entry;
    }

}
