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
package org.openengsb.config.editor;

import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.config.editor.fields.AbstractField;
import org.openengsb.config.editor.fields.CheckboxField;
import org.openengsb.config.editor.fields.DropdownChoiceField;
import org.openengsb.config.editor.fields.InputField;
import org.openengsb.config.jbi.BeanInfo;
import org.openengsb.config.jbi.types.AbstractType;
import org.openengsb.config.jbi.types.BoolType;
import org.openengsb.config.jbi.types.ChoiceType;
import org.openengsb.config.jbi.types.RefType;
import org.openengsb.config.service.AssemblyService;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public abstract class EditorPanel extends Panel {
    private final String componentId;
    private final FieldInfos fieldInfos;
    private final Map<String, String> map;
    @SpringBean
    private AssemblyService assemblyService;

    public EditorPanel(String id, String componentId, FieldInfos fieldInfos, Map<String, String> map) {
        super(id);
        this.componentId = componentId;
        this.fieldInfos = fieldInfos;
        this.map = map;
        createForm();
    }

    public Map<String, String> getValues() {
        return map;
    }

    public abstract void onSubmit();

    @SuppressWarnings( { "unchecked", "serial" })
    private void createForm() {
        Form<?> form = new Form("form") {
            @Override
            protected void onSubmit() {
                EditorPanel.this.onSubmit();
            }
        };
        add(form);

        RepeatingView fields = new RepeatingView("fields");
        form.add(fields);
        form.add(new FeedbackPanel("feedback"));

        for (AbstractType f : fieldInfos.getFieldTypes()) {
            WebMarkupContainer row = new WebMarkupContainer(fields.newChildId());
            fields.add(row);
            ResourceModel labelModel = new ResourceModel(fieldInfos.getName() + '.' + f.getName());
            row.add(new Label("name", labelModel));
            row.add(getEditor(f, new MapModel<String, String>(map, f.getName())).setLabel(labelModel));
        }
    }

    private AbstractField getEditor(AbstractType type, IModel<String> model) {
        if (type.getClass().equals(BoolType.class)) {
            return new CheckboxField("editor", model, type);
        } else if (type.getClass().equals(ChoiceType.class)) {
            return new DropdownChoiceField("editor", model, (ChoiceType) type);
        } else if (type.getClass().equals(RefType.class)) {
            List<BeanInfo> beans = assemblyService.getBeansForType(((RefType) type).getTheClass());
            List<String> values = Lists.transform(beans, new Function<BeanInfo,String>() {
                public String apply(BeanInfo input) {
                    return input.getMap().get("id");
                }
            });
            return new DropdownChoiceField("editor", model, type, values);
        } else {
            return new InputField("editor", model, type);
        }
    }
}
