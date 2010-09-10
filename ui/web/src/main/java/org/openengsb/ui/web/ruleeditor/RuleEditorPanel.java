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
package org.openengsb.ui.web.ruleeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.openengsb.core.workflow.RuleBaseException;
import org.openengsb.core.workflow.RuleManager;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;

@SuppressWarnings("serial")
public class RuleEditorPanel extends Panel {

    private RuleManager ruleManager;
    private TextArea<String> textArea;
    private DropDownChoice<RuleBaseElementId> ruleChoice;
    private DropDownChoice<RuleBaseElementType> typeChoice;

    public RuleEditorPanel(String id, RuleManager ruleManager) {
        super(id);
        this.ruleManager = ruleManager;
        try {
            initPage();
        } catch (RuleBaseException e) {
            throw new RuntimeException(e);
        }
    }

    private void initPage() throws RuleBaseException {
        Form<Object> form = new Form<Object>("form");
        List<RuleBaseElementType> ruleTypes = Arrays.asList(RuleBaseElementType.values());
        typeChoice = new DropDownChoice<RuleBaseElementType>("typeChoice", ruleTypes);
        typeChoice.setModel(new Model<RuleBaseElementType>());
        typeChoice.getModel().setObject(RuleBaseElementType.Rule);
        typeChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                reloadTextArea();
                reloadRuleChoice();
                target.addComponent(textArea);
                target.addComponent(ruleChoice);
            }
        });
        form.add(typeChoice);
        ruleChoice = new DropDownChoice<RuleBaseElementId>("ruleChoice");
        reloadRuleChoice();
        ruleChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.addComponent(textArea);
                reloadTextArea();
            }
        });
        ruleChoice.setOutputMarkupId(true);
        form.add(ruleChoice);
        form.add(new AjaxButton("save") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                RuleBaseElementId selection = ruleChoice.getModelObject();
                String text = textArea.getModelObject();
                if (selection != null && text != null) {
                    try {
                        ruleManager.update(selection, text);
                    } catch (RuleBaseException e) {
                        error("could not save rule");
                    }
                }
            }
        });
        form.add(new AjaxButton("cancel") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                reloadTextArea();
            }
        });
        form.add(new AjaxButton("new") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                throw new UnsupportedOperationException("nyi");
            }
        });
        Model<String> textAreaModel = new Model<String>();
        textArea = new TextArea<String>("text", textAreaModel);
        textArea.setEnabled(false);
        textArea.setOutputMarkupId(true);
        form.add(textArea);
        add(form);
    }

    private void reloadRuleChoice() {
        List<RuleBaseElementId> choices;
        RuleBaseElementType selectedType = typeChoice.getModelObject();
        if (selectedType != null) {
            choices = new ArrayList<RuleBaseElementId>(ruleManager.list(selectedType));
        } else {
            choices = new ArrayList<RuleBaseElementId>();
        }
        ruleChoice.setChoices(choices);
        ruleChoice.setModel(new Model<RuleBaseElementId>());
    }

    private void reloadTextArea() {
        RuleBaseElementId selection = ruleChoice.getModelObject();
        if (selection != null) {
            textArea.setModel(new Model<String>(ruleManager.get(selection)));
            textArea.setEnabled(true);
        } else {
            textArea.setModel(new Model<String>());
            textArea.setEnabled(false);
        }
    }
}
