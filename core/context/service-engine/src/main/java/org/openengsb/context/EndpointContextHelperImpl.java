package org.openengsb.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.openengsb.contextcommon.Context;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.contextcommon.ContextStore;
import org.openengsb.util.WorkingDirectory;

public class EndpointContextHelperImpl implements ContextHelper {

    private ContextStore contextStore = new ContextStore(WorkingDirectory.getFile("context", "contextdata.xml"));

    private String currentId;

    @Override
    public Map<String, String> getAllValues(String path) {
        Context ctx = contextStore.getContext(currentId + "/" + path);
        Set<String> keys = ctx.getKeys();
        Map<String, String> values = new HashMap<String, String>();
        for (String key : keys) {
            values.put(key, ctx.get(key));
        }
        return values;
    }

    @Override
    public String getValue(String pathAndKey) {
        if (pathAndKey.lastIndexOf('/') == -1) {
            pathAndKey = "/" + pathAndKey;
        }

        String path = pathAndKey.substring(0, pathAndKey.lastIndexOf('/'));
        String key = pathAndKey.substring(pathAndKey.lastIndexOf('/') + 1);

        Context ctx = contextStore.getContext(currentId + "/" + path);
        return ctx.get(key);
    }

    @Override
    public void remove(List<String> paths) {
        for (String path : paths) {
            contextStore.removeValue(currentId + "/" + path);
        }
    }

    @Override
    public void store(Map<String, String> values) {
        for (Entry<String, String> entry : values.entrySet()) {
            contextStore.setValue(currentId + "/" + entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void addEmptyContext(List<String> paths) {
        for (String path : paths) {
            contextStore.addContext(currentId + "/" + path);
        }
    }

    public void setCurrentId(String currentId) {
        this.currentId = currentId;
    }

}
