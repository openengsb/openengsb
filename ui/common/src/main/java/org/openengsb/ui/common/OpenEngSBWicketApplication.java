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

package org.openengsb.ui.common;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.ops4j.pax.wicket.api.InjectorHolder;

/**
 * Base class for Wicket Applications in OpenEngSB and client projects. It enforces authentication and initializes the
 * spring-injector.
 */
public abstract class OpenEngSBWicketApplication extends AuthenticatedWebApplication {

    @Override
    protected void init() {
        super.init();
        addInjector();
        DomainAuthorizationStrategy strategy = new DomainAuthorizationStrategy();
        InjectorHolder.getInjector().inject(strategy);
        getSecuritySettings().setAuthorizationStrategy(strategy);
        getResourceSettings().setAddLastModifiedTimeToResourceReferenceUrl(true);
    }

    protected void addInjector() {

    }

    @Override
    public AjaxRequestTarget newAjaxRequestTarget(Page page) {
        if (page instanceof OpenEngSBPage) {
            ((OpenEngSBPage) page).initContextForCurrentThread();
        }
        return super.newAjaxRequestTarget(page);
    }

}
