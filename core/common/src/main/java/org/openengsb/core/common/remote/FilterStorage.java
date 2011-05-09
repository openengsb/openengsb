package org.openengsb.core.common.remote;

import java.util.HashMap;
import java.util.Map;

public final class FilterStorage {

    private static ThreadLocal<Map<String, Object>> storage = new ThreadLocal<Map<String, Object>>();

    public static Map<String, Object> getStorage() {
        Map<String, Object> map = storage.get();
        if (map == null) {
            map = new HashMap<String, Object>();
            storage.set(map);
        }
        return map;
    }

    public static void clear() {
        Map<String, Object> map = storage.get();
        if (map != null) {
            map.clear();
        } else {
            storage.set(new HashMap<String, Object>());
        }
    }

    private FilterStorage() {
    }

}
