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
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.config.dao.PersistedObjectDao;
import org.openengsb.config.dao.ServiceAssemblyDao;
import org.openengsb.config.domain.PersistedObject;
import org.openengsb.config.editor.EditorPanel;
import org.openengsb.config.editor.FieldInfos;
import org.openengsb.config.jbi.types.AbstractType;
import org.openengsb.config.jbi.types.BeanType;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointType;
import org.openengsb.config.model.Models;
import org.openengsb.config.service.EditorService;

import com.google.common.collect.Lists;

public class BeanEditorPage extends BasePage {

    @SpringBean
    PersistedObjectDao dao;

    @SpringBean
    ServiceAssemblyDao sadao;

    @SpringBean
    EditorService editorService;

    private final EditorPanel editor;

    @SuppressWarnings("serial")
    public BeanEditorPage(PersistedObject po) {
        if (po.getId() != null) {
            setDefaultModel(Models.domain(dao, po));
        } else {
            setDefaultModel(Models.model(po));
        }

        final ComponentType ct = componentService.getComponent(po.getComponentType());

        add(new Label("name", ct.getName()));
        add(new Label("namespace", ct.getNamespace()));

        ArrayList<AbstractType> fields = Lists.newArrayList();
        String name = ct.getName() + '.';

        if (po.isBean()) {
            BeanType bt = ct.getBean(po.getDeclaredType());
            name += bt.getClazz();
            fields.addAll(bt.getProperties());
        } else {
            EndpointType et = ct.getEndpoint(po.getDeclaredType());
            name += et.getName();
            fields.addAll(et.getAttributes());
            fields.addAll(et.getProperties());
        }

        FieldInfos fi = new FieldInfos(name, fields);

        editor = new EditorPanel("editor", po.getServiceAssembly(), fi, po.getDetachedValues()) {
            @Override
            public void onSubmit() {
                BeanEditorPage.this.onSubmit(getValues());
            }
        };
        add(editor);
    }

    private void onSubmit(Map<String, String> map) {
        PersistedObject po = (PersistedObject) getDefaultModelObject();

        if (!editorService.validateNameUpdate(po, map)) {
            editor.showError(new StringResourceModel(po.isBean() ? "beanNotUnique" : "endpointNotUnique", this, null)
                    .getString());
            RequestCycle.get().setResponsePage(this);
            return;
        }

        editorService.updatePersistedObject(po, map);

        RequestCycle.get().setResponsePage(new ShowServiceAssemblyPage(sadao.find(po.getServiceAssembly().getId())));
    }
}
