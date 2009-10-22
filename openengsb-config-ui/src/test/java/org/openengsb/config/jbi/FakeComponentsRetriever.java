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
package org.openengsb.config.jbi;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.openengsb.config.jbi.component.ComponentDescriptor;
import org.openengsb.config.jbi.component.ComponentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class FakeComponentsRetriever implements ComponentsRetriever {
    private static Logger log = LoggerFactory.getLogger(FakeComponentsRetriever.class);

    private ServletContext context;

    private ArrayList<ComponentDescriptor> components;

    @Override
    public List<ComponentDescriptor> lookupComponents() {
        if (components != null) {
            return components;
        }
        components = new ArrayList<ComponentDescriptor>();
        ComponentParser parser = new ComponentParser();
        ArrayList<String> toread = new ArrayList<String>();
        toread.add("simple");
        toread.add("svn");
        for (String s : toread) {
            try {
                ComponentDescriptor jbi = parser.parseJbi(new InputSource(context.getResourceAsStream("/descriptors/"
                        + s + ".xml")));
                ComponentDescriptor schema = parser.parseSchema(new InputSource(context
                        .getResourceAsStream("/descriptors/" + s + ".xsd")));
                components.add(new ComponentDescriptor(jbi.getType(), jbi.getName(), jbi.getDescription(), schema
                        .getTargetNamespace(), schema.getEndpoints()));
            } catch (Exception e) {
                log.warn("error parsing jbi/schema", e);
            }
        }
        return components;
    }

    public void setServletContext(ServletContext context) {
        this.context = context;
    }
}