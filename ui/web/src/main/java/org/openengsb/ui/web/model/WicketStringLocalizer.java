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

package org.openengsb.ui.web.model;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.common.l10n.LocalizableString;
import org.openengsb.core.common.l10n.StringLocalizer;

/**
 * String localizer that implements the OpenEngSB's {@link StringLocalizer} interface and uses Wicket to localize the
 * actual string.
 */
@SuppressWarnings("serial")
public class WicketStringLocalizer implements StringLocalizer {

    private final Component component;

    public WicketStringLocalizer(Component component) {
        this.component = component;
    }

    @Override
    public String getString(String key, Locale locale, String... parameters) {
        return new StringResourceModel(key, component, null).getString();
    }

    @Override
    public LocalizableString getString(final String key, String... parameters) {
        return new LocalizableString() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public String getString(Locale locale) {
                return new StringResourceModel(key, component, null).getString();
            }
        };
    }
}
