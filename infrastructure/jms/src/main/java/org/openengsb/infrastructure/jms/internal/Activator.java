package org.openengsb.infrastructure.jms.internal;

import java.io.File;
import java.util.Dictionary;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class Activator implements BundleActivator {
    private BrokerService brokerService;

    @Override
    public void start(BundleContext context) throws Exception {
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

        ConfigurationAdmin configadmin = context.getService(context.getServiceReference(ConfigurationAdmin.class));
        Configuration configuration = configadmin.getConfiguration("org.openengsb.infrastructure.jms");
        @SuppressWarnings("unchecked")
        Dictionary<String, Object> props = configuration.getProperties();

        brokerService.addConnector("tcp://0.0.0.0:" + props.get("openwire"));
        brokerService.addConnector("stomp://0.0.0.0:" + props.get("stomp"));

        brokerService.start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        brokerService.stop();
    }

}
