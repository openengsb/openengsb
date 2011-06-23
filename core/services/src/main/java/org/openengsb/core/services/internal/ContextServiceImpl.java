/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.services.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.context.Context;
import org.openengsb.core.api.context.ContextConnectorService;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.context.ContextPath;
import org.openengsb.core.api.context.ContextStorageBean;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.osgi.framework.BundleContext;

import com.google.common.base.Preconditions;

public class ContextServiceImpl implements ContextCurrentService, ContextConnectorService {

    private Context rootContext;

    private PersistenceManager persistenceManager;

    private PersistenceService persistence;

    private BundleContext bundleContext;

    public void init() {
        try {
            persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());

            List<ContextStorageBean> contexts = persistence.query(new ContextStorageBean(null));
            if (contexts.isEmpty()) {
                Context root = new Context();
                persistence.create(new ContextStorageBean(root));
                rootContext = root;
            } else {
                rootContext = contexts.get(0).getRootContext();
            }
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    private void storeContext() {
        try {
            persistence.update(new ContextStorageBean(null), new ContextStorageBean(rootContext));
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putValue(String pathAndKey, String value) {
        String[] split = splitPath(pathAndKey);
        Context context = getContext(split[0], true);
        context.put(split[1], value);
        storeContext();
    }

    @Override
    public String getValue(String pathAndKey) {
        String[] split = splitPath(pathAndKey);
        Context context = getContext(split[0]);
        if (context != null) {
            return context.get(split[1]);
        } else {
            return null;
        }
    }

    @Override
    public Context getContext(String path) {
        return getContext(path, false);
    }

    @Override
    public Context getContext() {
        String currentContextId = ContextHolder.get().getCurrentContextId();
        if (currentContextId == null) {
            return null;
        }
        return rootContext.getChild(currentContextId);
    }

    @Deprecated
    @Override
    public String getThreadLocalContext() {
        return ContextHolder.get().getCurrentContextId();
    }

    @Deprecated
    @Override
    public void setThreadLocalContext(String contextId) {
        ContextHolder.get().setCurrentContextId(contextId);
        Context context = rootContext.getChild(contextId);
        Preconditions.checkArgument(context != null, "no context exists for given context id");
        ContextHolder.get().setCurrentContextId(contextId);
    }

    @Override
    public void createContext(String contextId) {
        rootContext.createChild(contextId);
        storeContext();
    }

    @Override
    public List<String> getAvailableContexts() {
        Map<String, Context> availableContexts = rootContext.getChildren();
        List<String> result = new ArrayList<String>(availableContexts.keySet());
        Collections.sort(result);
        return result;
    }

    private Context getContext(String path, boolean create) {
        Context c = getContext();
        if (c == null) {
            return null;
        }
        Context parent = null;
        for (String pathElem : new ContextPath(path).getElements()) {
            parent = c;
            c = c.getChild(pathElem);
            if (c == null) {
                if (!create) {
                    return null;
                }
                c = parent.createChild(pathElem);
            }
        }
        return c;
    }

    private String[] splitPath(String pathAndKey) {
        String path = new ContextPath(pathAndKey).getPath();
        String[] s = new String[2];
        int index = path.lastIndexOf('/');

        if (index == -1) {
            s[0] = "";
            s[1] = path;
        } else {
            s[0] = path.substring(0, index);
            s[1] = path.substring(index + 1);
        }

        return s;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public String getDefaultConnectorServiceId(String domainName) {
        return getValue(String.format("/domain/%s/defaultConnector/id", domainName));
    }

    @Override
    public void registerDefaultConnector(String domainName, String serviceId) {
        putValue(String.format("/domain/%s/defaultConnector/id", domainName), serviceId);
    }
}
