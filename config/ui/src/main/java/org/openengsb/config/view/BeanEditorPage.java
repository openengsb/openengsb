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

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.basic.Label;
import org.openengsb.config.editor.EditorPanel;
import org.openengsb.config.editor.FieldInfos;
import org.openengsb.config.jbi.BeanInfo;
import org.openengsb.config.jbi.EndpointInfo;
import org.openengsb.config.jbi.ServiceUnitInfo;
import org.openengsb.config.jbi.types.AbstractType;
import org.openengsb.config.jbi.types.BeanType;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointType;

import com.google.common.collect.Lists;

public class BeanEditorPage extends BasePage {

    public BeanEditorPage(String componentName, String beanIdentifier) {
        final ComponentType desc = componentService.getComponent(componentName);
        add(new Label("name", desc.getName()));
        add(new Label("namespace", desc.getNamespace()));

        final EndpointType endpoint = desc.getEndpoint(beanIdentifier);
        final BeanType bean = desc.getBean(beanIdentifier);

        FieldInfos fi = null;
        ArrayList<AbstractType> fields = new ArrayList<AbstractType>();
        if (endpoint != null) {
            fields.addAll(endpoint.getAttributes());
            fields.addAll(endpoint.getProperties());
            fi = new FieldInfos(endpoint.getName(), fields);
        } else {
            fields.addAll(bean.getProperties());
            fi = new FieldInfos(bean.getClazz(), fields);
        }

        EditorPanel editor = new EditorPanel("editor", desc.getName(), fi) {
            @Override
            public void onSubmit() {
                if (endpoint != null) {
                    assemblyService.getServiceUnits().add(
                            new ServiceUnitInfo(desc, Lists.newArrayList(new EndpointInfo(endpoint, getValues()))));
                } else {
                    assemblyService.getBeans().add(new BeanInfo(bean, getValues()));
                }
                RequestCycle.get().setResponsePage(CreateAssemblyPage.class);
            }
        };
        add(editor);
    }
}
