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
package org.openengsb.config.view;

import java.util.ArrayList;
import java.util.Map;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.config.dao.EndpointDao;
import org.openengsb.config.dao.ServiceAssemblyDao;
import org.openengsb.config.domain.Endpoint;
import org.openengsb.config.domain.KeyValue;
import org.openengsb.config.editor.EditorPanel;
import org.openengsb.config.editor.FieldInfos;
import org.openengsb.config.jbi.types.AbstractType;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointType;
import org.openengsb.config.model.Models;

import com.google.common.collect.Lists;

public class BeanEditorPage extends BasePage {

    @SpringBean
    EndpointDao dao;
    @SpringBean
    ServiceAssemblyDao sadao;

    @SuppressWarnings("serial")
    public BeanEditorPage(Endpoint endpoint) {
        if (endpoint.getId() != null) {
            setDefaultModel(Models.domain(dao, endpoint));
        } else {
            setDefaultModel(Models.model(endpoint));
        }

        final ComponentType ct = componentService.getComponent(endpoint.getComponentType());
        final EndpointType et = ct.getEndpoint(endpoint.getEndpointType());

        add(new Label("name", ct.getName()));
        add(new Label("namespace", ct.getNamespace()));

        ArrayList<AbstractType> fields = Lists.newArrayList();
        fields.addAll(et.getAttributes());
        fields.addAll(et.getProperties());
        FieldInfos fi = new FieldInfos(ct.getName() + '.' + et.getName(), fields);

        EditorPanel editor = new EditorPanel("editor", ct.getName(), fi, endpoint.getDetachedValues()) {
            @Override
            public void onSubmit() {
                BeanEditorPage.this.onSubmit(getValues());
            }
        };
        add(editor);
    }

    private void onSubmit(Map<String, String> map) {
        Endpoint endpoint = (Endpoint) getDefaultModelObject();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            KeyValue kv = endpoint.getValues().get(entry.getKey());
            if (kv == null) {
                endpoint.getValues().put(entry.getKey(), new KeyValue(endpoint, entry.getKey(), entry.getValue()));
            } else {
                kv.setValue(entry.getValue());
            }
        }

        endpoint.setName(endpoint.getValues().get("service").getValue() + "."
                + endpoint.getValues().get("endpoint").getValue());
        dao.persist(endpoint);

        RequestCycle.get().setResponsePage(
                new ShowServiceAssemblyPage(sadao.find(endpoint.getServiceAssembly().getId())));
    }

    // public BeanEditorPage(String componentName, String beanIdentifier) {
    // this(componentName, beanIdentifier, new HashMap<String, String>());
    // }

    // public BeanEditorPage(String componentName, String beanIdentifier,
    // Map<String, String> map) {
    // final ComponentType desc = componentService.getComponent(componentName);
    // add(new Label("name", desc.getName()));
    // add(new Label("namespace", desc.getNamespace()));
    //
    // final EndpointType endpoint = desc.getEndpoint(beanIdentifier);
    // final BeanType bean = desc.getBean(beanIdentifier);
    //
    // FieldInfos fi = null;
    // ArrayList<AbstractType> fields = new ArrayList<AbstractType>();
    // if (endpoint != null) {
    // fields.addAll(endpoint.getAttributes());
    // fields.addAll(endpoint.getProperties());
    // fi = new FieldInfos(endpoint.getName(), fields);
    // } else {
    // fields.addAll(bean.getProperties());
    // fi = new FieldInfos(bean.getClazz(), fields);
    // }
    //
    // EditorPanel editor = new EditorPanel("editor", desc.getName(), fi, map) {
    // @Override
    // public void onSubmit() {
    // if (endpoint != null) {
    // assemblyService.getEndpoints().add(new EndpointInfo(endpoint,
    // getValues()));
    // } else {
    // assemblyService.getBeans().add(new BeanInfo(bean, getValues()));
    // }
    // RequestCycle.get().setResponsePage(ShowServiceAssemblyPage.class);
    // }
    // };
    // add(editor);
    // }
}
