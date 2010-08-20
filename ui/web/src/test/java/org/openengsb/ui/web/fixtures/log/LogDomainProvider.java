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
package org.openengsb.ui.web.fixtures.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.openengsb.core.common.Domain;
import org.openengsb.core.common.Event;
import org.openengsb.core.config.DomainProvider;

public class LogDomainProvider implements DomainProvider {

    @Override
    public String getId() {
        return "domains.log";
    }

    @Override
    public String getName() {
        return getName(Locale.getDefault());
    }

    @Override
    public String getName(Locale locale) {
        return "de".equals(locale.getLanguage()) ? "Logging Provider" : "Logging Provider";
    }

    @Override
    public String getDescription() {
        return getDescription(Locale.getDefault());
    }

    @Override
    public String getDescription(Locale locale) {
        return "de".equals(locale.getLanguage()) ? "Stellt Services zum Loggen zur Verfuegung"
                : "Provides logging services";
    }

    @Override
    public Class<? extends Domain> getDomainInterface() {
        return LogDomain.class;
    }

    @Override
    public List<Class<? extends Event>> getEvents() {
        return new ArrayList<Class<? extends Event>>();
    }

}
