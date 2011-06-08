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

package org.openengsb.core.api.workflow.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A WorkflowRepresentation contains all the necessary data to describe and export a Workflow created via the
 * WorkflowEditor. A Name as well as a root ActionRepresentation have to be set. Every Workflow has to start with an
 * Action that is triggered when the workflow is started. EndRepresentations are stored in the WorkflowRepresentation
 * and referenced in ActionRepresentations and EventRepresentations to be able to share End nodes between them.
 */
@XmlRootElement
public class WorkflowRepresentation implements Serializable {
    private static final long serialVersionUID = -6870242194273870758L;

    private String name;
    private ActionRepresentation root = new ActionRepresentation();

    private List<EndRepresentation> endNodes = new ArrayList<EndRepresentation>();

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final ActionRepresentation getRoot() {
        return root;
    }

    public final void setRoot(ActionRepresentation root) {
        this.root = root;
    }

    public void addEndNode(EndRepresentation end) {
        this.endNodes.add(end);
    }

    public List<EndRepresentation> getEndNodes() {
        return this.endNodes;
    }

}
