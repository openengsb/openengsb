package org.openengsb.ui.web.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openengsb.core.config.DomainProvider;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.openengsb.ui.web.fixtures.log.LogDomain;
import org.openengsb.ui.web.fixtures.log.LogDomainProvider;
import org.openengsb.ui.web.fixtures.log.StdoutLogServiceManager;
import org.openengsb.ui.web.mock.BundleContextMock;
import org.osgi.framework.ServiceReference;

public class OsgiManagedServicesTest {

    Log log = LogFactory.getLog(OsgiManagedServicesTest.class);

    @Test
    public void testGetManagedServiceInstances() {
        OsgiManagedServices services = new OsgiManagedServices();
        ServiceManager[] serviceManagers = new ServiceManager[] { new StdoutLogServiceManager(),
                new StdoutLogServiceManager() };

        BundleContextMock context = new BundleContextMock(Arrays.asList(serviceManagers));
        OsgiDomainService domainService = new OsgiDomainService();
        DomainProvider[] providers = new DomainProvider[] { new LogDomainProvider(), new LogDomainProvider() };
        domainService.setDomains(Arrays.asList(providers));
        services.setBundleContext(context);
        services.setDomainService(domainService);

        Map<Class<?>, List<ServiceReference>> managedServiceInstances = services.getManagedServiceInstances();
        assertEquals(1, managedServiceInstances.size());
        assertTrue(managedServiceInstances.containsKey(LogDomain.class));
        List<ServiceReference> list = managedServiceInstances.get(LogDomain.class);
        assertEquals(1, list.size());
        ServiceReference reference = list.get(0);

        ServiceDescriptor descriptor = serviceManagers[0].getDescriptor();
        assertEquals(descriptor.getName(), reference.getProperty("name"));
        assertEquals(descriptor.getId(), reference.getProperty("id"));
    }

}
