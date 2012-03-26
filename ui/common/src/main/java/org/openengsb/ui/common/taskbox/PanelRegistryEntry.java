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

package org.openengsb.ui.common.taskbox;

import java.io.Serializable;

import org.apache.wicket.markup.html.panel.Panel;

public class PanelRegistryEntry implements Serializable {

    private static final long serialVersionUID = 8597716307328752344L;
    private String taskType;
    private Class<? extends Panel> panelClass;

    public PanelRegistryEntry(String taskType) {
        this.taskType = taskType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((taskType == null) ? 0 : taskType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PanelRegistryEntry other = (PanelRegistryEntry) obj;
        if (taskType == null) {
            return true;
        } else if (!taskType.equals(other.taskType)) {
            return false;
        }
        return true;
    }

    public PanelRegistryEntry(String taskType, Class<? extends Panel> panelClass) {
        this.taskType = taskType;
        this.panelClass = panelClass;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Class<? extends Panel> getPanelClass() {
        return panelClass;
    }

    public void setPanelClass(Class<? extends Panel> panelClass) {
        this.panelClass = panelClass;
    }
}
