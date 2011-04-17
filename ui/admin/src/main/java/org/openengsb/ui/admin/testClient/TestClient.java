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
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.commons.lang.exception.ExceptionUtils;
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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.Comparators;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.connectorEditorPage.ConnectorEditorPage;
import org.openengsb.ui.admin.methodArgumentPanel.MethodArgumentPanel;
import org.openengsb.ui.admin.model.Argument;
import org.openengsb.ui.admin.model.MethodCall;
import org.openengsb.ui.admin.model.MethodId;
import org.openengsb.ui.admin.model.ServiceId;
import org.openengsb.ui.common.model.LocalizableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AuthorizeInstantiation("ROLE_USER")
public class TestClient extends BasePage {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestClient.class);

    @SpringBean
    private WiringService wiringService;

    @SpringBean
    private OsgiUtilsService serviceUtils;

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
                List<DomainProvider> serviceList = serviceUtils.listServices(DomainProvider.class);
                Collections.sort(serviceList, Comparators.forDomainProvider());
                return serviceList;
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
                LOGGER.info("edit button pressed");
                String serviceId = call.getService().getServiceId();
                ConnectorId connectorId = ConnectorId.parse(serviceId);
                setResponsePage(new ConnectorEditorPage(connectorId));
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
                LOGGER.info("clicked on node {} of type {}", node, node.getClass());

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
                            return serviceUtils.listServices(ConnectorProvider.class,
                                    String.format("(domain=%s)", domainType));
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
        LOGGER.info("adding domains");
        List<? extends DomainProvider> providerList = domainProvider.getObject();
        Collections.sort(providerList, Comparators.forDomainProvider());
        for (DomainProvider provider : providerList) {
            LOGGER.info("adding " + provider.getName());
            addDomainProvider(provider, node);
        }
        LOGGER.info("done adding domains;");
        return model;
    }

    private void addDomainProvider(DomainProvider provider, DefaultMutableTreeNode node) {
        String providerName = provider.getName().getString(getSession().getLocale());
        DefaultMutableTreeNode providerNode =
                new DefaultMutableTreeNode(providerName);
        node.add(providerNode);

        // add domain entry to call via domain endpoint factory
        ServiceId domainProviderServiceId = new ServiceId();
        Class<? extends Domain> domainInterface = provider.getDomainInterface();
        domainProviderServiceId.setServiceClass(domainInterface.getName());
        domainProviderServiceId.setDomainName(providerName);
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
                LOGGER.info("result: {}", result);
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
            if (service.getDomainName() != null) {
                serviceObject = getServiceViaDomainEndpointFactory(service);
            } else {
                serviceObject = getService(service);
            }
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
        LOGGER.error(e.getMessage(), e);
    }

    private void populateMethodList() {
        ServiceId service = call.getService();
        List<Method> methods = getServiceMethods(service);
        List<MethodId> methodChoices = new ArrayList<MethodId>();
        for (Method m : methods) {
            methodChoices.add(new MethodId(m));
        }
        methodList.setChoices(methodChoices);
        LOGGER.info("populating list with: {}", methodChoices);
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
        String serviceId = service.getServiceId();
        if (serviceId != null) {
            return OpenEngSBCoreServices.getServiceUtilsService().getServiceWithId(service.getServiceClass(),
                serviceId);
        } else {
            String domainName = service.getDomainName();
            String location = "domain/" + domainName + "/default";
            Class<?> serviceClazz;
            try {
                serviceClazz = this.getClass().getClassLoader().loadClass(service.getServiceClass());
            } catch (ClassNotFoundException e) {
                throw new OsgiServiceNotAvailableException(e);
            }
            return OpenEngSBCoreServices.getServiceUtilsService().getServiceForLocation(serviceClazz,
                location);
        }

    }

    private Object getServiceViaDomainEndpointFactory(ServiceId service) {
        String name = service.getDomainName();
        Class<? extends Domain> aClass;
        try {
            aClass = (Class<? extends Domain>) Class.forName(service.getServiceClass());
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }

        if (wiringService.isConnectorCurrentlyPresent(aClass)) {
            return wiringService.getDomainEndpoint(aClass, "domain/" + name + "/default");
        }
        throw new OsgiServiceNotAvailableException("no default service found for service: "
                + service.getServiceClass());
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
