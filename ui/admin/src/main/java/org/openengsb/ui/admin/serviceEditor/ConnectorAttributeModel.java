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

package org.openengsb.ui.admin.serviceEditor;

import org.apache.wicket.model.IModel;
import org.openengsb.core.api.model.ConnectorDescription;

@SuppressWarnings("serial")
class ConnectorAttributeModel implements IModel<String> {

    private IModel<ConnectorDescription> desc;
    private String key;

    public ConnectorAttributeModel(IModel<ConnectorDescription> desc, String key) {
        this.desc = desc;
        this.key = key;
    }

    @Override
    public void detach() {
        // do nothing
    }

    @Override
    public String getObject() {
        return desc.getObject().getAttributes().get(key);
    }

    @Override
    public void setObject(String object) {
        desc.getObject().getAttributes().put(key, object);
    }
}
