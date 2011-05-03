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
import java.util.Dictionary;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.commons.lang.ArrayUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.common.util.Comparators;
import org.openengsb.ui.admin.basePage.BasePage;
import org.osgi.framework.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AuthorizeInstantiation("ROLE_ADMIN")
public class WiringPage extends BasePage {
    private static final Logger LOGGER = LoggerFactory.getLogger(WiringPage.class);
    
    @SpringBean
    private WiringService wiringService;

    @SpringBean
    private OsgiUtilsService serviceUtils;
    
    @SpringBean
    private ConnectorManager serviceManager;
    
    @SpringBean
    private RuleManager ruleManager;
    
    private DropDownChoice<Class<? extends Domain>> domains;
    private LinkTree globals;
    private LinkTree endpoints;
    private TextField<String> txtGlobalName;
    private TextField<String> txtInstanceId;
    private AjaxSubmitLink wireButton;
    private FeedbackPanel feedbackPanel;
    
    private String globalName = "";
    private String instanceId = "";
    
    public WiringPage() {
        init();
    }
    
    private void init() {
        Form<Void> domainChooseForm = new Form<Void>("domainChooseForm");
        initDomainChooseForm(domainChooseForm);
        add(domainChooseForm);
        
        globals = new WiringSubjectTree("globals");
        globals.getTreeState().expandAll();
        globals.setOutputMarkupId(true);
        globals.setOutputMarkupPlaceholderTag(true);
        add(globals);
        
        endpoints = new WiringSubjectTree("endpoints");
        endpoints.getTreeState().expandAll();
        endpoints.setOutputMarkupId(true);
        endpoints.setOutputMarkupPlaceholderTag(true);
        add(endpoints);
        
        Form<Object> wiringForm = new Form<Object>("wiringForm");
        initWiringForm(wiringForm);
        add(wiringForm);
        
        ((WiringSubjectTree) globals).setSubject(txtGlobalName);
        ((WiringSubjectTree) endpoints).setSubject(txtInstanceId);
        
        feedbackPanel = new FeedbackPanel("feedbackPanel");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
    }

    @SuppressWarnings("serial")
    private void initDomainChooseForm(Form<Void> form) {
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
                target.addComponent(globals);
                target.addComponent(endpoints);
            }
        });
        form.add(domains);
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
    private void initWiringForm(Form<Object> form) {
        form.setOutputMarkupId(true);
        form.setDefaultModel(new CompoundPropertyModel<Object>(this));
        
        txtGlobalName = new TextField<String>("globalName");
        txtGlobalName.setOutputMarkupId(true);
        form.add(txtGlobalName);
        txtInstanceId = new TextField<String>("instanceId");
        txtInstanceId.setOutputMarkupId(true);
        txtInstanceId.setEnabled(false);
        form.add(txtInstanceId);
        
        wireButton = new AjaxSubmitLink("wireButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                LOGGER.debug("Start wiring");
                if (globalName == null || globalName.trim().isEmpty()) {
                    error(new StringResourceModel("globalNotSet", this, null).getString());
                    target.addComponent(feedbackPanel);
                    return;
                }
                if (instanceId == null || instanceId.isEmpty()) {
                    error(new StringResourceModel("instanceIdNotSet", this, null).getString());
                    target.addComponent(feedbackPanel);
                    return;
                }
                
                try {
                    ConnectorId connectorId = ConnectorId.fromFullId(instanceId);
                    String domainTypeOfGlobal = getDomainTypeOfGlobal(globalName);
                    String domainTypeOfService = getDomainTypeOfServiceName(connectorId.getDomainType());
                    if (domainTypeOfGlobal != null) {
                        if (alreadySetForOtherDomain(domainTypeOfGlobal, domainTypeOfService)) {
                            info(new StringResourceModel("globalAlreadySet", this, null).getString());
                            target.addComponent(feedbackPanel);
                            LOGGER.info("cannot wire {} to {}, because {} has type {}", 
                                new Object[] {globalName, instanceId, globalName, domainTypeOfGlobal});
                            return;
                        }
                    } else {
                        ruleManager.addGlobal(domainTypeOfService, globalName);
                        LOGGER.info("created global {} of type {}", globalName, domainTypeOfService);
                    }
                    ConnectorDescription description = serviceManager.getAttributeValues(connectorId);
                    String context = getContext();
                    if (setLocation(globalName, context, description.getProperties())) {
                        serviceManager.forceUpdate(connectorId, description);
                        info(new StringResourceModel("wiringSuccess", this, null).getString());
                        LOGGER.info("{} got wired with {} in context {}", 
                            new Object[] { globalName, instanceId, context });
                    } else {
                        info(new StringResourceModel("doubleWiring", this, null).getString());
                        LOGGER.info("{} already wired with {} in context {}", 
                            new Object[] { globalName, instanceId, context });
                    }
                } catch (Exception e) {
                    String message = new StringResourceModel("wiringError", this, null).getString();
                    error(message + "\n" + e.getLocalizedMessage());
                    LOGGER.error("Error during wiring", e);
                } finally {
                    target.addComponent(feedbackPanel);
                    resetWiringForm(target);
                }
            }
        };
        form.add(wireButton);
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

    private boolean alreadySetForOtherDomain(String domainTypeOfGlobal, String domainTypeOfService) {
        return domainTypeOfGlobal != null && !domainTypeOfGlobal.equals(domainTypeOfService);
    }

    private String getContext() {
        String context = ContextHolder.get().getCurrentContextId();
        if (context == null) { //should never be normally at deployment
            context = "root";
        }
        return context;
    }

    /**
     * returns true if location is not already set in the properties, otherwise false 
     */
    private boolean setLocation(String global, String context, Dictionary<String, Object> properties) {
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
                        DefaultMutableTreeNode child = new DefaultMutableTreeNode(d.getInstanceId());
                        rootNode.add(child);
                    }
                }
                return new DefaultTreeModel(rootNode);
            }
        };
    }

    private void resetWiringForm(AjaxRequestTarget target) {
        globalName = "";
        instanceId = "";
        target.addComponent(txtGlobalName);
        target.addComponent(txtInstanceId);
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
        
        public WiringSubjectTree(String id) {
            super(id);
             
        }
        
        @Override
        protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target) {
            DefaultMutableTreeNode mnode = (DefaultMutableTreeNode) node;
            if (mnode.isRoot()) {
                return;
            }
            subject.setDefaultModelObject(mnode.getUserObject());
            target.addComponent(subject);
        }

        @Override
        public boolean isVisible() {
            if (this.getModelObject() == null) {
                return false;
            }
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.getModelObject().getRoot();
            return root != null && !root.isLeaf();
        }

        public void setSubject(TextField<String> subject) {
            this.subject = subject;
        }
    }

}
