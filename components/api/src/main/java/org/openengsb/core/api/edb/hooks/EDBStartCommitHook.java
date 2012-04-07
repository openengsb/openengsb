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

package org.openengsb.core.api.edb.hooks;

import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.ekb.EKBCommit;

/**
 * Defines the functions of the start-commit hook for the EDB component. All services in the OSGi environment providing
 * this interface which are exported by any bundle, will be called when the EDB gets an EKBCommit to persist. It is the
 * first hook that is called in the commit procedure.
 */
public interface EDBStartCommitHook {

    /**
     * This function is called when the EDB gets an EKBCommit object to persist and before the pre-commit hook is
     * called. If this method throws an exception, it is directly returned to the calling instance.
     */
    void onStartCommit(EKBCommit commit) throws EDBException;
}
