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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
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
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.model.BeanDescription;
import org.openengsb.core.api.model.ConnectorDefinition;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.core.api.security.annotation.SecurityAttributes;
import org.openengsb.core.api.security.model.SecurityAttributeEntry;
import org.openengsb.core.common.SecurityAttributeProviderImpl;
import org.openengsb.core.common.util.Comparators;
import org.openengsb.core.common.util.JsonUtils;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.connectorEditorPage.ConnectorEditorPage;
import org.openengsb.ui.admin.methodArgumentPanel.MethodArgumentPanel;
import org.openengsb.ui.admin.model.Argument;
import org.openengsb.ui.admin.model.MethodCall;
import org.openengsb.ui.admin.model.MethodId;
import org.openengsb.ui.admin.model.ServiceId;
import org.openengsb.ui.admin.organizeGlobalsPage.OrganizeGlobalsPage;
import org.openengsb.ui.admin.organizeImportsPage.OrganizeImportsPage;
import org.openengsb.ui.admin.util.MethodComparator;
import org.openengsb.ui.common.model.LocalizableStringModel;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

@SecurityAttributes({
    @SecurityAttribute(key = "org.openengsb.ui.component", value = "SERVICE_USER"),
    @SecurityAttribute(key = "org.openengsb.ui.component", value = "SERVICE_EDITOR")
})
@PaxWicketMountPoint(mountPoint = "tester")
public class TestClient extends BasePage {

    private static final long serialVersionUID = 2993665629913347770L;

    private static final Logger LOGGER = LoggerFactory.getLogger(TestClient.class);

    @PaxWicketBean(name = "wiringService")
    private WiringService wiringService;

    @PaxWicketBean(name = "osgiUtilsService")
    private OsgiUtilsService utilsService;

    @PaxWicketBean(name = "serviceManager")
    private ConnectorManager serviceManager;

    @PaxWicketBean(name = "attributeStore")
    private SecurityAttributeProviderImpl attributeStore;

    private DropDownChoice<MethodId> methodList;

    private final MethodCall call = new MethodCall();

    private RepeatingView argumentList;

    private WebMarkupContainer argumentListContainer;

    private LinkTree serviceList;

    private FeedbackPanel feedbackPanel;

    private AjaxButton editButton;
    private AjaxButton deleteButton;
    private AjaxButton submitButton;
    private AjaxButton jsonButton;

    @SuppressWarnings("serial")
    private IModel<? extends List<? extends DomainProvider>> domainProvider =
        new LoadableDetachableModel<List<? extends DomainProvider>>() {
            @Override
            protected List<? extends DomainProvider> load() {
                List<DomainProvider> serviceList = utilsService.listServices(DomainProvider.class);
                Collections.sort(serviceList, Comparators.forDomainProvider());
                return serviceList;
            }
        };

    public TestClient() {
        super();
        initContent();
    }

    public TestClient(PageParameters parameters) {
        super(parameters);
        initContent();
    }

    private void initContent() {
        WebMarkupContainer serviceManagementContainer = new WebMarkupContainer("serviceManagementContainer");
        serviceManagementContainer.setOutputMarkupId(true);
        add(serviceManagementContainer);
        attributeStore.putAttribute(serviceManagementContainer, new SecurityAttributeEntry(
            "org.openengsb.ui.component", "SERVICE_EDITOR"));
        serviceManagementContainer.add(makeServiceList());

        Form<Object> organize = createOrganizeForm();
        add(organize);

        Form<MethodCall> form = createMethodCallForm();
        add(form);

        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
    }

    @SuppressWarnings("serial")
    private Form<MethodCall> createMethodCallForm() {
        Form<MethodCall> form = new Form<MethodCall>("methodCallForm");
        form.setModel(new Model<MethodCall>(call));
        form.setOutputMarkupId(true);

        editButton = initializeEditButton(form);
        editButton.setEnabled(false);
        editButton.setOutputMarkupId(true);

        deleteButton = initializeDeleteButton(form);
        deleteButton.setEnabled(false);
        deleteButton.setOutputMarkupId(true);

        methodList = new DropDownChoice<MethodId>("methodList");
        methodList.setModel(new PropertyModel<MethodId>(call, "method"));
        methodList.setChoiceRenderer(new ChoiceRenderer<MethodId>());
        methodList.setOutputMarkupId(true);
        methodList.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                LOGGER.info("method selected: " + call.getMethod());
                populateArgumentList();
                target.add(argumentListContainer);
            }
        });
        form.add(methodList);

        argumentListContainer = new WebMarkupContainer("argumentListContainer");
        argumentListContainer.setOutputMarkupId(true);
        argumentList = new RepeatingView("argumentList");
        argumentList.setOutputMarkupId(true);
        argumentListContainer.add(argumentList);
        form.add(argumentListContainer);

        submitButton = initializeSubmitButton(form);
        jsonButton = initializeJsonButton(form);

        serviceList = new LinkTree("serviceList", createModel()) {
            @Override
            protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target) {
                DefaultMutableTreeNode mnode = (DefaultMutableTreeNode) node;
                try {
                    argumentList.removeAll();
                    target.add(argumentListContainer);
                    ServiceId service = (ServiceId) mnode.getUserObject();
                    LOGGER.info("clicked on node {} of type {}", node, node.getClass());
                    call.setService(service);
                    populateMethodList();
                    updateModifyButtons(service);
                    jsonButton.setEnabled(true);
                } catch (ClassCastException ex) {
                    LOGGER.info("clicked on not ServiceId node");
                    methodList.setChoices(new ArrayList<MethodId>());
                    editButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    submitButton.setEnabled(false);
                    jsonButton.setEnabled(false);
                }
                target.add(methodList);
                target.add(editButton);
                target.add(deleteButton);
                target.add(submitButton);
                target.add(jsonButton);
                target.add(feedbackPanel);
            }
        };
        serviceList.setOutputMarkupId(true);
        form.add(serviceList);
        serviceList.getTreeState().expandAll();

        submitButton.setOutputMarkupId(true);
        submitButton.setEnabled(false);

        jsonButton.setOutputMarkupId(true);
        jsonButton.setEnabled(false);

        form.add(submitButton);
        form.add(editButton);
        form.add(deleteButton);
        form.add(jsonButton);

        return form;
    }

    @SuppressWarnings("serial")
    private AjaxButton initializeEditButton(Form<MethodCall> form) {
        return new AjaxButton("editButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                LOGGER.info("edit button pressed");
                String serviceId = call.getService().getServiceId();
                ConnectorDefinition connectorId = ConnectorDefinition.fromFullId(serviceId);
                setResponsePage(new ConnectorEditorPage(connectorId));
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                LOGGER.warn("Submit error during editButton.");
            }
        };
    }

    @SuppressWarnings("serial")
    private AjaxButton initializeDeleteButton(Form<MethodCall> form) {
        return new AjaxButton("deleteButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                LOGGER.info("delete button pressed");
                String serviceId = call.getService().getServiceId();
                ConnectorDefinition connectorId = ConnectorDefinition.fromFullId(serviceId);
                try {
                    serviceManager.delete(connectorId);
                    info("service " + serviceId + " successfully deleted");
                    serviceList.setModelObject(createModel());
                    serviceList.getTreeState().expandAll();
                    target.add(serviceList);
                } catch (PersistenceException e) {
                    error("Unable to delete Service due to: " + e.getLocalizedMessage());
                }
                target.add(feedbackPanel);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                LOGGER.warn("Submit error during editButton.");
            }
        };
    }

    @SuppressWarnings("serial")
    private IndicatingAjaxButton initializeSubmitButton(Form<MethodCall> form) {
        return new IndicatingAjaxButton("submitButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(feedbackPanel);
                performCall();
                call.getArguments().clear();
                argumentList.removeAll();
                call.setMethod(null);
                populateMethodList();
                target.add(methodList);
                target.add(argumentListContainer);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                LOGGER.warn("Error during submitting with submitButton");
            }
        };
    }

    @SuppressWarnings("serial")
    private IndicatingAjaxButton initializeJsonButton(Form<MethodCall> form) {
        return new IndicatingAjaxButton("jsonButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(feedbackPanel);
                displayJSONMessages();
                call.getArguments().clear();
                argumentList.removeAll();
                call.setMethod(null);
                populateMethodList();
                target.add(methodList);
                target.add(argumentListContainer);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                LOGGER.warn("Error during submissiong with jsonButton");
            }
        };
    }

    /**
     * creates the form for organize section (globals, imports)
     */
    private Form<Object> createOrganizeForm() {
        Form<Object> organize = new Form<Object>("organizeForm");
        organize.setOutputMarkupId(true);

        @SuppressWarnings("serial")
        AjaxButton globalsButton = new AjaxButton("globalsButton", organize) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                setResponsePage(OrganizeGlobalsPage.class);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                LOGGER.warn("Error during submit of globalButton ajax link");
            }
        };
        globalsButton.setOutputMarkupId(true);
        organize.add(globalsButton);

        @SuppressWarnings("serial")
        AjaxButton importsButton = new AjaxButton("importsButton", organize) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                setResponsePage(OrganizeImportsPage.class);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                LOGGER.warn("Error during submit of importsButton page");
            }
        };
        importsButton.setOutputMarkupId(true);
        organize.add(importsButton);

        return organize;
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
                            return utilsService.listServices(ConnectorProvider.class,
                                String.format("(%s=%s)", Constants.DOMAIN_KEY, domainType));
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

    /**
     * Returns the ID of the currently selected Service or null if none was selected
     *
     * @return the ID of the currently selected Service or null if none was selected
     */
    private ServiceId fetchCurrentSelectService() {
        return call.getService();
    }

    /**
     * Returns the ID of the currently selected Method or null if none was selected
     *
     * @return the ID of the currently selected Method or null if none was selected
     */
    private MethodId fetchCurrentSelectMethod() {
        return call.getMethod();
    }

    /**
     * Returns a Standard MethodCall with of the selected Method
     *
     * @param methodId Id of the refered Method
     * @return a Standard MethodCall with of the selected Method
     */
    private org.openengsb.core.api.remote.MethodCall createRealMethodCall(MethodId methodId) {
        Class<?>[] classes = methodId.getArgumentTypesAsClasses();
        List<String> classList = new ArrayList<String>();
        for (Class<?> clazz : classes) {
            classList.add(clazz.getName());
        }
        return new org.openengsb.core.api.remote.MethodCall(methodId.getName(), call.getArgumentsAsArray(), classList);
    }

    /**
     * Creates a MethodCall and wraps the it in a MethodCallRequest with addiontal MetaData.<br/>
     * Returns this MethodCallRequest.
     *
     * @param serviceId Id of the refered Service
     * @param methodId Id of the refered Method
     * @return a MethodCallRequest with MetaData corresponding to the given ServiceId and MethodId
     */
    private MethodCallRequest createMethodCallRequest(ServiceId serviceId, MethodId methodId) {
        org.openengsb.core.api.remote.MethodCall realMethodCall = createRealMethodCall(methodId);
        realMethodCall.setMetaData(createMetaDataForMethodCallRequest(serviceId));
        return new MethodCallRequest(realMethodCall, "randomCallId");
    }

    /**
     * Creates a MethodCallRequest and wraps it in a SecureRequest, this adds the authentication block to the Message
     * Returns this SecureRequest.
     *
     * @param serviceId Id of the refered Service
     * @param methodId Id of the refered Method
     * @return a SecureRequest corresponding to the given ServiceId and MethodId
     */
    private MethodCallRequest createSecureRequest(ServiceId serviceId, MethodId methodId) {
        MethodCallRequest methodCallRequest = createMethodCallRequest(serviceId, methodId);
        BeanDescription beanDescription = BeanDescription.fromObject(new Password("yourpassword"));
        methodCallRequest.setPrincipal("yourusername");
        methodCallRequest.setCredentials(beanDescription);
        return methodCallRequest;
    }

    /**
     * create nessecary MetaData for the Json Message
     *
     * @param serviceId to fetch the context Data of the message
     * @return a Map with the nessecary MetaData for the Message
     */
    private Map<String, String> createMetaDataForMethodCallRequest(ServiceId serviceId) {
        Map<String, String> metaData = new HashMap<String, String>();
        if (serviceId.getServiceId() == null) {
            metaData.put("serviceId", serviceId.getDomainName());
        } else {
            metaData.put("serviceId", serviceId.getServiceId());
        }
        metaData.put("contextId", getSessionContextId());
        return metaData;
    }

    /**
     * Returns the constructed SecureRequest, via an ObjectMapper, as a JsonMessage String
     *
     * @param secureRequest the request to parse to a JsonString
     * @return the constructed SecureRequest, via an ObjectMapper, as a JsonMessage String
     */
    private String parseRequestToJsonString(MethodCallRequest secureRequest) {
        String jsonResult = "";
        try {
            jsonResult =
                JsonUtils.createObjectMapperWithIntroSpectors().configure(Feature.FAIL_ON_EMPTY_BEANS, false)
                    .writeValueAsString(secureRequest);
        } catch (IOException ex) {
            handleExceptionWithFeedback(ex);
            jsonResult = "";
        }
        return jsonResult;
    }

    /**
     * filter (unwanted) metaData entries from the args list, this is a dirty hack and should be replaced if possible.
     * TODO [Openengsb 1411] replace this with stable filter mechanism
     *
     * @param jsonMessage Message to filter
     * @return the jsonMessage filtered from the unnessecary data
     */
    private String filterUnnessecaryArgumentsFromJsonMessage(String jsonMessage) {
        String typeToReplace = ",\"type\":";
        while (jsonMessage.contains(typeToReplace)) {
            int posAfterType = jsonMessage.indexOf(typeToReplace) + typeToReplace.length();
            String firstPart = jsonMessage.substring(0, jsonMessage.indexOf(typeToReplace));
            String lastPart = jsonMessage.substring(posAfterType, jsonMessage.length());

            int endOfArgs = lastPart.indexOf("}]");
            int firstSemicolon = lastPart.indexOf(",");

            if (firstSemicolon < endOfArgs) {
                lastPart = lastPart.substring(lastPart.indexOf(","), lastPart.length());
            } else {
                lastPart = lastPart.substring(lastPart.indexOf("}]"), lastPart.length());
            }
            jsonMessage = firstPart + lastPart;
        }
        jsonMessage = jsonMessage.replaceAll(",\"processId\":null,\"origin\":null", "");
        return jsonMessage;
    }

    /**
     * Displays the corresponding message to the currently selected Method of the currently active Service in the
     * "ServiceTree"
     */
    private void displayJSONMessages() {
        ServiceId serviceId = fetchCurrentSelectService();
        MethodId methodId = fetchCurrentSelectMethod();
        if (serviceId == null) {
            String serviceNotSet = new StringResourceModel("json.view.ServiceNotSet", this, null).getString();
            info(serviceNotSet);
            return;
        }
        if (methodId == null) {
            String methodNotSet = new StringResourceModel("json.view.MethodNotSet", this, null).getString();
            info(methodNotSet);
            return;
        }
        String jsonResult = parseRequestToJsonString(createSecureRequest(serviceId, methodId));
        String jsonPrefix = new StringResourceModel("json.view.MessagePrefix", this, null).getString();
        jsonResult = filterUnnessecaryArgumentsFromJsonMessage(jsonResult);
        info(String.format("%s %s", jsonPrefix, jsonResult));
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

    private void updateModifyButtons(ServiceId serviceId) {
        editButton.setEnabled(false);
        editButton.setEnabled(serviceId.getServiceId() != null);
        deleteButton.setEnabled(false);
        deleteButton.setEnabled(serviceId.getServiceId() != null);
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
        domainProviderServiceId.setDomainName(provider.getId());

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

    private Method getMethodOfService(Object service, MethodId methodId) throws NoSuchMethodException {
        Method method;
        if (methodId == null) {
            String string = new StringResourceModel("serviceError", this, null).getString();
            error(string);
            return null;
        }
        method = service.getClass().getMethod(methodId.getName(), methodId.getArgumentTypesAsClasses());
        return method;
    }

    protected void performCall() {
        Object service;
        try {
            service = getService(call.getService());
        } catch (OsgiServiceNotAvailableException e1) {
            handleExceptionWithFeedback(e1);
            return;
        }
        Method method;
        try {
            method = getMethodOfService(service, call.getMethod());
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(ex);
        }
        if (method == null) {
            return;
        }

        try {
            Object result = method.invoke(service, call.getArgumentsAsArray());
            info("Methodcall called successfully");
            Class<?> returnType = method.getReturnType();
            if (returnType.equals(void.class)) {
                return;
            }
            String resultString;
            if (returnType.isArray()) {
                try {
                    // to handle byte[] and char[]
                    Constructor<String> constructor = String.class.getConstructor(returnType);
                    resultString = constructor.newInstance(result);
                } catch (Exception e) {
                    resultString = ArrayUtils.toString(result);
                }
            } else {
                resultString = result.toString();
            }
            info("Result " + returnType.getName() + ": " + resultString);
            LOGGER.info("result: {}", resultString);
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
        if (call.getMethod() == null) {
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
        LOGGER.error(e.getMessage(), e);
    }

    private void populateMethodList() {
        ServiceId service = call.getService();
        List<Method> methods = getServiceMethods(service);
        Collection<String> methodSignatures = Collections2.transform(methods, new Function<Method, String>() {
            @Override
            public String apply(Method input) {
                Class<?>[] parameterTypes = input.getParameterTypes();
                String[] parameterTypeNames = new String[parameterTypes.length];
                for (int i = 0; i < parameterTypeNames.length; i++) {
                    parameterTypeNames[i] = parameterTypes[i].getSimpleName();
                }
                return input.getName() + "(" + StringUtils.join(parameterTypeNames, ", ") + ")";
            }
        });
        LOGGER.info("found {} methods: {}", methods.size());
        for (String s : methodSignatures) {
            LOGGER.info("# " + s);
        }
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
        if (wiringService.isConnectorCurrentlyPresent((Class<? extends Domain>) connectorInterface)) {
            submitButton.setEnabled(true);
            List<Method> result = Arrays.asList(connectorInterface.getMethods());
            Collections.sort(result, new MethodComparator());
            return result;
        }
        error("No service found for domain: " + connectorInterface.getName());
        submitButton.setEnabled(false);
        return new ArrayList<Method>();
    }

    private Object getService(ServiceId service) throws OsgiServiceNotAvailableException {
        String serviceId = service.getServiceId();
        if (serviceId != null) {
            return utilsService.getServiceWithId(service.getServiceClass(),
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
            return utilsService.getServiceForLocation(serviceClazz,
                location);
        }

    }

    @SuppressWarnings("unchecked")
    private Object getServiceViaDomainEndpointFactory(ServiceId service) {
        String name = service.getDomainName();
        Class<? extends Domain> aClass;
        try {
            aClass = (Class<? extends Domain>) Class.forName(service.getServiceClass());
        } catch (ClassNotFoundException e) {
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
