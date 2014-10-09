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
import java.util.List;

import org.openengsb.core.api.model.ModelDescription;

public class XLinkObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private String connectorId;
    private String applicationName;
    private Object modelObject;
    private ModelDescription modelDescription;
    private List<XLinkConnectorView> views;

    public XLinkObject() {

    }

    public XLinkObject(String connectorId, String applicationName, Object modelObject, 
            ModelDescription modelDescription, List<XLinkConnectorView> views) {
        this.connectorId = connectorId;
        this.applicationName = applicationName;
        this.modelObject = modelObject;
        this.modelDescription = modelDescription;
        this.views = views;
    }

    public String getConnectorId() {
        return connectorId;
    }
    
    public String getApplicationName() {
        return applicationName;
    }
    
    public Object getModelObject() {
        return modelObject;
    }

    public void setModelObject(Object modelObject) {
        this.modelObject = modelObject;
    }

    public ModelDescription getModelDescription() {
        return modelDescription;
    }

    public void setModelDescription(ModelDescription modelDescription) {
        this.modelDescription = modelDescription;
    }

    public List<XLinkConnectorView> getViews() {
        return views;
    }

    public void setViews(List<XLinkConnectorView> views) {
        this.views = views;
    }
}
