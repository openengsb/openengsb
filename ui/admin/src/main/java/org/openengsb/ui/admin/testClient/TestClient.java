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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.proxy.ProxyFactory;
import org.openengsb.core.common.proxy.ProxyServiceManager;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.common.util.OsgiServiceNotAvailableException;
import org.openengsb.core.common.util.OsgiServiceUtils;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.connectorEditorPage.ConnectorEditorPage;
import org.openengsb.ui.admin.methodArgumentPanel.MethodArgumentPanel;
import org.openengsb.ui.admin.model.Argument;
import org.openengsb.ui.admin.model.MethodCall;
import org.openengsb.ui.admin.model.MethodId;
import org.openengsb.ui.admin.model.ServiceId;
import org.openengsb.ui.common.model.LocalizableStringModel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

@AuthorizeInstantiation("ROLE_USER")
public class TestClient extends BasePage {

    private static Log log = LogFactory.getLog(TestClient.class);

    @SpringBean
    private DomainService services;

    @SpringBean
    private BundleContext bundleContext;

    @SpringBean
    private ProxyFactory proxyFactory;

    private DropDownChoice<MethodId> methodList;

    private final MethodCall call = new MethodCall();

    private RepeatingView argumentList;

    private WebMarkupContainer argumentListContainer;

    private LinkTree serviceList;

    private FeedbackPanel feedbackPanel;

    private AjaxButton editButton;

    private ServiceId lastServiceId;

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
        IModel<List<DomainProvider>> domainModel = new LoadableDetachableModel<List<DomainProvider>>() {
            @Override
            protected List<DomainProvider> load() {
                return services.domains();
            }
        };

        serviceManagementContainer.add(new ListView<DomainProvider>("domains", domainModel) {

            @Override
            protected void populateItem(final ListItem<DomainProvider> item) {
                item.add(new Label("domain.name", new LocalizableStringModel(this, item.getModelObject().getName())));
                item.add(new Link<DomainProvider>("proxy.create.new", item.getModel()) {

                    @Override
                    public void onClick() {
                        ProxyServiceManager serviceManager = proxyFactory.createProxyForDomain(item.getModelObject());
                        setResponsePage(new ConnectorEditorPage(serviceManager));
                    }
                });
                item.add(new Label("domain.description", new LocalizableStringModel(this, item.getModelObject()
                    .getDescription())));

                item.add(new Label("domain.class", item.getModelObject().getDomainInterface().getName()));
                IModel<List<ServiceManager>> managersModel = new LoadableDetachableModel<List<ServiceManager>>() {
                    @Override
                    protected List<ServiceManager> load() {
                        return services.serviceManagersForDomain(item.getModelObject().getDomainInterface());
                    }
                };
                item.add(new ListView<ServiceManager>("services", managersModel) {

                    @Override
                    protected void populateItem(ListItem<ServiceManager> item) {
                        ServiceDescriptor desc = item.getModelObject().getDescriptor();
                        item.add(new Link<ServiceManager>("create.new", item.getModel()) {

                            @Override
                            public void onClick() {
                                setResponsePage(new ConnectorEditorPage(getModelObject()));
                            }
                        });
                        item.add(new Label("service.name", new LocalizableStringModel(this, desc.getName())));
                        item.add(new Label("service.description", new LocalizableStringModel(this, desc
                            .getDescription())));
                    }
                });
            }
        });

        Form<MethodCall> form = new Form<MethodCall>("methodCallForm");
        form.setModel(new Model<MethodCall>(call));
        form.setOutputMarkupId(true);
        add(form);

        editButton = new AjaxButton("editButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                log.info("edit button pressed");

                if (lastServiceId != null) {
                    ServiceManager lastManager = getLastManager(lastServiceId);
                    if (lastManager != null) {
                        setResponsePage(new ConnectorEditorPage(lastManager, lastServiceId.getServiceId()));
                    }

                }
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

        AjaxButton submitButton = new IndicatingAjaxButton("submitButton", form) {
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

        // the message-attribute doesn't work for some reason
        submitButton.setModel(new ResourceModel("form.call"));
        form.add(submitButton);
        form.add(editButton);
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
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
        lastServiceId = null;
        editButton.setEnabled(false);
        editButton.setEnabled(getLastManager(serviceId) != null);
    }

    private ServiceManager getLastManager(ServiceId serviceId) {
        ServiceReference[] references = null;
        try {
            references =
                bundleContext.getServiceReferences(Domain.class.getName(),
                    String.format("(id=%s)", serviceId.getServiceId()));
            String id = "";
            String domain = null;
            if (references != null && references.length > 0) {
                id = (String) references[0].getProperty("managerId");
                domain = (String) references[0].getProperty("domain");
            }
            List<ServiceManager> managerList = new ArrayList<ServiceManager>();

            for (DomainProvider ref : services.domains()) {
                Class<? extends Domain> domainInterface = ref.getDomainInterface();
                if (domainInterface.getName().equals(domain)) {
                    managerList.addAll(services.serviceManagersForDomain(domainInterface));
                }
            }

            for (ServiceManager sm : managerList) {
                if (sm.getDescriptor().getId().equals(id)) {
                    lastServiceId = serviceId;
                    return sm;
                }
            }
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private TreeModel createModel() {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Select Instance");
        TreeModel model = new DefaultTreeModel(node);
        log.info("adding domains");
        for (DomainProvider provider : services.domains()) {
            log.info("adding " + provider.getName());
            addDomainProvider(provider, node);
        }
        log.info("done adding domains;");
        return model;
    }

    private void addDomainProvider(DomainProvider provider, DefaultMutableTreeNode node) {
        DefaultMutableTreeNode providerNode =
            new DefaultMutableTreeNode(provider.getName().getString(getSession().getLocale()));
        node.add(providerNode);
        for (ServiceReference serviceReference : services.serviceReferencesForDomain(provider.getDomainInterface())) {
            String id = (String) serviceReference.getProperty("id");
            if (id != null) {
                ServiceId serviceId = new ServiceId();
                serviceId.setServiceId(id);
                serviceId.setServiceClass(provider.getDomainInterface().getName());
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
            serviceObject = getService(service);
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

        List<Method> methods = Arrays.asList(connectorInterface.getMethods());
        return methods;
    }

    private Object getService(ServiceId service) throws OsgiServiceNotAvailableException {
        return OsgiServiceUtils.getServiceWithId(service.getServiceClass(), service.getServiceId());
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
