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

package org.openengsb.ui.admin.testClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.connectorEditorPage.ConnectorEditorPage;
import org.openengsb.ui.admin.methodArgumentPanel.MethodArgumentPanel;
import org.openengsb.ui.admin.model.Argument;
import org.openengsb.ui.admin.model.MethodCall;
import org.openengsb.ui.admin.model.MethodId;
import org.openengsb.ui.admin.model.ServiceId;
import org.openengsb.ui.common.model.LocalizableStringModel;

@AuthorizeInstantiation("ROLE_USER")
public class TestClient extends BasePage {

    private static Log log = LogFactory.getLog(TestClient.class);

    private static WiringService wiringService = OpenEngSBCoreServices.getWiringService();

    private static OsgiUtilsService serviceUtils = OpenEngSBCoreServices.getServiceUtilsService();

    private DropDownChoice<MethodId> methodList;

    private final MethodCall call = new MethodCall();

    private RepeatingView argumentList;

    private WebMarkupContainer argumentListContainer;

    private LinkTree serviceList;

    private FeedbackPanel feedbackPanel;

    private AjaxButton editButton;

    private static final String DOMAINSTRING = "[domain.%1$s]";
    private AjaxButton submitButton;

    @SuppressWarnings("serial")
    private IModel<? extends List<? extends DomainProvider>> domainProvider =
        new LoadableDetachableModel<List<? extends DomainProvider>>() {
            @Override
            protected List<? extends DomainProvider> load() {
                return serviceUtils.listServices(DomainProvider.class);
            }
        };;

    public TestClient() {
        super();
        initContent();
    }

    public TestClient(PageParameters parameters) {
        super(parameters);
        initContent();
    }

    @SuppressWarnings("serial")
    private void initContent() {
        WebMarkupContainer serviceManagementContainer = new WebMarkupContainer("serviceManagementContainer");
        serviceManagementContainer.setOutputMarkupId(true);
        add(serviceManagementContainer);
        MetaDataRoleAuthorizationStrategy.authorize(serviceManagementContainer, RENDER, "ROLE_ADMIN");

        serviceManagementContainer.add(makeServiceList());

        Form<MethodCall> form = new Form<MethodCall>("methodCallForm");
        form.setModel(new Model<MethodCall>(call));
        form.setOutputMarkupId(true);
        add(form);

        editButton = new AjaxButton("editButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                log.info("edit button pressed");

                // if (lastServiceId != null) {
                // TODO
                // InternalServiceRegistrationManager lastManager = getLastManager(lastServiceId);
                // if (lastManager != null) {
                // setResponsePage(new ConnectorEditorPage(lastManager, lastServiceId.getServiceId()));
                // }

                // }
            }

        };
        editButton.setEnabled(false);
        editButton.setOutputMarkupId(true);

        serviceList = new LinkTree("serviceList", createModel()) {
            @Override
            protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target) {
                DefaultMutableTreeNode mnode = (DefaultMutableTreeNode) node;
                if (!mnode.isLeaf()) {
                    return;
                }
                call.setService((ServiceId) mnode.getUserObject());
                populateMethodList();
                target.addComponent(methodList);
                argumentList.removeAll();
                target.addComponent(argumentListContainer);
                log.info(node);
                log.info(node.getClass());

                updateEditButton((ServiceId) mnode.getUserObject());
                target.addComponent(editButton);
                target.addComponent(submitButton);
                target.addComponent(feedbackPanel);
            }
        };
        serviceList.setOutputMarkupId(true);
        form.add(serviceList);
        serviceList.getTreeState().expandAll();

        methodList = new DropDownChoice<MethodId>("methodList");
        methodList.setModel(new PropertyModel<MethodId>(call, "method"));
        methodList.setChoiceRenderer(new ChoiceRenderer<MethodId>());
        methodList.setOutputMarkupId(true);
        methodList.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                populateArgumentList();
                target.addComponent(argumentListContainer);
            }
        });
        form.add(methodList);

        argumentListContainer = new WebMarkupContainer("argumentListContainer");
        argumentListContainer.setOutputMarkupId(true);
        argumentList = new RepeatingView("argumentList");
        argumentList.setOutputMarkupId(true);
        argumentListContainer.add(argumentList);
        form.add(argumentListContainer);

        submitButton = new IndicatingAjaxButton("submitButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(feedbackPanel);
                performCall();
                call.getArguments().clear();
                argumentList.removeAll();

                call.setMethod(null);
                populateMethodList();

                target.addComponent(methodList);
                target.addComponent(argumentListContainer);
            }
        };
        submitButton.setOutputMarkupId(true);
        // the message-attribute doesn't work for some reason
        submitButton.setModel(new ResourceModel("form.call"));
        form.add(submitButton);
        form.add(editButton);
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
    }

    @SuppressWarnings("serial")
    private ListView<DomainProvider> makeServiceList() {
        return new ListView<DomainProvider>("domains", domainProvider) {
            @Override
            protected void populateItem(final ListItem<DomainProvider> item) {
                final String domainType = item.getModelObject().getId();
                item.add(new Label("domain.name", new LocalizableStringModel(this, item.getModelObject().getName())));
                item.add(new Link<DomainProvider>("proxy.create.new", item.getModel()) {
                    @Override
                    public void onClick() {
                        setResponsePage(new ConnectorEditorPage(getModelObject().getId(),
                            Constants.EXTERNAL_CONNECTOR_PROXY));
                    }
                });
                item.add(new Label("domain.description", new LocalizableStringModel(this, item.getModelObject()
                        .getDescription())));

                item.add(new Label("domain.class", item.getModelObject().getDomainInterface().getName()));

                IModel<? extends List<? extends ConnectorProvider>> connectorProviderModel =
                    new LoadableDetachableModel<List<? extends ConnectorProvider>>() {
                        @Override
                        protected List<? extends ConnectorProvider> load() {
                            return serviceUtils.listServices(ConnectorProvider.class);
                        }
                    };
                item.add(new ListView<ConnectorProvider>("services", connectorProviderModel) {

                    @Override
                    protected void populateItem(ListItem<ConnectorProvider> item) {
                        ServiceDescriptor desc = item.getModelObject().getDescriptor();
                        item.add(new Link<ConnectorProvider>("create.new", item.getModel()) {
                            @Override
                            public void onClick() {
                                setResponsePage(new ConnectorEditorPage(domainType, getModelObject().getId()));
                            }
                        });
                        item.add(new Label("service.name", new LocalizableStringModel(this, desc.getName())));
                        item.add(new Label("service.description", new LocalizableStringModel(this, desc
                                .getDescription())));
                    }
                });
            }
        };
    }

    public TestClient(ServiceId jumpToService) {
        this();
        serviceList.getTreeState().collapseAll();
        TreeModel treeModel = serviceList.getModelObject();
        DefaultMutableTreeNode serviceNode = findService((DefaultMutableTreeNode) treeModel.getRoot(), jumpToService);
        expandAllUntilChild(serviceNode);
        serviceList.getTreeState().selectNode(serviceNode, true);
        call.setService(jumpToService);
        populateMethodList();

    }

    private void expandAllUntilChild(DefaultMutableTreeNode child) {
        for (TreeNode n : child.getPath()) {
            serviceList.getTreeState().expandNode(n);
        }
    }

    private DefaultMutableTreeNode findService(DefaultMutableTreeNode node, ServiceId jumpToService) {
        if (node.isLeaf()) {
            Object userObject = node.getUserObject();
            if (jumpToService.equals(userObject)) {
                return node;
            }
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode result = findService((DefaultMutableTreeNode) node.getChildAt(i), jumpToService);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private void updateEditButton(ServiceId serviceId) {
        editButton.setEnabled(false);
        editButton.setEnabled(serviceId.getServiceId() != null);
    }

    private TreeModel createModel() {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Select Instance");
        TreeModel model = new DefaultTreeModel(node);
        log.info("adding domains");
        List<? extends DomainProvider> providerList = domainProvider.getObject();
        Collections.sort(providerList, new Comparator<DomainProvider>() {
            @Override
            public int compare(DomainProvider o1, DomainProvider o2) {
                return o1.getId().compareToIgnoreCase(o2.getId());
            }
        });
        for (DomainProvider provider : providerList) {
            log.info("adding " + provider.getName());
            addDomainProvider(provider, node);
        }
        log.info("done adding domains;");
        return model;
    }

    private void addDomainProvider(DomainProvider provider, DefaultMutableTreeNode node) {
        String providerName = provider.getName().getString(getSession().getLocale());
        DefaultMutableTreeNode providerNode =
                new DefaultMutableTreeNode(providerName);
        node.add(providerNode);

        // add domain entry to call via domain endpoint factory
        ServiceId domainProviderServiceId = new ServiceId();
        String name = String.format(DOMAINSTRING, providerName);
        domainProviderServiceId.setServiceId(name);
        Class<? extends Domain> domainInterface = provider.getDomainInterface();
        domainProviderServiceId.setServiceClass(domainInterface.getName());
        DefaultMutableTreeNode endPointReferenceNode = new DefaultMutableTreeNode(domainProviderServiceId, false);
        providerNode.add(endPointReferenceNode);

        // add all corresponding services
        List<? extends Domain> domainEndpoints = wiringService.getDomainEndpoints(domainInterface, "*");
        for (Domain serviceReference : domainEndpoints) {
            String id = serviceReference.getInstanceId();
            if (id != null) {
                ServiceId serviceId = new ServiceId();
                serviceId.setServiceId(id);
                serviceId.setServiceClass(domainInterface.getName());
                DefaultMutableTreeNode referenceNode = new DefaultMutableTreeNode(serviceId, false);
                providerNode.add(referenceNode);
            }
        }
    }

    protected void performCall() {
        Object service;
        try {
            service = getService(call.getService());
        } catch (OsgiServiceNotAvailableException e1) {
            handleExceptionWithFeedback(e1);
            return;
        }
        MethodId mid = call.getMethod();
        Method m;
        if (mid == null) {
            String s = new StringResourceModel("serviceError", this, null).getString();
            error(s);
            return;
        }
        try {
            m = service.getClass().getMethod(mid.getName(), mid.getArgumentTypesAsClasses());
        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
        try {
            Object result = m.invoke(service, call.getArgumentsAsArray());
            info("Methodcall called successfully");
            if (!m.getReturnType().equals(void.class)) {
                info("Result: " + result);
                log.info("result: " + result);
            }
        } catch (IllegalAccessException e) {
            handleExceptionWithFeedback(e);
        } catch (InvocationTargetException e) {
            handleExceptionWithFeedback(e.getCause());
        }
    }

    protected void populateArgumentList() {
        argumentList.removeAll();
        ServiceId service = call.getService();
        Object serviceObject;
        try {
            // if (availableDomains.getObject().containsKey(service.getServiceId())) {
            // serviceObject = getServiceViaDomainEndpointFactory(service);
            // } else {
            serviceObject = getService(service);
            // }
        } catch (OsgiServiceNotAvailableException e) {
            handleExceptionWithFeedback(e);
            return;
        }
        Method m = findMethod(serviceObject.getClass(), call.getMethod());
        List<Argument> arguments = new ArrayList<Argument>();
        call.setArguments(arguments);
        int i = 0;
        for (Class<?> p : m.getParameterTypes()) {
            Argument argModel = new Argument(i + 1, p, null);
            arguments.add(argModel);
            MethodArgumentPanel argumentPanel = new MethodArgumentPanel("arg" + i + "panel", argModel);
            argumentList.add(argumentPanel);
            i++;
        }
        call.setArguments(arguments);
    }

    // private Domain getServiceViaDomainEndpointFactory(ServiceId serviceId) {
    // DomainProvider domainProvider = availableDomains.getObject().get(serviceId.getServiceId());
    // Class<? extends Domain> aClass = domainProvider.getDomainInterface();
    // String name = domainProvider.getName().getString(Locale.getDefault());
    // Domain defaultDomain = null;
    // WiringService wireingService = OpenEngSBCoreServices.getWiringService();
    // if (wireingService.isConnectorCurrentlyPresent(aClass)) {
    // defaultDomain = wireingService.getDomainEndpoint(aClass, "domain/" + name + "/default");
    // }
    // if (defaultDomain != null) {
    // return defaultDomain;
    // }
    // throw new OsgiServiceNotAvailableException("no default service found for service: "
    // + serviceId.getServiceClass());
    // }

    private void handleExceptionWithFeedback(Throwable e) {
        String stackTrace = ExceptionUtils.getFullStackTrace(e);
        error(stackTrace);
        log.error(e);
    }

    private void populateMethodList() {
        ServiceId service = call.getService();
        List<Method> methods = getServiceMethods(service);
        List<MethodId> methodChoices = new ArrayList<MethodId>();
        for (Method m : methods) {
            methodChoices.add(new MethodId(m));
        }
        methodList.setChoices(methodChoices);
        log.info("populating list with: " + methodChoices);
    }

    @SuppressWarnings("unchecked")
    private List<Method> getServiceMethods(ServiceId service) {
        if (service == null) {
            return Collections.emptyList();
        }
        Class<?> connectorInterface;
        try {
            connectorInterface = Class.forName(service.getServiceClass());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        WiringService wireingService = OpenEngSBCoreServices.getWiringService();
        if (wireingService.isConnectorCurrentlyPresent((Class<? extends Domain>) connectorInterface)) {
            submitButton.setEnabled(true);
            return Arrays.asList(connectorInterface.getMethods());
        }
        error("No service found for domain: " + connectorInterface.getName());
        submitButton.setEnabled(false);
        return new ArrayList<Method>();
    }

    private Object getService(ServiceId service) throws OsgiServiceNotAvailableException {
        return OpenEngSBCoreServices.getServiceUtilsService().getServiceWithId(service.getServiceClass(),
            service.getServiceId());
    }

    private Method findMethod(Class<?> serviceClass, MethodId methodId) {
        try {
            return serviceClass.getMethod(methodId.getName(), methodId.getArgumentTypesAsClasses());
        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
