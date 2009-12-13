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
package org.openengsb.config;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.openengsb.config.jbi.ComponentParser;
import org.openengsb.config.jbi.types.ComponentType;

public class ComponentsHolder {
    private List<ComponentType> components;

    private ServletContext context;

    public void setContext(ServletContext context) {
        this.context = context;
    }

    public ComponentsHolder() {

    }

    public void init() {
        ArrayList<URI> descriptors = new ArrayList<URI>();
        try {
            descriptors.add(context.getResource("/descriptors/servicemix-file.xml").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
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
