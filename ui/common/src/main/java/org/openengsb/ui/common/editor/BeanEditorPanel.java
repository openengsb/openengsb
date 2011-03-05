/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.ui.common.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.ui.common.model.MapModel;
import org.openengsb.ui.common.util.MethodUtil;

/**
 * A Panel that serves as an Editor for Beans. It generates fields for each property (using a
 *
 * @link{org.openengsb.ui.common.wicket.editor.AttributeEditorUtil ) of the given type, and saves the data in a Map
 */
@SuppressWarnings("serial")
public class BeanEditorPanel extends Panel {

    private final Map<String, String> fieldViewIds = new HashMap<String, String>();

    public BeanEditorPanel(String id, Class<?> type, Map<String, String> values) {
        super(id);
        RepeatingView fields = new RepeatingView("fields");
        List<AttributeDefinition> attributes = MethodUtil.buildAttributesList(type);
        for (AttributeDefinition a : attributes) {
            String fieldViewId = fields.newChildId();
            fieldViewIds.put(a.getId(), fieldViewId);
            WebMarkupContainer row = new WebMarkupContainer(fieldViewId);
            fields.add(row);
            row.add(AttributeEditorUtil.createEditorField("row", new MapModel<String, String>(values, a.getId()), a));
        }
        add(fields);
    }

    public String getFieldViewId(String fieldId) {
        return fieldViewIds.get(fieldId);
    }

}
