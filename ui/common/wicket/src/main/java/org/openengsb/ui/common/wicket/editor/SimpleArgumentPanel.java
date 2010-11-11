/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.common.wicket.editor;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.AttributeDefinition.Builder;
import org.openengsb.core.common.l10n.PassThroughStringLocalizer;
import org.openengsb.core.common.util.MethodUtil;
import org.openengsb.ui.common.wicket.model.ArgumentModel;

@SuppressWarnings("serial")
public class SimpleArgumentPanel extends Panel {
    public SimpleArgumentPanel(String id, ArgumentModel arg) {
        super(id);
        Builder builder = AttributeDefinition.builder(new PassThroughStringLocalizer());
        MethodUtil.addEnumValues(arg.getType(), builder);
        builder.id("value").name(new StringResourceModel("argument", this, new Model<ArgumentModel>(arg)).getString());
        add(AttributeEditorUtil.createEditorField("value", new PropertyModel<String>(arg, "value"), builder.build()));
    }
}
