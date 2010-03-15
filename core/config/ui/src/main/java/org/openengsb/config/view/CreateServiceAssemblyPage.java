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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.config.dao.ServiceAssemblyDao;
import org.openengsb.config.domain.ServiceAssembly;
import org.openengsb.config.model.Models;

public class CreateServiceAssemblyPage extends BasePage {
    @SpringBean
    ServiceAssemblyDao dao;

    @SuppressWarnings("serial")
    public CreateServiceAssemblyPage() {
        Form<ServiceAssembly> form = new Form<ServiceAssembly>("form") {
            @Override
            public void onSubmit() {
                ServiceAssembly sa = getModelObject();
                dao.persist(sa);
                setResponsePage(new ShowServiceAssemblyPage(sa));
            }
        };
        form.setModel(Models.compound(new ServiceAssembly()));
        form.add(new TextField<String>("name").setRequired(true));
        add(form);
    }
}
