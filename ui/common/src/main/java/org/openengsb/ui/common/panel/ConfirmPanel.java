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
package org.openengsb.ui.common.panel;

import org.apache.wicket.model.IModel;
import org.openengsb.ui.common.modaldialog.YesNoModalDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConfirmPanel<T> extends YesNoModalDialog {

    private static final long serialVersionUID = 7137438656270166861L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmPanel.class);

    private String username;

    public ConfirmPanel(String id, IModel<T> model) {
        super(id, model);
        initContent();
    }
    
    public ConfirmPanel(String id, IModel<T> model, String username) {
        super(id, model);
        initContent();
        this.username = username;
    }

 
    private void initContent() {
        setOutputMarkupId(true);
    }
 
}
