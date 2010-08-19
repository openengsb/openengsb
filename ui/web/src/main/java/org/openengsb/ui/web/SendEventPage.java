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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.core.config.descriptor.AttributeDefinition.Builder;
import org.openengsb.ui.web.editor.EditorPanel;

public class SendEventPage extends BasePage {

    SendEventPage(List<Class<?>> classes) {
        List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
        Map<String, String> defaults = new HashMap<String, String>();
        try {

            BeanInfo beanInfo = Introspector.getBeanInfo(classes.get(0));
            beanInfo.getBeanDescriptor().getDisplayName();
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if(propertyDescriptor.getWriteMethod() == null || !Modifier.isPublic(propertyDescriptor.getWriteMethod().getModifiers())) {
                    continue;
                }
                Builder builder = AttributeDefinition.builder();
                builder.name(propertyDescriptor.getDisplayName());
                builder.description(propertyDescriptor.getShortDescription());
                builder.id(propertyDescriptor.getName());
                attributes.add(builder.build());
            }
        } catch (IntrospectionException ex) {
            Logger.getLogger(SendEventPage.class.getName()).log(Level.SEVERE, null, ex);
        }
        ChoiceRenderer<Class<?>> choiceRenderer = new ChoiceRenderer<Class<?>>("canonicalName", "simpleName");
        DropDownChoice<Class<?>> dropDownChoice = new DropDownChoice<Class<?>>("dropdown", classes, choiceRenderer);
        dropDownChoice.setModel(new Model<Class<?>>(classes.get(0)));
        add(dropDownChoice);
        add(new EditorPanel("editor", attributes, defaults));
    }
}
