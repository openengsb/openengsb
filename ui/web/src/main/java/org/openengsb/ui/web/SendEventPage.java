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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.ui.web.editor.EditorPanel;

public class SendEventPage extends BasePage {

    SendEventPage(List<Class> classes) {
        List<String> names = new ArrayList<String>(classes.size());
        for (Class clazz : classes) {
            names.add(clazz.getSimpleName());
        }
        add(new DropDownChoice<String>("dropdown", names));
        List<AttributeDefinition> attributes = null;
        Map<String, String> defaults = new HashMap<String, String>();
        add(new EditorPanel("editor", attributes, defaults));
    }
}
