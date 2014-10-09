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

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.xlink.model.XLinkConnectorView;
import org.openengsb.core.api.xlink.model.XLinkObject;
// CHECKSTYLE:OFF
import org.openengsb.core.api.xlink.service.XLinkConnectorManager;
// CHECkSTYLE:ON

/**
 * Every Domain that wants to offer XLinking to its Connectors, must implement this interface. Connectors can choose
 * whether they want to participate in XLinking, or not.
 */
@MixinDomain("linking")
public interface LinkingSupport {

    /**
     * Callback method that is triggered as a response 
     * to {@link XLinkConnectorManager#requestXLinkSwitch(String, String, Object, boolean)}. 
     * 
     * @param xLinkObjects the list of {@link XLinkObject}s that can be opened.
     */
    void showXLinks(XLinkObject[] xLinkObjects);
    
    /**
     * Implementors should open the requested view with the given object. 
     * 
     * @param modelDescription the model description
     * @param modelObject the model object to display
     * @param view the view to display the model object with
     */
    void openXLink(ModelDescription modelDescription, Object modelObject, XLinkConnectorView view);
}
