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
package org.openengsb.domains.example.connector;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import org.openengsb.core.common.Domain;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.openengsb.core.config.util.BundleStrings;
import org.openengsb.domains.example.ExampleDomain;
import org.openengsb.domains.example.connector.internal.LogService;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

public class LogServiceManager implements ServiceManager, BundleContextAware {

    private BundleContext bundleContext;
    private BundleStrings strings;
    private final Map<String, LogService> services = new HashMap<String, LogService>();

    @Override
    public ServiceDescriptor getDescriptor() {
        return getDescriptor(Locale.getDefault());
    }

    @Override
    public ServiceDescriptor getDescriptor(Locale locale) {
        return ServiceDescriptor.builder()
                .id(LogService.class.getName())
                .implementsInterface(ExampleDomain.class.getName())
                .type(LogService.class)
                .name(strings.getString("log.name", locale))
                .description(strings.getString("log.description", locale))
                .attribute(AttributeDefinition.builder()
                        .id("outputMode")
                        .name(strings.getString("log.outputMode.name", locale))
                        .description(strings.getString("log.outputMode.description", locale))
                        .defaultValue("INFO")
                        .required()
                        .build())
                .build();
    }

    @Override
    public void update(String id, Map<String, String> attributes) {
        boolean isNew = false;
        LogService s = null;
        synchronized (services) {
            s = services.get(id);
            if (s == null) {
                s = new LogService(id);
                services.put(id, s);
                isNew = true;
            }
            if (attributes.containsKey("outputMode")) {
                s.setOutputMode(attributes.get("outputMode"));
            }
        }
        if (isNew) {
            Hashtable<String, String> props = new Hashtable<String, String>();
            props.put("id", id);
            props.put("domain", ExampleDomain.class.getName());
            props.put("class", LogService.class.getName());
            bundleContext.registerService(new String[] { LogService.class.getName(), ExampleDomain.class.getName(),
                    Domain.class.getName() },
                    s, props);
        }
    }

    @Override
    public void delete(String id) {
        synchronized (services) {
            services.remove(id);
        }
    }

    public void init() {
        strings = new BundleStrings(bundleContext.getBundle());
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
