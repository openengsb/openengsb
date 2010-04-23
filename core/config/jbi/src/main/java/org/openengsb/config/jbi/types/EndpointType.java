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
package org.openengsb.config.jbi.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class EndpointType implements Serializable {
    private String name;
    private List<AbstractType> attributes;
    private List<AbstractType> properties;
    private ComponentType parent;

    public EndpointType() {
        readResolve();
    }

    public EndpointType(String name) {
        this();
        this.name = name;
    }

    private Object readResolve() {
        if (attributes == null) {
            attributes = new ArrayList<AbstractType>();
        }
        if (properties == null) {
            properties = new ArrayList<AbstractType>();
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AbstractType> getAttributes() {
        return attributes;
    }

    public AbstractType getAttribute(String name) {
        for (AbstractType a : attributes) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    public void setAttributes(List<AbstractType> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(AbstractType attribute) {
        attributes.add(attribute);
    }

    public List<AbstractType> getProperties() {
        return properties;
    }

    public void setProperties(List<AbstractType> properties) {
        this.properties = properties;
    }

    public ComponentType getParent() {
        return parent;
    }

    public void setParent(ComponentType c) {
        this.parent = c;
    }
}
