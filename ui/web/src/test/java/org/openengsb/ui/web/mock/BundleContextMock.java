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
    
    private final Map<String, Object> objects = new HashMap<String, Object>();

    public BundleContextMock(List<ServiceManager> services) {
        log.info("Services " + services.size());
        for (ServiceManager serviceManager : services) {
            String name = serviceManager.getClass().getName();
            objects.put(name, serviceManager);
            
            addReference(name, "org.openengsb.core.config.ServiceManager");
            
            String serviceInterfaceId = serviceManager.getDescriptor().getServiceInterfaceId();
            addReference(serviceInterfaceId, serviceInterfaceId);
        }
    }

    private void addReference(String name, String id) {
        List<ServiceReference> list = getReferenceList(id);
        list.add(new ServiceReferenceMock(name, id));
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
        List<ServiceReference> list = references.get(clazz);
        if (list != null) {
            return list.toArray(new ServiceReference[list.size()]);
        }else{
            return new ServiceReference[0];
        }
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
    @SuppressWarnings("rawtypes")
    public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("rawtypes")
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
        return this.objects.get(reference.getProperty("name"));
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
