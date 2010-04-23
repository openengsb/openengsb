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
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;

public class EditContextPage extends BasePage {
    private final Model<String> contextModel;

    @SuppressWarnings("serial")
    public EditContextPage() {
        Form<?> form = new Form<Object>("form") {
            @Override
            public void onSubmit() {
                EditContextPage.this.onSubmit();
            }
        };
        add(form);

        contextModel = new Model<String>(contextService.getContext());
        TextArea<String> area = new TextArea<String>("area", contextModel);
        form.add(area);
    }

    protected void onSubmit() {
        contextService.updateContext(contextModel.getObject());
    }
}
