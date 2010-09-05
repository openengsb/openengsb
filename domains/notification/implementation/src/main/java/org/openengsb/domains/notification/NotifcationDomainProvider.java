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
package org.openengsb.domains.notification;

import org.openengsb.core.common.Event;
import org.openengsb.core.config.Domain;
import org.openengsb.core.config.DomainProvider;
import org.openengsb.core.config.util.BundleStrings;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotifcationDomainProvider implements DomainProvider, BundleContextAware {

    private BundleContext bundleContext;
    private BundleStrings strings;

    public NotifcationDomainProvider() {
    }

    @Override
    public String getId() {
        return "domains.notification";
    }

    @Override
    public String getName() {
        return getName(Locale.getDefault());
    }

    @Override
    public String getName(Locale locale) {
        return strings.getString("notification.domain.name", locale);
    }

    @Override
    public String getDescription() {
        return getDescription(Locale.getDefault());
    }

    @Override
    public String getDescription(Locale locale) {
        return strings.getString("notification.domain.description", locale);
    }

    public void init() {
        strings = new BundleStrings(bundleContext.getBundle());
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public Class<? extends Domain> getDomainInterface() {
        return NotificationDomain.class;
    }

    @Override
    public List<Class<? extends Event>> getEvents() {
        List<Class<? extends Event>> events = new ArrayList<Class<? extends Event>>();
        return events;
    }
}
