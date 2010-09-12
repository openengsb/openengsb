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
package org.openengsb.core.common;

import java.util.Locale;

import org.openengsb.core.common.util.BundleStrings;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;


public abstract class AbstractDomainProvider implements DomainProvider, BundleContextAware {

    private BundleContext bundleContext;
    private BundleStrings strings;

    @Override
    public String getName() {
        return getName(Locale.getDefault());
    }

    @Override
    public String getName(Locale locale) {
        return strings.getString("domain.name", locale);
    }

    @Override
    public String getDescription() {
        return getDescription(Locale.getDefault());
    }

    @Override
    public String getDescription(Locale locale) {
        return strings.getString("domain.description", locale);
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.strings = new BundleStrings(this.bundleContext.getBundle());
    }
}
