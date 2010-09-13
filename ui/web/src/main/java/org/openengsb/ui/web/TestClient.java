/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.ui.web;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.ui.web.editor.BeanArgumentPanel;
import org.openengsb.ui.web.editor.SimpleArgumentPanel;
import org.openengsb.ui.web.model.MethodCall;
import org.openengsb.ui.web.model.MethodId;
import org.openengsb.ui.web.model.ServiceId;
import org.openengsb.ui.web.service.DomainService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("serial")
public class TestClient extends BasePage {

    private static Log log = LogFactory.getLog(TestClient.class);

    @SpringBean
    private DomainService services;

    @SpringBean
    BundleContext bundleContext;

    private final DropDownChoice<MethodId> methodList;

    private final MethodCall call = new MethodCall();

    private final RepeatingView argumentList;

    private final WebMarkupContainer argumentListContainer;

    private final LinkTree serviceList;

    private FeedbackPanel feedbackPanel;

    private AjaxButton editButton;

    private ServiceManager lastManager;

    public TestClient() {
        Form<MethodCall> form = new Form<MethodCall>("methodCallForm");
        form.setModel(new Model<MethodCall>(call));
        form.setOutputMarkupId(true);
        add(form);

        editButton = new AjaxButton("editButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                log.info("edit button pressed");
                setResponsePage(new EditorPage(lastManager));
            }

        };
        editButton.setEnabled(false);

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
                log.info(node);
                log.info(node.getClass());

                updateEditButton((ServiceId) mnode.getUserObject());
            };
        };
        serviceList.setOutputMarkupId(true);
        form.add(serviceList);

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

        AjaxButton submitButton = new AjaxButton("submitButton", form) {
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
        this.add(new BookmarkablePageLink<Index>("index", Index.class));
    }

    private void updateEditButton(ServiceId serviceId) {
         Object serviceObject = getService(serviceId);

        this.editButton.setEnabled(true);

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
        DefaultMutableTreeNode providerNode = new DefaultMutableTreeNode(provider.getName());
        node.add(providerNode);
        for (ServiceReference serviceReference : this.services.serviceReferencesForConnector(provider
                .getDomainInterface())) {
            String id = (String) serviceReference.getProperty("id");
            if (id != null) {
                ServiceId serviceId = new ServiceId();
                serviceId.setServiceId(id);
                serviceId.setServiceClass(services.getService(serviceReference).getClass().getName());
                DefaultMutableTreeNode referenceNode = new DefaultMutableTreeNode(serviceId, false);
                providerNode.add(referenceNode);
            }
        }
    }

    protected void performCall() {
        Object service = getService(call.getService());
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
            error(e);
        } catch (InvocationTargetException e) {
            error(e.getCause());
        }
    }

    protected void populateArgumentList() {
        argumentList.removeAll();
        Method m = findMethod();
        List<ArgumentModel> arguments = new ArrayList<ArgumentModel>();
        call.setArguments(arguments);
        int i = 0;
        for (Class<?> p : m.getParameterTypes()) {
            ArgumentModel argModel = new ArgumentModel(i + 1, p, null);
            arguments.add(argModel);
            if (p.isPrimitive() || p.equals(String.class)) {
                SimpleArgumentPanel arg = new SimpleArgumentPanel("arg" + i, argModel);
                argumentList.add(arg);
            } else {
                Map<String, String> beanAttrs = new HashMap<String, String>();
                argModel.setValue(beanAttrs);
                argModel.setBean(true);
                BeanArgumentPanel arg = new BeanArgumentPanel("arg" + i, argModel, beanAttrs);
                argumentList.add(arg);
            }
            i++;
        }
        call.setArguments(arguments);
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
        Object serviceObject = getService(service);
        log.info("retrieved service Object of type " + serviceObject.getClass().getName());
        List<Method> methods = MethodUtil.getServiceMethods(serviceObject);
        return methods;
    }

    private Object getService(ServiceId service) {
        Object serviceObject = services.getService(service.getServiceClass(), service.getServiceId());
        return serviceObject;
    }

    private Method findMethod() {
        ServiceId service = call.getService();
        Object serviceObject = getService(service);
        Class<?> serviceClass = serviceObject.getClass();
        MethodId methodId = call.getMethod();
        try {
            return serviceClass.getMethod(methodId.getName(), methodId.getArgumentTypesAsClasses());
        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }

    }
}
