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

/**
 * This interface is used by the CallRouter to route calls to the explicit ports implementations. If you like to
 * implement a port you have to implement this interface and export is as an OSGi service adding a portsId. For example
 * this could look like the following in blueprint:
 *
 * <code>
 * &lt;service interface=&quot;org.openengsb.core.common.communication.OutgoingPort&quot;&gt;
 *  &lt;service-properties&gt;
 *    &lt;entry key=&quot;id&quot; value=&quot;jms-json&quot; /&gt;
 *  &lt;/service-properties&gt;
 *  &lt;ref component-id=&quot;portBean&quot;/&gt;
 * &lt;/service&gt;
 * </code>
 */
public interface OutgoingPort {

    /**
     * This method is typically called by the CallRouter. Since this method is called in an own thread it is not
     * required to implement such logic in the service.
     */
    void send(MethodCallMessage call) throws RemoteCommunicationException;

    /**
     * This method is to be called synced which means that the service itself should block till an answer is available
     * and return the answer the moment it becomes available.
     */
    MethodResultMessage sendSync(MethodCallMessage call) throws RemoteCommunicationException;

}
