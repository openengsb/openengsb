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

import org.openengsb.core.api.edb.EDBCommit;
import org.openengsb.core.api.edb.EDBException;

/**
 * Defines the functions of the error hook for the EDB component. All services in the OSGi environment providing this
 * interface which are exported by any bundle, will be called if an error in the EDB commit procedure happens.
 */
public interface EDBErrorHook {

    /**
     * This function will be called if any error happens in the EDB commit procedure. This function can define the next
     * steps happening in the EDB in that way: 
     * - it returns an EDBCommit object: In this case the EDB try to commit the returned EDBCommit 
     *   instead of the error-prone one. 
     * - it throws an EDBException: the EDB throws the new EDBException instead of the old one. 
     * - it returns null: The EDB ignore the result of this ErrorHook.
     * 
     * If it throws any other exception, it gets caught, logged and ignored.
     */
    EDBCommit onError(EDBCommit commit, Exception cause) throws EDBException;
}
