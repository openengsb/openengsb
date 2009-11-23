/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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

package org.openengsb.context;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openengsb.core.messaging.Segment;
import org.openengsb.util.serialization.SerializationException;

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

                last.createChild(pathElement);
                ctx = last.getChild(pathElement);
            }
        }

        return ctx;
    }

    private void load() {
        if (settings == null || !settings.isFile()) {
            return;
        }

        try {
            Segment segment = Segment.fromXML(FileUtils.readFileToString(settings));
            rootContext = ContextSegmentTransformer.toContext(segment);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    private void save() {
        if (settings == null) {
            return;
        }

        try {
            Segment segment = ContextSegmentTransformer.toSegment(rootContext);
            FileUtils.writeStringToFile(settings, segment.toXML(), "UTF-8");
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
