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
package org.openengsb.config.editor.fields;

import java.util.Arrays;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.openengsb.config.jbi.types.ChoiceType;

public class DropdownChoiceField extends AbstractField {
    private static final long serialVersionUID = 1L;

    public DropdownChoiceField(String id, IModel<String> model, ChoiceType choiceType) {
        super(id, choiceType);
        DropDownChoice<String> ch = new DropDownChoice<String>("component", model, Arrays.asList(choiceType.getValues()));
        ch.setRequired(isRequired());
        add(ch);
        setFormComponent(ch);
    }
}
