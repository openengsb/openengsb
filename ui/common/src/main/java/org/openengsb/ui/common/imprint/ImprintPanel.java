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
package org.openengsb.ui.common.imprint;

import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class ImprintPanel extends Panel {

    private static final long serialVersionUID = 3426187409658223545L;

    public ImprintPanel(String id) {
    super(id);
        initContent();
    }

    private void initContent() {
        String openEngSBUrl = "http://www.openengsb.org";
        String openEngSBMail = "info@openengsb.org";

        ExternalLink websiteLink = new ExternalLink("openEngSBWebsiteLink", openEngSBUrl, openEngSBUrl);
        ExternalLink mailLink = new ExternalLink("openEngSBEmailLink", "mailto:" + openEngSBMail, openEngSBMail);
        add(websiteLink);
        add(mailLink);
    }
}
