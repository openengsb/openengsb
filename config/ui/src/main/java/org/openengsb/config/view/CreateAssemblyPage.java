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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.openengsb.config.jbi.BeanInfo;
import org.openengsb.config.jbi.ServiceAssemblyInfo;
import org.openengsb.config.jbi.ServiceUnitInfo;
import org.openengsb.config.jbi.types.BeanType;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointType;
import org.openengsb.config.view.util.ChoiceOption;

public class CreateAssemblyPage extends BasePage {
    public ChoiceOption selected;

    @SuppressWarnings("unchecked")
    public CreateAssemblyPage(PageParameters pp) {
        if (pp != null && pp.containsKey("reset")) {
            assemblyService.createNewAssembly();
        }

        Form<?> suform = new Form("suForm") {
            @Override
            protected void onSubmit() {
                CreateAssemblyPage.this.onDeploySubmit();
            }
        };
        add(suform);

        ListView<ServiceUnitInfo> suListView = new ListView<ServiceUnitInfo>("serviceUnits", assemblyService
                .getServiceUnits()) {
            @Override
            protected void populateItem(ListItem<ServiceUnitInfo> item) {
                ServiceUnitInfo su = item.getModelObject();
                item.add(new Label("name", su.getIdentifier()));
            }
        };
        add(suListView);
        ListView<BeanInfo> beanListView = new ListView<BeanInfo>("beans", assemblyService
                .getBeans()) {
            @Override
            protected void populateItem(ListItem<BeanInfo> item) {
                BeanInfo bean = item.getModelObject();
                item.add(new Label("name", bean.getBeanType().getClazz() + ":" + bean.getMap().get("id")));
            }
        };
        add(beanListView);

        Form<?> form = new Form("form") {
            @Override
            protected void onSubmit() {
                CreateAssemblyPage.this.onSubmit();
            }
        };
        add(form);

        ArrayList<ChoiceOption> names = new ArrayList<ChoiceOption>();
        for (ComponentType c : componentService.getComponents()) {
            String localeKey = c.getName() + ".";
            for (EndpointType e : c.getEndpoints()) {
                String display = getLocalizer().getString(localeKey + e.getName() + "._name", null);
                names.add(new ChoiceOption(c.getName() + ":" + e.getName(), display));
            }
            for (BeanType b : c.getBeans()) {
                String display = getLocalizer().getString(localeKey + b.getClazz() + "._name", null);
                names.add(new ChoiceOption(c.getName() + ":" + b.getClazz(), display));
            }
        }
        DropDownChoice<ChoiceOption> choice = new DropDownChoice<ChoiceOption>("suSelect",
                new PropertyModel<ChoiceOption>(this, "selected"), names, ChoiceOption.createRenderer());
        choice.setRequired(true);
        form.add(choice);
    }

    protected void onDeploySubmit() {
        try {
            File tmp = File.createTempFile("openengsb", ".zip");
            FileOutputStream fos = new FileOutputStream(tmp);
            ServiceAssemblyInfo sa = new ServiceAssemblyInfo("openengsb-test", assemblyService
                    .getServiceUnits(), assemblyService.getBeans());
            sa.toZip(fos);
            assemblyService.deploy(tmp, "openengsb-test-sa.zip");
            assemblyService.createNewAssembly();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onSubmit() {
        String[] s = selected.getId().split(":");
        RequestCycle.get().setResponsePage(new BeanEditorPage(s[0], s[1]));
    }
}
