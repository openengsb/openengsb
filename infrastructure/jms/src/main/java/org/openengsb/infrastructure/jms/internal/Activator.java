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
package org.openengsb.infrastructure.jms.internal;

import java.io.File;
import java.util.Dictionary;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private BrokerService brokerService;

    Thread startThread;

    @Override
    public void start(final BundleContext context) throws Exception {
        brokerService = new BrokerService();
        brokerService.setBrokerName("openengsb");

        String karafData = System.getProperty("karaf.data");
        brokerService.setDataDirectory(new File(karafData, "/activemq/openengsb").getAbsolutePath());
        KahaDBPersistenceAdapter persistenceAdapter = new KahaDBPersistenceAdapter();
        persistenceAdapter.setDirectory(new File(karafData, "/activemq/openengsb/kahadb"));
        brokerService.setPersistenceAdapter(persistenceAdapter);

        brokerService.getManagementContext().setCreateConnector(true);

        brokerService.getSystemUsage().getMemoryUsage().setLimit(20 * 1024 * 1024);
        brokerService.getSystemUsage().getStoreUsage().setLimit(1024 * 1024 * 1024);
        brokerService.getSystemUsage().getTempUsage().setLimit(100 * 1024 * 1024);

        final ServiceTracker serviceTracker = new ServiceTracker(context, ConfigurationAdmin.class.getName(), null);
        serviceTracker.open();
        startThread = new Thread() {
            public void run() {
                try {
                    ConfigurationAdmin configadmin = (ConfigurationAdmin) serviceTracker.waitForService(60000);
                    Configuration configuration = configadmin.getConfiguration("org.openengsb.infrastructure.jms");
                    @SuppressWarnings("unchecked")
                    Dictionary<String, Object> props = configuration.getProperties();
                    brokerService.addConnector("tcp://0.0.0.0:" + props.get("openwire"));
                    brokerService.addConnector("stomp://0.0.0.0:" + props.get("stomp"));
                    brokerService.start();
                } catch (Exception e) {
                    LOGGER.error("could not initialize brokerService", e);
                }
            };
        };
        startThread.start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        brokerService.stop();
    }

}
