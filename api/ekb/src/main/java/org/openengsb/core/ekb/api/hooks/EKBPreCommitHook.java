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

package org.openengsb.core.ekb.api.hooks;

import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.EKBException;

/**
 * Defines the functions of the pre-commit hook for the EKB persistence persist component. All services in the OSGi
 * environment providing this interface which are exported by any bundle, will be called before the EKB tries to run a
 * commit.
 * 
 * Normally this hook is used to extend and adapt the EKBCommit (and with it the elements of the EKBCommit) if needed.
 */
public interface EKBPreCommitHook {

    /**
     * This function is called before the EKB tries to persist the changes of the EKBCommit. If this methods throws an
     * EKBException, the commit process get aborted. If an EKBException is thrown in a pre commit hook, the commit
     * procedure is aborted. Every other Exception will be logged and ignored.
     */
    void onPreCommit(EKBCommit commit) throws EKBException;
}
