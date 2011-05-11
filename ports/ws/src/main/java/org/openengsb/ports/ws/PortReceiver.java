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

package org.openengsb.ports.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * Webservice which is used by external clients to send messages (call to) internal services in the OpenEngSB.
 */
@WebService
public interface PortReceiver {

    /**
     * Takes a well defined message in json format and manages the call to the interface which should be used. The
     * answer is also returned as serialized json object.
     */
    @WebMethod
    String receive(String message);

}
