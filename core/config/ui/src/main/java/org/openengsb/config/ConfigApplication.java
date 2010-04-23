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
package org.openengsb.config;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.openengsb.config.editor.ContextStringResourceLoader;
import org.openengsb.config.view.OverviewPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 *
 * @see org.openengsb.config.Start#main(String[])
 */
public class ConfigApplication extends WebApplication {
    @Autowired
    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext context) {
        this.applicationContext = context;
    }

    public ConfigApplication() {
    }

    @Override
    protected void init() {
        super.init();
        this.addComponentInstantiationListener(new SpringComponentInjector(this, applicationContext));
        this.getResourceSettings().addStringResourceLoader(ContextStringResourceLoader.instance);
    }

    @Override
    public Class<OverviewPage> getHomePage() {
        return OverviewPage.class;
    }
}
