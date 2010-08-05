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

import java.util.Locale;
import java.util.Map;

import org.openengsb.core.config.ServiceManager;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.core.config.descriptor.ServiceDescriptor;

public class StdoutLogServiceManager implements ServiceManager {

    @Override
    public ServiceDescriptor getDescriptor() {
        return getDescriptor(Locale.getDefault());
    }

    @Override
    public ServiceDescriptor getDescriptor(Locale locale) {
        boolean de = "de".equals(locale.getLanguage());
        return ServiceDescriptor.builder()
                .id(StdoutLogService.class.getName())
                .implementsInterface(LogDomain.class.getName())
                .name(de ? "Stdout Log Service" : "Stdout Log Service")
                .description(de ? "Loggt Nachrichten auf den Stdout." : "Logs messages to the stdout.")
                .attribute(AttributeDefinition.builder()
                .id("flush")
                .name(de ? "Ausgabe flushen" : "Flush Output")
                .description(de ? "" : "")
                .defaultValue("true")
                .required()
                .build())
        .build();
    }

    @Override
    public void update(String id, Map<String, String> attributes) {
    }

    @Override
    public void delete(String id) {
    }

}
