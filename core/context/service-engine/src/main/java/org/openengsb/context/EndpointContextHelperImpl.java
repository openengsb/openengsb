/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE\-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.openengsb.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openengsb.contextcommon.Context;
import org.openengsb.contextcommon.ContextHelperExtended;
import org.openengsb.contextcommon.ContextStore;
import org.openengsb.util.WorkingDirectory;

public class EndpointContextHelperImpl implements ContextHelperExtended {

    private ContextStore contextStore = new ContextStore(WorkingDirectory.getFile("context", "contextdata.xml"));

    private String currentId;

    @Override
    public Map<String, String> getAllValues(String path) {
        Context ctx = contextStore.getContext(currentId + "/" + path);
        Map<String, String> values = new HashMap<String, String>();

        collectValuesFromContext(values, ctx);

        if (ctx.getChild("SU") != null) {
            collectSUValues(values, ctx.getChild("SU"));
        }

        if (ctx.getChild("SE") != null) {
            collectValuesFromContext(values, ctx.getChild("SE"));
        }

        return values;
    }
    
    private void collectValuesFromContext(Map<String, String> values, Context ctx) {
        for (String key : ctx.getKeys()) {
            if (!values.containsKey(key)) {
                values.put(key, ctx.get(key));
            }
        }
    }
    
    private void collectSUValues(Map<String, String> values, Context sUs) {
        for (String name : sUs.getChildrenNames()) {
            collectValuesFromContext(values, sUs.getChild(name));
        }
    }

    @Override
    public String getValue(String pathAndKey) {
        if (pathAndKey.lastIndexOf('/') == -1) {
            pathAndKey = "/" + pathAndKey;
        }

        String path = pathAndKey.substring(0, pathAndKey.lastIndexOf('/'));
        String key = pathAndKey.substring(pathAndKey.lastIndexOf('/') + 1);

        Context ctx = contextStore.getContext(currentId + "/" + path);

        do {
            String value = getValueFromCoreSUSE(ctx, key);
            if (value != null) {
                return value;
            }

            ctx = ctx.getParent();
        } while (ctx != null);

        return null;
    }

    private String getValueFromCoreSUSE(Context ctx, String key) {
        if (ctx.containsKey(key)) {
            return ctx.get(key);
        } else if (ctx.getChild("SU") != null) {
            Context sUs = ctx.getChild("SU");
            for (String name : sUs.getChildrenNames()) {
                Context su = sUs.getChild(name);
                if (su.containsKey(key)) {
                    return su.get(key);
                }
            }
        }
        if (ctx.getChild("SE") != null && ctx.getChild("SE").containsKey(key)) {
            return ctx.getChild("SE").get(key);
        }

        return null;
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

    @Override
    public Context getContext(String path) {
        return contextStore.getContext(path);
    }

    public void setCurrentId(String currentId) {
        this.currentId = currentId;
    }
}
