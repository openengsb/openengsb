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

package org.openengsb.core.api.remote;

import org.openengsb.core.api.OsgiServiceNotAvailableException;

/**
 * The request handler is registered as OSGi service by core common and should be used for ports implementations to
 * handle method calls to the OpenEngSB..
 */
public interface RequestHandler {

    /**
     * Assumes that a method call is received from an external resource. Besides of the
     * {@link OsgiServiceNotAvailableException} all exceptions thrown by the clients themselves are wrapped in the
     * {@link MethodResultMessage} object and do not have to bother the client.
     *
     * @throws OsgiServiceNotAvailableException To call methods on the OpenEngSB this method uses OSGi services baring
     *         the risk that endpoint services are currently not available. In this case the caller has to decide if to
     *         try later again or how to abort the process.
     */
    MethodResult handleCall(MethodCall request) throws OsgiServiceNotAvailableException;

}
