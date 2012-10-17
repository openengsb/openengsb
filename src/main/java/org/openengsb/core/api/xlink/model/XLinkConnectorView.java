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
import java.util.Map;

/**
 * Modelclass of a XLink view, of a remote tool. 
 * Viewinformation is sent by a remote connector during a XLink registration. 
 * Must provide an unique viewId, a human readable name and descriptions 
 * in different languages.
 */
public class XLinkConnectorView  implements Serializable {
    
    /**
     * Unique Id of the View
     */
    private String viewId;
    
    /**
     * Human readable name of the view
     */
    private String name;
    
    /**
     * Map with locale strings as key (such as "en" and "de") and an description of the
     * view in the specified language. Implementation must make sure that a default 
     * value is returned if a locale is not contained. If the system encounters
     * a null-value for a certain locale, the first entry of the map is taken 
     * instead.
     */
    private Map<String, String> descriptions;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final XLinkConnectorView other = (XLinkConnectorView) obj;
        if ((this.viewId == null) ? (other.viewId != null) : !this.viewId.equals(other.viewId)) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.descriptions != other.descriptions 
                && (this.descriptions == null || !this.descriptions.equals(other.descriptions))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.viewId != null ? this.viewId.hashCode() : 0);
        hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 71 * hash + (this.descriptions != null ? this.descriptions.hashCode() : 0);
        return hash;
    }
    
    public XLinkConnectorView() {
        
    }

    public XLinkConnectorView(String viewId, String name, Map<String, String> descriptions) {
        this.viewId = viewId;
        this.name = name;
        this.descriptions = descriptions;
    }

    /**
     * Human readable name of the view
     */    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Unique Id of the View
     */
    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    /**
     * Map with locale strings as key (such as "en" and "de") and an description of the
     * view in the specified language. Implementation must make sure that a default 
     * value is returned if a locale is not contained. If the system encounters
     * a null-value for a certain locale, the first entry of the map is taken 
     * instead.
     */   
    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Map<String, String> descriptions) {
        this.descriptions = descriptions;
    }
    
}
