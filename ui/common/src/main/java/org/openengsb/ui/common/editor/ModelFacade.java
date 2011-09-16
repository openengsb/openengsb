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

package org.openengsb.ui.common.editor;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 * With this class, a field has the possibility to show more than one component if it needs more. The OauthField
 * for example needs a link in addition to the text field and it's label. The main component defines which
 * value will be the result of the model (e.g. the string of a text field). All help components will be added to
 * the field left to the main component.
 */
public class ModelFacade<T> {
    private FormComponent<T> mainComponent;
    private List<Component> helpComponents;

    public FormComponent<T> getMainComponent() {
        return mainComponent;
    }

    public void setMainComponent(FormComponent<T> wicketMainComponent) {
        mainComponent = wicketMainComponent;
    }

    public List<Component> getHelpComponents() {
        return helpComponents;
    }

    public void setHelpComponents(List<Component> wicketHelpComponents) {
        helpComponents = wicketHelpComponents;
    }
}
