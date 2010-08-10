package org.openengsb.ui.web.mock;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class ServiceReferenceMock implements ServiceReference {

    private final Map<String, String> properties = new HashMap<String, String>();

    public ServiceReferenceMock(String name, String id) {
        this.properties.put("name", name);
        this.properties.put("id", id);
    }

    @Override
    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    @Override
    public String[] getPropertyKeys() {
        return this.properties.keySet().toArray(new String[2]);
    }

    @Override
    public Bundle getBundle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle[] getUsingBundles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAssignableTo(Bundle bundle, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Object reference) {
        throw new UnsupportedOperationException();
    }

}
