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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.context.ContextCurrentService;

@SuppressWarnings("serial")
public class BasePage extends WebPage {

    @SpringBean
    private ContextCurrentService contextService;

    public BasePage() {
        initDummyContext();
    }

    private void initDummyContext() {
        try {
            if (contextService != null) {
                contextService.setThreadLocalContext("foo");
            }
        } catch (IllegalArgumentException e) {
            contextService.createContext("foo");
            contextService.setThreadLocalContext("foo");
            contextService.putValue("foo/bar/fix/fox", "fux");
            contextService.putValue("foo/bar/fix/bar", "1");
            contextService.putValue("foo/bar/fix/baz", "2");
            contextService.putValue("foo/bar/fix/buz", "3");
            contextService.putValue("xyz/bar/fix/bar", "1");
            contextService.putValue("xyz/bar/fix/baz", "2");
            contextService.putValue("xyz/bar/fix/buz", "3");
        }
    }
}
