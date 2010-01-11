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
package org.openengsb.config.jbi;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.openengsb.config.jbi.internal.XStreamFactory;
import org.openengsb.config.jbi.types.BeanType;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

public class ComponentParser {
    private static Logger log = LoggerFactory.getLogger(ComponentParser.class);

    public static List<ComponentType> parseComponents(List<InputStream> descriptors) {
        XStream x = XStreamFactory.createXStream();
        ArrayList<ComponentType> components = new ArrayList<ComponentType>();
        for (InputStream s : descriptors) {
                try {
                ComponentType c = (ComponentType) x.fromXML(s);
                linkEndpointsToComponent(c);
                linkBeansToComponent(c);
                components.add(c);
                    s.close();
                } catch (Exception e) {
                    log.error("Parsing of component xml file failed", e);
                }
        }
        return components;
    }

    private static void linkEndpointsToComponent(ComponentType c) {
        for (EndpointType e : c.getEndpoints()) {
            e.setParent(c);
        }
    }

    private static void linkBeansToComponent(ComponentType c) {
        for (BeanType b : c.getBeans()) {
            b.setParent(c);
        }
    }
}
