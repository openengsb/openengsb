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

package org.openengsb.core.api;

import org.openengsb.core.api.xlink.model.XLinkConnector;

/**
 * Every Domain that wants to offer XLinking to itÂ´s Connectors, must implement this interface. Connectors can choose
 * whether they want to participate in XLinking, or not.
 */
@MixinDomain("linking")
public interface LinkingSupport {

    // @extract-start LinkableDomainOpenLinks
    /**
     * PushMethod to transfere a List of potential Matches, of modelObjects, to the Clienttool. Also defines the Id of
     * View to open Matches in. The transfered modelObjects are instances of the Clienttools model.
     */
    void openXLinks(Object[] modelObjects, String viewId);

    // @extract-end

    // @extract-start LinkableDomainUpdateEvent
    /**
     * During the registration each connector receives an array of all other currently 
     * registered tools from the same host. This function is called every time this array changes.
     * Via this method, the array is kept uptodate. 
     * This information is used to support local switching between tools.
     */
    void onRegisteredToolsChanged(XLinkConnector[] registeredTools);

    // @extract-end

}
