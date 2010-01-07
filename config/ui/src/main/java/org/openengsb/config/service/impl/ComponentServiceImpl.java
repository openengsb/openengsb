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
package org.openengsb.config.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.openengsb.config.editor.ContextStringResourceLoader;
import org.openengsb.config.jbi.ComponentParser;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.service.ComponentService;

public class ComponentServiceImpl implements ComponentService {
    private List<ComponentType> components;
    private ServletContext context;

    public void setContext(ServletContext context) {
        this.context = context;
    }

    public ComponentServiceImpl() {

    }

    @SuppressWarnings("unchecked")
    public void init() {
        ArrayList<InputStream> descriptors = new ArrayList<InputStream>();
        Set<String> paths = context.getResourcePaths("/descriptors/");
        for (String path : paths) {
            if (path.endsWith(".xml")) {
                descriptors.add(context.getResourceAsStream(path));
            } else if (path.endsWith(".properties")) {
                File f = new File(path.substring(0, path.lastIndexOf('.')));
                ContextStringResourceLoader.instance.addResourceFiles(f.getName(), context.getResourceAsStream(path));
            }
        }
        components = ComponentParser.parseComponents(descriptors);
    }

    public List<ComponentType> getComponents() {
        return components;
    }

    public ComponentType getComponent(String name) {
        for (ComponentType c : components) {
            if (c.getName().equals(name))
                return c;
        }
        return null;
    }
}
