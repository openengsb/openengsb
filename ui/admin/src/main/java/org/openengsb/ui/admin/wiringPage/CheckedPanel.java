/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.admin.wiringPage;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class CheckedPanel extends Panel {

    private CheckBox checkBox;

    public CheckedPanel(String id, IModel<Boolean> checkModel, IModel<String> labelModel) {
        super(id);
        initCheckBox(checkModel, labelModel);
        initSimpleLabel();
    }

    private void initCheckBox(IModel<Boolean> checkModel, IModel<String> labelModel) {
        checkBox = new CheckBox("check", checkModel);
        checkBox.setLabel(labelModel);
        checkBox.setOutputMarkupId(true);
        add(checkBox);
    }

    private void initSimpleLabel() {
        SimpleFormComponentLabel simpleLabel = new SimpleFormComponentLabel("label", checkBox);
        simpleLabel.add(AttributeModifier.replace("for", checkBox.getMarkupId()));
        add(simpleLabel);
    }

}
