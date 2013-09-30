package org.openengsb.framework.vfs.vfstestservices;

import java.util.ArrayList;
import java.util.List;
import org.openengsb.framework.vfs.configurationserviceapi.configurableservice.ConfigurableService;
import org.openengsb.framework.vfs.configurationserviceapi.remoteservice.RemoteService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        List<String> propertyList1 = new ArrayList<>();
        propertyList1.add(""); //TODO

        ConfigurableService configurableService1 = new TestConfigurableService(propertyList1, true);

        context.registerService(ConfigurableService.class.getName(), configurableService1, null);

        List<String> propertyList2 = new ArrayList<>();
        propertyList1.add(""); //TODO

        ConfigurableService configurableService2 = new TestConfigurableService(propertyList2, true);

        context.registerService(ConfigurableService.class.getName(), configurableService2, null);

        RemoteService remoteService = new TestRemoteService(true, true);
        context.registerService(RemoteService.class.getName(), remoteService, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
