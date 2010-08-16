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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.ui.web.service.DomainService;
import org.osgi.framework.ServiceReference;

public class TestClient extends BasePage {

    @SpringBean
    private DomainService services;

    private DropDownChoice<Method> methodList;

    private MethodCall call = new MethodCall();

    public TestClient() {
        Form<?> form = new Form("methodCallForm");
        DropDownChoice<ServiceId> serviceList = new DropDownChoice<ServiceId>("serviceList",
                new PropertyModel<ServiceId>(call, "service"), getServiceInstances());
        serviceList.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                call.setMethod(null);
                populateMethodList();
            }
        });
        form.add(serviceList);
        methodList = new DropDownChoice<Method>("methodList");
        methodList.setModel(new PropertyModel<Method>(call, "method"));
        form.add(methodList);

        add(form);
    }

    private List<ServiceId> getServiceInstances() {
        List<ServiceId> result = new ArrayList<ServiceId>();
        for (ServiceReference s : services.getManagedServiceInstances()) {
            String id = (String) s.getProperty("id");
            ServiceId serviceId = new ServiceId();
            serviceId.setServiceId(id);
            serviceId.setServiceClass(services.getService(s).getClass().getName());
            result.add(serviceId);
        }
        return result;
    }

    private void populateMethodList() {
        ServiceId service = call.getService();
        Object serviceObject = services.getService(service.getServiceClass(), service.getServiceId());
        Method[] methods = serviceObject.getClass().getMethods();
        methodList.setChoices(Arrays.asList(methods));
    }
}
