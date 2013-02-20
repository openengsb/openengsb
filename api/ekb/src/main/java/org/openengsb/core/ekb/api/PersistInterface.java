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

package org.openengsb.core.ekb.api;

/**
 * The persist interface provides the functions to maintain the models of the EDB. This includes the conversion of
 * models and sanity checks of models.
 */
public interface PersistInterface {

    /**
     * Does a sanity check of the EKBCommit and the status of the EDB when this models are changed. After passed sanity
     * check, the models are persisted.
     */
    void commit(EKBCommit commit) throws SanityCheckException, EKBException;

    /**
     * Persist the changes of the EKBCommit without performing sanity checks of them.
     */
    void forceCommit(EKBCommit commit) throws EKBException;

    /**
     * Only perform the sanity checks of the EKBCommit.
     */
    SanityCheckReport check(EKBCommit commit) throws EKBException;
}
