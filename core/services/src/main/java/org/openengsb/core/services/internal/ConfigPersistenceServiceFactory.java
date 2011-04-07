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

package org.openengsb.core.services.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

public class ConfigPersistenceServiceFactory implements ManagedServiceFactory {

    private static final Log LOGGER = LogFactory.getLog(ConfigPersistenceServiceFactory.class);
    private static final String BACKEND_ID = "backend.id";
    private static final String CONFIGURATION_ID = "configuration.id";
    private BundleContext bundleContext;
    private OsgiUtilsService serviceUtils;

    private HashMap<String, ServiceRegistration> serviceMap =
        new HashMap<String, ServiceRegistration>();

    @Override
    public String getName() {
        return "Configuration Persistence Service Factory";
    }

    @Override
    public synchronized void updated(String pid, Dictionary properties) throws ConfigurationException {
        preconditionPropertyExists(BACKEND_ID, properties);
        preconditionPropertyExists(CONFIGURATION_ID, properties);
        ConfigPersistenceBackendService backendService = retrieveBackendService(properties);
        DefaultConfigPersistenceService configPersistenceService = new DefaultConfigPersistenceService(backendService);
        Dictionary exportProperties = new Hashtable();
        exportProperties.put(CONFIGURATION_ID, properties.get(CONFIGURATION_ID));
        ServiceRegistration registration =
            bundleContext.registerService(ConfigPersistenceService.class.getName(), configPersistenceService,
                exportProperties);
        serviceMap.put(pid, registration);
    }

    private ConfigPersistenceBackendService retrieveBackendService(Dictionary properties) throws ConfigurationException {
        try {
            return serviceUtils.getOsgiServiceProxy(serviceUtils.makeFilter(ConfigPersistenceBackendService.class,
                    String.format("(%s=%s)", BACKEND_ID, properties.get(BACKEND_ID))),
                    ConfigPersistenceBackendService.class);
        } catch (OsgiServiceNotAvailableException e) {
            throw new ConfigurationException(BACKEND_ID, String.format(
                "backend service %s could not be found; please recheck documentation", properties.get(BACKEND_ID)), e);
        } catch (InvalidSyntaxException e) {
            throw new ConfigurationException(BACKEND_ID, String.format("(%s=%s) is not welcomed", BACKEND_ID,
                properties.get(BACKEND_ID)));
        }
    }

    private void preconditionPropertyExists(String key, Dictionary properties) throws ConfigurationException {
        if (properties.get(key) == null) {
            throw new ConfigurationException(key,
                String.format("Property %s is required to configure a ConfigPersistenceService correctly"));
        }
    }

    @Override
    public synchronized void deleted(String pid) {
        serviceMap.get(pid).unregister();
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setServiceUtils(OsgiUtilsService serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

}
