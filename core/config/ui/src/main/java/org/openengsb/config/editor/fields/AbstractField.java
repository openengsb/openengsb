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
package org.openengsb.config.editor.fields;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.openengsb.config.jbi.types.AbstractType;

@SuppressWarnings("serial")
public abstract class AbstractField extends Panel {
    private final AbstractType abstractType;
    private FormComponent<?> formComponent;

    public AbstractField(String id, AbstractType abstractType) {
        super(id);
        this.abstractType = abstractType;
    }

    public boolean isRequired() {
        return !abstractType.isOptional();
    }

    protected void setFormComponent(FormComponent<?> formComponent) {
        this.formComponent = formComponent;
    }

    public AbstractField setLabel(IModel<String> labelModel) {
        formComponent.setLabel(labelModel);
        return this;
    }
}
