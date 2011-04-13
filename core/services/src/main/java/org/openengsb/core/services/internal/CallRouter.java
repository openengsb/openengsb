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

package org.openengsb.core.services.internal;

import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;
import org.openengsb.core.api.remote.RemoteCommunicationException;

/**
 * The {@link CallRouter} is registered as OSGi Service and should be used to send a {@link MethodCall} via a specific
 * OutgoingPort (via it's portId) to a specific destination.
 */
public interface CallRouter {

    /**
     * Executes a {@link MethodCall} via a defined port at a given destination.
     */
    void call(String portId, String destination, MethodCall call) throws OsgiServiceNotAvailableException,
        RemoteCommunicationException;

    /**
     * This method does the same as {@link #call(String, String, MethodCall)} but blocks till a result is available
     * returned as {@link MethodReturn}. If no result is available within an error is returned.
     */
    MethodReturn callSync(String portId, String destination, MethodCall call) throws OsgiServiceNotAvailableException,
        RemoteCommunicationException;

}
