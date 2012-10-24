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

package org.openengsb.core.api.xlink.model;

import java.io.Serializable;

import org.openengsb.core.api.model.ModelDescription;

/**
 * Modelclass to transfere XLink Model/View associations from a remote tool
 * during the XLink registration.
 */
public class ModelToViewsTuple implements Serializable {
    
    /**
     * Identifier of an OpenEngSBModel
     */
    private ModelDescription description;
    
    /**
     * Array of Views, offered by the remote tool
     */
    private XLinkConnectorView[] views;

    public ModelToViewsTuple() {
        
    }
    
    public ModelToViewsTuple(ModelDescription description, XLinkConnectorView[] views) {
        this.description = description;
        this.views = views;
    }
        
    /**
     * Identifier of an OpenEngSBModel
     */
    public ModelDescription getDescription() {
        return description;
    }

    public void setDescription(ModelDescription description) {
        this.description = description;
    }
    
    /**
     * Array of Views, offered by the remote tool
     */
    public XLinkConnectorView[] getViews() {
        return views;
    }

    public void setViews(XLinkConnectorView[] views) {
        this.views = views;
    }
    
}
