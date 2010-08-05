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

import java.util.HashMap;

import org.openengsb.core.config.ServiceManager;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.openengsb.ui.web.editor.EditorPanel;

public class EditorPage extends BasePage {

    @SuppressWarnings("serial")
    public EditorPage(ServiceManager service) {
        ServiceDescriptor descriptor = service.getDescriptor(getSession().getLocale());
        HashMap<String, String> values = new HashMap<String, String>();
        for (AttributeDefinition attribute : descriptor.getAttributes()) {
            values.put(attribute.getId(), attribute.getDefaultValue());
        }
        add(new EditorPanel("editor", descriptor.getAttributes(), values) {
            @Override
            public void onSubmit() {
            }
        });
    }

}
