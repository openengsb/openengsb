package org.openengsb.core.api.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import org.openengsb.core.api.security.model.SecurityAttributeEntry;

import com.google.common.collect.MapMaker;

public final class SecurityAttributeManager {

    private static ConcurrentMap<Object, Collection<SecurityAttributeEntry>> runtimeAttributes = new MapMaker()
        .weakKeys().makeMap();

    public static void storeAttribute(Object o, SecurityAttributeEntry... attributes) {
        storeAttribute(o, Arrays.asList(attributes));
    }

    public static void storeAttribute(Object o, Collection<SecurityAttributeEntry> securityAttributes) {
        runtimeAttributes.put(o, securityAttributes);
    }

    public static Collection<SecurityAttributeEntry> getAttribute(Object o) {
        return runtimeAttributes.get(o);
    }

    private SecurityAttributeManager() {
    }

}
