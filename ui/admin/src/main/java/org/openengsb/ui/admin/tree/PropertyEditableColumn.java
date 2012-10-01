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

package org.openengsb.ui.admin.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.IRenderable;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyRenderableColumn;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.WiringService;
import org.openengsb.ui.admin.tree.dropDownPanel.DropDownPanel;
import org.openengsb.ui.admin.tree.editablePanel.EditablePanel;
import org.ops4j.pax.wicket.api.PaxWicketBean;

public class PropertyEditableColumn extends PropertyRenderableColumn<Void> {

    private static final long serialVersionUID = -6534594928754905369L;

    @PaxWicketBean
    private List<DomainProvider> domains;

    @PaxWicketBean
    private WiringService wiringService;

    public PropertyEditableColumn(ColumnLocation location, String header, String propertyExpression) {
        super(location, header, propertyExpression);
    }

    @SuppressWarnings("serial")
    @Override
    public Component newCell(MarkupContainer parent, String id, TreeNode node, int level) {
        DefaultMutableTreeNode fieldNode = (DefaultMutableTreeNode) node;
        final ModelBean userObject = (ModelBean) fieldNode.getUserObject();

        if (Pattern.matches("/domain/.+/defaultConnector/id", userObject.getKey())) {
            return new DropDownPanel(id, new PropertyModel<String>(node, getPropertyExpression()),
                new LoadableDetachableModel<List<String>>() {
                    @Override
                    protected List<String> load() {
                        return getServices(userObject.getKey());
                    }
                });
        }
        return new EditablePanel(id, new PropertyModel<String>(node, getPropertyExpression()));
    }

    @Override
    public IRenderable newCell(TreeNode node, int level) {
        if (getTreeTable().getTreeState().isNodeSelected(node)) {
            return null;
        } else {
            return super.newCell(node, level);
        }
    }

    private List<String> getServices(String keyPath) {
        List<String> services = new ArrayList<String>();
        for (DomainProvider domainProvider : domains) {
            String domainProvierName = domainProvider.getId();
            if (("/domain/" + domainProvierName + "/defaultConnector/id").equals(keyPath)) {
                Class<? extends Domain> domainInterface = domainProvider.getDomainInterface();
                List<? extends Domain> connectorInstances =
                    wiringService.getDomainEndpoints(domainInterface, "*");
                for (Domain service : connectorInstances) {
                    services.add(service.getInstanceId());
                }
            }
        }
        return services;
    }
}
