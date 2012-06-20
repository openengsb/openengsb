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

import java.util.List;
import org.openengsb.core.api.ekb.ModelDescription;

public class ModelToViewsTupel {
    
    private ModelDescription description;
    private List<XLinkToolView> views;

    public ModelToViewsTupel(ModelDescription description, List<XLinkToolView> views) {
        this.description = description;
        this.views = views;
    }
    
    public ModelDescription getDescription() {
        return description;
    }

    public void setDescription(ModelDescription description) {
        this.description = description;
    }

    public List<XLinkToolView> getViews() {
        return views;
    }

    public void setViews(List<XLinkToolView> views) {
        this.views = views;
    }
    
}
