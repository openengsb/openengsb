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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.ui.web.model.MethodCall;
import org.openengsb.ui.web.model.MethodId;
import org.openengsb.ui.web.model.ServiceId;
import org.openengsb.ui.web.service.DomainService;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("serial")
public class TestClient extends BasePage {

    private static Log log = LogFactory.getLog(TestClient.class);

    @SpringBean
    private DomainService services;

    private final DropDownChoice<MethodId> methodList;

    private final MethodCall call = new MethodCall();

    private final ListView<ArgumentModel> argumentList;

    private final WebMarkupContainer argumentListContainer;

    private final DropDownChoice<ServiceId> serviceList;

    @SuppressWarnings("serial")
    public TestClient() {
        Form<Object> form = new Form<Object>("methodCallForm");
        form.add(new AjaxFormSubmitBehavior(form, "onsubmit") {
            @Override
            protected void onError(AjaxRequestTarget target) {
                throw new RuntimeException("submit error");
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                performCall();
                call.getArguments().clear();
                call.setMethod(null);
                call.setService(null);

                populateMethodList();
                target.addComponent(serviceList);
                target.addComponent(methodList);
                target.addComponent(argumentListContainer);
            }
        });
        serviceList = new DropDownChoice<ServiceId>("serviceList", new PropertyModel<ServiceId>(call, "service"),
                getServiceInstances());
        serviceList.setOutputMarkupId(true);
        serviceList.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                log.info("onchange triggered");
                call.setMethod(null);
                populateMethodList();
                target.addComponent(methodList);
            }
        });
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
        argumentList = new ListView<ArgumentModel>("argumentList") {
            @Override
            protected void populateItem(ListItem<ArgumentModel> item) {
                item.add(new Label("index", new PropertyModel<ArgumentModel>(item.getModelObject(), "index")));
                item.add(new TextField<ArgumentModel>("value", new PropertyModel<ArgumentModel>(item.getModelObject(),
                        "value")));
            }
        };
        argumentList.setOutputMarkupId(true);
        argumentListContainer.add(argumentList);
        form.add(argumentListContainer);

        add(form);
        this.add(new BookmarkablePageLink<Index>("index", Index.class));
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
            log.info("result: " + result);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }

    }

    protected void populateArgumentList() {
        Method m = findMethod();
        List<ArgumentModel> arguments = new ArrayList<ArgumentModel>();
        call.setArguments(arguments);
        int i = 0;
        for (@SuppressWarnings("unused")
        Class<?> p : m.getParameterTypes()) {
            arguments.add(new ArgumentModel(i, ""));
            i++;
        }
        argumentList.setList(arguments);
    }

    private List<ServiceId> getServiceInstances() {
        List<ServiceId> result = new ArrayList<ServiceId>();
        for (ServiceReference s : services.getManagedServiceInstances()) {
            String id = (String) s.getProperty("id");
            if (id != null) {
                ServiceId serviceId = new ServiceId();
                serviceId.setServiceId(id);
                serviceId.setServiceClass(services.getService(s).getClass().getName());
                result.add(serviceId);
            }
        }
        return result;
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
