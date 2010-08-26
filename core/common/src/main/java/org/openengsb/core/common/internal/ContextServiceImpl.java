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
package org.openengsb.core.common.internal;

import org.openengsb.core.common.context.Context;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.context.ContextService;

import com.google.common.base.Preconditions;

public class ContextServiceImpl implements ContextCurrentService, ContextService {

    ThreadLocal<Context> currentContext = new ThreadLocal<Context>();
    private final Context rootContext = new ContextImpl();

    @Override
    public void putValue(String pathAndKey, String value) {
        String[] split = splitPath(pathAndKey);
        Context context = getContext(split[0], true);
        context.put(split[1], value);
    }

    @Override
    public String getValue(String pathAndKey) {
        String[] split = splitPath(pathAndKey);
        Context context = getContext(split[0]);
        return context != null ? context.get(split[1]) : null;
    }

    @Override
    public Context getContext(String path) {
        return getContext(path, false);
    }

    @Override
    public Context getContext() {
        return currentContext.get();
    }

    @Override
    public void setThreadLocalContext(String contextId) {
        Context context = rootContext.getChild(contextId);
        Preconditions.checkNotNull(context, "no context exists for given context id");
        currentContext.set(context);
    }

    @Override
    public void createContext(String contextId) {
        rootContext.createChild(contextId);
    }

    private Context getContext(String path, boolean create) {
        Context c = currentContext.get();
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
}
