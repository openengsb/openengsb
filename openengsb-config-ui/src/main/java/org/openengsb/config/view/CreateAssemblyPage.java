/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
import org.openengsb.config.jbi.ServiceAssemblyCreator;
import org.openengsb.config.jbi.ServiceAssemblyInfo;
import org.openengsb.config.jbi.ServiceUnitInfo;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointType;

public class CreateAssemblyPage extends BasePage {
    public String selected = "";

    public CreateAssemblyPage(PageParameters pp) {
        if (pp != null && pp.containsKey("reset")) {
            assemblyService.createNewAssembly();
        }

        Form suform = new Form("suForm") {
            @Override
            protected void onSubmit() {
                CreateAssemblyPage.this.onDeploySubmit();
            }
        };
        add(suform);

        ListView<ServiceUnitInfo> listView = new ListView<ServiceUnitInfo>("serviceUnits", assemblyService
                .getServiceUnits()) {
            @Override
            protected void populateItem(ListItem<ServiceUnitInfo> item) {
                ServiceUnitInfo su = item.getModelObject();
                item.add(new Label("suName", su.getComponent().getName() + ":" + su.getEndpoint().getName()));
            }
        };
        add(listView);

        Form form = new Form("form") {
            @Override
            protected void onSubmit() {
                CreateAssemblyPage.this.onSubmit();
            }
        };
        add(form);

        ArrayList<String> names = new ArrayList<String>();
        for (ComponentType c : componentService.getComponents()) {
            for (EndpointType e : c.getEndpoints()) {
                names.add(c.getName() + ":" + e.getName());
            }
        }
        DropDownChoice<String> choice = new DropDownChoice<String>("suSelect", new PropertyModel<String>(this,
                "selected"), names);
        choice.setRequired(true);
        form.add(choice);
    }

    protected void onDeploySubmit() {
        try {
            File tmp = File.createTempFile("openengsb", ".zip");
            FileOutputStream fos = new FileOutputStream(tmp);
            ServiceAssemblyCreator.createServiceAssembly(fos, new ServiceAssemblyInfo("openengsb-test", assemblyService
                    .getServiceUnits()));
            assemblyService.deploy(tmp, "openengsb-test-sa.zip");
            assemblyService.createNewAssembly();
            //ResourceStreamRequestTarget target = new ResourceStreamRequestTarget(new FileResourceStream(tmp));
            //target.setFileName("testname-sa.zip");
            //RequestCycle.get().setRequestTarget(target);
            //RequestCycle.get().getResponse().setContentType("application/zip");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onSubmit() {
        String[] s = selected.split(":");
        RequestCycle.get().setResponsePage(new ComponentEditorPage(s[0], s[1]));
    }
}
