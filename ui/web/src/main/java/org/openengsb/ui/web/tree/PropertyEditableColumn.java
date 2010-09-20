/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.web.tree;

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
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.service.DomainService;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("serial")
public class PropertyEditableColumn extends PropertyRenderableColumn {

    private DomainService domainService;

    public PropertyEditableColumn(ColumnLocation location, String header, String propertyExpression,
            DomainService domainService) {
        super(location, header, propertyExpression);
        this.domainService = domainService;
    }

    @Override
    public Component newCell(MarkupContainer parent, String id, TreeNode node, int level) {
        DefaultMutableTreeNode fieldNode = (DefaultMutableTreeNode) node;
        final ModelBean userObject = (ModelBean) fieldNode.getUserObject();

        if (Pattern.matches("/domains/.+/defaultConnector/id", userObject.getKey())) {
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
        List<DomainProvider> domains = domainService.domains();
        for (DomainProvider domainProvider : domains) {
            String domainProvierName = domainProvider.getId();
            if (("/domains/" + domainProvierName + "/defaultConnector/id").equals(keyPath)) {
                Class<? extends Domain> domainInterface = domainProvider.getDomainInterface();
                List<ServiceReference> serviceReferencesForConnector = domainService
                        .serviceReferencesForDomain(domainInterface);
                for (ServiceReference serviceReferce : serviceReferencesForConnector) {
                    String type = (String) serviceReferce.getProperty("openengsb.service.type");
                    if (!"domain".equals(type)) { // it is an connector
                        services.add((String) (serviceReferce.getProperty("id")));
                    }

                }
            }
        }
        return services;
    }
}
