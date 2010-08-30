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

import org.apache.wicket.markup.html.WebPage;
import org.openengsb.ui.web.global.footer.FooterTemplate;
import org.openengsb.ui.web.global.header.HeaderTemplate;

public abstract class BasePage extends WebPage {

    public BasePage() {
        super();
        this.initWebPage();
    }

    private void initWebPage() {
        this.add(new HeaderTemplate("header", this.getHeaderMenuItem()));
        this.add(new FooterTemplate("footer"));
    }


    public abstract String getHeaderMenuItem();
}
