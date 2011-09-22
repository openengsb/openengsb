package org.openengsb.core.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.openengsb.core.api.security.model.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public abstract class AbstractPermissionProvider implements PermissionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPermissionProvider.class);

    protected final Map<String, Class<? extends Permission>> supported;

    protected AbstractPermissionProvider(Class<? extends Permission>... classes) {
        Map<String, Class<? extends Permission>> map = Maps.newHashMap();
        for (Class<? extends Permission> p : classes) {
            map.put(p.getName(), p);
        }
        supported = Collections.unmodifiableMap(map);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Permission> getPermissionClass(String className) {
        try {
            return (Class<? extends Permission>) this.getClass().getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("permission class not found: ", e);
            return null;
        }
    }

    @Override
    public Collection<Class<? extends Permission>> getSupportedPermissionClasses() {
        return supported.values();
    }

}
