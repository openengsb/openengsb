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

import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class BoolToStringModel implements IModel<Boolean> {
    private final IModel<String> model;

    public BoolToStringModel(IModel<String> model) {
        this.model = model;
    }

    @Override
    public Boolean getObject() {
        String v = model.getObject();
        return "1".equals(v) || "true".equals(v);
    }

    @Override
    public void setObject(Boolean object) {
        model.setObject(object.toString());
    }

    @Override
    public void detach() {
        // noop
    }
}