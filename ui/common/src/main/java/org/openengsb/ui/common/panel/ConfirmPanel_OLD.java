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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.openengsb.ui.common.modaldialog.YesNoModalDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConfirmPanel_OLD<T> extends YesNoModalDialog {

    private static final long serialVersionUID = 7137438656270166861L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmPanel_OLD.class);

    private WebMarkupContainer parent;
    
    private String username;

    public ConfirmPanel_OLD(String id, IModel<T> model, WebMarkupContainer parent) {
        super(id, model);
        this.parent = parent;
        initContent();
    }
    
    public ConfirmPanel_OLD(String id, IModel<T> model, WebMarkupContainer parent, String username) {
        super(id, model);
        this.parent = parent;
        initContent();
        this.username = username;
    }

    public ConfirmPanel_OLD(String id, WebMarkupContainer parent) {
        super(id);
        this.parent = parent;
        initContent();
    }

    private void initContent() {
        setOutputMarkupId(true);
        add(new Label("username",this.username));
        add(new AjaxButton("yes") {
            private static final long serialVersionUID = 1883099779596621667L;

            @Override
            protected void onSubmit(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                onConfirm(ajaxRequestTarget, form);
                hideSelf(ajaxRequestTarget);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                LOGGER.warn("Form submit errors of yes button");
            }
        });
        add(new AjaxButton("no") {
            private static final long serialVersionUID = -2124017726733077652L;

            @Override
            protected void onSubmit(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                onCancel(ajaxRequestTarget, form);
                hideSelf(ajaxRequestTarget);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                LOGGER.warn("Form submit errors of no button");
            }
        });
    }

    protected void hideSelf(AjaxRequestTarget ajaxRequestTarget) {
        replaceWith(new EmptyPanel(getId()));
        ajaxRequestTarget.add(parent);
    }

    protected abstract void onConfirm(AjaxRequestTarget ajaxRequestTarget, Form<?> form);

    protected abstract void onCancel(AjaxRequestTarget ajaxRequestTarget, Form<?> form);

}
