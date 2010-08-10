package org.openengsb.ui.web.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockBundleContext;

public class BundleContextMock extends MockBundleContext {

    private final Log log = LogFactory.getLog(BundleContextMock.class);

    private final Map<String, List<ServiceReference>> references = new HashMap<String, List<ServiceReference>>();

    public BundleContextMock(List<ServiceManager> services) {
        log.info("Services " + services.size());
        for (ServiceManager serviceManager : services) {
            ServiceDescriptor descriptor = serviceManager.getDescriptor();
            log.info(descriptor);
            String serviceInterfaceId = descriptor.getServiceInterfaceId();
            List<ServiceReference> list = getReferenceList(serviceInterfaceId);
            log.debug(descriptor.getName() + " " + descriptor.getId());
            list.add(new ServiceReferenceMock(descriptor.getName(), descriptor.getId()));
        }
    }

    private List<ServiceReference> getReferenceList(String serviceInterfaceId) {
        if (!this.references.containsKey(serviceInterfaceId)) {
            references.put(serviceInterfaceId, new ArrayList<ServiceReference>());
        }
        List<ServiceReference> list = references.get(serviceInterfaceId);
        return list;
    }

    @Override
    public ServiceReference[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        log.info("Getting Service References for: " + clazz);
        if (filter != null) {
            throw new IllegalArgumentException();
        }
        List<ServiceReference> list = references.get(clazz);
        return list.toArray(new ServiceReference[list.size()]);
    }
}
