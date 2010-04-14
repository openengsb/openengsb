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
package org.openengsb.contextcommon;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ContextStore {

    private Context rootContext = new Context();

    private final File settings;

    public ContextStore() {
        settings = null;
    }

    public ContextStore(File file) {
        settings = file;
        load();
    }

    public Context getContext(String path) {
        return new Context(resolve(new ContextPath(path)));
    }

    public Context getContext(String path, int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Depth must be positive");
        }
        Context context = getContext(path);
        prune(context, depth, 1);
        return context;
    }

    private void prune(Context ctx, int depth, int currentDepth) {
        if (currentDepth >= depth) {
            for (String child : ctx.getChildrenNames()) {
                ctx.removeChild(child);
            }
            return;
        }

        for (String child : ctx.getChildrenNames()) {
            prune(ctx.getChild(child), depth, currentDepth + 1);
        }
    }

    public void setValue(String path, String value) {
        String[] splitPath = splitPath(new ContextPath(path));
        Context ctx = resolveAndCreate(new ContextPath(splitPath[0]));
        ctx.set(splitPath[1], value);
        save();
    }

    public void addContext(String path) {
        resolveAndCreate(new ContextPath(path));
        save();
    }

    public void removeValue(String path) {
        String[] splitPath = splitPath(new ContextPath(path));
        Context ctx = resolve(new ContextPath(splitPath[0]));

        if (ctx.getChild(splitPath[1]) != null) {
            ctx.removeChild(splitPath[1]);
            save();
            return;
        }

        ctx.remove(splitPath[1]);
        save();
    }

    public String getValue(String path) {
        try {
            String[] splitPath = splitPath(new ContextPath(path));
            Context ctx = resolve(new ContextPath(splitPath[0]));
            return ctx.get(splitPath[1]);
        } catch (ContextNotFoundException e) {
            return null;
        }
    }

    private String[] splitPath(ContextPath contextPath) {
        String path = contextPath.getPath();
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

    private Context resolveAndCreate(ContextPath path) {
        return resolve(path, true);
    }

    private Context resolve(ContextPath path) {
        return resolve(path, false);
    }

    private Context resolve(ContextPath path, boolean create) {
        if (path.isRoot()) {
            return rootContext;
        }

        Context ctx = rootContext;
        Context last;

        for (String pathElement : path.getElements()) {
            last = ctx;
            ctx = ctx.getChild(pathElement);

            if (ctx == null) {
                if (!create) {
                    throw new ContextNotFoundException("Can't find context " + path);
                }

                if (last.get(pathElement) != null) {
                    throw new ContextNameClashException(String.format("An entry with name '%s' already exists",
                            pathElement));
                }

                last.createChild(pathElement);
                ctx = last.getChild(pathElement);
            }
        }

        return ctx;
    }

    private void load() {
        if (fileExists()) {
            loadDefaultConfig();
        } else {
            try {
                rootContext = ContextTransformer.fromXml(FileUtils.readFileToString(settings));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean fileExists() {
        return settings == null || !settings.isFile() || !settings.exists();
    }

    private void loadDefaultConfig() {
        setValue("42/event/defaultTarget/namespace", "urn:openengsb:drools");
        setValue("42/event/defaultTarget/servicename", "droolsService");

        setValue("42/workflows/ci/notification/recipient", "email@openengsb.org");

        /* hydro-edb-commit-usecase */
        setValue("42/workflows/edbcommit/notification/email/max.mustermann@openengsb.org", "true");
        setValue("42/workflows/edbcommit/notification/email/martina.musterfrau@openegnsb.org", "true");

       // FIXME: What is this for? It seems that this path is not even created
        addContext("42/test/maven-test/config/executionRequestProperties");
    }

    private void save() {
        if (settings == null) {
            return;
        }

        try {
            String xml = ContextTransformer.toXml(rootContext);
            FileUtils.writeStringToFile(settings, xml, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
