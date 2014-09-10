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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Modelclass of a XLink view of a remote tool. Viewinformation is sent by a remote connector during an XLink
 * registration.
 */
public class XLinkConnectorView implements Serializable {

    private static final long serialVersionUID = 1L;

    private String viewId;
    private String name;
    private Map<Locale, String> descriptions;

    public XLinkConnectorView() {

    }

    public XLinkConnectorView(String viewId, String name, Map<Locale, String> descriptions) {
        this.viewId = viewId;
        this.name = name;
        this.descriptions = descriptions;
    }

    /**
     * Returns the unique id of the view.
     * 
     * @return the unique id of the view.
     */
    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    /**
     * 
     * @return human readable name of the view.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return the view description.
     */
    public Map<Locale, String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Map<Locale, String> descriptions) {
        this.descriptions = descriptions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof XLinkConnectorView) {
            final XLinkConnectorView other = (XLinkConnectorView) obj;
            return Objects.equals(viewId, other.viewId)
                    && Objects.equals(name, other.name)
                    && Objects.equals(descriptions, other.descriptions);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(viewId, name, descriptions);
    }
}
