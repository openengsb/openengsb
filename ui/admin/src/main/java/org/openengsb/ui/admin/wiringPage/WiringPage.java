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

package org.openengsb.ui.admin.wiringPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.commons.lang.ArrayUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LabelTree;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.ValueMap;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.common.util.Comparators;
import org.openengsb.ui.admin.basePage.BasePage;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import org.osgi.framework.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SecurityAttribute(key = "org.openengsb.ui.component", value = "WORKFLOW_ADMIN")
@PaxWicketMountPoint(mountPoint = "wiring")
public class WiringPage extends BasePage {

    private static final long serialVersionUID = 4196803215701011090L;

    private static final Logger LOGGER = LoggerFactory.getLogger(WiringPage.class);

    @PaxWicketBean(name = "wiringService")
    private WiringService wiringService;

    @PaxWicketBean(name = "osgiUtilsService")
    private OsgiUtilsService serviceUtils;

    @PaxWicketBean(name = "serviceManager")
    private ConnectorManager serviceManager;

    @PaxWicketBean(name = "ruleManager")
    private RuleManager ruleManager;

    private DropDownChoice<Class<? extends Domain>> domains;
    private LinkTree globals;
    private LinkTree endpoints;
    private TextField<String> txtGlobalName;
    private TextField<String> txtInstanceId;
    private CheckedTree contextList;
    private AjaxSubmitLink wireButton;
    private FeedbackPanel feedbackPanel;

    private String globalName = "";
    private String instanceId = "";

    public WiringPage() {
        initializeDomainChooseForm();
        initializeGlobalNameField();
        initializeInstanceIdField();
        initializeGlobals();
        initializeEndpoints();
        initializeWiringForm();
        initializeFeedbackPanel();
    }

    @SuppressWarnings("serial")
    private void initializeDomainChooseForm() {
        Form<Void> domainChooseForm = new Form<Void>("domainChooseForm");
        domains = new DropDownChoice<Class<? extends Domain>>("domains");
        domains.setOutputMarkupId(true);
        domains.setChoiceRenderer(new ChoiceRenderer<Class<? extends Domain>>("canonicalName"));
        domains.setChoices(createDomainListModel());
        domains.setModel(new Model<Class<? extends Domain>>());
        domains.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                Class<? extends Domain> domainType = domains.getModelObject();
                LOGGER.debug("chosen {}", domainType);
                globals.setModel(createGlobalTreeModel(domainType));
                endpoints.setModel(createEndpointsModel(domainType));
                resetWiringForm(target);
                target.add(globals);
                target.add(endpoints);
            }
        });
        domainChooseForm.add(domains);
        add(domainChooseForm);
    }

    private void initializeGlobalNameField() {
        txtGlobalName = new TextField<String>("globalName");
        txtGlobalName.setOutputMarkupId(true);
        txtGlobalName.setMarkupId("globalName");
    }

    private void initializeInstanceIdField() {
        txtInstanceId = new TextField<String>("instanceId");
        txtInstanceId.setOutputMarkupId(true);
        txtInstanceId.setMarkupId("instanceId");
        txtInstanceId.setEnabled(false);
    }

    private void initializeGlobals() {
        globals = new WiringSubjectTree("globals", txtGlobalName);
        globals.getTreeState().expandAll();
        globals.setOutputMarkupId(true);
        globals.setOutputMarkupPlaceholderTag(true);
        add(globals);
    }

    private void initializeEndpoints() {
        endpoints = new WiringSubjectTree("endpoints", txtInstanceId);
        endpoints.getTreeState().expandAll();
        endpoints.setOutputMarkupId(true);
        endpoints.setOutputMarkupPlaceholderTag(true);
        add(endpoints);
    }

    private void initializeFeedbackPanel() {
        feedbackPanel = new FeedbackPanel("feedbackPanel");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
    }

    @SuppressWarnings("serial")
    private void initializeWiringForm() {
        Form<Object> wiringForm = new Form<Object>("wiringForm");
        wiringForm.setOutputMarkupId(true);
        wiringForm.setDefaultModel(new CompoundPropertyModel<Object>(this));
        wiringForm.add(txtGlobalName);
        wiringForm.add(txtInstanceId);

        contextList = new CheckedTree("contextList", createContextModel());
        contextList.getTreeState().expandAll();
        wiringForm.add(contextList);

        wireButton = new AjaxSubmitLink("wireButton", wiringForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                LOGGER.debug("Start wiring {} with {}", globalName, instanceId);
                if (noGlobalNameSet() || noInstanceIdSet() || noContextSet()) {
                    target.add(feedbackPanel);
                    return;
                }
                ConnectorDescription description;
                try {
                    description = serviceManager.getAttributeValues(instanceId);
                    if (!typeOfGlobalAndServiceAreEqual(description.getDomainType())) {
                        target.add(feedbackPanel);
                        return;
                    }
                } catch (Exception e) {
                    presentAndLogError(new StringResourceModel("wiringInitError", this, null).getString(), e);
                    resetWiringForm(target);
                    return;
                }
                try {
                    updateLocations(instanceId, description);
                } catch (Exception e) {
                    presentAndLogError(new StringResourceModel("wiringError", this, null).getString(), e);
                } finally {
                    resetWiringForm(target);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                LOGGER.warn("Linking Ajax Link produces an error.");
            }
        };
        wiringForm.add(wireButton);
        add(wiringForm);
    }

    private void updateLocations(String connectorId, ConnectorDescription description) throws Exception {
        boolean updated = false;
        ValueMap vmap = new ValueMap();
        vmap.put("globalName", globalName);
        Model<ValueMap> vmapModel = new Model<ValueMap>(vmap);
        for (String context : contextList.getAllChecked()) {
            vmap.put("context", context);
            if (setLocation(globalName, context, description.getProperties())) {
                updated = true;
                info(new StringResourceModel("wiringSuccess", this, vmapModel).getString());
                LOGGER.info("{} got wired with {} in context {}",
                    new Object[]{ globalName, instanceId, context });
            } else {
                info(new StringResourceModel("doubleWiring", this, vmapModel).getString());
                LOGGER.info("{} already wired with {} in context {}",
                    new Object[]{ globalName, instanceId, context });
            }
        }
        if (updated) {
            serviceManager.forceUpdate(connectorId, description);
        }
    }

    /**
     * returns true if location is not already set in the properties, otherwise false
     */
    private boolean setLocation(String global, String context, Map<String, Object> properties) {
        String locationKey = "location." + context;
        Object propvalue = properties.get(locationKey);
        if (propvalue == null) {
            properties.put(locationKey, global);
        } else if (propvalue.getClass().isArray()) {
            Object[] locations = (Object[]) propvalue;
            if (ArrayUtils.contains(locations, global)) {
                return false;
            }
            Object[] newArray = Arrays.copyOf(locations, locations.length + 1);
            newArray[locations.length] = global;
            properties.put(locationKey, newArray);
        } else {
            if (((String) propvalue).equals(global)) {
                return false;
            }
            Object[] newArray = new Object[2];
            newArray[0] = propvalue;
            newArray[1] = global;
            properties.put(locationKey, newArray);
        }
        return true;
    }

    private boolean typeOfGlobalAndServiceAreEqual(String domainNameOfService) {
        String domainTypeOfGlobal = getDomainTypeOfGlobal(globalName);
        String domainTypeOfService = getDomainTypeOfServiceName(domainNameOfService);
        if (domainTypeOfGlobal != null) {
            if (!domainTypeOfGlobal.equals(domainTypeOfService)) {
                info(new StringResourceModel("globalAlreadySet", this, null).getString());
                LOGGER.info("cannot wire {} with {}, because {} has type {}",
                    new Object[]{ globalName, instanceId, globalName, domainTypeOfGlobal });
                return false;
            }
        } else {
            ruleManager.addGlobal(domainTypeOfService, globalName);
            LOGGER.info("created global {} of type {}", globalName, domainTypeOfService);
        }
        return true;
    }

    private String getDomainTypeOfServiceName(String domainName) {
        Filter filter =
            serviceUtils.makeFilter(DomainProvider.class, String.format("(%s=%s)", Constants.DOMAIN_KEY, domainName));
        DomainProvider dp = (DomainProvider) serviceUtils.getService(filter);
        if (dp == null || dp.getDomainInterface() == null) {
            return null;
        }
        return dp.getDomainInterface().getCanonicalName();
    }

    private String getDomainTypeOfGlobal(String glob) {
        return ruleManager.getGlobalType(glob);
    }

    private boolean noGlobalNameSet() {
        if (globalName == null || globalName.trim().isEmpty()) {
            error(new StringResourceModel("globalNotSet", this, null).getString());
            return true;
        }
        return false;
    }

    private boolean noInstanceIdSet() {
        if (instanceId == null || instanceId.isEmpty()) {
            error(new StringResourceModel("instanceIdNotSet", this, null).getString());
            return true;
        }
        return false;
    }

    private boolean noContextSet() {
        if (contextList.getAllChecked().isEmpty()) {
            error(new StringResourceModel("contextNotSet", this, null).getString());
            return true;
        }
        return false;
    }

    private void presentAndLogError(String message, Exception e) {
        error(message + "\n" + e.getLocalizedMessage());
        LOGGER.error("Error during wiring", e);
    }

    @SuppressWarnings("serial")
    private IModel<? extends List<? extends Class<? extends Domain>>> createDomainListModel() {
        return new LoadableDetachableModel<List<? extends Class<? extends Domain>>>() {
            @Override
            protected List<? extends Class<? extends Domain>> load() {
                List<DomainProvider> serviceList = serviceUtils.listServices(DomainProvider.class);
                Collections.sort(serviceList, Comparators.forDomainProvider());
                List<Class<? extends Domain>> domains = new ArrayList<Class<? extends Domain>>();
                for (DomainProvider dp : serviceList) {
                    domains.add(dp.getDomainInterface());
                }
                return domains;
            }
        };
    }

    @SuppressWarnings("serial")
    private IModel<TreeModel> createGlobalTreeModel(final Class<? extends Domain> domainType) {
        return new LoadableDetachableModel<TreeModel>() {
            @Override
            protected TreeModel load() {
                DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Globals");
                if (domainType != null) {
                    for (Entry<String, String> e : ruleManager.listGlobals().entrySet()) {
                        if (e.getValue().equals(domainType.getCanonicalName())) {
                            DefaultMutableTreeNode child = new DefaultMutableTreeNode(e.getKey());
                            rootNode.add(child);
                        }
                    }
                }
                return new DefaultTreeModel(rootNode);
            }
        };
    }

    @SuppressWarnings("serial")
    private IModel<TreeModel> createEndpointsModel(final Class<? extends Domain> domainType) {
        return new LoadableDetachableModel<TreeModel>() {
            @Override
            protected TreeModel load() {
                DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Domain endpoints");
                if (domainType != null) {
                    for (Domain d : wiringService.getDomainEndpoints(domainType, "*")) {
                        String id = d.getInstanceId();
                        if (id != null) {
                            DefaultMutableTreeNode child = new DefaultMutableTreeNode(id);
                            rootNode.add(child);
                        }
                    }
                }
                return new DefaultTreeModel(rootNode);
            }
        };
    }

    private TreeModel createContextModel() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Contexts");
        for (String c : getAvailableContexts()) {
            rootNode.add(new DefaultMutableTreeNode(c));
        }
        return new DefaultTreeModel(rootNode);
    }

    private void resetWiringForm(AjaxRequestTarget target) {
        globalName = "";
        instanceId = "";
        target.add(txtGlobalName);
        target.add(txtInstanceId);
        target.add(feedbackPanel);
    }

    public String getGlobalName() {
        return globalName;
    }

    public void setGlobalName(String globalName) {
        this.globalName = globalName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @SuppressWarnings("serial")
    private class WiringSubjectTree extends LinkTree {
        private TextField<String> subject;

        public WiringSubjectTree(String id, TextField<String> subject) {
            super(id);
            this.subject = subject;
        }

        @Override
        protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target) {
            DefaultMutableTreeNode mnode = (DefaultMutableTreeNode) node;
            if (mnode.isRoot()) {
                return;
            }
            subject.setDefaultModelObject(mnode.getUserObject());
            target.add(subject);
        }

        @Override
        public boolean isVisible() {
            if (getModelObject() == null) {
                return false;
            }
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModelObject().getRoot();
            return root != null && !root.isLeaf();
        }
    }

    @SuppressWarnings("serial")
    public static class CheckedTree extends LabelTree {
        private Map<String, IModel<Boolean>> checks = new HashMap<String, IModel<Boolean>>();

        public CheckedTree(String id, TreeModel model) {
            super(id, model);
        }

        @Override
        protected Component newNodeComponent(String id, IModel<Object> model) {
            DefaultMutableTreeNode mnode = (DefaultMutableTreeNode) model.getObject();
            if (mnode.isRoot()) {
                return super.newNodeComponent(id, model);
            }
            String name = (String) mnode.getUserObject();
            Model<String> labelModel = new Model<String>();
            labelModel.setObject(name);
            Model<Boolean> checkModel = new Model<Boolean>();
            checkModel.setObject(Boolean.FALSE);
            checks.put(name, checkModel);
            return new CheckedPanel(id, checkModel, labelModel);
        }

        @Override
        protected Component newJunctionLink(MarkupContainer parent, final String id, final Object node) {
            return new WebMarkupContainer(id) {
                @Override
                protected void onComponentTag(ComponentTag tag) {
                    super.onComponentTag(tag);
                    tag.setName("span");
                    tag.put("class", "junction-corner");
                }
            };
        }

        public Set<String> getAllChecked() {
            Set<String> checked = new HashSet<String>();
            for (Entry<String, IModel<Boolean>> e : checks.entrySet()) {
                if (Boolean.TRUE.equals(e.getValue().getObject())) {
                    checked.add(e.getKey());
                }
            }
            return checked;
        }
    }
}
