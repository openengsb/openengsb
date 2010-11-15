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

package org.openengsb.ui.common.wicket;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

public abstract class OpenEngSBWicketApplication extends AuthenticatedWebApplication {

    @Override
    protected void init() {
        super.init();
        addInjector();
        getResourceSettings().setAddLastModifiedTimeToResourceReferenceUrl(true);
    }

    protected void addInjector() {
        addComponentInstantiationListener(new SpringComponentInjector(this));
    }

    @Override
    public AjaxRequestTarget newAjaxRequestTarget(Page page) {
        if (page instanceof OpenEngSBPage) {
            ((OpenEngSBPage) page).initContextForCurrentThread();
        }
        return super.newAjaxRequestTarget(page);
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new OpenEngSBWebSession(request);
    }

    @Override
    protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
        return OpenEngSBWebSession.class;
    }

}
