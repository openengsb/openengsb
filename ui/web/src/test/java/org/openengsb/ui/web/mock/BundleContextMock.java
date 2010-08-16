package org.openengsb.ui.web.mock;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class BundleContextMock implements BundleContext {

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

    public ServiceReference[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        log.info("Getting Service References for: " + clazz);
        if (filter != null) {
            throw new IllegalArgumentException();
        }
        List<ServiceReference> list = references.get(clazz);
        return list.toArray(new ServiceReference[list.size()]);
    }

    @Override
    public String getProperty(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getBundle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle installBundle(String location, InputStream input) throws BundleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle installBundle(String location) throws BundleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getBundle(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle[] getBundles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addServiceListener(ServiceListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeServiceListener(ServiceListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addBundleListener(BundleListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeBundleListener(BundleListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addFrameworkListener(FrameworkListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeFrameworkListener(FrameworkListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceRegistration registerService(String clazz, Object service, Dictionary properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceReference getServiceReference(String clazz) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Object getService(ServiceReference reference) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean ungetService(ServiceReference reference) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getDataFile(String filename) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Filter createFilter(String filter) throws InvalidSyntaxException {
        throw new UnsupportedOperationException();
    }
}
